// ADS I Class Project
// Pipelined RISC-V Core - EX Stage
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/09/2026 by Tobias Jauch (@tojauch)

/*
Instruction Execute (EX) Stage: ALU operations and exception detection

Instantiated Modules:
    ALU: Integrate your module from Assignment02 for arithmetic/logical operations

ALU Interface:
    alu.io.operandA: first operand input
    alu.io.operandB: second operand input
    alu.io.operation: operation code controlling ALU function
    alu.io.aluResult: computation result output

Internal Signals:
    Map uopc codes to ALUOp values

Functionality:
    Map instruction uop to ALU operation code
    Pass operands to ALU
    Output results to pipeline

Outputs:
    aluResult: computation result from ALU
    exception: pass exception flag
    branchTarget: calculated branch target address for conditional branch instructions
    flush: control signal to flush pipeline on mispredicted branches
*/

package core_tile

import chisel3._
import chisel3.util._
import Assignment02.{ALU, ALUOp}

class EXStage extends Module {
  val io = IO(new Bundle {
    val inUop        = Input(UOpCode())
    val inRs1Data    = Input(UInt(32.W)) // Raw rs1 register data from ID/EX barrier
    val inRs2Data    = Input(UInt(32.W)) // Raw rs2 register data from ID/EX barrier
    val inImm        = Input(UInt(32.W)) // Raw sign-extended immediate
    val inXcptId     = Input(Bool())
    val inPC         = Input(UInt(32.W)) // Current PC of the executing instruction
    
    // BTB Prediction Information parallel-pipelined alongside instructions
    val btbValid     = Input(Bool())     // BTB hit in Fetch stage
    val btbPredTaken = Input(Bool())     // BTB prediction made in Fetch stage

    // Forwarding Control Inputs from Forwarding Unit
    val forwardA     = Input(UInt(2.W))
    val forwardB     = Input(UInt(2.W))
    
    // Forwarding Data Paths
    val memAluResult = Input(UInt(32.W)) // Data from EX/MEM stage
    val wbAluResult  = Input(UInt(32.W)) // Data from MEM/WB stage

    // Outputs for handling branch/jump flow redirect control
    val pcSelOut     = Output(Bool())      // Asserted high if the pipeline must correct/flush PC
    val targetPCOut  = Output(UInt(32.W))  // Correction target address

    val aluResult    = Output(UInt(32.W))
    val exception    = Output(Bool())
  })

  // 1. Determine ALU Operation Type and Instruction Type Flag
  val alu_op = Wire(ALUOp())
  val isIType = Wire(Bool())
  
  alu_op  := ALUOp.ADD
  isIType := false.B

  switch(io.inUop) {
    is(UOpCode.uopADD)   { alu_op := ALUOp.ADD;   isIType := false.B }
    is(UOpCode.uopSUB)   { alu_op := ALUOp.SUB;   isIType := false.B }
    is(UOpCode.uopSLL)   { alu_op := ALUOp.SLL;   isIType := false.B }
    is(UOpCode.uopSLT)   { alu_op := ALUOp.SLT;   isIType := false.B }
    is(UOpCode.uopSLTU)  { alu_op := ALUOp.SLTU;  isIType := false.B }
    is(UOpCode.uopXOR)   { alu_op := ALUOp.XOR;   isIType := false.B }
    is(UOpCode.uopSRL)   { alu_op := ALUOp.SRL;   isIType := false.B }
    is(UOpCode.uopSRA)   { alu_op := ALUOp.SRA;   isIType := false.B }
    is(UOpCode.uopOR)    { alu_op := ALUOp.OR;    isIType := false.B }
    is(UOpCode.uopAND)   { alu_op := ALUOp.AND;   isIType := false.B }
    
    is(UOpCode.uopADDI)  { alu_op := ALUOp.ADD;   isIType := true.B }
    is(UOpCode.uopSLTI)  { alu_op := ALUOp.SLT;   isIType := true.B }
    is(UOpCode.uopSLTIU) { alu_op := ALUOp.SLTU;  isIType := true.B }
    is(UOpCode.uopXORI)  { alu_op := ALUOp.XOR;   isIType := true.B }
    is(UOpCode.uopORI)   { alu_op := ALUOp.OR;    isIType := true.B }
    is(UOpCode.uopANDI)  { alu_op := ALUOp.AND;   isIType := true.B }
    is(UOpCode.uopSLLI)  { alu_op := ALUOp.SLL;   isIType := true.B }
    is(UOpCode.uopSRLI)  { alu_op := ALUOp.SRL;   isIType := true.B }
    is(UOpCode.uopSRAI)  { alu_op := ALUOp.SRA;   isIType := true.B }
  }

  // 2. 3-Input Multiplexers for Forwarding
  val muxA = MuxLookup(io.forwardA, io.inRs1Data, Seq(
    "b00".U -> io.inRs1Data,
    "b10".U -> io.memAluResult,
    "b01".U -> io.wbAluResult
  ))

  val muxB = MuxLookup(io.forwardB, io.inRs2Data, Seq(
    "b00".U -> io.inRs2Data,
    "b10".U -> io.memAluResult,
    "b01".U -> io.wbAluResult
  ))

  // 3. Branch Condition Evaluation Engine (Using fully forwarded values)
  val br_taken = WireDefault(false.B)
  val isBranch = WireDefault(false.B)

  switch(io.inUop) {
    is(UOpCode.uopBEQ)  { br_taken := (muxA === muxB); isBranch := true.B }
    is(UOpCode.uopBNE)  { br_taken := (muxA =/= muxB); isBranch := true.B }
    is(UOpCode.uopBLT)  { br_taken := (muxA.asSInt < muxB.asSInt); isBranch := true.B }
    is(UOpCode.uopBGE)  { br_taken := (muxA.asSInt >= muxB.asSInt); isBranch := true.B }
    is(UOpCode.uopBLTU) { br_taken := (muxA < muxB); isBranch := true.B }
    is(UOpCode.uopBGEU) { br_taken := (muxA >= muxB); isBranch := true.B }
  }

  val isJump = (io.inUop === UOpCode.uopJAL || io.inUop === UOpCode.uopJALR)
  
  // What did the Fetch stage predict? (Default to Not Taken if BTB missed)
  val predictedTaken = io.btbValid && io.btbPredTaken

  // A misprediction happens if the actual outcome does not match our execution prediction
  val mispredicted = isBranch && (br_taken =/= predictedTaken)

  // Redirect PC if there's an unconditional jump OR a conditional branch misprediction
  io.pcSelOut := isJump || mispredicted

  // 4. Calculate Correction Target Address
  val calculatedTarget = Wire(UInt(32.W))
  val basePC = Mux(io.inUop === UOpCode.uopJALR, muxA, io.inPC)
  calculatedTarget := (basePC + io.inImm) & "hFFFF_FFFE".U // JALR alignment compliance

  // Fallback PC if the branch was predicted taken but actually evaluated to NOT taken
  val fallthroughPC = io.inPC + 4.U

  // If a branch misprediction happens:
  // - If it was predicted taken but actually NOT taken, correct back to fallthroughPC
  // - If it was predicted not taken but actually TAKEN, correct to calculatedTarget
  // Jumps always go straight to the calculated target.
  io.targetPCOut := Mux(isJump, calculatedTarget, 
                    Mux(predictedTaken, fallthroughPC, calculatedTarget))

  // 5. Connect to standard ALU
  val alu = Module(new ALU)
  val aluA = WireDefault(muxA)
  val aluB = WireDefault(Mux(isIType, io.inImm, muxB))
  alu.io.operandA  := aluA
  alu.io.operandB  := aluB
  alu.io.operation := alu_op

  // 6. Output Selection
  // Jumps write the return address PC + 4 into the destination register (rd)
  io.aluResult := Mux(isJump, io.inPC + 4.U, alu.io.aluResult)
  io.exception := io.inXcptId
}
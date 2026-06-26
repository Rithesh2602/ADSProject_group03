package core_tile

import chisel3._
import chisel3.util._
import Assignment02.{ALU, ALUOp}

class EXStage extends Module {
  val io = IO(new Bundle {
    val inUop       = Input(UOpCode())
    val inRs1Data   = Input(UInt(32.W)) // Raw rs1 register data from ID/EX barrier
    val inRs2Data   = Input(UInt(32.W)) // Raw rs2 register data from ID/EX barrier
    val inImm       = Input(UInt(32.W)) // Raw sign-extended immediate
    val inXcptId    = Input(Bool())
    
    // Forwarding Control Inputs from Forwarding Unit
    val forwardA    = Input(UInt(2.W))
    val forwardB    = Input(UInt(2.W))
    
    // Forwarding Data Paths
    val memAluResult = Input(UInt(32.W)) // Data from EX/MEM stage
    val wbAluResult  = Input(UInt(32.W)) // Data from MEM/WB stage

    val aluResult   = Output(UInt(32.W))
    val exception   = Output(Bool())
  })

  // 1. Determine ALU Operation Type
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

  // 2. 3-Input Multiplexers for Forwarding (Matches the [0, 1, 2] muxes in Slide 6-26)
  // 00 -> ID/EX Pipeline Register value
  // 10 -> Forwarded from EX/MEM stage (Priority)
  // 01 -> Forwarded from MEM/WB stage
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

  // 3. Connect to ALU Inputs
  val alu = Module(new ALU)
  alu.io.operandA  := muxA
  alu.io.operandB  := Mux(isIType, io.inImm, muxB) // Choose immediate for I-type, otherwise forwarded register
  alu.io.operation := alu_op

  io.aluResult := alu.io.aluResult
  io.exception := io.inXcptId
}
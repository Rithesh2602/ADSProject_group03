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
*/

package core_tile

import chisel3._
import chisel3.util._
import Assignment02.{ALU, ALUOp}
import UOpCode._

// -----------------------------------------
// EX Stage Module
// -----------------------------------------

class EXStage extends Module {
  val io = IO(new Bundle {
    // Inputs from ID/EX Pipeline Register
    val inUop        = Input(UOpCode())
    val inRs1Data    = Input(UInt(32.W))
    val inRs2Data    = Input(UInt(32.W))
    val inImm        = Input(UInt(32.W))
    val inXcptId     = Input(Bool()) // Exception incoming from decode stage

    // Outputs to EX/MEM Barrier
    val aluResult    = Output(UInt(32.W))
    val exception    = Output(Bool())
  })

  // 1. Instantiate the ALU from Assignment 02
  val alu = Module(new ALU)

  // 2. Control Translation Logic Wires
  val aluOp       = Wire(ALUOp())
  val srcBIsImm   = Wire(Bool())

  // Default Fallback States
  aluOp     := ALUOp.ADD 
  srcBIsImm := false.B

  // 3. Map Micro-Operation Codes to ALU Operations
  switch(io.inUop) {
    // R-Type Direct Mapping
    is(UOpCode.uopADD)   { aluOp := ALUOp.ADD;   srcBIsImm := false.B }
    is(UOpCode.uopSUB)   { aluOp := ALUOp.SUB;   srcBIsImm := false.B }
    is(UOpCode.uopSLL)   { aluOp := ALUOp.SLL;   srcBIsImm := false.B }
    is(UOpCode.uopSLT)   { aluOp := ALUOp.SLT;   srcBIsImm := false.B }
    is(UOpCode.uopSLTU)  { aluOp := ALUOp.SLTU;  srcBIsImm := false.B }
    is(UOpCode.uopXOR)   { aluOp := ALUOp.XOR;   srcBIsImm := false.B }
    is(UOpCode.uopSRL)   { aluOp := ALUOp.SRL;   srcBIsImm := false.B }
    is(UOpCode.uopSRA)   { aluOp := ALUOp.SRA;   srcBIsImm := false.B }
    is(UOpCode.uopOR)    { aluOp := ALUOp.OR;    srcBIsImm := false.B }
    is(UOpCode.uopAND)   { aluOp := ALUOp.AND;   srcBIsImm := false.B }
    
    // I-Type Immediate Mapping
    is(UOpCode.uopADDI)  { aluOp := ALUOp.ADD;   srcBIsImm := true.B }
    is(UOpCode.uopSLTI)  { aluOp := ALUOp.SLT;   srcBIsImm := true.B }
    is(UOpCode.uopSLTIU) { aluOp := ALUOp.SLTU;  srcBIsImm := true.B }
    is(UOpCode.uopXORI)  { aluOp := ALUOp.XOR;   srcBIsImm := true.B }
    is(UOpCode.uopORI)   { aluOp := ALUOp.OR;    srcBIsImm := true.B }
    is(UOpCode.uopANDI)  { aluOp := ALUOp.AND;   srcBIsImm := true.B }
    is(UOpCode.uopSLLI)  { aluOp := ALUOp.SLL;   srcBIsImm := true.B }
    is(UOpCode.uopSRLI)  { aluOp := ALUOp.SRL;   srcBIsImm := true.B }
    is(UOpCode.uopSRAI)  { aluOp := ALUOp.SRA;   srcBIsImm := true.B }
  }

  // 4. Wire up ALU Inputs
  alu.io.operandA  := io.inRs1Data
  alu.io.operandB  := Mux(srcBIsImm, io.inImm, io.inRs2Data)
  alu.io.operation := aluOp

  // 5. Connect Module Outputs
  io.aluResult := alu.io.aluResult
  
  // Pass-through execution or handle new local execution-level exceptions if necessary
  io.exception := io.inXcptId 
}

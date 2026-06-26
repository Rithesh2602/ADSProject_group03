// ADS I Class Project
// Pipelined RISC-V Core - ID Barrier
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/09/2026 by Tobias Jauch (@tojauch)

/*
ID-Barrier: pipeline register between Decode and Execute stages

Internal Registers:
    uop: micro-operation code (from uopc enum)
    rd: destination register index, initialized to 0
    operandA: first source operand, initialized to 0
    operandB: second operand/immediate, initialized to 0

Inputs:
    inUOP: micro-operation code from ID stage
    inRD: destination register from ID stage
    inOperandA: first operand from ID stage
    inOperandB: second operand/immediate from ID stage
    inXcptInvalid: exception flag from ID stage

Outputs:
    outUOP: micro-operation code to EX stage
    outRD: destination register to EX stage
    outOperandA: first operand to EX stage
    outOperandB: second operand to EX stage
    outXcptInvalid: exception flag to EX stage
Functionality:
    Save all input signals to a register and output them in the following clock cycle
*/

package core_tile

import chisel3._
import chisel3.util._

class IDBarrier extends Module {
  val io = IO(new Bundle {
    val inUOP           = Input(UOpCode())
    val inRD            = Input(UInt(5.W))
    val inOperandA      = Input(UInt(32.W))
    val inOperandB      = Input(UInt(32.W))
    val inXcptInvalid   = Input(Bool())
    
    val inRs1           = Input(UInt(5.W))
    val inRs2           = Input(UInt(5.W))
    val inRegWrite      = Input(Bool())
    val inInstr         = Input(UInt(32.W))

    val outUOP          = Output(UOpCode())
    val outRD           = Output(UInt(5.W))
    val outOperandA     = Output(UInt(32.W))
    val outOperandB     = Output(UInt(32.W))
    val outXcptInvalid  = Output(Bool())
    
    val outRs1          = Output(UInt(5.W))
    val outRs2          = Output(UInt(5.W))
    val outRegWrite     = Output(Bool())
    val outInstr        = Output(UInt(32.W))
  })

  io.outUOP          := RegNext(io.inUOP)
  io.outRD           := RegNext(io.inRD)
  io.outOperandA     := RegNext(io.inOperandA)
  io.outOperandB     := RegNext(io.inOperandB)
  io.outXcptInvalid  := RegNext(io.inXcptInvalid)
  
  io.outRs1          := RegNext(io.inRs1)
  io.outRs2          := RegNext(io.inRs2)
  io.outRegWrite     := RegNext(io.inRegWrite, false.B)
  io.outInstr        := RegNext(io.inInstr, 0.U)
}
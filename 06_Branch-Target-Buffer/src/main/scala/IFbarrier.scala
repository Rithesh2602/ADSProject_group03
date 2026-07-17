// ADS I Class Project
// Pipelined RISC-V Core - IF Barrier
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/09/2026 by Tobias Jauch (@tojauch)

/*
IF-Barrier: pipeline register between Fetch and Decode stages

Internal Registers:
    instrReg: holds instruction between pipeline stages, initialized to 0
    pcReg: holds PC between pipeline stages, initialized to 0

Inputs:
    inPC: program counter from IF stage
    inInstr: fetched instruction from IF stage
    flush: flushes the stage on a control hazard

Outputs:
    outPC: program counter to ID stage
    outInstr: instruction to ID stage

Functionality:
    Save all input signals to a register and output them in the following clock cycle.
    If flush is high, clear data and inject a NOP instruction.
*/

package core_tile

import chisel3._

// -----------------------------------------
// IF-Barrier
// -----------------------------------------

class IFBarrier extends Module {
  val io = IO(new Bundle {
    val inPC     = Input(UInt(32.W))
    val inInstr  = Input(UInt(32.W))
    val flush    = Input(Bool())  // Flushes the stage on a control hazard

    val outPC    = Output(UInt(32.W))
    val outInstr = Output(UInt(32.W))
  })

  val pcReg    = RegInit(0.U(32.W))
  val instrReg = RegInit(0.U(32.W))

  // Flush overrides incoming data and inserts a pipeline bubble (NOP)
  when(io.flush) {
    pcReg    := 0.U
    instrReg := 0x00000013.U  // RISC-V NOP: addi x0, x0, 0
  }.otherwise {
    pcReg    := io.inPC
    instrReg := io.inInstr
  }

  io.outPC    := pcReg
  io.outInstr := instrReg
}
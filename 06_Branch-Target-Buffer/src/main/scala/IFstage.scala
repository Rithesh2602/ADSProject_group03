// ADS I Class Project
// Pipelined RISC-V Core - IF Stage
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/09/2026 by Tobias Jauch (@tojauch)

/*
The Instruction Fetch (IF) stage is the first stage of the pipeline and handles instruction retrieval from memory.

Memory:
    IMem: instruction memory with 4096 32-bit unsigned integer entries, loaded from a binary file at compile time

Internal Registers:
    PC: 32-bit unsigned integer register, initialized to 0 holding the current program counter address

Internal Signals:
    none

Functionality:
    Fetch the instruction at the current PC (word-aligned addressing)
    Increment the PC (word-aligned) each clock cycle to fetch the next sequential instruction
    Handle flushes due to mispredicted branches

Parameters:
    BinaryFile: String - path to the binary file to load into instruction memory

Inputs:
    none

Outputs:
    instr: send the fetched instruction to IF Barrier
*/

package core_tile

import chisel3._
import chisel3.util._

class IFstage extends Module {
  val io = IO(new Bundle {
    // Current PC and fetched instruction from parent memory environment
    val inPC       = Input(UInt(32.W))
    val inInstr    = Input(UInt(32.W))

    // Control signals from Execute/Hazard Unit (Assignment 05)
    val pcSel      = Input(Bool())         // High if branch is taken or it's an unconditional jump
    val targetPC   = Input(UInt(32.W))     // The calculated jump/branch target address

    val outPC      = Output(UInt(32.W))
    val outInstr   = Output(UInt(32.W))
    
    // Target PC going back to the parent memory/PC updater wrapper
    val nextPC     = Output(UInt(32.W))
  })

  // Select between sequential execution and our new branch/jump target path
  io.nextPC := Mux(io.pcSel, io.targetPC, io.inPC + 4.U)

  // Pass current signals forward into the IF/ID pipeline barrier
  io.outPC    := io.inPC
  io.outInstr := io.inInstr
}
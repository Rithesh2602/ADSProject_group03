// ADS I Class Project
// Pipelined RISC-V Core - IF Stage
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/09/2026 by Tobias Jauch (@tojauch)

/*
The Instruction Fetch (IF) stage is the first stage of the pipeline and handles instruction retrieval from memory.

Memory:
    IMem: instruction memory with 4096 32-bit unsigned integer entires, loaded from a binary file at compile time

Internal Registers:
    PC: 32-bit unsigned integer register, initialized to 0 holding the current program counter address

Internal Signals:
    none

Functionality:
    Fetch the instruction at the current PC (word-aligned addressing)
    Increment the PC (word-aligned) each clock cycle to fetch the next sequential instruction

Parameters:
    BinaryFile: String - path to the binary file to load into instruction memory

Inputs:
    none

Outputs:
    instr: send the fetched instruction to IF Barrier
*/

package core_tile

import chisel3._
import chisel3.util.experimental.loadMemoryFromFile //pre-load a hardware memory module with data from an external text file

// -----------------------------------------
// Fetch Stage
// -----------------------------------------

class IF (BinaryFile: String) extends Module {
  val io = IO(new Bundle {
    // Output to the IF-Barrier
    val instr = Output(UInt(32.W))
  })

  // 1. Instantiate Instruction Memory (IMem) with 4096 32-bit entries
  val imem = Mem(4096, UInt(32.W)) //Allocates an internal, synchronous memory block (RAM) inside the FPGA/ASIC
  loadMemoryFromFile(imem, BinaryFile)

  // 2. Program Counter (PC) Initialization (Starts at 0)
  val pcReg = RegInit(0.U(32.W))

  // 3. Sequential Next-PC Logic (Increment by 4 bytes each cycle)
  pcReg := pcReg + 4.U

  // 4. Word-Aligned Memory Addressing 
  // Because imem is an array of 32-bit words, byte addresses must be divided by 4 (shifted right by 2).
  val fetchedInstr = imem(pcReg >> 2)

  // 5. Connect Stage Output
  io.instr := fetchedInstr
}
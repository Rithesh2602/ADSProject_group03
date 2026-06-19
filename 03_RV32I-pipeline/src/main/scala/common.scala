// ADS I Class Project
// Pipelined RISC-V Core - Common Definitions
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/09/2026 by Tobias Jauch (@tojauch)

/*
Global Definitions and Data Types

Enumerations:
    uopc: ChiselEnum defining micro-operation codes for all supported RV32I instructions:
        R-type instructions 
        I-type instructions
        NOP (no operation, default case)

This enum is used throughout the pipeline:
    Decode stage assigns uop based on instruction fields
    Execute stage maps uop to ALU operations
*/

package core_tile

import chisel3._
import chisel3.experimental.ChiselEnum

// -----------------------------------------
// Global Definitions and Data Types
// -----------------------------------------

//ToDo: Add your implementation according to the specification above here 

object UOpCode extends ChiselEnum {
  // --- Control / Default Operations ---
  val uopNOP   = Value

  // --- R-Type Instructions ---
  val uopADD   = Value
  val uopSUB   = Value
  val uopSLL   = Value
  val uopSLT   = Value
  val uopSLTU  = Value
  val uopXOR   = Value
  val uopSRL   = Value
  val uopSRA   = Value
  val uopOR    = Value
  val uopAND   = Value

  // --- I-Type Instructions ---
  val uopADDI  = Value
  val uopSLTI  = Value
  val uopSLTIU = Value
  val uopXORI  = Value
  val uopORI   = Value
  val uopANDI  = Value
  val uopSLLI  = Value
  val uopSRLI  = Value
  val uopSRAI  = Value
}
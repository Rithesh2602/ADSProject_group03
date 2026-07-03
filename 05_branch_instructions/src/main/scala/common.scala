// ADS I Class Project
// Pipelined RISC-V Core - Common Definitions
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern

package core_tile

import chisel3._
import chisel3.experimental.ChiselEnum

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

  // --- B-Type Instructions (Assignment 05) ---
  val uopBEQ   = Value
  val uopBNE   = Value
  val uopBLT   = Value
  val uopBGE   = Value
  val uopBLTU  = Value
  val uopBGEU  = Value

  // --- Jump Instructions (Assignment 05) ---
  val uopJAL   = Value
  val uopJALR  = Value
}
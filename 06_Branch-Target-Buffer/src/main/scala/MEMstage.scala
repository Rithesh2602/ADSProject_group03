// ADS I Class Project
// Pipelined RISC-V Core - MEM Stage
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/09/2026 by Tobias Jauch (@tojauch)

/*
Memory (MEM) Stage: load/store operations (placeholder for RV32I R/I-type subset)

Current Implementation:
    Passes data forward through the module with no active memory accesses
    In current RV32I subset (R-type, I-type), no memory operations are performed

Rationale:
    Ensures proper pipeline depth and timing alignment
    Allows future extension without architectural changes
*/

package core_tile

import chisel3._

// -----------------------------------------
// Memory Stage
// -----------------------------------------

class MEM extends Module {
  val io = IO(new Bundle {
    // Inputs from EX/MEM Barrier
    val inAluResult  = Input(UInt(32.W))
    val inRD         = Input(UInt(5.W))
    val inException  = Input(Bool())
    val inRegWrite   = Input(Bool())

    // Outputs to MEM/WB Barrier
    val outAluResult = Output(UInt(32.W))
    val outRD        = Output(UInt(5.W))
    val outException = Output(Bool())
    val outRegWrite  = Output(Bool())
  })

  // No active data memory operations are required for the branch/jump subset.
  // We simply feed the signals forward to preserve pipeline depth and timing alignment.
  io.outAluResult := io.inAluResult
  io.outRD        := io.inRD
  io.outException := io.inException
  io.outRegWrite  := io.inRegWrite
}
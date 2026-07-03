// ADS I Class Project
// Pipelined RISC-V Core - MEM Stage
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern

package core_tile

import chisel3._

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
  // We simply feed the signals forward to preserve pipeline depth and timing.
  io.outAluResult := io.inAluResult
  io.outRD        := io.inRD
  io.outException := io.inException
  io.outRegWrite  := io.inRegWrite
}
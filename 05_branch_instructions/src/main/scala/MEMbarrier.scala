// ADS I Class Project
// Pipelined RISC-V Core - MEM Barrier
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern

package core_tile

import chisel3._
import chisel3.util._

class MEMBarrier extends Module {
  val io = IO(new Bundle {
    val inAluResult   = Input(UInt(32.W))
    val inRD          = Input(UInt(5.W))
    val inException   = Input(Bool())
    val inRegWrite    = Input(Bool())

    val outAluResult  = Output(UInt(32.W))
    val outRD         = Output(UInt(5.W))
    val outException  = Output(Bool())
    val outRegWrite   = Output(Bool())
  })

  // Synchronous registers pass values safely to the Writeback (WB) stage
  io.outAluResult   := RegNext(io.inAluResult)
  io.outRD          := RegNext(io.inRD)
  io.outException   := RegNext(io.inException)
  io.outRegWrite    := RegNext(io.inRegWrite, false.B)
}
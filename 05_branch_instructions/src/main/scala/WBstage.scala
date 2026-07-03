// ADS I Class Project
// Pipelined RISC-V Core - WB Stage
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern

package core_tile

import chisel3._

class WB extends Module {
  val io = IO(new Bundle {
    // Inputs from MEM/WB Barrier
    val aluResult   = Input(UInt(32.W))
    val rd          = Input(UInt(5.W))
    val inException = Input(Bool())
    val inRegWrite  = Input(Bool()) // Added to respect branch control fields

    // Interface to Register File Port 3 (Write Port)
    val regFileReq  = Output(new regFileWriteReq)

    // Output for external verification / WB Barrier hook
    val check_res   = Output(UInt(32.W))
  })

  // 1. Control Logic: Respect the structural RegWrite flag decoded in the ID stage
  val writeEnable = io.inRegWrite && (io.rd =/= 0.U) && (!io.inException)

  // 2. Drive the Register File Request Bundle Structure
  io.regFileReq.addr  := io.rd
  io.regFileReq.data  := io.aluResult
  io.regFileReq.wr_en := writeEnable

  // 3. Connect output validation hook
  io.check_res := io.aluResult
}
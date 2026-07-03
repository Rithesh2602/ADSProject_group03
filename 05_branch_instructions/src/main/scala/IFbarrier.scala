// ADS I Class Project
// Pipelined RISC-V Core - IF Barrier
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern

package core_tile

import chisel3._

class IFbarrier extends Module {
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
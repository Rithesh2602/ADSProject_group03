// ADS I Class Project
// Pipelined RISC-V Core - IF Stage
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern

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
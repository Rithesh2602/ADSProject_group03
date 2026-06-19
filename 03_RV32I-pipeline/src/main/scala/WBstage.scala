// ADS I Class Project
// Pipelined RISC-V Core - WB Stage
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/09/2026 by Tobias Jauch (@tojauch)


/*
Writeback (WB) Stage: result storage and register file updates

Register File Interface:
    regFileReq: write request bundle
        regFileReq.addr: destination register index
        regFileReq.data: result value to write
        regFileReq.wr_en: write enable signal

Inputs:
    aluResult: computation result from pipeline
    rd: destination register address

Internal Signals:
    Result forwarding paths
    Write enable control

Functionality:
    Forward aluResult to register file write port
    Set write address to rd
    Assert wr_en = true for all R-type and I-type instructions
    Output result on check_res for verification and debugging

Outputs:
    check_res: result value for verification
*/

package core_tile

import chisel3._

// -----------------------------------------
// Writeback Stage
// -----------------------------------------

class WB extends Module {
  val io = IO(new Bundle {
    // Inputs from MEM/WB Barrier
    val aluResult   = Input(UInt(32.W))
    val rd          = Input(UInt(5.W))
    val inException = Input(Bool())

    // Interface to Register File Port 3 (Write Port)
    val regFileReq  = Output(new regFileWriteReq)

    // Output for external verification / WB Barrier hook
    val check_res   = Output(UInt(32.W))
  })

  // 1. Control Logic: Assert write-enable if destination is not x0 and there is no active exception
  // (In this pure R-type and I-type subset, every non-exceptional instruction writes to rd)
  val writeEnable = (io.rd =/= 0.U) && (!io.inException)

  // 2. Drive the Register File Request Bundle Structure
  io.regFileReq.addr  := io.rd
  io.regFileReq.data  := io.aluResult
  io.regFileReq.wr_en := writeEnable

  // 3. Connect output validation hook
  io.check_res := io.aluResult
}
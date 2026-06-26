// ADS I Class Project
// Pipelined RISC-V Core - Forwarding Unit
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 05/09/2026 by Tobias Jauch (@tojauch)

/*
Forwarding Unit: resolves data hazards by forwarding results from later pipeline stages to the ID stage

Functionality (cf. slide 6-24ff of the lecture slides):
    Detects data hazards by comparing source registers in the EX stage with destination registers in MEM and WB stages (EX and MEM barriers).
    Generates control signals for the multiplexers in the EX stage to select the correct data source for the ALU inputs
    Handles cases where multiple hazards occur simultaneously (e.g., forwarding from both MEM and WB stages)

Inputs:
    rs1_EX: source register 1 in EX stage
    rs2_EX: source register 2 in EX stage
    rd_MEM: destination register in MEM stage
    rd_WB: destination register in WB stage
    wrEn_MEM: write enable signal for MEM stage
    wrEn_WB: write enable signal for WB stage

Outputs:
    forwardA: control signal for selecting source of operand A in EX stage
    forwardB: control signal for selecting source of operand B in EX stage

*/

// ADS I Class Project
// Pipelined RISC-V Core - Forwarding Unit
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 05/09/2026 by Tobias Jauch (@tojauch)

package core_tile

import chisel3._
import chisel3.util._

class ForwardingUnit extends Module {
  val io = IO(new Bundle {
    // Inputs
    val rs1_EX   = Input(UInt(5.W))
    val rs2_EX   = Input(UInt(5.W))
    val rd_MEM   = Input(UInt(5.W))
    val rd_WB    = Input(UInt(5.W))
    val wrEn_MEM = Input(Bool())
    val wrEn_WB  = Input(Bool())

    // Outputs (00 -> Register File, 10 -> From MEM, 01 -> From WB)
    val forwardA = Output(UInt(2.W))
    val forwardB = Output(UInt(2.W))
  })

  // Default assignments: No forwarding
  io.forwardA := "b00".U
  io.forwardB := "b00".U

  // -------------------------------------------------------------
  // Forwarding Logic for Operand A (rs1_EX)
  // PRIORITIZE MEM STAGE: Only forward from WB if MEM stage is NOT writing to rs1_EX
  // -------------------------------------------------------------
  when(io.wrEn_MEM && (io.rd_MEM =/= 0.U) && (io.rd_MEM === io.rs1_EX)) {
    io.forwardA := "b10".U  // Forward from MEM Stage (Highest Priority)
  }
  .elsewhen(io.wrEn_WB && (io.rd_WB =/= 0.U) && (io.rd_WB === io.rs1_EX)) {
    io.forwardA := "b01".U  // Forward from WB Stage
  }

  // -------------------------------------------------------------
  // Forwarding Logic for Operand B (rs2_EX)
  // PRIORITIZE MEM STAGE: Only forward from WB if MEM stage is NOT writing to rs2_EX
  // -------------------------------------------------------------
  when(io.wrEn_MEM && (io.rd_MEM =/= 0.U) && (io.rd_MEM === io.rs2_EX)) {
    io.forwardB := "b10".U  // Forward from MEM Stage (Highest Priority)
  }
  .elsewhen(io.wrEn_WB && (io.rd_WB =/= 0.U) && (io.rd_WB === io.rs2_EX)) {
    io.forwardB := "b01".U  // Forward from WB Stage
  }
}
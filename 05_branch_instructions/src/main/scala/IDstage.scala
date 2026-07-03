// ADS I Class Project
// Pipelined RISC-V Core - ID Stage
// Chair of Electronic Design Automation, RPTU in Kaiserslautern

package core_tile

import chisel3._
import chisel3.util._
import core_tile.UOpCode._

class IDStage extends Module {
  val io = IO(new Bundle {
    val inInstruction  = Input(UInt(32.W))
    val regFileReq_A   = Output(UInt(5.W))
    val regFileResp_A  = Input(UInt(32.W))
    val regFileReq_B   = Output(UInt(5.W))
    val regFileResp_B  = Input(UInt(32.W))
    
    val outUop         = Output(UOpCode())
    val outRD          = Output(UInt(5.W))
    val outOperandA    = Output(UInt(32.W))
    val outOperandB    = Output(UInt(32.W)) 
    val outXcptInvalid = Output(Bool())
    val outRegWrite    = Output(Bool()) 
  })

  val opcode = io.inInstruction(6, 0)
  val id_rd  = io.inInstruction(11, 7)
  val funct3 = io.inInstruction(14, 12)
  val id_rs1 = io.inInstruction(19, 15)
  val id_rs2 = io.inInstruction(24, 20)
  val funct7 = io.inInstruction(31, 25)

  io.regFileReq_A := id_rs1
  io.regFileReq_B := id_rs2

  val id_uop       = Wire(UOpCode())
  val id_illegal   = Wire(Bool())
  val id_reg_write = Wire(Bool())

  id_uop       := UOpCode.uopNOP
  id_illegal   := (io.inInstruction =/= 0.U)
  id_reg_write := false.B

  switch(opcode) {
    // R-Type Instructions
    is("b0110011".U) {
      id_reg_write := true.B
      id_illegal   := false.B
      id_uop := MuxLookup(Cat(funct7, funct3), UOpCode.uopNOP, Seq(
        "b0000000_000".U -> UOpCode.uopADD,
        "b0100000_000".U -> UOpCode.uopSUB,
        "b0000000_001".U -> UOpCode.uopSLL,
        "b0000000_010".U -> UOpCode.uopSLT,
        "b0000000_011".U -> UOpCode.uopSLTU,
        "b0000000_100".U -> UOpCode.uopXOR,
        "b0000000_101".U -> UOpCode.uopSRL,
        "b0100000_101".U -> UOpCode.uopSRA,
        "b0000000_110".U -> UOpCode.uopOR,
        "b0000000_111".U -> UOpCode.uopAND
      ))
      when (id_uop === UOpCode.uopNOP) { id_illegal := true.B }
    }
    
    // I-Type ALU Instructions
    is("b0010011".U) {
      id_reg_write := true.B
      id_illegal   := false.B
      id_uop := MuxLookup(funct3, UOpCode.uopNOP, Seq(
        "b000".U -> UOpCode.uopADDI,
        "b010".U -> UOpCode.uopSLTI,
        "b011".U -> UOpCode.uopSLTIU,
        "b100".U -> UOpCode.uopXORI,
        "b110".U -> UOpCode.uopORI,
        "b111".U -> UOpCode.uopANDI,
        "b001".U -> Mux(funct7 === "b0000000".U, UOpCode.uopSLLI, UOpCode.uopNOP),
        "b101".U -> Mux(funct7 === "b0000000".U, UOpCode.uopSRLI, 
                        Mux(funct7 === "b0100000".U, UOpCode.uopSRAI, UOpCode.uopNOP))
      ))
      when (id_uop === UOpCode.uopNOP) { id_illegal := true.B }
    }

    // B-Type Instructions (Branches)
    is("b1100011".U) {
      id_reg_write := false.B
      id_illegal   := false.B
      id_uop := MuxLookup(funct3, UOpCode.uopNOP, Seq(
        "b000".U -> UOpCode.uopBEQ,
        "b001".U -> UOpCode.uopBNE,
        "b100".U -> UOpCode.uopBLT,
        "b101".U -> UOpCode.uopBGE,
        "b110".U -> UOpCode.uopBLTU,
        "b111".U -> UOpCode.uopBGEU
      ))
      when (id_uop === UOpCode.uopNOP) { id_illegal := true.B }
    }

    // JAL Instruction (J-Type Jump)
    is("b1101111".U) {
      id_reg_write := true.B
      id_illegal   := false.B
      id_uop       := UOpCode.uopJAL
    }

    // JALR Instruction (I-Type Jump)
    is("b1100111".U) {
      id_reg_write := true.B
      id_illegal   := false.B
      id_uop       := Mux(funct3 === "b000".U, UOpCode.uopJALR, UOpCode.uopNOP)
      when (id_uop === UOpCode.uopNOP) { id_illegal := true.B }
    }
  }

  io.outUop         := id_uop
  io.outRD          := id_rd
  io.outOperandA    := io.regFileResp_A
  io.outOperandB    := io.regFileResp_B 
  io.outXcptInvalid := id_illegal
  io.outRegWrite    := id_reg_write 
}
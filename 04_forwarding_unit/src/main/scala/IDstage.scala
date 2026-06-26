// ADS I Class Project
// Pipelined RISC-V Core - ID Stage
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/09/2026 by Tobias Jauch (@tojauch)

/*
Instruction Decode (ID) Stage: decoding and operand fetch

Extracted Fields from 32-bit Instruction (see RISC-V specification for reference):
    opcode: instruction format identifier
    funct3: selects variant within instruction format
    funct7: further specifies operation type (R-type only)
    rd: destination register address
    rs1: first source register address
    rs2: second source register address
    imm: 12-bit immediate value (I-type, sign-extended)

Register File Interfaces:
    regFileReq_A, regFileResp_A: read port for rs1 operand
    regFileReq_B, regFileResp_B: read port for rs2 operand

Internal Signals:
    Combinational decoders for instructions

Functionality:
    Decode opcode to determine instruction and identify operation (ADD, SUB, XOR, ...)
    Output: uop (operation code), rd, operandA (from rs1), operandB (rs2 or immediate)

Outputs:
    uop: micro-operation code (identifies instruction type)
    rd: destination register index
    operandA: first operand
    operandB: second operand 
    XcptInvalid: exception flag for invalid instructions
*/

package core_tile

import chisel3._
import chisel3.util._
import UOpCode._

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
    
    
  }

  io.outUop         := id_uop
  io.outRD          := id_rd
  io.outOperandA    := io.regFileResp_A
  io.outOperandB    := io.regFileResp_B 
  io.outXcptInvalid := id_illegal
  io.outRegWrite    := id_reg_write 
}
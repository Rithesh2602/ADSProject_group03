// ADS I Class Project
// Pipelined RISC-V Core
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/15/2023 by Tobias Jauch (@tojauch)

/*
The goal of this task is to implement a 5-stage pipeline that features a subset of RV32I (all R-type and I-type instructions). 

    Instruction Memory:
        The CPU has an instruction memory (IMem) with 4096 words, each of 32 bits.
        The content of IMem is loaded from a binary file specified during the instantiation of the MultiCycleRV32Icore module.

    CPU Registers:
        The CPU has a program counter (PC) and a register file (regFile) with 32 registers, each holding a 32-bit value.
        Register x0 is hard-wired to zero.

    Microarchitectural Registers / Wires:
        Various signals are defined as either registers or wires depending on whether they need to be used in the same cycle or in a later cycle.

    Processor Stages:
        The FSM of the processor has five stages: fetch, decode, execute, memory, and writeback.
        All stages are active at the same time and process different instructions simultaneously.

        Fetch Stage:
            The instruction is fetched from the instruction memory based on the current value of the program counter (PC).

        Decode Stage:
            Instruction fields such as opcode, rd, funct3, and rs1 are extracted.
            For R-type instructions, additional fields like funct7 and rs2 are extracted.
            Control signals (isADD, isSUB, etc.) are set based on the opcode and funct3 values.
            Operands (operandA and operandB) are determined based on the instruction type.

        Execute Stage:
            Arithmetic and logic operations are performed based on the control signals and operands.
            The result is stored in the aluResult register.

        Memory Stage:
            No memory operations are implemented in this basic CPU.

        Writeback Stage:
            The result of the operation (writeBackData) is written back to the destination register (rd) in the register file.

    Check Result:
        The final result (writeBackData) is output to the io.check_res signal.
        The exception signal is also passed to the wrapper module. It indicates whether an invalid instruction has been encountered.
        In the fetch stage, a default value of 0 is assigned to io.check_res.
*/

package core_tile

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile //pre-load a hardware memory module with data from an external text file

import Assignment02.{ALU, ALUOp}
import UOpCode._ 

class PipelinedRISCV32Icore (BinaryFile: String) extends Module {
  val io = IO(new Bundle {
    val check_res = Output(UInt(32.W)) 
    val exception = Output(Bool())
  })

  // -----------------------------------------
  // Architectural Elements & Memories
  // -----------------------------------------
  val imem = Mem(4096, UInt(32.W))
  loadMemoryFromFile(imem, BinaryFile)

  // Register File: 32 entries of 32-bit width
  val regFile = Mem(32, UInt(32.W))

  // -----------------------------------------
  // 1. FETCH (IF) STAGE
  // -----------------------------------------
  val if_pc = RegInit(0.U(32.W))
  val if_instr = imem(if_pc >> 2) 

  // Next PC logic (No branches/jumps in this subset)
  if_pc := if_pc + 4.U

  // --- IF/ID Pipeline Register ---
  val id_pc    = RegNext(if_pc)
  val id_instr = RegNext(if_instr)

  // -----------------------------------------
  // 2. DECODE (ID) STAGE
  // -----------------------------------------
  val opcode = id_instr(6, 0)
  val id_rd  = id_instr(11, 7)
  val funct3 = id_instr(14, 12)
  val id_rs1 = id_instr(19, 15)
  val id_rs2 = id_instr(24, 20)
  val funct7 = id_instr(31, 25)

  // Sign-extend 12-bit immediate for I-type instructions
  val id_imm = Cat(Fill(20, id_instr(31)), id_instr(31, 20))

  // Synchronous Register File Read with x0 hardwired to 0
  val id_rs1_data = Mux(id_rs1 === 0.U, 0.U, regFile(id_rs1))
  val id_rs2_data = Mux(id_rs2 === 0.U, 0.U, regFile(id_rs2))

  // Control Logic using your UOpCode Enum
  val id_uop       = Wire(UOpCode())
  val id_illegal   = Wire(Bool())
  val id_reg_write = Wire(Bool())

  // Default assignments: If no 'is' block matches below, these remain active!
  id_uop       := UOpCode.uopNOP
  id_illegal   := (id_instr =/= 0.U) // Illegal if it isn't a structural NOP bubble (0x00000000)
  id_reg_write := false.B

  switch(opcode) {
    // R-type instructions
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
    // I-type instructions
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

  // --- ID/EX Pipeline Register ---
  val ex_uop       = RegNext(id_uop, UOpCode.uopNOP)
  val ex_rs1_data  = RegNext(id_rs1_data)
  val ex_rs2_data  = RegNext(id_rs2_data)
  val ex_imm       = RegNext(id_imm)
  val ex_rd        = RegNext(id_rd)
  val ex_reg_write = RegNext(id_reg_write, false.B)
  val ex_exception = RegNext(id_illegal, false.B)

  // -----------------------------------------
  // 3. EXECUTE (EX) STAGE
  // -----------------------------------------
  val alu_op = Wire(ALUOp())
  val srcB_is_imm = Wire(Bool())

  alu_op := ALUOp.ADD 
  srcB_is_imm := false.B

  switch(ex_uop) {
    is(UOpCode.uopADD)   { alu_op := ALUOp.ADD;   srcB_is_imm := false.B }
    is(UOpCode.uopSUB)   { alu_op := ALUOp.SUB;   srcB_is_imm := false.B }
    is(UOpCode.uopSLL)   { alu_op := ALUOp.SLL;   srcB_is_imm := false.B }
    is(UOpCode.uopSLT)   { alu_op := ALUOp.SLT;   srcB_is_imm := false.B }
    is(UOpCode.uopSLTU)  { alu_op := ALUOp.SLTU;  srcB_is_imm := false.B }
    is(UOpCode.uopXOR)   { alu_op := ALUOp.XOR;   srcB_is_imm := false.B }
    is(UOpCode.uopSRL)   { alu_op := ALUOp.SRL;   srcB_is_imm := false.B }
    is(UOpCode.uopSRA)   { alu_op := ALUOp.SRA;   srcB_is_imm := false.B }
    is(UOpCode.uopOR)    { alu_op := ALUOp.OR;    srcB_is_imm := false.B }
    is(UOpCode.uopAND)   { alu_op := ALUOp.AND;   srcB_is_imm := false.B }
    
    is(UOpCode.uopADDI)  { alu_op := ALUOp.ADD;   srcB_is_imm := true.B }
    is(UOpCode.uopSLTI)  { alu_op := ALUOp.SLT;   srcB_is_imm := true.B }
    is(UOpCode.uopSLTIU) { alu_op := ALUOp.SLTU;  srcB_is_imm := true.B }
    is(UOpCode.uopXORI)  { alu_op := ALUOp.XOR;   srcB_is_imm := true.B }
    is(UOpCode.uopORI)   { alu_op := ALUOp.OR;    srcB_is_imm := true.B }
    is(UOpCode.uopANDI)  { alu_op := ALUOp.AND;   srcB_is_imm := true.B }
    is(UOpCode.uopSLLI)  { alu_op := ALUOp.SLL;   srcB_is_imm := true.B }
    is(UOpCode.uopSRLI)  { alu_op := ALUOp.SRL;   srcB_is_imm := true.B }
    is(UOpCode.uopSRAI)  { alu_op := ALUOp.SRA;   srcB_is_imm := true.B }
  }

  // Instantiate the ALU from Assignment02
  val alu = Module(new ALU)
  alu.io.operandA  := ex_rs1_data
  alu.io.operandB  := Mux(srcB_is_imm, ex_imm, ex_rs2_data)
  alu.io.operation := alu_op

  // --- EX/MEM Pipeline Register ---
  val mem_alu_res   = RegNext(alu.io.aluResult)
  val mem_rd        = RegNext(ex_rd)
  val mem_reg_write = RegNext(ex_reg_write, false.B)  
  val mem_exception = RegNext(ex_exception, false.B)

  // -----------------------------------------
  // 4. MEMORY (MEM) STAGE
  // -----------------------------------------

  // --- MEM/WB Pipeline Register ---
  val wb_alu_res   = RegNext(mem_alu_res)
  val wb_rd        = RegNext(mem_rd)
  val wb_reg_write = RegNext(mem_reg_write, false.B)
  val wb_exception = RegNext(mem_exception, false.B)

  // -----------------------------------------
  // 5. WRITEBACK (WB) STAGE
  // -----------------------------------------
  when(wb_reg_write && (wb_rd =/= 0.U)) {
    regFile(wb_rd) := wb_alu_res
  }

  io.check_res := wb_alu_res
  io.exception := wb_exception
}
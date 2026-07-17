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

// FIXED NAME: Changed from PipelinedRISCV32Icore to PipelinedRV32Icore to match the wrapper
class PipelinedRV32Icore (BinaryFile: String) extends Module {
  val io = IO(new Bundle {
    val check_res = Output(UInt(32.W)) 
    val exception = Output(Bool())
  })
// -----------------------------------------
  // 1. Central Register File Module Instance
  // -----------------------------------------
  val rFile = Module(new regFile)

  // -----------------------------------------
  // 2. Instantiate All Modular Stages & Barriers
  // -----------------------------------------
  val stage_IF   = Module(new IF(BinaryFile))
  val bar_IF_ID  = Module(new IFBarrier)
  
  val stage_ID   = Module(new IDStage)
  val bar_ID_EX  = Module(new IDBarrier)
  
  val stage_EX   = Module(new EXStage)
  val bar_EX_MEM = Module(new EXBarrier)
  
  val stage_MEM  = Module(new MEM)
  val bar_MEM_WB = Module(new MEMBarrier)

  val stage_WB   = Module(new WB)
  val bar_WB_Out = Module(new WBBarrier)

  // -----------------------------------------
  // 3. Pipeline Interconnections & Wiring
  // -----------------------------------------

  // --- FETCH (IF) STAGE -> IF/ID BARRIER ---
  bar_IF_ID.io.inInstr := stage_IF.io.instr

  // --- IF/ID BARRIER -> DECODE (ID) STAGE ---
  stage_ID.io.inInstruction := bar_IF_ID.io.outInstr

  // --- DECODE (ID) STAGE <-> REGISTER FILE READ PORTS ---
  rFile.io.req_1.addr       := stage_ID.io.regFileReq_A
  rFile.io.req_2.addr       := stage_ID.io.regFileReq_B
  stage_ID.io.regFileResp_A := rFile.io.resp_1.data
  stage_ID.io.regFileResp_B := rFile.io.resp_2.data

  // --- DECODE (ID) STAGE -> ID/EX BARRIER ---
  bar_ID_EX.io.inUOP           := stage_ID.io.outUop
  bar_ID_EX.io.inRD            := stage_ID.io.outRD
  bar_ID_EX.io.inOperandA      := stage_ID.io.outOperandA
  bar_ID_EX.io.inOperandB      := stage_ID.io.outOperandB
  bar_ID_EX.io.inXcptInvalid   := stage_ID.io.outXcptInvalid

  // --- ID/EX BARRIER -> EXECUTE (EX) STAGE ---
  stage_EX.io.inUop         := bar_ID_EX.io.outUOP
  stage_EX.io.inRs1Data     := bar_ID_EX.io.outOperandA
  stage_EX.io.inRs2Data     := bar_ID_EX.io.outOperandB
  stage_EX.io.inImm         := bar_ID_EX.io.outOperandB 
  stage_EX.io.inXcptId      := bar_ID_EX.io.outXcptInvalid

  // --- EXECUTE (EX) STAGE -> EX/MEM BARRIER ---
  bar_EX_MEM.io.inAluResult   := stage_EX.io.aluResult
  bar_EX_MEM.io.inRD          := bar_ID_EX.io.outRD 
  bar_EX_MEM.io.inXcptInvalid := stage_EX.io.exception

  // --- EX/MEM BARRIER -> MEMORY (MEM) STAGE ---
  stage_MEM.io.inAluResult := bar_EX_MEM.io.outAluResult
  stage_MEM.io.inRD        := bar_EX_MEM.io.outRD
  stage_MEM.io.inException := bar_EX_MEM.io.outXcptInvalid

  // --- MEMORY (MEM) STAGE -> MEM/WB BARRIER ---
  bar_MEM_WB.io.inAluResult := stage_MEM.io.outAluResult
  bar_MEM_WB.io.inRD        := stage_MEM.io.outRD
  bar_MEM_WB.io.inException := stage_MEM.io.outException

  // --- MEM/WB BARRIER -> WRITEBACK (WB) STAGE ---
  stage_WB.io.aluResult     := bar_MEM_WB.io.outAluResult
  stage_WB.io.rd            := bar_MEM_WB.io.outRD
  stage_WB.io.inException   := bar_MEM_WB.io.outException

  // --- WRITEBACK (WB) STAGE -> REGISTER FILE WRITE PORT ---
  rFile.io.req_3            := stage_WB.io.regFileReq

  // --- WRITEBACK (WB) STAGE -> WB OUTPUT BARRIER ---
  bar_WB_Out.io.inCheckRes    := stage_WB.io.check_res
  bar_WB_Out.io.inXcptInvalid := bar_MEM_WB.io.outException
  
  // --- DEFINE LOCAL WIRES AND MAP TO TOP IO ---
  val wb_alu_res   = stage_WB.io.check_res
  val wb_exception = bar_WB_Out.io.outXcptInvalid

  io.check_res := wb_alu_res
  io.exception := wb_exception
}
// ADS I Class Project
// Pipelined RISC-V Core
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 05/10/2023 by Tobias Jauch (@tojauch)

/*
This file contains the top-level module for the Pipelined RISC-V 32I core and acts as the interface between the core and external testbenches.
*/

// ADS I Class Project
// Pipelined RISC-V Core Top-Level Integration
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File updated on 06/13/2026 by Gemini AI collaborator

package PipelinedRV32I

import chisel3._
import chisel3.util._
import core_tile._

class PipelinedRV32I (BinaryFile: String) extends Module {
  val io = IO(new Bundle {
    val result    = Output(UInt(32.W)) 
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
  bar_WB_Out.io.inCheckRes  := stage_WB.io.check_res
  bar_WB_Out.io.inXcptInvalid := bar_MEM_WB.io.outException

  // -----------------------------------------
  // 4. Connect Core Outputs to Outer Wrapper Ports
  // -----------------------------------------
  io.result    := bar_WB_Out.io.outCheckRes
  io.exception := bar_WB_Out.io.outXcptInvalid
} 
// ADS I Class Project
// Pipelined RISC-V Core
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern

package core_tile

import chisel3._
import chisel3.util._
import Assignment02.{ALU, ALUOp}
import UOpCode._ 

class PipelinedRV32Icore (BinaryFile: String) extends Module {
  val io = IO(new Bundle {
    val check_res = Output(UInt(32.W)) 
    val exception = Output(Bool())
  })

  val rFile = Module(new regFile)

  // --- INTERNAL INSTRUCTION MEMORY SPECIFICATION ---
  // 4096 words, each 32 bits wide, loaded from the binary target path file
  val iMem = java.nio.file.Files.exists(java.nio.file.Paths.get(BinaryFile)) match {
    case true  => 
      val hexStrings = scala.io.Source.fromFile(BinaryFile).getLines().flatMap(_.split("\\s+")).filter(_.nonEmpty).toList
      val uints = hexStrings.map(h => s"h$h".U(32.W))
      if (uints.nonEmpty) VecInit(uints ++ Seq.fill(4096 - uints.length)(0.U(32.W)))
      else VecInit(Seq.fill(4096)(0.U(32.W)))
    case false => 
      VecInit(Seq.fill(4096)(0.U(32.W)))
  }

  val stage_IF   = Module(new IFstage) 
  val bar_IF_ID  = Module(new IFbarrier)
  
  val stage_ID   = Module(new IDStage)
  val bar_ID_EX  = Module(new IDBarrier)
  
  val forwarding = Module(new ForwardingUnit) 
  
  val stage_EX   = Module(new EXStage)
  val bar_EX_MEM = Module(new EXBarrier)
  
  val stage_MEM  = Module(new MEM)
  val bar_MEM_WB = Module(new MEMBarrier)

  val stage_WB   = Module(new WB)
  val bar_WB_Out = Module(new WBBarrier)

  // --- PROGRAM COUNTER MANAGEMENT CORE ---
  val pcReg = RegInit(0.U(32.W))
  pcReg := stage_IF.io.nextPC

  // --- FETCH (IF) STAGE ---
  stage_IF.io.inPC      := pcReg
  
  // Read from instruction memory array using PC shifted right by 2 to convert to word-aligned index
  val wordAddress = pcReg(13, 2)
  stage_IF.io.inInstr   := iMem(wordAddress)
  
  stage_IF.io.pcSel     := stage_EX.io.pcSelOut
  stage_IF.io.targetPC  := stage_EX.io.targetPCOut

  // --- IF/ID BARRIER ---
  bar_IF_ID.io.inPC     := stage_IF.io.outPC
  bar_IF_ID.io.inInstr  := stage_IF.io.outInstr
  bar_IF_ID.io.flush    := stage_EX.io.pcSelOut 

  // --- DECODE (ID) STAGE ---
  stage_ID.io.inInstruction := bar_IF_ID.io.outInstr
  rFile.io.req_1.addr       := stage_ID.io.regFileReq_A
  rFile.io.req_2.addr       := stage_ID.io.regFileReq_B
  stage_ID.io.regFileResp_A := rFile.io.resp_1.data
  stage_ID.io.regFileResp_B := rFile.io.resp_2.data

  // --- ID/EX BARRIER ---
  bar_ID_EX.io.inUOP           := stage_ID.io.outUop
  bar_ID_EX.io.inRD            := stage_ID.io.outRD
  bar_ID_EX.io.inOperandA      := stage_ID.io.outOperandA
  bar_ID_EX.io.inOperandB      := stage_ID.io.outOperandB 
  bar_ID_EX.io.inXcptInvalid   := stage_ID.io.outXcptInvalid
  bar_ID_EX.io.inRs1           := stage_ID.io.regFileReq_A
  bar_ID_EX.io.inRs2           := stage_ID.io.regFileReq_B
  bar_ID_EX.io.inRegWrite      := stage_ID.io.outRegWrite
  bar_ID_EX.io.inInstr         := bar_IF_ID.io.outInstr
  bar_ID_EX.io.inPC            := bar_IF_ID.io.outPC
  bar_ID_EX.io.flush           := stage_EX.io.pcSelOut 

  // --- FORWARDING UNIT CONTROL SIGNALS ---
  forwarding.io.rs1_EX   := bar_ID_EX.io.outRs1
  forwarding.io.rs2_EX   := bar_ID_EX.io.outRs2
  forwarding.io.rd_MEM   := bar_EX_MEM.io.outRD
  forwarding.io.rd_WB    := bar_MEM_WB.io.outRD
  forwarding.io.wrEn_MEM := bar_EX_MEM.io.outRegWrite
  forwarding.io.wrEn_WB  := bar_MEM_WB.io.outRegWrite

  // --- EXTENDED IMMEDIATE GENERATOR LOGIC ---
  val current_instr = bar_ID_EX.io.outInstr
  val uop           = bar_ID_EX.io.outUOP

  val i_imm = Cat(Fill(20, current_instr(31)), current_instr(31, 20))
  val b_imm = Cat(Fill(19, current_instr(31)), current_instr(31), current_instr(7), current_instr(30, 25), current_instr(11, 8), 0.U(1.W))
  val j_imm = Cat(Fill(11, current_instr(31)), current_instr(31), current_instr(19, 12), current_instr(20), current_instr(30, 21), 0.U(1.W))

  val ex_imm = MuxCase(i_imm, Seq(
    (uop === UOpCode.uopJAL) -> j_imm,
    (uop === UOpCode.uopBEQ  || uop === UOpCode.uopBNE  || uop === UOpCode.uopBLT || 
     uop === UOpCode.uopBGE  || uop === UOpCode.uopBLTU || uop === UOpCode.uopBGEU) -> b_imm
  ))

  // --- EXECUTE (EX) STAGE ---
  stage_EX.io.inUop         := bar_ID_EX.io.outUOP
  stage_EX.io.inRs1Data     := bar_ID_EX.io.outOperandA
  stage_EX.io.inRs2Data     := bar_ID_EX.io.outOperandB 
  stage_EX.io.inImm         := ex_imm 
  stage_EX.io.inXcptId      := bar_ID_EX.io.outXcptInvalid
  stage_EX.io.inPC          := bar_ID_EX.io.outPC
  
  val fwdA_wire = WireDefault(forwarding.io.forwardA)
  val fwdB_wire = WireDefault(forwarding.io.forwardB)
  val memAlu_wire = WireDefault(bar_EX_MEM.io.outAluResult)
  val wbAlu_wire = WireDefault(bar_MEM_WB.io.outAluResult)

  stage_EX.io.forwardA      := fwdA_wire
  stage_EX.io.forwardB      := fwdB_wire
  stage_EX.io.memAluResult  := memAlu_wire
  stage_EX.io.wbAluResult   := wbAlu_wire

  // --- EX/MEM BARRIER ---
  bar_EX_MEM.io.inAluResult   := stage_EX.io.aluResult
  bar_EX_MEM.io.inRD          := bar_ID_EX.io.outRD 
  bar_EX_MEM.io.inXcptInvalid := stage_EX.io.exception
  bar_EX_MEM.io.inRegWrite    := bar_ID_EX.io.outRegWrite

  // --- MEMORY (MEM) STAGE ---
  stage_MEM.io.inAluResult := bar_EX_MEM.io.outAluResult
  stage_MEM.io.inRD        := bar_EX_MEM.io.outRD
  stage_MEM.io.inException := bar_EX_MEM.io.outXcptInvalid
  stage_MEM.io.inRegWrite  := bar_EX_MEM.io.outRegWrite  

  // --- MEM/WB BARRIER ---
  bar_MEM_WB.io.inAluResult := stage_MEM.io.outAluResult
  bar_MEM_WB.io.inRD        := stage_MEM.io.outRD
  bar_MEM_WB.io.inException := stage_MEM.io.outException
  bar_MEM_WB.io.inRegWrite  := stage_MEM.io.outRegWrite 

  // --- WRITEBACK (WB) STAGE ---
  stage_WB.io.aluResult     := bar_MEM_WB.io.outAluResult
  stage_WB.io.rd            := bar_MEM_WB.io.outRD
  stage_WB.io.inException   := bar_MEM_WB.io.outException
  stage_WB.io.inRegWrite    := bar_MEM_WB.io.outRegWrite

  rFile.io.req_3            := stage_WB.io.regFileReq

  // --- WB/OUT BARRIER ---
  bar_WB_Out.io.inCheckRes    := stage_WB.io.check_res
  bar_WB_Out.io.inXcptInvalid := bar_MEM_WB.io.outException

  // --- OUTPUTS ---
  io.check_res := bar_WB_Out.io.outCheckRes
  io.exception := bar_WB_Out.io.outXcptInvalid
}
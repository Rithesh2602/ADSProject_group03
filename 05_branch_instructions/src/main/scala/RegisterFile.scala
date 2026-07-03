// ADS I Class Project
// Pipelined RISC-V Core - Register File
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/09/2026 by Tobias Jauch (@tojauch)

package core_tile

import chisel3._


class regFileReadReq extends Bundle {
  val addr = UInt(5.W)
}

class regFileReadResp extends Bundle {
  val data = UInt(32.W)
}

class regFileWriteReq extends Bundle {
  val addr  = UInt(5.W)
  val data  = UInt(32.W)
  val wr_en = Bool()
}


class regFile extends Module {
  val io = IO(new Bundle {
    // Port 1: First Read Port
    val req_1  = Input(new regFileReadReq)
    val resp_1 = Output(new regFileReadResp)

    // Port 2: Second Read Port
    val req_2  = Input(new regFileReadReq)
    val resp_2 = Output(new regFileReadResp)

    // Port 3: Single Write Port
    val req_3  = Input(new regFileWriteReq)
  })

  // Instantiate the internal register file storage (32 registers, each 32 bits wide)
  val rf = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))

  // Extract write port signals for clarity
  val writeEnable = io.req_3.wr_en && (io.req_3.addr =/= 0.U)
  val writeAddr   = io.req_3.addr
  val writeData   = io.req_3.data

  // Read Operations with Internal Write-to-Read Bypass
  io.resp_1.data := Mux(io.req_1.addr === 0.U, 0.U,
                    Mux(writeEnable && (io.req_1.addr === writeAddr), writeData,
                        rf(io.req_1.addr)))
                        
  io.resp_2.data := Mux(io.req_2.addr === 0.U, 0.U,
                    Mux(writeEnable && (io.req_2.addr === writeAddr), writeData,
                        rf(io.req_2.addr)))

  // Write Operation (Synchronous Write) 
  when(writeEnable) {
    rf(io.req_3.addr) := io.req_3.data
  }
}
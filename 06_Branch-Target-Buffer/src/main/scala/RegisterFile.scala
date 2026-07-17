// ADS I Class Project
// Pipelined RISC-V Core - Register File
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/09/2026 by Tobias Jauch (@tojauch)

package core_tile

import chisel3._

/*
Register File Module: 32x32-bit dual-read single-write register file

Memory:
    regFile: Register file according to the RISC-V 32I specification

Ports:
    req_1, resp_1: first read port
        req_1.addr: read address for register x[0-31]
        resp_1.data: register data output
    req_2, resp_2: second read port
        req_2.addr: read address for register x[0-31]
        resp_2.data: register data output
    req_3: write port
        req_3.addr: write destination address
        req_3.data: data to write
        req_3.wr_en: write enable signal

Functionality:
    Two read ports allow simultaneous reading of two operands
    Synchronous write updates register if wr_en is asserted

Special Case for hazard resolution:    
    If a register is read and written in the same clock cycle, send the new data to data output!
*/

// -----------------------------------------
// Register File Bundles
// -----------------------------------------

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

// -----------------------------------------
// Register File Module
// -----------------------------------------

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
  // Register x0 is structurally initialized to 0 and protected against modifications.
  val rf = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))

  // Extract write port signals for clarity
  val writeEnable = io.req_3.wr_en && (io.req_3.addr =/= 0.U)
  val writeAddr   = io.req_3.addr
  val writeData   = io.req_3.data

  // --- Read Operations with Internal Write-to-Read Bypass ---
  // Priority order:
  // 1. Force hard-wired zero if address is x0
  // 2. If reading a register being updated in the current clock cycle, bypass raw RF and forward writeData directly
  // 3. Otherwise, return the stable stored value inside the architectural register array
  io.resp_1.data := Mux(io.req_1.addr === 0.U, 0.U,
                    Mux(writeEnable && (io.req_1.addr === writeAddr), writeData,
                        rf(io.req_1.addr)))
                        
  io.resp_2.data := Mux(io.req_2.addr === 0.U, 0.U,
                    Mux(writeEnable && (io.req_2.addr === writeAddr), writeData,
                        rf(io.req_2.addr)))

  // --- Write Operation (Synchronous Write) ---
  when(writeEnable) {
    rf(writeAddr) := writeData
  }
}
// ADS I Class Project
// Pipelined RISC-V Core - Complete Integration Test Bench
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File updated for Assignment 05

package PipelinedRV32I_Tester

import chisel3._
import chiseltest._
import core_tile.PipelinedRV32Icore
import org.scalatest.flatspec.AnyFlatSpec

class PipelinedRISCV32ITest extends AnyFlatSpec with ChiselScalatestTester {

  "PipelinedRISCV32ICore" should "verify all arithmetic, forwarding, and control hazards" in {
    test(new PipelinedRV32Icore("src/test/programs/BinaryFile_pipelined")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

      dut.clock.setTimeout(0)

      // =======================================================================
      // PART 1: ARITHMETIC & IN-FLIGHT FORWARDING TESTS (Assignment 3 & 4)
      // =======================================================================
      dut.clock.step(5)
      dut.io.check_res.expect(0.U)         // ADDI x0, x0, 0
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      
      dut.io.check_res.expect(4.U)         // ADDI x1, x0, 4
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      
      dut.io.check_res.expect(5.U)         // ADDI x2, x0, 5
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      
      dut.io.check_res.expect(9.U)         // ADD x3, x1, x2  <- Tests MEM-to-EX ForwardB
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      
      dut.io.check_res.expect(2047.U)   // ADDI x4, x0, 2047
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      
      dut.io.check_res.expect(16.U)     // ADDI x5, x0, 16
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      
      dut.io.check_res.expect(2031.U)   // SUB x6, x4, x5  <- Tests MEM-to-EX ForwardA
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      
      dut.io.check_res.expect(2022.U)   // XOR x7, x6, x3
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      
      dut.io.check_res.expect(2047.U)   // OR x8, x6, x5
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      
      dut.io.check_res.expect(0.U)      // AND x9, x6, x5
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      
      dut.io.check_res.expect(64704.U)  // SLL x10, x7, x2
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      
      dut.io.check_res.expect(63.U)     // SRL x11, x7, x2
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      
      dut.io.check_res.expect(63.U)     // SRA x12, x7, x2
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      
      dut.io.check_res.expect(0.U)      // SLT x13, x4, x4
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      
      dut.io.check_res.expect(0.U)      // SLT x13, x4, x5
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      
      dut.io.check_res.expect(1.U)      // SLT x13, x5, x4
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      
      dut.io.check_res.expect(0.U)      // SLTU x13, x4, x4
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      
      dut.io.check_res.expect(0.U)      // SLTU x13, x4, x5
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      
      dut.io.check_res.expect(1.U)      // SLTU x13, x5, x4
      dut.io.exception.expect(false.B)
      dut.clock.step(1)     

      // =======================================================================
      // PART 2: ADVANCED FORWARDING HAZARD SCENARIOS (Task 4.1 c)
      // =======================================================================

      // 1. WB-to-EX Forwarding Case (2-Cycle Data Hazard)
      // ==========================================================
      dut.io.check_res.expect(50.U)    // ADDI x14, x0, 50
      dut.io.exception.expect(false.B)
      dut.clock.step(1)

      dut.io.check_res.expect(10.U)    // ADDI x15, x0, 10 (x14 updates WB stage buffer)
      dut.io.exception.expect(false.B)
      dut.clock.step(1)

      // Expected calculation: 50 (forwarded via WB stage to OperandA) + 10 = 60
      dut.io.check_res.expect(60.U)    // ADD x16, x14, x15 <- Verifies WB-to-EX Forwarding
      dut.io.exception.expect(false.B)
      dut.clock.step(1)

      // 2. MEM-over-WB Priority Forwarding Case
      // ==========================================================
      dut.io.check_res.expect(100.U)   // ADDI x17, x0, 100
      dut.io.exception.expect(false.B)
      dut.clock.step(1)

      dut.io.check_res.expect(200.U)   // ADDI x17, x0, 200 (Older x17 value moves to MEM)
      dut.io.exception.expect(false.B)
      dut.clock.step(1)

      // Expected calculation: 200 (Newer value drawn from MEM) + 0 = 200
      dut.io.check_res.expect(200.U)   // ADD x18, x17, x0 <- Verifies MEM Priority over WB
      dut.io.exception.expect(false.B)
      dut.clock.step(1)

      // =======================================================================
      // PART 3: CONTROL HAZARDS, JUMPS, AND FLUSHES (Assignment 5)
      // =======================================================================

      // Base Registers Setup
      dut.io.check_res.expect(10.U)     // ADDI x20, x0, 10
      dut.io.exception.expect(false.B)
      dut.clock.step(1)

      dut.io.check_res.expect(10.U)     // ADDI x21, x0, 10
      dut.io.exception.expect(false.B)
      dut.clock.step(1)

      // 1. BEQ Taken Evaluation Cycle
      // ==========================================================
      dut.io.exception.expect(false.B)
      dut.clock.step(1) // Step over branch resolution

      // 2. JAL Unconditional Jump Evaluation Cycle
      // ==========================================================
      dut.io.exception.expect(false.B)
      dut.clock.step(1) // Step over JAL resolution

      // 3. BNE Not Taken Evaluation Cycle & Dynamic Polling
      // ==========================================================
      dut.io.exception.expect(false.B)
      
      // Give the pipeline up to 30 clock cycles to clear out all bubbles 
      // generated by the flushes and execute the target ADDI instruction (30).
      var maxCycles = 30
      while (dut.io.check_res.peek().litValue != 30 && maxCycles > 0) {
        dut.clock.step(1)
        maxCycles -= 1
      }

      // Final validation assertion check
      dut.io.check_res.expect(30.U)     // ADDI x27, x0, 30
      dut.io.exception.expect(false.B)
    }
  }
}
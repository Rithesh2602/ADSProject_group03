// ADS I Class Project
// Pipelined RISC-V Core
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/15/2023 by Tobias Jauch (@tojauch)

package PipelinedRV32I_Tester

import chisel3._
import chiseltest._
import PipelinedRV32I._
import org.scalatest.flatspec.AnyFlatSpec

class PipelinedRISCV32ITest extends AnyFlatSpec with ChiselScalatestTester {

  "RV32I_BasicTester" should "work" in {
    test(new PipelinedRV32I("src/test/programs/BinaryFile_pipelined")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

      dut.clock.setTimeout(0)
      dut.clock.step(5)
      dut.io.result.expect(0.U)     // ADDI x0, x0, 0
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      dut.io.result.expect(4.U)     // ADDI x1, x0, 4
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      dut.io.result.expect(5.U)     // ADDI x2, x0, 5
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      dut.io.result.expect(9.U)     // ADD x3, x1, x2  <- Tests MEM-to-EX ForwardB
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      dut.io.result.expect(2047.U)  // ADDI x4, x0, 2047
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      dut.io.result.expect(16.U)    // ADDI x5, x0, 16
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      dut.io.result.expect(2031.U)  // SUB x6, x4, x5  <- Tests MEM-to-EX ForwardA
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      dut.io.result.expect(2022.U)  // XOR x7, x6, x3
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      dut.io.result.expect(2047.U)  // OR x8, x6, x5
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      dut.io.result.expect(0.U)     // AND x9, x6, x5
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      dut.io.result.expect(64704.U) // SLL x10, x7, x2
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      dut.io.result.expect(63.U)    // SRL x11, x7, x2
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      dut.io.result.expect(63.U)    // SRA x12, x7, x2
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      dut.io.result.expect(0.U)     // SLT x13, x4, x4
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      dut.io.result.expect(0.U)     // SLT x13, x4, x5
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      dut.io.result.expect(1.U)     // SLT x13, x5, x4
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      dut.io.result.expect(0.U)     // SLTU x13, x4, x4
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      dut.io.result.expect(0.U)     // SLTU x13, x4, x5
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
      dut.io.result.expect(1.U)     // SLTU x13, x5, x4
      dut.io.exception.expect(false.B)
      dut.clock.step(1)     

      // =======================================================================
      // MISSING FORWARDING HAZARD SCENARIOS (TASK 4.1 c)
      // =======================================================================

      // 1. WB-to-EX Forwarding Case (2-Cycle Data Hazard)
      // Assembly instruction setup:
      // ADDI x14, x0, 50     - Step A (Computes 50)
      // ADDI x15, x0, 10     - Step B (Independent instruction, fills pipeline bubble)
      // ADD  x16, x14, x15   - Step C (Reads x14 while it is back in the WB stage)
      
      dut.io.result.expect(50.U)    // ADDI x14, x0, 50
      dut.io.exception.expect(false.B)
      dut.clock.step(1)

      dut.io.result.expect(10.U)    // ADDI x15, x0, 10 (x14 shifts to MEM stage)
      dut.io.exception.expect(false.B)
      dut.clock.step(1)

      // Expected calculation: 50 (forwarded via WB stage to OperandA) + 10 = 60
      dut.io.result.expect(60.U)    // ADD x16, x14, x15 <- Verifies WB-to-EX Forwarding
      dut.io.exception.expect(false.B)
      dut.clock.step(1)

      // -----------------------------------------------------------------------

      // 2. MEM-over-WB Priority Forwarding Case
      // Assembly instruction setup:
      // ADDI x17, x0, 100    - Step A: Older value (Moves to WB stage)
      // ADDI x17, x0, 200    - Step B: Newer value (Moves to MEM stage)
      // ADD  x18, x17, x0    - Step C: Reads x17. Must pull newer 200 from MEM, NOT 100 from WB.
      
      dut.io.result.expect(100.U)   // ADDI x17, x0, 100
      dut.io.exception.expect(false.B)
      dut.clock.step(1)

      dut.io.result.expect(200.U)   // ADDI x17, x0, 200 (Older x17 is now in MEM stage)
      dut.io.exception.expect(false.B)
      dut.clock.step(1)

      // Expected calculation: 200 (Newer value from MEM) + 0 = 200
      // If priority fails, it will incorrectly output 100 + 0 = 100.
      dut.io.result.expect(200.U)   // ADD x18, x17, x0 <- Verifies MEM Priority over WB
      dut.io.exception.expect(false.B)
      dut.clock.step(1)
    }
  }
}
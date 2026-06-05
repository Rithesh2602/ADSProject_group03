// ADS I Class Project
// Pipelined RISC-V Core with Hazard Detection and Resolution
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 10/31/2025 by Tobias Jauch (tobias.jauch@rptu.de)

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

import Assignment02._

// Test ADD operation
class ALUAddTest extends AnyFlatSpec with ChiselScalatestTester {
  "ALU_Add_Tester" should "test ALL operation" in {
    test(new ALU).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
     dut.clock.setTimeout(0)

     // --- 1. ADD OPERATION (Your Base Case) ---
      dut.io.operandA.poke(10.U)
      dut.io.operandB.poke(10.U)
      dut.io.operation.poke(ALUOp.ADD)
      dut.io.aluResult.expect(20.U)
      dut.clock.step(1)

      // --- 2. ADD OVERFLOW CORNER CASE ---
      // Max 32-bit Unsigned Integer + 1 should wrap around to 0
      dut.io.operandA.poke("hFFFF_FFFF".U)
      dut.io.operandB.poke(1.U)
      dut.io.operation.poke(ALUOp.ADD)
      dut.io.aluResult.expect(0.U)
      dut.clock.step(1)

      // --- 3. SUB UNDERFLOW CORNER CASE ---
      // 0 - 1 should wrap around to Max 32-bit Unsigned Integer
      dut.io.operandA.poke(0.U)
      dut.io.operandB.poke(1.U)
      dut.io.operation.poke(ALUOp.SUB)
      dut.io.aluResult.expect("hFFFF_FFFF".U)
      dut.clock.step(1)

      // --- 4. SHIFT LEFT LOGICAL (SLL) WITH 5-BIT MASKING ---
      // 35 gets masked to 3 (35 MOD 32). 7 shifted left by 3 is 56.
      dut.io.operandA.poke(7.U)
      dut.io.operandB.poke(35.U)
      dut.io.operation.poke(ALUOp.SLL)
      dut.io.aluResult.expect(56.U)
      dut.clock.step(1)

      // --- 5. SHIFT RIGHT ARITHMETIC (SRA) SIGN-EXTENSION ---
      // Top bit is 1 (negative). Shifting right preserves the 1s.
      dut.io.operandA.poke("h8000_0000".U)
      dut.io.operandB.poke(4.U)
      dut.io.operation.poke(ALUOp.SRA)
      dut.io.aluResult.expect("hF800_0000".U)
      dut.clock.step(1)

      // --- 6. SIGNED COMPARISON (SLT) ---
      // Interpreted as: -1 < 1 which is TRUE (1)
      dut.io.operandA.poke("hFFFF_FFFF".U) // -1 in signed
      dut.io.operandB.poke(1.U)
      dut.io.operation.poke(ALUOp.SLT)
      dut.io.aluResult.expect(1.U)
      dut.clock.step(1)

      // --- 7. UNSIGNED COMPARISON (SLTU) ---
      // Interpreted as: 4,294,967,295 < 1 which is FALSE (0)
      dut.io.operandA.poke("hFFFF_FFFF".U) 
      dut.io.operandB.poke(1.U)
      dut.io.operation.poke(ALUOp.SLTU)
      dut.io.aluResult.expect(0.U)
      dut.clock.step(1)

      // --- 8. BITWISE LOGICAL AND ---
      dut.io.operandA.poke("hAAAA_BBBB".U)
      dut.io.operandB.poke("h5555_BBBB".U)
      dut.io.operation.poke(ALUOp.AND)
      dut.io.aluResult.expect("h0000_BBBB".U)
      dut.clock.step(1)

      // --- 9. PASSB ---
      dut.io.operandB.poke("hDEAD_BEEF".U)
      dut.io.operation.poke(ALUOp.PASSB)
      dut.io.aluResult.expect("hDEAD_BEEF".U)
      dut.clock.step(1)
    }
  }
}

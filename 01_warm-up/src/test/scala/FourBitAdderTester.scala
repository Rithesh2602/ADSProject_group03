// ADS I Class Project
// Chisel Introduction
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 18/10/2022 by Tobias Jauch (@tojauch)

package adder

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class FourBitAdderTester extends AnyFlatSpec with ChiselScalatestTester {

  "4-bit Adder" should "work" in {
    test(new FourBitAdder).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

      // Test all 4-bit input combinations
      for (a <- 0 until 16) {
        for (b <- 0 until 16) {

          // Apply inputs
          dut.io.a.poke(a.U)
          dut.io.b.poke(b.U)

          // Advance simulation
          dut.clock.step(1)

          // Expected result
          val result = a + b

          // Check 4-bit sum
          dut.io.sum.expect((result % 16).U)

          // Check carry-out
          if (result >= 16) {
            dut.io.cout.expect(true.B)
          } else {
            dut.io.cout.expect(false.B)
          }
        }
      }

    }
  }
}
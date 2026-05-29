/// ADS I Class Project
// Chisel Introduction
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 18/10/2022 by Tobias Jauch (@tojauch)

package adder

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class FullAdderTester extends AnyFlatSpec with ChiselScalatestTester {

  "FullAdder" should "work" in {
    test(new FullAdder).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

      // ------------------------------------------------
      // Test case 1: 0 + 0 + 0 = 0, Carry = 0
      // ------------------------------------------------
      dut.io.a.poke(false.B)
      dut.io.b.poke(false.B)
      dut.io.cin.poke(false.B)

      dut.clock.step(1)

      dut.io.sum.expect(false.B)
      dut.io.cout.expect(false.B)

      // HA1 outputs
      dut.io.ha1_sum.expect(false.B)
      dut.io.ha1_carry.expect(false.B)

      // HA2 outputs
      dut.io.ha2_sum.expect(false.B)
      dut.io.ha2_carry.expect(false.B)

      // ------------------------------------------------
      // Test case 2: 0 + 0 + 1 = 1, Carry = 0
      // ------------------------------------------------
      dut.io.a.poke(false.B)
      dut.io.b.poke(false.B)
      dut.io.cin.poke(true.B)

      dut.clock.step(1)

      dut.io.sum.expect(true.B)
      dut.io.cout.expect(false.B)

      // HA1 outputs
      dut.io.ha1_sum.expect(false.B)
      dut.io.ha1_carry.expect(false.B)

      // HA2 outputs
      dut.io.ha2_sum.expect(true.B)
      dut.io.ha2_carry.expect(false.B)

      // ------------------------------------------------
      // Test case 3: 0 + 1 + 0 = 1, Carry = 0
      // ------------------------------------------------
      dut.io.a.poke(false.B)
      dut.io.b.poke(true.B)
      dut.io.cin.poke(false.B)

      dut.clock.step(1)

      dut.io.sum.expect(true.B)
      dut.io.cout.expect(false.B)

      // HA1 outputs
      dut.io.ha1_sum.expect(true.B)
      dut.io.ha1_carry.expect(false.B)

      // HA2 outputs
      dut.io.ha2_sum.expect(true.B)
      dut.io.ha2_carry.expect(false.B)

      // ------------------------------------------------
      // Test case 4: 0 + 1 + 1 = 0, Carry = 1
      // ------------------------------------------------
      dut.io.a.poke(false.B)
      dut.io.b.poke(true.B)
      dut.io.cin.poke(true.B)

      dut.clock.step(1)

      dut.io.sum.expect(false.B)
      dut.io.cout.expect(true.B)

      // HA1 outputs
      dut.io.ha1_sum.expect(true.B)
      dut.io.ha1_carry.expect(false.B)

      // HA2 outputs
      dut.io.ha2_sum.expect(false.B)
      dut.io.ha2_carry.expect(true.B)

      // ------------------------------------------------
      // Test case 5: 1 + 0 + 0 = 1, Carry = 0
      // ------------------------------------------------
      dut.io.a.poke(true.B)
      dut.io.b.poke(false.B)
      dut.io.cin.poke(false.B)

      dut.clock.step(1)

      dut.io.sum.expect(true.B)
      dut.io.cout.expect(false.B)

      // HA1 outputs
      dut.io.ha1_sum.expect(true.B)
      dut.io.ha1_carry.expect(false.B)

      // HA2 outputs
      dut.io.ha2_sum.expect(true.B)
      dut.io.ha2_carry.expect(false.B)

      // ------------------------------------------------
      // Test case 6: 1 + 0 + 1 = 0, Carry = 1
      // ------------------------------------------------
      dut.io.a.poke(true.B)
      dut.io.b.poke(false.B)
      dut.io.cin.poke(true.B)

      dut.clock.step(1)

      dut.io.sum.expect(false.B)
      dut.io.cout.expect(true.B)

      // HA1 outputs
      dut.io.ha1_sum.expect(true.B)
      dut.io.ha1_carry.expect(false.B)

      // HA2 outputs
      dut.io.ha2_sum.expect(false.B)
      dut.io.ha2_carry.expect(true.B)

      // ------------------------------------------------
      // Test case 7: 1 + 1 + 0 = 0, Carry = 1
      // ------------------------------------------------
      dut.io.a.poke(true.B)
      dut.io.b.poke(true.B)
      dut.io.cin.poke(false.B)

      dut.clock.step(1)

      dut.io.sum.expect(false.B)
      dut.io.cout.expect(true.B)

      // HA1 outputs
      dut.io.ha1_sum.expect(false.B)
      dut.io.ha1_carry.expect(true.B)

      // HA2 outputs
      dut.io.ha2_sum.expect(false.B)
      dut.io.ha2_carry.expect(false.B)

      // ------------------------------------------------
      // Test case 8: 1 + 1 + 1 = 1, Carry = 1
      // ------------------------------------------------
      dut.io.a.poke(true.B)
      dut.io.b.poke(true.B)
      dut.io.cin.poke(true.B)

      dut.clock.step(1)

      dut.io.sum.expect(true.B)
      dut.io.cout.expect(true.B)

      // HA1 outputs
      dut.io.ha1_sum.expect(false.B)
      dut.io.ha1_carry.expect(true.B)

      // HA2 outputs
      dut.io.ha2_sum.expect(true.B)
      dut.io.ha2_carry.expect(false.B)

      dut.clock.step(5)
    }
  }
}

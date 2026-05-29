// ADS I Class Project
// Chisel Introduction
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 18/10/2022 by Tobias Jauch (@tojauch)

package adder

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class HalfAdderTester extends AnyFlatSpec with ChiselScalatestTester {

  "HalfAdder" should "work" in {
    test(new HalfAdder).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      
      // Test case 1: 0 + 0 = 0, Carry = 0
      dut.io.a.poke(false.B)
      dut.io.b.poke(false.B)
      dut.clock.step(1)
      dut.io.sum.expect(false.B)
      dut.io.carry.expect(false.B)

      // Test case 2: 0 + 1 = 1, Carry = 0
      dut.io.a.poke(false.B)
      dut.io.b.poke(true.B)
      dut.clock.step(1)
      dut.io.sum.expect(true.B)
      dut.io.carry.expect(false.B)

      // Test case 3: 1 + 0 = 1, Carry = 0
      dut.io.a.poke(true.B)
      dut.io.b.poke(false.B)
      dut.clock.step(1)
      dut.io.sum.expect(true.B)
      dut.io.carry.expect(false.B)

      // Test case 4: 1 + 1 = 0, Carry = 1
      dut.io.a.poke(true.B)
      dut.io.b.poke(true.B)
      dut.clock.step(1)
      dut.io.sum.expect(false.B)
      dut.io.carry.expect(true.B)
    }
  } 
}

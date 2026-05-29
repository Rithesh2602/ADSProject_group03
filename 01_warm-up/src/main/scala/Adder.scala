// ADS I Class Project
// Chisel Introduction
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 18/10/2022 by Tobias Jauch (@tojauch)

package adder

import chisel3._
import chisel3.util._

/**
* Half Adder Class
* Each signal is one bit wide (inputs and outputs)[cite: 77, 80].
* This component has combinational behavior[cite: 108].
*/
class HalfAdder extends Module {
  val io = IO(new Bundle {
    val a = Input(Bool())
    val b = Input(Bool())
    val sum = Output(Bool())
    val carry = Output(Bool())
  })

  // Logical behavior: Sum is XOR, Carry is AND
  io.sum := io.a ^ io.b
  io.carry := io.a & io.b
}

/**
* Full Adder Class
* Implemented using two half adders and an OR gate.
*/

class FullAdder extends Module {
  val io = IO(new Bundle {
    // Top-Level Inputs
    val a         = Input(Bool())
    val b         = Input(Bool())
    val cin       = Input(Bool())
    
    // Top-Level Outputs
    val sum       = Output(Bool())
    val cout      = Output(Bool())
    
    // Exposed Internal Outputs for Testbench Monitoring
    val ha1_sum   = Output(Bool())
    val ha1_carry = Output(Bool())
    val ha2_sum   = Output(Bool())
    val ha2_carry = Output(Bool())
  })

  // Instantiate the sub-modules
  val ha1 = Module(new HalfAdder())
  val ha2 = Module(new HalfAdder())

  // Wire Half Adder 1 (Takes external inputs A and B)
  ha1.io.a := io.a
  ha1.io.b := io.b

  // Wire Half Adder 2 (Takes HA1 Sum and external Carry In)
  ha2.io.a := ha1.io.sum
  ha2.io.b := io.cin

  // Drive the exposed testbench debug ports
  io.ha1_sum   := ha1.io.sum
  io.ha1_carry := ha1.io.carry
  io.ha2_sum   := ha2.io.sum
  io.ha2_carry := ha2.io.carry

  // Drive the final top-level circuit outputs
  io.sum  := ha2.io.sum
  io.cout := ha1.io.carry | ha2.io.carry
}

/**
* 4-bit Adder class
* Implements a 4-bit ripple-carry-adder manually using 4 Full Adders.
*/
class FourBitAdder extends Module {
  val io = IO(new Bundle {
    val a    = Input(UInt(4.W))
    val b    = Input(UInt(4.W))
    val sum  = Output(UInt(4.W))
    val cout = Output(Bool())
  })

  // Instantiate 1 Half Adder for the LSB and 3 Full Adders for the rest
  val ha  = Module(new HalfAdder())
  val fa2 = Module(new FullAdder())
  val fa3 = Module(new FullAdder())
  val fa4 = Module(new FullAdder())

  // Bit 0: Handled by the Half Adder (No cin needed)
  ha.io.a := io.a(0)
  ha.io.b := io.b(0)

  // Bit 1: Connect second bits and ripple carry from the Half Adder
  fa2.io.a   := io.a(1)
  fa2.io.b   := io.b(1)
  fa2.io.cin := ha.io.carry

  // Bit 2: Connect third bits and ripple carry from fa2
  fa3.io.a   := io.a(2)
  fa3.io.b   := io.b(2)
  fa3.io.cin := fa2.io.cout

  // Bit 3: Connect fourth bits and ripple carry from fa3
  fa4.io.a   := io.a(3)
  fa4.io.b   := io.b(3)
  fa4.io.cin := fa3.io.cout

  // Combine individual sum bits into a 4-bit result (MSB to LSB)
  io.sum := Cat(fa4.io.sum, fa3.io.sum, fa2.io.sum, ha.io.sum)
  
  // The final carry-out comes from the last Full Adder
  io.cout := fa4.io.cout
}
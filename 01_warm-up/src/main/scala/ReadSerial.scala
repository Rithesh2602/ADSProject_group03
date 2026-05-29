// ADS I Class Project
// Chisel Introduction
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 18/10/2022 by Tobias Jauch (@tojauch)


package readserial

import chisel3._
import chisel3.util._

/** controller class */
class Controller extends Module {

  val io = IO(new Bundle {
    val rxd       = Input(Bool())
    val countDone = Input(Bool())

    val enable    = Output(Bool())
    val valid     = Output(Bool())
  })

  // States
  val idle :: receive :: done :: Nil = Enum(3)

  val state = RegInit(idle)

  // Default outputs
  io.enable := false.B
  io.valid := false.B

  switch(state) {

    is(idle) {
      when(io.rxd === false.B) {
        state := receive
      }
    }

    is(receive) {
      io.enable := true.B

      when(io.countDone) {
        state := done
      }
    }

    is(done) {
      io.valid := true.B
      state := idle
    }
  }
}


/** counter class */
class Counter extends Module {

  val io = IO(new Bundle {
    val enable = Input(Bool())

    val done   = Output(Bool())
    val count  = Output(UInt(4.W))
  })

  val counter = RegInit(0.U(4.W))

  when(io.enable) {
    counter := counter + 1.U
  }.otherwise {
    counter := 0.U
  }

  io.count := counter

  io.done := (counter === 7.U)
}


/** shift register class */
class ShiftRegister extends Module {

  val io = IO(new Bundle {
    val enable = Input(Bool())
    val rxd    = Input(Bool())

    val data   = Output(UInt(8.W))
  })

  val shiftReg = RegInit(0.U(8.W))

  when(io.enable) {

    // MSB first shifting
    shiftReg := Cat(shiftReg(6,0), io.rxd)
  }

  io.data := shiftReg
}


/** Serial Receiver */
class ReadSerial extends Module {

  val io = IO(new Bundle {

    val rxd   = Input(Bool())

    val data  = Output(UInt(8.W))
    val valid = Output(Bool())
  })

  // Instantiate modules
  val controller = Module(new Controller())
  val counter    = Module(new Counter())
  val shiftReg   = Module(new ShiftRegister())

  // Connections

  controller.io.rxd := io.rxd
  controller.io.countDone := counter.io.done

  counter.io.enable := controller.io.enable

  shiftReg.io.enable := controller.io.enable
  shiftReg.io.rxd := io.rxd

  // Outputs
  io.data := shiftReg.io.data
  io.valid := controller.io.valid
}
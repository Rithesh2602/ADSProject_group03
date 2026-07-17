// ADS I Class Project
// Pipelined RISC-V Core - Branch Target Buffer
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 05/12/2026 by Tobias Jauch (@tojauch)

/*
Branch Target Buffer (BTB): a hardware component that predicts the target address of conditional branch instructions to improve pipeline performance

Functionality (cf. slide 6-48 of the lecture slides):
    Stores target addresses and prediction information for conditional branch instructions
    On a branch instruction, checks if the instruction is in the BTB and retrieves the predicted target address and prediction state
    If the prediction is taken, the processor fetches the instruction from the predicted target address; if not taken, it continues sequentially
    Updates the BTB entry based on the actual outcome of the branch instruction (taken or not taken) and updates the prediction state accordingly

Inputs:
    PC: A 32-bit program counter representing the address of the branch instruction being fetched or executed.
    update: A 1-bit signal indicating whether the BTB should be updated with new information.
    updatePC: A 32-bit program counter associated with the branch instruction being updated.
    updateTarget: A 32-bit branch target address to be stored in the BTB.
    mispredicted: A 1-bit signal indicating whether the prediction turned out to be incorrect during execution (used to update the predictor).

Outputs:
    valid: A 1-bit signal indicating whether the BTB has a valid prediction for the provided program counter.
    target: A 32-bit signal representing the predicted branch target address when a valid prediction exists.
    predictTaken: A 1-bit signal indicating whether the branch is predicted to be taken or not.

*/

package core_tile

import chisel3._
import chisel3.util._
import core_tile.UOpCode._

// -----------------------------------------
// Branch Target Buffer
// -----------------------------------------

class BTB extends Module {
  val io = IO(new Bundle {
    // Inputs
    val PC           = Input(UInt(32.W))
    val update       = Input(Bool())
    val updatePC     = Input(UInt(32.W))
    val updateTarget = Input(UInt(32.W))
    val mispredicted = Input(Bool())

    // Outputs
    val valid        = Output(Bool())
    val target       = Output(UInt(32.W))
    val predictTaken = Output(Output(Bool()))
  })

  // 2-bit Saturating Counter State Definitions (Slide 6-47)
  val sStrongNotTaken = 0.U(2.W)
  val sWeakNotTaken   = 1.U(2.W)
  val sWeakTaken      = 2.U(2.W)
  val sStrongTaken    = 3.U(2.W)

  // Internal BTB Entry structural layout
  class BTBEntry extends Bundle {
    val valid     = Bool()
    val tag       = UInt(27.W) // 32 bits - 2 (alignment) - 3 (index) = 27 bits
    val target    = UInt(32.W)
    val predState = UInt(2.W)
  }

  // Hardware Storage: 8 sets, 2 ways per set
  val btbRegs = RegInit(VecInit(Seq.fill(8)(VecInit(Seq.fill(2)(0.U.asTypeOf(new BTBEntry))))))
  
  // LRU Tracking Strategy: 1 bit per set. 
  // If 0 -> Way 0 is Least Recently Used (evict Way 0). If 1 -> Way 1 is LRU.
  val lruRegs = RegInit(VecInit(Seq.fill(8)(0.U(1.W))))

  // -------------------------------------------------------------------------
  // 1. LOOKUP OPERATION (Combinational / Fetch Stage)
  // -------------------------------------------------------------------------
  val lookupIdx = io.PC(4, 2)   // Bits [4:2] to address 8 sets
  val lookupTag = io.PC(31, 5)  // Bits [31:5] for unique tag check

  val hitWay0 = btbRegs(lookupIdx)(0).valid && (btbRegs(lookupIdx)(0).tag === lookupTag)
  val hitWay1 = btbRegs(lookupIdx)(1).valid && (btbRegs(lookupIdx)(1).tag === lookupTag)

  // Default output state when there is a cache miss
  io.valid        := false.B
  io.target       := 0.U
  io.predictTaken := false.B

  when(hitWay0) {
    io.valid        := true.B
    io.target       := btbRegs(lookupIdx)(0).target
    io.predictTaken := btbRegs(lookupIdx)(0).predState(1) // MSB determines Taken (1) vs Not Taken (0)
    
    // Maintain LRU state: Way 0 was just accessed, so Way 1 becomes the next candidate for eviction
    when(!io.update) { lruRegs(lookupIdx) := 1.U }
  }.elsewhen(hitWay1) {
    io.valid        := true.B
    io.target       := btbRegs(lookupIdx)(1).target
    io.predictTaken := btbRegs(lookupIdx)(1).predState(1)
    
    // Maintain LRU state: Way 1 was just accessed, so Way 0 becomes the next candidate for eviction
    when(!io.update) { lruRegs(lookupIdx) := 0.U }
  }

  // -------------------------------------------------------------------------
  // 2. UPDATE OPERATION (Sequential / Execute Phase Commit)
  // -------------------------------------------------------------------------
  val updateIdx = io.updatePC(4, 2)
  val updateTag = io.updatePC(31, 5)

  val updateHitWay0 = btbRegs(updateIdx)(0).valid && (btbRegs(updateIdx)(0).tag === updateTag)
  val updateHitWay1 = btbRegs(updateIdx)(1).valid && (btbRegs(updateIdx)(1).tag === updateTag)
  val updateHit     = updateHitWay0 || updateHitWay1

  // Determine actual baseline outcome of the branch execution
  // If we predicted TAKEN and got mispredicted -> actual outcome was NOT TAKEN
  // If we predicted NOT TAKEN (or Missed) and got mispredicted -> actual outcome was TAKEN
  val wasPredictedTaken = Mux(updateHitWay0, btbRegs(updateIdx)(0).predState(1), 
                          Mux(updateHitWay1, btbRegs(updateIdx)(1).predState(1), false.B))
  val actualTaken       = wasPredictedTaken ^ io.mispredicted

  // Helper FSM function to step up/down the saturating counter 
  def getNextState(currentState: UInt, taken: Bool): UInt = {
    val nextState = Wire(UInt(2.W))
    nextState := currentState
    when(taken) {
      when(currentState =/= sStrongTaken) { nextState := currentState + 1.U }
    }.otherwise {
      when(currentState =/= sStrongNotTaken) { nextState := currentState - 1.U }
    }
    nextState
  }

  when(io.update) {
    when(updateHit) {
      // --- Case A: Entry already present. Adjust state machine & update target address ---
      when(updateHitWay0) {
        btbRegs(updateIdx)(0).target    := io.updateTarget
        btbRegs(updateIdx)(0).predState := getNextState(btbRegs(updateIdx)(0).predState, actualTaken)
        lruRegs(updateIdx)              := 1.U // Way 0 touched, Way 1 becomes LRU
      }.otherwise {
        btbRegs(updateIdx)(1).target    := io.updateTarget
        btbRegs(updateIdx)(1).predState := getNextState(btbRegs(updateIdx)(1).predState, actualTaken)
        lruRegs(updateIdx)              := 0.U // Way 1 touched, Way 0 becomes LRU
      }
    }.otherwise {
      // --- Case B: Cache Allocation Miss! Evict using LRU replacement policy ---
      val victimWay = lruRegs(updateIdx)
      
      btbRegs(updateIdx)(victimWay).valid     := true.B
      btbRegs(updateIdx)(victimWay).tag       := updateTag
      btbRegs(updateIdx)(victimWay).target    := io.updateTarget
      
      // Initialize to a weak state corresponding directly with this first actual outcome
      btbRegs(updateIdx)(victimWay).predState := Mux(actualTaken, sWeakTaken, sWeakNotTaken)
      
      // Pivot the LRU bit to protect our freshly updated way
      lruRegs(updateIdx) := ~victimWay
    }
  }
}
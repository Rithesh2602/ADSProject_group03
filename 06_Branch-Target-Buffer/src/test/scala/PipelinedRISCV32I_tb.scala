// ADS I Class Project
// Pipelined RISC-V Core - Branch Target Buffer Unit Test Bench
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created for Assignment 06

package PipelinedRV32I_Tester

import chisel3._
import chiseltest._
import core_tile.BTB
import org.scalatest.flatspec.AnyFlatSpec

class BTBTest extends AnyFlatSpec with ChiselScalatestTester {

  "BranchTargetBuffer" should "verify hits, updates, state machine transitions, and LRU eviction" in {
    test(new BTB).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      
      // FSM State Encoding References
      // 0 -> strongNotTaken, 1 -> weakNotTaken, 2 -> weakTaken, 3 -> strongTaken

      // =======================================================================
      // TEST 1: CACHE MISSES ON COLD INITIALIZATION //BTB IS EMPTY IN THE BEGINNING
      // =======================================================================
      // Querying an empty BTB should return an invalid prediction flag
      dut.io.PC.poke("h0000_1000".U)
      dut.io.valid.expect(false.B)
      dut.io.predictTaken.expect(false.B)
      dut.clock.step(1)

      // =======================================================================
      // TEST 2: ALLOCATION AND INITIALIZATION (WEAK STATE ASSIGNMENTS) //UPDATES IN EX STAGE, MISPREICTED TRUE, ACTUALLY TAKEN-- INITIALIZE TO WEAK TAKEN
      // =======================================================================
      // Insert a new branch that was ACTUALLY TAKEN (mispredicted high, since it missed)
      dut.io.update.poke(true.B)
      dut.io.updatePC.poke("h0000_1000".U)
      dut.io.updateTarget.poke("h0000_2000".U)
      dut.io.mispredicted.poke(true.B) // Missed + Mispredicted = Actually Taken
      dut.clock.step(1)
      
      // Disable update mode
      dut.io.update.poke(false.B)
      
      // Verify look-ahead hit and prediction behavior
      dut.io.PC.poke("h0000_1000".U)
      dut.io.valid.expect(true.B)
      dut.io.target.expect("h0000_2000".U)
      dut.io.predictTaken.expect(true.B) // Should initialize to weakTaken (2 or 3)
      dut.clock.step(1)

      // =======================================================================
      // TEST 3: 2-BIT SATURATING FSM STATE TRANSITIONS //VERIFIES THAT IF THE STAGE TAKEN OR NOT TAKEN
      // =======================================================================
      // Let's force our branch to be evaluated as NOT TAKEN in the EX stage.
      // Current State: weakTaken. We predict Taken, but it is Mispredicted.
      dut.io.update.poke(true.B)
      dut.io.updatePC.poke("h0000_1000".U)
      dut.io.updateTarget.poke("h0000_2000".U)
      dut.io.mispredicted.poke(true.B) // Weak Taken -> step down to Weak Not Taken
      dut.clock.step(1)
      dut.io.update.poke(false.B)

      // Query the branch again. It should now predict NOT TAKEN (MSB = 0)
      dut.io.PC.poke("h0000_1000".U)
      dut.io.valid.expect(true.B)
      dut.io.predictTaken.expect(false.B) // Transitioned to weakNotTaken!
      dut.clock.step(1)

      // Step down again: Weak Not Taken -> Strong Not Taken
      dut.io.update.poke(true.B)
      dut.io.updatePC.poke("h0000_1000".U)
      dut.io.updateTarget.poke("h0000_2000".U)
      dut.io.mispredicted.poke(false.B) // Predicted Not Taken, Actually Not Taken (mispredict=false)
      dut.clock.step(1)
      dut.io.update.poke(false.B)

      // Verify it stays at Not Taken
      dut.io.PC.poke("h0000_1000".U)
      dut.io.predictTaken.expect(false.B)
      dut.clock.step(1)

      // =======================================================================
      // TEST 4: LEAST RECENTLY USED (LRU) EVICTION ON SET COLLISONS //BTB HAS 2 SETS IF THIRD ONE APPEARS TO THE SAME INDEX MUST THROW OUT LRY ENTRY
      // =======================================================================
      // Our BTB uses bits [4:2] as index. 
      // PC: 0x1000 -> Index = 000 (Bits [4:2] of 0x0)
      // We will fill the rest of Set 0 by adding two completely new tags mapping to Index 000.
      // Let's use PC: 0x0000_2000 (Index 0) and PC: 0x0000_3000 (Index 0).

      // Set 0, Way 0 is currently occupied by PC: 0x1000.
      // Touch PC: 0x1000 combinationally to ensure Way 0 is marked MOST recently used.
      dut.io.PC.poke("h0000_1000".U) 
      dut.clock.step(1) // LRU pointer flips to protect Way 0, targeting Way 1 for eviction

      // Allocate a branch to PC: 0x0000_2000. This must fill Way 1.
      dut.io.update.poke(true.B)
      dut.io.updatePC.poke("h0000_2000".U)
      dut.io.updateTarget.poke("h0000_A000".U)
      dut.io.mispredicted.poke(true.B)
      dut.clock.step(1)
      dut.io.update.poke(false.B)

      // Both Way 0 (0x1000) and Way 1 (0x2000) are full. 
      // Touch PC: 0x2000 combinationally to make Way 1 the most recently used.
      dut.io.PC.poke("h0000_2000".U)
      dut.clock.step(1) // LRU pointer updates to point to Way 0 as the eviction victim!

      // Allocate a third entry to PC: 0x0000_3000 (Index 0). 
      // This should trigger an eviction conflict. Way 0 (0x1000) must be evicted!
      dut.io.update.poke(true.B)
      dut.io.updatePC.poke("h0000_3000".U)
      dut.io.updateTarget.poke("h0000_B000".U)
      dut.io.mispredicted.poke(true.B)
      dut.clock.step(1)
      dut.io.update.poke(false.B)

      // --- Final Eviction Verification Assertions ---
      // 1. Check PC 0x3000 (New allocation): Should HIT
      dut.io.PC.poke("h0000_3000".U)
      dut.io.valid.expect(true.B)
      dut.io.target.expect("h0000_B000".U)

      // 2. Check PC 0x2000 (Most recently used old entry): Should HIT
      dut.io.PC.poke("h0000_2000".U)
      dut.io.valid.expect(true.B)
      dut.io.target.expect("h0000_A000".U)

      // 3. Check PC 0x1000 (Least recently used victim): Must be EVICTED (Cache Miss)
      dut.io.PC.poke("h0000_1000".U)
      dut.io.valid.expect(false.B) // Success! LRU correctly kicked this entry out.
    }
  }
}
// ADS I Class Project
// Assignment 02: Arithmetic Logic Unit and UVM Testbench
//
// Chair of Electronic Design Automation, RPTU University Kaiserslautern-Landau
// File created on 09/21/2025 by Tharindu Samarakoon (gug75kex@rptu.de)
// File updated on 10/31/2025 by Tobias Jauch (tobias.jauch@rptu.de)

import alu_tb_config_pkg::*;

interface alu_if (input clk);
    logic [DATA_WIDTH-1:0] operandA;
    logic [DATA_WIDTH-1:0] operandB;
    ALUOp operation;
    logic [DATA_WIDTH-1:0] aluResult;
 

    // Fix the driver output/input alignments
    clocking cb @(posedge clk);
        default input #3ns output #2ns;
        output operandA;   // Changed to output so Driver can push data
        output operandB;   // Changed to output
        output operation;  // Changed to output
        input  aluResult;  // Changed to input so Monitor can read results
    endclocking 
endinterface

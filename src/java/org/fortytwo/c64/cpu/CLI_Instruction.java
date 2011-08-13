package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * Clear Interrupt Disable Bit
 */
public class CLI_Instruction extends SingleByteInstruction
{
    public CLI_Instruction(AddressingMode mode){
	super(mode, "CLI");
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
	cpu.setInterruptsDisabled(false);
	return 2;
    }
}

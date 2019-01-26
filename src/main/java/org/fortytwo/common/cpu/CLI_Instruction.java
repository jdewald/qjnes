package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

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

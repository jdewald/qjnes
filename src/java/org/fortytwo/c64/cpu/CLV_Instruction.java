package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * Clear Overflow Flag
 */
public class CLV_Instruction extends SingleByteInstruction
{
    public CLV_Instruction(AddressingMode mode){
	super(mode, "CLV");
	cycles = 2;
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
	cpu.setOverflowFlag(false);
	return cycles;
    }
}

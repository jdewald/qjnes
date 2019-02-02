package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

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

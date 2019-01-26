package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

/**
 * Clear Carry Flag
 */
public class CLC_Instruction extends SingleByteInstruction
{
    public CLC_Instruction(AddressingMode mode){
	super(mode, "CLC");
	
	switch (mode){
	case Implied: cycles = 2; break;
	default: throw new IllegalArgumentException("Only Implied mode supported!");
	}
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
	cpu.setCarryFlag(false);
	return cycles;
    }
}

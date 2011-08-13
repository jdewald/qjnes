package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

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

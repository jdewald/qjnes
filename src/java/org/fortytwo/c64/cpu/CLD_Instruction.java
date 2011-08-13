package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * Clear decimal flag
 */
public class CLD_Instruction extends SingleByteInstruction
{
    public CLD_Instruction(AddressingMode mode){
	super(mode, "CLD");
	switch (mode){
	case Implied: cycles = 2; break;
	default: throw new IllegalArgumentException("Only Implied mode supported!");
	}
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
	cpu.setDecimalFlag(false);
	return cycles;
    }
}

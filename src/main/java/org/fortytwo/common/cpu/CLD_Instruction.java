package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

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

package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * Conditional
 */
public class BEQ_Instruction extends Instruction
{
    int cycles;
    public BEQ_Instruction(AddressingMode mode){
	super(mode, "BEQ");
	
	switch (mode){
	case Relative: cycles = 2; break;
	default: throw new IllegalArgumentException("Only Relative mode supported!");
	}
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
	if (cpu.getZeroFlag()){
	    cpu.writeRegister(RegisterType.programCounter,toInt(operands));
	}
	return cycles;
    }
}

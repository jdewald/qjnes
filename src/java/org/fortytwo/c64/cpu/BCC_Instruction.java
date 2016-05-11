package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * Conditional
 */
public class BCC_Instruction extends Instruction
{
    int cycles;
    public BCC_Instruction(AddressingMode mode){
	super(mode, "BCC");
	
	switch (mode){
	case Relative: cycles = 2; break;
	default: throw new IllegalArgumentException("Only Relative mode is supported!");
	}

    }

	public int execute(int[] operands, Memory memory, CPU cpu, boolean pageCrossed) {
		int cycles = this.execute(operands, memory, cpu);
		return pageCrossed ? (cycles + 1) : (cycles );
	}

    public int execute(int[] operands, Memory memory, CPU cpu){
	if (! cpu.getCarryFlag()){
	    cpu.writeRegister(RegisterType.programCounter,toInt(operands));
		return cycles + 1;
	}
	return cycles;
    }
}

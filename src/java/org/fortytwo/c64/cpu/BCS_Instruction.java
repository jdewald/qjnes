package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * Conditional
 */
public class BCS_Instruction extends Instruction
{
    int cycles;
    BCS_Instruction(AddressingMode mode){
	super(mode, "BCS");

	switch (mode){
	case Relative: cycles = 2; break;
	default: throw new IllegalArgumentException("Only Relative mode supported!");
	}
    }

	public int execute(int[] operands, Memory memory, CPU cpu, boolean pageCrossed) {
		int cycles = this.execute(operands, memory, cpu);
		return pageCrossed ? (cycles + 1) : (cycles );
	}
    public int execute(int[] operands, Memory memory, CPU cpu){
	if (cpu.getCarryFlag()){
	    cpu.writeRegister(RegisterType.programCounter,toInt(operands));
		return cycles +1;
	} else { 
		return cycles;
	}
    }
}


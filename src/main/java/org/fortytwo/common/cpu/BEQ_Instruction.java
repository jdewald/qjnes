package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

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

	public int execute(int[] operands, Memory memory, CPU cpu, boolean pageCrossed) {
		int cycles = this.execute(operands, memory, cpu);
		return pageCrossed ? (cycles + 1) : (cycles );
	}
    public int execute(int[] operands, Memory memory, CPU cpu){
	if (cpu.getZeroFlag()){
	    cpu.writeRegister(RegisterType.programCounter,toInt(operands));
		return cycles + 1;
	}
	return cycles;
    }
}

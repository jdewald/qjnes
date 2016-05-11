package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * Branch to the address given by the operand if the zero flag is not set
 */
public class BNE_Instruction extends Instruction
{
    int cycles;
    public BNE_Instruction(AddressingMode mode){
	super(mode, "BNE");
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
	if (! cpu.getZeroFlag()){
	    cpu.writeRegister(RegisterType.programCounter,toInt(operands));
		return cycles + 1;
	}
	return cycles;
    }
    
}

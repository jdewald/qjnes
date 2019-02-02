package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;
/**
 * Branch on result minus
 */
public class BMI_Instruction extends Instruction
{
    int cycles;
    public BMI_Instruction(AddressingMode mode){
	super(mode, "BMI");
	switch (mode){
	case Relative: cycles = 2; break;
	default: throw new IllegalArgumentException("Only Relative mode supported!");
	}
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
	if (cpu.getSignFlag()){
	    cpu.writeRegister(RegisterType.programCounter, toInt(operands));
	}
	return cycles;
    }
}

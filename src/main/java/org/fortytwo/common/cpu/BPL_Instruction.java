package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

/**
 * Conditional
 */
public class BPL_Instruction extends Instruction
{
    public BPL_Instruction(AddressingMode mode){
	super(mode, "BPL");
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
        if (! cpu.getSignFlag()){
            cpu.writeRegister(RegisterType.programCounter,toInt(operands));
			return cycles + 1;
        }
		return cycles;
    }
}

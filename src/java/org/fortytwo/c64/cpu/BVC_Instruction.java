package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;
/**
 * Conditional
 */
public class BVC_Instruction extends Instruction
{
    public BVC_Instruction(AddressingMode mode){
	super(mode, "BVC");
	cycles = 2;
    }

	public int execute(int[] operands, Memory memory, CPU cpu, boolean pageCrossed) {
		int cycles = this.execute(operands, memory, cpu);
		return pageCrossed ? (cycles + 1) : (cycles );
	}
    public int execute(int[] operands, Memory memory, CPU cpu){
	if (! cpu.getOverflowFlag()){
	    cpu.writeRegister(RegisterType.programCounter, toInt(operands));
		return cycles + 1;
	}
	return cycles;
    }
}

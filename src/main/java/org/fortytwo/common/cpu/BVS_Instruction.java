package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;
/**
 * Conditional
 */
public class BVS_Instruction extends Instruction
{
    public BVS_Instruction(AddressingMode mode){
	super(mode, "BVS");

	cycles = 2;
    }

	public int execute(int[] operands, Memory memory, CPU cpu, boolean pageCrossed) {
		int cycles = this.execute(operands, memory, cpu);
		return pageCrossed ? (cycles + 1) : (cycles );
	}

    public int execute(int[] operands, Memory memory, CPU cpu){
	if ( cpu.getOverflowFlag()){
	    cpu.writeRegister(RegisterType.programCounter, toInt(operands));
		return cycles + 1;
	}
	return cycles;
    }
}

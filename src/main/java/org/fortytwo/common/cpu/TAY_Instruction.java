package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

/**
 * Transfer accumulator contents to Y-registers
 */
public class TAY_Instruction extends SingleByteInstruction
{
    public TAY_Instruction(AddressingMode mode){
	super(mode, "TAY");

	cycles = 2;
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
	int accum = cpu.readRegister(RegisterType.accumulator);
	cpu.setSignFlag((accum & 0x80) != 0);
	cpu.setZeroFlag((0xFF & accum) == 0);
	cpu.writeRegister(RegisterType.Y,accum);

	return cycles;
    }
}

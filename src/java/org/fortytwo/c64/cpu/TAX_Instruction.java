package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * Transfer accumulator contents to X-registers
 */
public class TAX_Instruction extends SingleByteInstruction
{
    public TAX_Instruction(AddressingMode mode){
	super(mode, "TAX");

	cycles = 2;
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
	int accum = cpu.readRegister(RegisterType.accumulator);
	cpu.setSignFlag((accum & 0x80) != 0);
	cpu.setZeroFlag((0xFF & accum) == 0);
	cpu.writeRegister(RegisterType.X,accum);

	return cycles;
    }
}

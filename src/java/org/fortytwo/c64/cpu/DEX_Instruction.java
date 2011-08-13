package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * Decrement index X by 1
 */
public class DEX_Instruction extends Instruction
{
    DEX_Instruction(AddressingMode mode){
	super(mode, "DEX");

	switch (mode){
	case Implied: cycles = 2; break;
	default: throw new IllegalArgumentException("Mode not supported: " + mode);
	}
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
	int x = 0xFF & cpu.readRegister(RegisterType.X);
    x = x - 1;
	cpu.setZeroFlag(x == 0);
	cpu.setSignFlag((x & 0x80) != 0);
	cpu.writeRegister(RegisterType.X, x & 0xFF);

	return cycles;
    }
}

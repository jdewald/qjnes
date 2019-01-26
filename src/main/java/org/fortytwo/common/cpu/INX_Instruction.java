package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

/**
 * Increment index X by one
 */
public class INX_Instruction extends SingleByteInstruction
{
    public INX_Instruction(AddressingMode mode){
	super(mode, "INX");

	switch (mode){
	case Implied: cycles = 2; break;
	default: throw new IllegalArgumentException("Mode not supported: " + mode);
	}
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
	int x = 0xFF & cpu.readRegister(RegisterType.X);
	x = (x + 1) & 0xFF;
	cpu.setZeroFlag(x == 0);
	cpu.setSignFlag((x & 0x80) != 0);
	cpu.writeRegister(RegisterType.X,x);

	return cycles;
    }    
}

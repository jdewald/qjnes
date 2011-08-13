package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;
/**
 * Store index Y in memory
 */
public class STY_Instruction extends Instruction
{
    public STY_Instruction(AddressingMode mode){
	super(mode, "STY");

	switch (mode){
	case ZeroPageAbsolute: cycles = 3; break;
	case ZeroPageIndexedX: cycles = 4; break;
	case Absolute: cycles = 4; break;
	default: throw new IllegalArgumentException("AddressMode not supported: " + mode);
	}
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
	memory.write(toInt(operands), 0xFF & cpu.readRegister(RegisterType.Y));

	return cycles;
    }
}

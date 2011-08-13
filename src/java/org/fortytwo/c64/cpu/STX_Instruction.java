package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * Store index X in memory
 */
public class STX_Instruction extends Instruction
{
    public STX_Instruction(AddressingMode mode){
	super(mode, "STX");


	switch (mode){
	case ZeroPageAbsolute: cycles = 3; break;
	case ZeroPageIndexedY: cycles = 4; break;
	case Absolute: cycles = 4; break;
	default: throw new IllegalArgumentException("AddressMode not supported: " + mode);
	}
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        memory.write(toInt(operands), 0xFF & cpu.readRegister(RegisterType.X));

	return cycles;
    }
}

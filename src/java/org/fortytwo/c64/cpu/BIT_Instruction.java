package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;
/**
 * Test bits in memory with accumulator
 */
public class BIT_Instruction extends Instruction
{
    public BIT_Instruction(AddressingMode mode){
	super(mode, "BIT");

	switch (mode){
	case ZeroPageAbsolute: cycles = 3;break;
	case Absolute: cycles = 4; break;
	default: throw new IllegalArgumentException("Mode not supported; " + mode);
	}
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        int val = 0xFF & memory.read(toInt(operands));
        cpu.setSignFlag((val & 0x80) != 0); // bit 7 of original
        cpu.setOverflowFlag((val & 0x40) != 0); // bit 6 of original
        
        val = val & cpu.readRegister(RegisterType.accumulator);
        cpu.setZeroFlag((val & 0xFF) == 0);
        
        return cycles;
    }
}

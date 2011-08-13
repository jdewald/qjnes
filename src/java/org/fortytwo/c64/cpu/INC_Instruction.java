package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;
/**
 * Increment memory by one
 */
public class INC_Instruction extends Instruction
{
    public INC_Instruction(AddressingMode mode){
	super(mode, "INC");

	switch (mode){

	case ZeroPageAbsolute: cycles = 5; break;
	case ZeroPageIndexedX: cycles = 6; break; 
	case Absolute: cycles = 6; break;
	case IndexedX: cycles = 7; break; 
	default: throw new IllegalArgumentException("Mode note supported: " + mode);
	}

    }

    public int execute(int[] operands, Memory memory, CPU cpu){
	int addr = toInt(operands);
	int val = memory.read(addr);
	val = (val + 1) & 0xFF;
	cpu.setSignFlag((val & 0x80) != 0);
	cpu.setZeroFlag(val == 0);
	memory.write(addr, (val & 0xFF));

	return cycles;
    }
}

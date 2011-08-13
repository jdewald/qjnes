package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * Does a logical "OR" of memory with accumulator and stores in the accumulator
 */
public class ORA_Instruction extends Instruction
{
    ORA_Instruction(AddressingMode mode){
	super(mode, "ORA");
	switch (mode){
	case Immediate:cycles = 2; break;
	case ZeroPageAbsolute: cycles = 3; break;
	case ZeroPageIndexedX: cycles = 4; break;
	case Absolute: cycles = 4; break;
	case IndexedX: cycles = 4; break;
	case IndexedY: cycles = 4; break; // supposed to add 1 if boundary crossed
	case PreIndexedIndirect: cycles = 6; break;
	case PostIndexedIndirect: cycles = 5; break; 
	default: throw new IllegalArgumentException("AddressMode not supported: " + mode);
	}
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        int accum = cpu.readRegister(RegisterType.accumulator);
        int value = toInt(operands);
        if (getAddressingMode() != AddressingMode.Immediate){
            value = memory.read(toInt(operands));
        }
        value = 0xFF & value;
        value = accum | value;
        cpu.writeRegister(RegisterType.accumulator,value);
        cpu.setSignFlag((value & 0x80) != 0);
        cpu.setZeroFlag((value & 0xFF) == 0);
        return cycles;
    }	

}

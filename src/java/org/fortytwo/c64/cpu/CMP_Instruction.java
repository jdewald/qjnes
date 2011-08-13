package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * Compare memory and accumulator
 */
public class CMP_Instruction extends Instruction
{

    public CMP_Instruction(AddressingMode mode){
	super(mode, "CMP");
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

    public int execute(int operands[], Memory memory, CPU cpu){
        int val = toInt(operands);
        if (getAddressingMode() != AddressingMode.Immediate){
            val = memory.read(val); // assume it's an address
        }
        val = val & 0xFF;

        int accum = 0xFF & cpu.readRegister(RegisterType.accumulator);
        int newVal = 0x1FF & (accum + (~val) + 1); // 2s complement addition
        //        int newVal = accum - val;
        cpu.setCarryFlag(newVal < 256);
        cpu.setSignFlag((newVal & 0x80) != 0);
        cpu.setZeroFlag((newVal & 0xFF) == 0);
        
        return cycles;
    }
}

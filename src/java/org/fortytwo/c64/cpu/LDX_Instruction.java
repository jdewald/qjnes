package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * Load index X with Memory
 */
public class LDX_Instruction extends Instruction
{
    public LDX_Instruction(AddressingMode mode){
	super(mode, "LDX");

	switch (mode){
	case Immediate:cycles = 2; break;
	case ZeroPageAbsolute: cycles = 3; break;
	case ZeroPageIndexedY: cycles = 4; break;
	case Absolute: cycles = 4; break;
	case IndexedY: cycles = 4; break; // supposed to add 1 if boundary crossed
	default: throw new IllegalArgumentException("AddressMode not supported: " + mode);
	}
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        int val = toInt(operands);
        if (getAddressingMode() != AddressingMode.Immediate){
            val = 0xFF & memory.read(val);
        }
        cpu.setZeroFlag((val & 0xFF) == 0);
        cpu.setSignFlag((val & 0x80) != 0);
        cpu.writeRegister(RegisterType.X,val);
        
        return cycles;
    }
}

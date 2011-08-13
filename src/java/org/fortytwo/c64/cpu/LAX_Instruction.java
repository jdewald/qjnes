package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * Load index X *and* A
 */
public class LAX_Instruction extends Instruction
{
    public LAX_Instruction(AddressingMode mode){
	super(mode, "LAX");

	switch (mode){
	case Immediate:cycles = 2; break;
	case ZeroPageAbsolute: cycles = 3; break;
	case ZeroPageIndexedY: cycles = 4; break;
	case Absolute: cycles = 4; break;
	case IndexedY: cycles = 4; break; // supposed to add 1 if boundary crossed
    case PreIndexedIndirect: cycles = 6; break;
    case PostIndexedIndirect: cycles = 5; break;
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
        cpu.writeRegister(RegisterType.accumulator, val);
        
        return cycles;
    }
}

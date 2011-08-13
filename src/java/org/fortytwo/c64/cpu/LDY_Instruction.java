package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;
/**
 * Load Index Y from memory
 */
public class LDY_Instruction extends Instruction
{
    public LDY_Instruction(AddressingMode mode){
	super(mode, "LDY");

	switch (mode){
	case Immediate:cycles = 2; break;
	case ZeroPageAbsolute: cycles = 3; break;
	case ZeroPageIndexedX: cycles = 4; break;
	case Absolute: cycles = 4; break;
	case IndexedX: cycles = 4; break;
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
        cpu.writeRegister(RegisterType.Y,0xFF & val);
        
        return cycles;
    }
}

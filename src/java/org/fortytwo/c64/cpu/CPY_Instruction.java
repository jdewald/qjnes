package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;
/**
 * Compare memory and index Y
 */
public class CPY_Instruction extends Instruction
{
    public CPY_Instruction(AddressingMode mode){
	super(mode, "CPY");

	switch (mode){
	case Immediate:cycles = 2; break;
	case ZeroPageAbsolute: cycles = 3; break;
	case Absolute: cycles = 4; break;
	default: throw new IllegalArgumentException("AddressMode not supported: " + mode);
	}
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
	int val = toInt(operands);
	if (getAddressingMode() != AddressingMode.Immediate){
	    val = 0xFF & memory.read(val); // assume it's an address
	}
    val = 0xFF & val;
	int y = 0xFF & cpu.readRegister(RegisterType.Y);
    int newVal = 0x1FF & (y + (~val) + 1); // 2s complement addition
        
	//int newVal = y - val;
	cpu.setCarryFlag(newVal < 256); // inverse of borrow
	cpu.setSignFlag((0x80 & newVal) != 0);
	cpu.setZeroFlag(newVal == 0);

	return cycles;
    }    
}

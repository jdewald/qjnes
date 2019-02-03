package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

/**
 * Shift Left One Bit (Memory or Accumulator)
 */
public class ASL_Instruction extends Instruction
{

    int cycles;
    ASL_Instruction(AddressingMode mode, String name){
        super(mode, name);
    }
    public ASL_Instruction(AddressingMode mode){
	super(mode, "ASL");

	switch (mode){
	case Accumulator: cycles = 2; break;
	case ZeroPageAbsolute: cycles = 5; break;
	case ZeroPageIndexedX: cycles = 6; break;
	case Absolute: cycles = 6; break;
	case IndexedX: cycles = 7; break;
	default: throw new IllegalArgumentException("AddressMode not supported: " + mode);
	}
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
	if (getAddressingMode() == AddressingMode.Accumulator){
	    int accum = 0xFF & cpu.readRegister(RegisterType.accumulator);
	    cpu.setCarryFlag(((accum & 0x80) != 0));
	    accum = accum << 1;
	    cpu.setZeroFlag((accum & 0xFF) == 0);
	    cpu.setSignFlag((accum & 0x80) != 0);
	    cpu.writeRegister(RegisterType.accumulator, 0xFF & accum);

	}
	else {
	    int val= 0xFF & memory.read(toInt(operands));
	    cpu.setCarryFlag(((val & 0x80) != 0));
	    val = val << 1;
	    cpu.setZeroFlag((val & 0xFF) == 0);
	    cpu.setSignFlag((val & 0x80) != 0);
	    memory.write(toInt(operands), 0xFF & val);
	}
	return cycles;
    }
}

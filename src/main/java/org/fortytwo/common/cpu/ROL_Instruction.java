package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

/**
 * Rotate One Bit left (Memory or Accumulator)
 */
public class ROL_Instruction extends Instruction
{
    public ROL_Instruction(AddressingMode mode){
	super(mode, "ROL");

	switch (mode){
	case Accumulator: cycles = 2; break;
	case ZeroPageAbsolute: cycles = 5; break;
	case ZeroPageIndexedX: cycles = 6; break;
	case Absolute: cycles = 6; break;
	case IndexedX: cycles = 7; break;
	default: throw new IllegalArgumentException("Invalid mode: " + mode);
	}
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        int val = 0;
        if (getAddressingMode() == AddressingMode.Accumulator){ // accumulator
            val = 0xFF & cpu.readRegister(RegisterType.accumulator);
        }
        else {
            val = 0xFF & memory.read(toInt(operands));
        }
        int oldval = val;
        val = val << 1;
        if (cpu.getCarryFlag()){
            val |= 0x1; // bring over the previous carry
        }
        cpu.setCarryFlag((oldval & 0x80) != 0);
        cpu.setSignFlag((val & 0x80) != 0);
        cpu.setZeroFlag((val & 0xFF) == 0);
        
        if (getAddressingMode() == AddressingMode.Accumulator){
            cpu.writeRegister(RegisterType.accumulator, (val & 0xFF));
        }
        else {
            memory.write(toInt(operands),val & 0xFF);
        }
        
        return cycles;
    }
}

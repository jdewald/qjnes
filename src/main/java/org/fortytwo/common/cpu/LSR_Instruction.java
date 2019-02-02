package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

/**
 * Shift Right One Bit (Memory or Accumulator)
 */
public class LSR_Instruction extends Instruction
{
    public LSR_Instruction(AddressingMode mode){
	super(mode, "LSR");


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
           cpu.setCarryFlag(((accum & 0x01) != 0));
           accum = accum >> 1;
           cpu.setZeroFlag((accum & 0xFF) == 0);
           cpu.writeRegister(RegisterType.accumulator, 0xFF & accum);
           
       }
       else {
           int val= 0xFF & memory.read(toInt(operands));
           cpu.setCarryFlag(((val & 0x01) != 0));
           val = val >> 1;
           cpu.setZeroFlag((val & 0xFF) == 0);
           memory.write(toInt(operands), val & 0xFF);
       }
       cpu.setSignFlag(false);
       return cycles;
   }
}

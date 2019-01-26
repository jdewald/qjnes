package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;
/**
 * Transfer stack pointer to index X
 */
public class TSX_Instruction extends SingleByteInstruction
{
    public TSX_Instruction(AddressingMode mode){
	super(mode, "TSX");
	cycles = 2;
    }

  public int execute(int[] operands, Memory memory, CPU cpu){
      int sp = cpu.readRegister(RegisterType.stackPointer);
      cpu.writeRegister(RegisterType.X, 0xFF & sp);
      cpu.setSignFlag((sp & 0x80) != 0);
      cpu.setZeroFlag((sp & 0xFF) == 0);
      return cycles;
    }
}

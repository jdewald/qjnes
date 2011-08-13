package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * Transfer index Y to Accumulator
 */
public class TYA_Instruction extends SingleByteInstruction
{
    public TYA_Instruction(AddressingMode mode){
	super(mode, "TYA");

	cycles = 2;
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
	int Y = 0xFF & cpu.readRegister(RegisterType.Y);
	cpu.writeRegister(RegisterType.accumulator,Y);
	cpu.setSignFlag((Y & 0x80) != 0);
	cpu.setZeroFlag((Y & 0xFF) == 0);
	
	return cycles;
    }
    
}

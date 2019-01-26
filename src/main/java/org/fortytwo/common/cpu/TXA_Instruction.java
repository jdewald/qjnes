package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

/**
 * Transfer index X to accumulator
 */
public class TXA_Instruction extends SingleByteInstruction
{
    public TXA_Instruction(AddressingMode mode){
	super(mode, "TXA");

	cycles = 2;
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
	int X = cpu.readRegister(RegisterType.X);
	cpu.writeRegister(RegisterType.accumulator, X & 0xFF);
	cpu.setSignFlag((X & 0x80) != 0);
	cpu.setZeroFlag((X & 0XFF) == 0);
	
	return cycles;
    }
}

package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * ORs A reg with $EE, then ANDs with immed value and stores in A and X
 * ORA #$EE, AND
 * bits 0 and 1 of X are AND'd with A before the transfer
 * also known as LXA
 */
public class OAL_Instruction extends SingleByteInstruction
{
    public OAL_Instruction(AddressingMode mode){
	super(mode, "OAL");

	cycles = 2;
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        int X = cpu.readRegister(RegisterType.X);
        
        int accum = cpu.readRegister(RegisterType.accumulator);
        accum = accum | 0xEE;
        accum = accum & toInt(operands);

        cpu.writeRegister(RegisterType.accumulator, accum & 0xFF);
        cpu.writeRegister(RegisterType.X, accum & 0xFF);
        cpu.setSignFlag((accum & 0x80) != 0);
        cpu.setZeroFlag((accum & 0XFF) == 0);
        
	return cycles;
    }
}

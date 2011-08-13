package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * Transfer index X to accumulator then ANDs with immed value
 * TXA, AND
 * bits 0 and 1 of X are AND'd with A before the transfer
 * also known as ANE
 */
public class XAA_Instruction extends SingleByteInstruction
{
    public XAA_Instruction(AddressingMode mode){
	super(mode, "XAA");

	cycles = 2;
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        int X = cpu.readRegister(RegisterType.X);
        
        int accum = cpu.readRegister(RegisterType.accumulator);
        int mask = (accum & X & 0x11); // bits 0 and 1 are AND'd
        mask = (0xEE | mask);
        accum = X & mask;
        accum = accum & toInt(operands);
        cpu.writeRegister(RegisterType.accumulator, accum & 0xFF);
        cpu.setSignFlag((accum & 0x80) != 0);
        cpu.setZeroFlag((accum & 0XFF) == 0);
        
	return cycles;
    }
}

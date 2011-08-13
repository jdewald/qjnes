package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;
/**
 * Increment index Y by one
 */
public class INY_Instruction extends SingleByteInstruction
{
    public INY_Instruction(AddressingMode mode){
	super(mode, "INY");

	switch (mode){
	case Implied: cycles = 2; break;
	default: throw new IllegalArgumentException("Mode not supported: " + mode);
	}
    }


    public int execute(int[] operands, Memory memory, CPU cpu){
        int y = 0xFF & cpu.readRegister(RegisterType.Y);
        y = (y + 1) & 0xFF;
        cpu.setZeroFlag(y == 0);
        cpu.setSignFlag((y & 0x80) != 0);
        cpu.writeRegister(RegisterType.Y,y);
        
        return cycles;
    }    
}

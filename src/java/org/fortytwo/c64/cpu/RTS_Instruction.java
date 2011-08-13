package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * Return from subroutine
 */
public class RTS_Instruction extends SingleByteInstruction
{
    public RTS_Instruction(AddressingMode mode){
	super(mode, "RTS");

	switch (mode){
	case Implied: cycles = 6; break;
	default: throw new IllegalArgumentException("Only Implied mode is supported");
	}
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        int sp = cpu.readRegister(RegisterType.stackPointer);
        int returnAddress = (0xFF & cpu.popStack()) | ((0xFF & cpu.popStack()) << 8);
        returnAddress++;
        cpu.writeRegister(RegisterType.programCounter, returnAddress);
        return cycles;
    }
}

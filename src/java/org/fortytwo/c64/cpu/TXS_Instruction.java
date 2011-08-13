package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * Initialize stack pointer from X
 */
public class TXS_Instruction extends SingleByteInstruction
{
    public TXS_Instruction(AddressingMode mode){
	super(mode, "TXS");

	cycles = 2;
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        cpu.writeRegister(RegisterType.stackPointer, 0xFF & cpu.readRegister(RegisterType.X));

        return cycles;
    }
}

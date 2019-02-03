package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

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

package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;
/**
 * Do nothing
 */
public class NOP_Instruction extends SingleByteInstruction
{
    public NOP_Instruction(AddressingMode mode){
	super(mode, "NOP");
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
	return 2;
	// do nothing
    }
}

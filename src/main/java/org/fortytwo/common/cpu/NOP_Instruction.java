package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;
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

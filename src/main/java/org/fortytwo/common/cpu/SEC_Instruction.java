package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;
/**
 * Set Carry Flag
 */
public class SEC_Instruction extends SingleByteInstruction
{
    public SEC_Instruction(AddressingMode mode){
	super(mode, "SEC");
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        cpu.setCarryFlag(true);
        return 2;
    }
}

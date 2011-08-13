package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;
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

package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;
/**
 * Set Decimal
 */
public class SED_Instruction extends SingleByteInstruction
{
    public SED_Instruction(AddressingMode mode){
	super(mode, "SED");
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
	cpu.setDecimalFlag(true);
	return 2;
    }
}

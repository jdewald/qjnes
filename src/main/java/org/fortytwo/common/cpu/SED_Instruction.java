package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;
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

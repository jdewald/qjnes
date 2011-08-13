package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;
/**
 * Set Interrupt Disable Status
 */
public class SEI_Instruction extends SingleByteInstruction
{
    public SEI_Instruction(AddressingMode mode){
	super(mode, "SEI");
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        // this is currently going to not optimal, but we'll optimize as necessary
        cpu.setInterruptsDisabled(true);
        
        return 2;
    }
}

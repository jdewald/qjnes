package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;
/**
 * Basically a 3-byte NOP
 */
public class SKW_Instruction extends Instruction
{
    SKW_Instruction(AddressingMode mode){
        super(mode,"SKW");
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        return 4;
    }
}

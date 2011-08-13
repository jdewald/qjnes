package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;
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

package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;
/**
 * Basically a 2-byte NOP
 */
public class SKB_Instruction extends Instruction
{
    SKB_Instruction(AddressingMode mode){
        super(mode,"SKB");
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        
        return 3;
    }
}

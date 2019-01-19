package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;
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

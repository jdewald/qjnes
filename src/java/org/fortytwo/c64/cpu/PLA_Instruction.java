package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * Pull Accumulator from stack
 */
public class PLA_Instruction extends SingleByteInstruction
{
    public PLA_Instruction(AddressingMode mode){
        super(mode, "PLA");
    }
    
    public int execute(int[] operands, Memory memory, CPU cpu){
        int accum = 0xFF & popStack(cpu,memory);
        cpu.setSignFlag((accum & 0x80) != 0);
        cpu.setZeroFlag((accum & 0xFF) == 0);
        cpu.writeRegister(RegisterType.accumulator, accum);
        return 3;
    }
}

package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * Push Accumulator on Stack
 */
public class PHA_Instruction extends SingleByteInstruction
{
    public PHA_Instruction(AddressingMode mode){
	super(mode, "PHA");
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        pushStack(cpu, memory, cpu.readRegister(RegisterType.accumulator));
        return 3;
    }
}

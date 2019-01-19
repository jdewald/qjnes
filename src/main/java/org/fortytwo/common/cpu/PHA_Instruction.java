package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

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

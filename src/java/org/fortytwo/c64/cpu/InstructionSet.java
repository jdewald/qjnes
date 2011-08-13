package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

import java.util.Hashtable;

/**
 * TODO: Think about better way to represent the instructions, they probably don't need to know
 * about their own opcode or even addressing mode. As long as they can get to their values they are good
 */
public interface InstructionSet
{
    public static final int CLASS_MASK = 0x3; // determines our "class"
    public static final int MODE_MASK = 0x1C;
    public static final int INSTRUCTION_MASK = 0xE0;

    public static final int OPCODE_MASK = 0xFF0000;
    public static final int LOW_BYTE = 0x00FF00;
    public static final int HIGH_BYTE = 0x0000FF;

    Instruction getByOpCode(int opcode);
    int executeInstruction(Instruction ins, int fullInstruction, Memory memory, CPU cpu);
}

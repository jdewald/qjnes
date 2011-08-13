package org.fortytwo.c64.cpu;

public abstract class SingleByteInstruction extends Instruction
{
    public SingleByteInstruction(AddressingMode mode, String name)
    {
	super(mode,name);
    }

    public boolean isSingleByte(){
	return true;
    }
}

package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * Conditional
 */
public class BPL_Instruction extends Instruction
{
    public BPL_Instruction(AddressingMode mode){
	super(mode, "BPL");
	switch (mode){
	case Relative: cycles = 3; break;
	default: throw new IllegalArgumentException("Only Relative mode supported!");
	}
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        int oldPC = 0xFFFF & cpu.readRegister(RegisterType.programCounter);
        if (! cpu.getSignFlag()){
            cpu.writeRegister(RegisterType.programCounter,toInt(operands));
        }
        if ((toInt(operands) & 0xFF00) != (oldPC & 0xFF00)){
            return cycles + 1;
        }
        else {
            return cycles;
        }
    }
}

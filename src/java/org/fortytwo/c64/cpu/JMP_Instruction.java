
package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * Jump to memory location
 */
public class JMP_Instruction extends Instruction
{
    public JMP_Instruction(AddressingMode mode){
	super(mode, "JMP");
    
	switch (mode){
	case Absolute: cycles = 3; break;
	case Indirect: cycles = 5; break;
	default: throw new IllegalArgumentException("Mode note supported: " + mode);
	}
    }
    
    public int execute(int[] operands, Memory memory, CPU cpu){
        cpu.writeRegister(RegisterType.programCounter,toInt(operands));
        return cycles;
    }
}

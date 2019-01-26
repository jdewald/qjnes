package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;
/**
 * Push Accumulator onto Stack
 */
public class PHP_Instruction extends SingleByteInstruction
{
    public PHP_Instruction(AddressingMode mode){
	super(mode, "PHP");
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        //System.out.println("Pushing status onto stack: " + Integer.toHexString(cpu.readRegister(RegisterType.status)));
        pushStack(cpu, memory, cpu.readRegister(RegisterType.status) | MOS6502Emulator.STATUS_FLAG_UNUSED | MOS6502Emulator.STATUS_FLAG_BREAK);
        
        return 3;
    }
}

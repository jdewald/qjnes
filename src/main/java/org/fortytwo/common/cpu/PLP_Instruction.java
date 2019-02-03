package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;
/**
 * Pull Status from stack
 */
public class PLP_Instruction extends SingleByteInstruction
{
    public PLP_Instruction(AddressingMode mode){
	super(mode, "PLP");
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        int status = popStack(cpu,memory);
        //System.out.println("Popped status from stack: " + Integer.toHexString(status));
        cpu.writeRegister(RegisterType.status, MOS6502Emulator.STATUS_FLAG_UNUSED | (status & ~MOS6502Emulator.STATUS_FLAG_BREAK));
        return 4;
    }
}

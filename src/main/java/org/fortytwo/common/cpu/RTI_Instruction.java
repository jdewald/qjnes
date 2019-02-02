package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

/**
 * Return from interrupt
 */
public class RTI_Instruction extends SingleByteInstruction
{
    public RTI_Instruction(AddressingMode mode){
	super(mode, "RTI");
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        int status = popStack(cpu,memory);
        //        status ^= MOS6502Emulator.STATUS_FLAG_INTERRUPT;
        int returnAddress = (0xFF & cpu.popStack()) | ((0xFF & cpu.popStack()) << 8);
        cpu.writeRegister(RegisterType.programCounter, returnAddress);
        cpu.writeRegister(RegisterType.status, status);

        return 3;
    }
}

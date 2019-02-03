package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

// basically the AND instruction except sets Carry as well as Sign
public class ANC_Instruction extends Instruction
{
    ANC_Instruction(AddressingMode mode){
        super(mode, "ANC");

    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        int val = toInt(operands);
        int accum = 0xFF & cpu.readRegister(RegisterType.accumulator);
        accum &= val;
        cpu.writeRegister(RegisterType.accumulator, 0xFF & accum);
        cpu.setZeroFlag((accum & 0xFF) == 0);
        cpu.setCarryFlag((accum & 0x80) != 0); // bit 7 goes into carry and neg
        cpu.setSignFlag((accum & 0x80) != 0);
        
        return 2;
    }
}

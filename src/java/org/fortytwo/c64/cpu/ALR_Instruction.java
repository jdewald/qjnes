package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

// basically the AND instruction except sets Carry as well as Sign
public class ALR_Instruction extends Instruction
{
    ALR_Instruction(AddressingMode mode){
        super(mode, "ALR");

    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        int val = toInt(operands);
        int accum = 0xFF & cpu.readRegister(RegisterType.accumulator);
        accum = 0xFF & (accum &  val); // AND
        cpu.setCarryFlag((accum & 0x01) != 0);
        accum = accum >> 1; // LSR
        cpu.writeRegister(RegisterType.accumulator, 0xFF & accum);
        cpu.setZeroFlag((accum & 0xFF) == 0);
        cpu.setSignFlag(false);
        
        return 2;
    }
}

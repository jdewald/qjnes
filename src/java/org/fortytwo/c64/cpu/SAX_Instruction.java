package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * "And" X with accumulator and stores in memory
 */
public class SAX_Instruction extends Instruction
{
    int cycles;
    public SAX_Instruction(AddressingMode mode){
        super(mode, "SAX");

        switch (mode){
        case Immediate: cycles = 2; break;
        default: throw new IllegalArgumentException("AddressMode not supported: " + mode);
        }
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        int accum = 0xFF & cpu.readRegister(RegisterType.accumulator);
        int value = 0xFF & toInt(operands);
        int x = 0xFF & cpu.readRegister(RegisterType.X);
        
        int tmp = x & accum;
        int newVal = 0x1FF & (tmp + (~value) + 1); // 2s complement addition
        //        int newVal = accum - val;
        cpu.setCarryFlag(newVal < 256);
        cpu.setSignFlag((newVal & 0x80) != 0);
        cpu.setZeroFlag((newVal & 0xFF) == 0);

        cpu.writeRegister(RegisterType.X, newVal & 0xFF);
        return cycles;
    }


}

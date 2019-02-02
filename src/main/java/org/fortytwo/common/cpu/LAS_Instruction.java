package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

/**
 * ANDS memory with SP and stores in accum, x and sp
 */
public class LAS_Instruction extends Instruction
{
    int cycles;
    public LAS_Instruction(AddressingMode mode){
        super(mode, "LAS");

        switch (mode){
        case IndexedY: cycles = 5; break;
        default: throw new IllegalArgumentException("AddressMode not supported: " + mode);
        }
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        //        int value = 0xFF & memory.read(toInt(operands)+1);
        int value = 0xFF & memory.read(toInt(operands));
        int sp = 0xFF & cpu.readRegister(RegisterType.stackPointer);
        
        int tmp = value & sp;

        cpu.writeRegister(RegisterType.X, 0xFF & tmp);
        cpu.writeRegister(RegisterType.accumulator, 0xFF & tmp);
        cpu.writeRegister(RegisterType.stackPointer, 0xFF & tmp);

        cpu.setZeroFlag((0xFF & tmp) == 0);
        cpu.setSignFlag((0x80 & tmp) != 0);
        return cycles;
    }


}

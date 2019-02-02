package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

/**
 * ANDs  Y with (high byte + 1) of the target address
 * also known as SHX
 */
public class TAS_Instruction extends Instruction
{
    int cycles;
    public TAS_Instruction(AddressingMode mode){
        super(mode, "TAS");

        switch (mode){
        case IndexedY: cycles = 5; break;
        default: throw new IllegalArgumentException("AddressMode not supported: " + mode);
        }
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        int accum = 0xFF & cpu.readRegister(RegisterType.accumulator);
        //        int value = 0xFF & memory.read(toInt(operands)+1);
        int value = (operands[1] + 1) & 0xFF;
        int x = 0xFF & cpu.readRegister(RegisterType.X);
              
        int tmp = accum & x & 0xFF;
        cpu.writeRegister(RegisterType.stackPointer, tmp);
        tmp &= value;
        //        int newVal = accum - val;
        memory.write(toInt(operands), 0xFF & tmp);
        return cycles;
    }


}

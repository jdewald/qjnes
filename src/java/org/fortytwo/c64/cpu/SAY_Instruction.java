package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * ANDs  Y with (high byte + 1) of the target address
 * also known as SHY
 */
public class SAY_Instruction extends Instruction
{
    int cycles;
    public SAY_Instruction(AddressingMode mode){
        super(mode, "SAY");

        switch (mode){
        case IndexedX: cycles = 5; break;
        default: throw new IllegalArgumentException("AddressMode not supported: " + mode);
        }
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        //        int value = 0xFF & memory.read(toInt(operands)+1);
        int value = (operands[1] + 1) & 0xFF;
        int y = 0xFF & cpu.readRegister(RegisterType.Y);
        
        int tmp = y & value;
        //        int newVal = accum - val;
        memory.write(toInt(operands), 0xFF & tmp);
        return cycles;
    }


}

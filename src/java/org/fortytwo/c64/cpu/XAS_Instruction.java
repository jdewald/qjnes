package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * ANDs A with X (high byte + 1) of the target address
 * also known as SHX
 */
public class XAS_Instruction extends Instruction
{
    int cycles;
    public XAS_Instruction(AddressingMode mode){
        super(mode, "XAS");

        switch (mode){
        case IndexedY: cycles = 5; break;
        default: throw new IllegalArgumentException("AddressMode not supported: " + mode);
        }
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        //        int value = 0xFF & memory.read(toInt(operands)+1);
        int value = (operands[1] + 1) & 0xFF;
        int x = 0xFF & cpu.readRegister(RegisterType.X);
        
        int tmp = x & value;
        //        int newVal = accum - val;
        memory.write(toInt(operands), 0xFF & tmp);
        return cycles;
    }


}

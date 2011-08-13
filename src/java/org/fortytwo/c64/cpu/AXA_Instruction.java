package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * ANDs A with X and the (high byte + 1) of the target address
 * also known as SHA
 */
public class AXA_Instruction extends Instruction
{
    int cycles;
    public AXA_Instruction(AddressingMode mode){
        super(mode, "AXA");

        switch (mode){
        case IndexedY: cycles = 5; break;
        case PostIndexedIndirect: cycles = 6; break;
        default: throw new IllegalArgumentException("AddressMode not supported: " + mode);
        }
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        int accum = 0xFF & cpu.readRegister(RegisterType.accumulator);
        //        int value = 0xFF & memory.read(toInt(operands)+1);
        int value = (operands[1] + 1) & 0xFF;
        int x = 0xFF & cpu.readRegister(RegisterType.X);
        
        int tmp = x & accum & value;
        //        int newVal = accum - val;
        memory.write(toInt(operands), 0xFF & tmp);
        return cycles;
    }


}

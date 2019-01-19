package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

/**
 * "And" X with accumulator and stores in memory
 */
public class AXS_Instruction extends Instruction
{
    int cycles;
    public AXS_Instruction(AddressingMode mode){
        super(mode, "AXS");

        switch (mode){
        case Absolute: cycles = 4; break;
        case ZeroPageAbsolute: cycles = 3; break;
        case ZeroPageIndexedY: cycles = 4; break;
        case PreIndexedIndirect: cycles = 6; break;
        default: throw new IllegalArgumentException("AddressMode not supported: " + mode);
        }
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        int accum = 0xFF & cpu.readRegister(RegisterType.accumulator);
        int x = 0xFF & cpu.readRegister(RegisterType.X);

        int value = x & accum;

        memory.write(toInt(operands), 0xFF & value);
        return cycles;
    }


}

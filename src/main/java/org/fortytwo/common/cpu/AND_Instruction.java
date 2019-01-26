package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

/**
 * "And" memory with accumulator
 */
public class AND_Instruction extends Instruction
{
    int cycles;
    public AND_Instruction(AddressingMode mode){
        super(mode, "AND");

        switch (mode){
        case Immediate:cycles = 2; break;
        case ZeroPageAbsolute: cycles = 3; break;
        case ZeroPageIndexedX: cycles = 4; break;
        case Absolute: cycles = 4; break;
        case IndexedX: cycles = 4; break;
        case IndexedY: cycles = 4; break; // supposed to add 1 if boundary crossed
        case PreIndexedIndirect: cycles = 6; break;
        case PostIndexedIndirect: cycles = 5; break; 
        default: throw new IllegalArgumentException("AddressMode not supported: " + mode);
        }
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        int accum = 0xFF & cpu.readRegister(RegisterType.accumulator);
        int value = 0;
        if (getAddressingMode() == AddressingMode.Immediate){
            value = accum & toInt(operands);
        }
        else {
            value = accum & memory.read(toInt(operands));
        }
        cpu.writeRegister(RegisterType.accumulator,0xFF & value);
        cpu.setSignFlag((value & 0x80) != 0);
        cpu.setZeroFlag((value & 0xFF) == 0);
        return cycles;
    }


}

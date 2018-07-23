package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * Rotate One Bit Right (Memory or Accumulator)
 */
public class ROR_Instruction extends Instruction {
    public ROR_Instruction(AddressingMode mode) {
        super(mode, "ROR");

        switch (mode) {
            case Accumulator:
                cycles = 2;
                break;
            case ZeroPageAbsolute:
                cycles = 5;
                break;
            case ZeroPageIndexedX:
                cycles = 6;
                break;
            case Absolute:
                cycles = 6;
                break;
            case IndexedX:
                cycles = 7;
                break;
            default:
                throw new IllegalArgumentException("Invalid mode: " + mode);
        }
    }

    public int execute(int[] operands, Memory memory, CPU cpu) {
        int val = 0;
        if (getAddressingMode() == AddressingMode.Accumulator) { // accumulator
            val = 0xFF & cpu.readRegister(RegisterType.accumulator);
        } else {
            val = 0xFF & memory.read(toInt(operands));
        }
        int oldval = val;
        if (cpu.getCarryFlag()) {
            val |= 0x100; // bring over the previous carry
        }
        cpu.setCarryFlag((val & 0x01) != 0);
        val = val >> 1;
        cpu.setSignFlag((val & 0x80) != 0);
        cpu.setZeroFlag((val & 0xFF) == 0);

        if (getAddressingMode() == AddressingMode.Accumulator) {
            cpu.writeRegister(RegisterType.accumulator, val & 0xFF);
        } else {
            memory.write(toInt(operands), val & 0xFF);
        }

        return cycles;

    }
}

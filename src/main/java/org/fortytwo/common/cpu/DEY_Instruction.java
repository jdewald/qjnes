package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

/**
 * Decrement index Y by 1
 */
public class DEY_Instruction extends Instruction
{
    DEY_Instruction(AddressingMode mode){
        super(mode, "DEY");
        switch (mode){
        case Implied: cycles = 2; break;
        default: throw new IllegalArgumentException("Mode not supported: " + mode);
        }
    }
    
    public int execute(int[] operands, Memory memory, CPU cpu){
        int y = 0xFF & cpu.readRegister(RegisterType.Y);
        y = y - 1;
        cpu.setZeroFlag(y == 0);
        cpu.setSignFlag((y & 0x80) != 0);
        cpu.writeRegister(RegisterType.Y, y & 0xFF);
        
        return cycles;
    }
}

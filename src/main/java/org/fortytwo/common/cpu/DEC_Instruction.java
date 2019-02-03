package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

/**
 * Decrement memory by one
 */
public class DEC_Instruction extends Instruction
{
    public DEC_Instruction(AddressingMode mode){
	    super(mode, "DEC");
        switch (mode){
            
        case ZeroPageAbsolute: cycles = 5; break;
        case ZeroPageIndexedX: cycles = 6; break; 
        case Absolute: cycles = 6; break;
        case IndexedX: cycles = 7; break; 
        default: throw new IllegalArgumentException("Mode note supported: " + mode);
        }
    }
    
    public int execute(int[] operands, Memory memory, CPU cpu){
        int addr = toInt(operands);
        int val = 0xFF & memory.read(addr);
        val = val - 1;
        cpu.setSignFlag((val & 0x80) != 0);
        cpu.setZeroFlag(val == 0);
        memory.write(addr, 0xFF & val);
        
        return cycles;
    }
}

package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

/**
 * DEC them CMP
 */
public class DCM_Instruction extends Instruction
{
    public DCM_Instruction(AddressingMode mode){
	    super(mode, "DCM");
        switch (mode){
            
        case ZeroPageAbsolute: cycles = 5; break;
        case ZeroPageIndexedX: cycles = 6; break; 
        case Absolute: cycles = 6; break;
        case IndexedX: cycles = 7; break; 
        case IndexedY: cycles = 7; break;
        case PreIndexedIndirect: cycles = 8; break;
        case PostIndexedIndirect: cycles = 8; break;
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
        
        val = 0xFF & val;
        // CMP
        int accum = 0xFF & cpu.readRegister(RegisterType.accumulator);
        int newVal = 0x1FF & (accum + (~val) + 1); // 2s complement addition
        //        int newVal = accum - val;
        cpu.setCarryFlag(newVal < 256);
        cpu.setSignFlag((newVal & 0x80) != 0);
        cpu.setZeroFlag((newVal & 0xFF) == 0);
        return cycles;
    }
}

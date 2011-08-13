package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

// basically RLA then AND
// TODO: refactor back to being based on ASL
public class RLA_Instruction extends Instruction
{
    RLA_Instruction(AddressingMode mode){
        super(mode, "RLA");
        switch (mode){
        case ZeroPageAbsolute: cycles = 5; break;
        case ZeroPageIndexedX: cycles = 6; break;
        case Absolute: cycles = 6; break;
        case IndexedX: cycles = 7; break;
        case IndexedY: cycles = 7; break;
        case PreIndexedIndirect: cycles = 8; break;
        case PostIndexedIndirect: cycles = 8; break;
        default: throw new IllegalArgumentException("AddressMode not supported: " + mode);
        }
    }
    public int execute(int[] operands, Memory memory, CPU cpu){
        int val = 0xFF & memory.read(toInt(operands));
        int oldval = val;
        val = val << 1;
        if (cpu.getCarryFlag()){
            val |= 0x1; // bring over the previous carry
        }
        cpu.setCarryFlag((oldval & 0x80) != 0);
        cpu.setSignFlag((val & 0x80) != 0);
        cpu.setZeroFlag((val & 0xFF) == 0);
        
        memory.write(toInt(operands),val & 0xFF);
        
        int accum = 0xFF & cpu.readRegister(RegisterType.accumulator);
        accum = accum & (0xFF & val);
        cpu.writeRegister(RegisterType.accumulator, accum);
 	    cpu.setZeroFlag((accum & 0xFF) == 0);
	    cpu.setSignFlag((accum & 0x80) != 0);       
        return cycles;
    }
}

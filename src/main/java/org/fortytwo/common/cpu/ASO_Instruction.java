package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

// basically ASL then ORA
// TODO: refactor back to being based on ASL
public class ASO_Instruction extends Instruction
{
    ASO_Instruction(AddressingMode mode){
        super(mode, "ASO");
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
        int val= 0xFF & memory.read(toInt(operands));
	    cpu.setCarryFlag(((val & 0x80) != 0));
	    val = val << 1;

        memory.write(toInt(operands), 0xFF & val);
        int accum = 0xFF & cpu.readRegister(RegisterType.accumulator);
        accum = accum | (0xFF & val);
        cpu.writeRegister(RegisterType.accumulator, accum);
 	    cpu.setZeroFlag((accum & 0xFF) == 0);
	    cpu.setSignFlag((accum & 0x80) != 0);       
        return cycles;
    }
}

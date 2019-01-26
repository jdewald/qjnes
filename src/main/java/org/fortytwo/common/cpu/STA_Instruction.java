package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

/**
 * Store accumulator in memory
 */
public class STA_Instruction extends Instruction
{

    STA_Instruction(AddressingMode mode){
	super(mode, "STA");

	switch (mode){
	case ZeroPageAbsolute: cycles = 3; break;
	case ZeroPageIndexedX: cycles = 4; break;
	case Absolute: cycles = 4; break;
	case IndexedX: cycles = 5; break;
	case IndexedY: cycles = 5; break; 
	case PreIndexedIndirect: cycles = 6; break;
	case PostIndexedIndirect: cycles = 7; break; 
	default: throw new IllegalArgumentException("AddressMode not supported: " + mode);
	}
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        memory.write(toInt(operands), 0xFF & cpu.readRegister(RegisterType.accumulator));
        
	return cycles;
    }
}

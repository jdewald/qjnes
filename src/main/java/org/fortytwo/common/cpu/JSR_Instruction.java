package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

/**
 * Jump to new location, saving return address
 */
public class JSR_Instruction extends Instruction
{
    public JSR_Instruction(AddressingMode mode){
	super(mode, "JSR");

	switch (mode){
	case Absolute: cycles = 6; break;
	default: throw new IllegalArgumentException("Only Absolute mode is supported");
	}
    }

    // stack is in page 1
    public int execute(int[] operands, Memory memory, CPU cpu){
        int pc = cpu.readRegister(RegisterType.programCounter);
        int sp = cpu.readRegister(RegisterType.stackPointer);

        //p--; // go up one so that we can write out our stack
        pc--;
        cpu.pushStack((byte)(0xFF & ((pc & 0xFF00) >> 8)));
        cpu.pushStack((byte)(pc & 0xFF));
        /*
        sp--;
        memory.writeWord(sp | 0x100, pc - 1); // PC has already moved past our real current
        //	System.out.println("Saved " + Integer.toHexString(pc) + " to stack");
        //sp--;
        */
        //sp -= 2;
        /*
          memory.write(sp | 0x100, (byte)((pc >> 8) & 0xFF)); // hibyte
          sp--;
          memory.write(sp | 0x100, (byte)(pc & 0x00FF)); // lowbyte
          sp--; // stack points to the next empty spot
        */
        //cpu.writeRegister(RegisterType.stackPointer, sp & 0xFF);
        cpu.writeRegister(RegisterType.programCounter,(0xFF & operands[0]) | ((0xFF & operands[1]) << 8));
        
        return cycles;
    }
}

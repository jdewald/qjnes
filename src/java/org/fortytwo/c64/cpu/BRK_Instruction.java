package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;
public class BRK_Instruction extends SingleByteInstruction
{
    public BRK_Instruction(AddressingMode mode){
	super(mode, "BRK");
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        cpu.setBreakFlag(true);
        //        System.out.println("BRK...");
        int irqAddress = memory.readWord(MOS6502Emulator.IRQ_VECTOR_ADDRESS);
        int progCounter = cpu.readRegister(RegisterType.programCounter);
        progCounter+=1;
        cpu.pushStack((byte)((progCounter & 0xFF00) >> 8));
        cpu.pushStack((byte)((progCounter & 0xFF)));
        int status = cpu.getCarryFlag() ? MOS6502Emulator.STATUS_FLAG_CARRY : 0x00;
        status |= cpu.getZeroFlag() ? MOS6502Emulator.STATUS_FLAG_ZERO : 0x00;
        status |= cpu.getDecimalFlag() ? MOS6502Emulator.STATUS_FLAG_DECIMAL : 0x00; // decimal flag cleared
        status |= cpu.getOverflowFlag() ? MOS6502Emulator.STATUS_FLAG_OVERFLOW : 0x00;
        status |= cpu.getSignFlag() ? MOS6502Emulator.STATUS_FLAG_SIGN : 0x00;
        status |= cpu.getInterruptsDisabled() ? MOS6502Emulator.STATUS_FLAG_INTERRUPT : 0x00;
        status |= MOS6502Emulator.STATUS_FLAG_BREAK;
        status |= MOS6502Emulator.STATUS_FLAG_UNUSED;
        //status |= STATUS_FLAG_INTERRUPT;
        cpu.pushStack((byte)status);
        cpu.setInterruptsDisabled(true);

        // System.out.println("Setting program counter to: " + Integer.toHexString(irqAddress));
        cpu.writeRegister(RegisterType.programCounter, irqAddress);
        

        /*
        cpu.setInterruptsDisabled(true);

        cpu.setBreakFlag(true);
        
        int pc = cpu.readRegister(RegisterType.programCounter);
        cpu.pushStack((byte)(0xFF & ((pc & 0xFF00) >> 8)));
        cpu.pushStack((byte)0xFF & pc);
        */
        
        return 7;
    }   
}

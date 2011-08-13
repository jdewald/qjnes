package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;
/**
 * Does an INC and then SBC from accum
 */
public class INS_Instruction extends Instruction
{
    public INS_Instruction(AddressingMode mode){
	super(mode, "INS");

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

        // INC
        int addr = toInt(operands);
        int val = memory.read(addr);
        val = (val + 1) & 0xFF;
        cpu.setSignFlag((val & 0x80) != 0);
        cpu.setZeroFlag(val == 0);
        memory.write(addr, (val & 0xFF));
        int accum = 0xFF & cpu.readRegister(RegisterType.accumulator);        
        int newVal = 0x1FF & (accum + (~val) + (cpu.getCarryFlag() ? 1 : 0)); // 2s complement addition
        cpu.setSignFlag((newVal & 0x80) != 0);
        cpu.setZeroFlag((newVal & 0xFF) == 0);
        cpu.setOverflowFlag(((accum ^ newVal) & 0x80) != 0 && ((accum ^ val) & 0x80) != 0); /* check to see if the
                                                                                               new value has a different
                                                                                               sign than the accumulator
                                                                                               and the sign of the 
                                                                                               accumulator and the 
                                                                                               original value was different*/
        
        /**
           This code's insane... clearly I don't really understand the subtraction process
         */
        if (cpu.getDecimalFlag()){
            boolean originalCarry = cpu.getCarryFlag();
            //System.out.println("VAL = " + val + " A = " + accum + " carry = " + cpu.getCarryFlag());
            //            int newVal = 0;
            //newVal = accum - (val + (cpu.getCarryFlag() ? 0x99 : 0));	    
            //System.out.println("A = " + Integer.toHexString(accum) + " val = " + Integer.toHexString(val) + "c = " + cpu.getCarryFlag() + " result = " + Integer.toHexString(newVal));
            cpu.setCarryFlag(newVal == 0 || (accum != 0 && newVal < 256));            
            //            System.out.println((((accum & 0x0f) - (originalCarry ? 0 : 1))));
            if ((((accum & 0x0f) - (originalCarry ? 0 : 1)) < (val & 0x0f))) {
                newVal -= 6; // sub 6 to each nibble group to handle conversion to BCD
                //System.out.println("Result = " + Integer.toHexString(newVal));
            }
            if (newVal > 0x99 && ((0xF & (0x1FF & (accum + (~val) + (originalCarry ? 1 : 0)))) < 6)){
                newVal -= 0x50;
            }
            else {
                if (newVal > 0x99 && (newVal & 0x100) != 0){
                    newVal -= 0x60;
                }
            }


            
        }
        else {
            cpu.setCarryFlag(newVal < 256);
        }
        cpu.writeRegister(RegisterType.accumulator,newVal & 0xFF);
        return cycles;
    }
}

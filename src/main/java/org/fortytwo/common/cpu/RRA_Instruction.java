package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

/**
   RRA - ROR then ADC
   One of the undocumented instructions
 */
public class RRA_Instruction extends Instruction
{
    public RRA_Instruction(AddressingMode mode){
	super(mode, "RRA");

	switch (mode){
	case Accumulator: cycles = 2; break;
	case ZeroPageAbsolute: cycles = 5; break;
	case ZeroPageIndexedX: cycles = 6; break;
	case Absolute: cycles = 6; break;
	case IndexedX: cycles = 7; break;
    case IndexedY: cycles = 7; break;
    case PreIndexedIndirect: cycles = 8; break;
    case PostIndexedIndirect: cycles = 8; break;
	default: throw new IllegalArgumentException("Invalid mode: " + mode);
	}
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        int val = 0;
        // ROR
        if (getAddressingMode() == AddressingMode.Accumulator){ // accumulator
            val = 0xFF & cpu.readRegister(RegisterType.accumulator);
        }
        else {
            val = 0xFF & memory.read(toInt(operands));
        }
        int oldval = val;
        if (cpu.getCarryFlag()){
            val |= 0x100; // bring over the previous carry
        }
        cpu.setCarryFlag((val & 0x01) != 0);
        val = val >> 1;
        cpu.setSignFlag((val & 0x80) != 0);
        cpu.setZeroFlag((val & 0xFF) == 0);
        
        if (getAddressingMode() == AddressingMode.Accumulator){
            cpu.writeRegister(RegisterType.accumulator,val & 0xFF);
        }
        else {
            memory.write(toInt(operands),val & 0xFF);
        }
        

        int accum = 0xFF & cpu.readRegister(RegisterType.accumulator);
        
        int newVal = 0;
        if (cpu.getDecimalFlag()){
            newVal = accum + val + (cpu.getCarryFlag() ? 1 : 0);	    
            cpu.setZeroFlag((newVal & 0xFF) == 0);
            //        System.out.println("A = " + Integer.toHexString(accum) + " val = " + Integer.toHexString(val) + "c = " + cpu.getCarryFlag() + " result = " + Integer.toHexString(newVal));
            // checks to see if just the addition of the last nibble needs to have a carry
            // occur
            if ((((accum & 0x0f) + (val & 0x0f) + (cpu.getCarryFlag() ? 1 : 0))) > 9) {
                newVal += 6; // add 6 to each nibble group to handle conversion to BCD
                //  System.out.println("Result = " + Integer.toHexString(newVal));
            }
            if (! (accum == 0xFF && val == 0xFF)){
                // kludge... too lazy right now but basically this
                // doesn't get calculated properly
                cpu.setSignFlag((newVal & 0x80) != 0);
                boolean originalSignsSame = ((accum ^ val) & 0x80) == 0;
                boolean newSignsDifferent = ((accum ^ newVal) & 0x80) != 0;
                cpu.setOverflowFlag(originalSignsSame && newSignsDifferent);
            }
            else {
                cpu.setSignFlag(true);
                cpu.setOverflowFlag(false);
            }
            if ((((accum & 0x0f) + (val & 0x0f) + (cpu.getCarryFlag() ? 1 : 0))) >= 26) {
                /*
                 * basically when you ave to actually carry over the 10 (gets you 16 which adds another carry for hex)
                 */
                newVal += 0x50;
            }
            else if (newVal > 0x99) {
                newVal += 0x60; // 0x60;
            }
            
            
            cpu.setCarryFlag(newVal > 0x99);
            //        System.out.println("Result = " + Integer.toHexString(newVal));
        }
        else {
            newVal = accum + val;	    
            
            if (cpu.getCarryFlag()){
                newVal++;
            }
            
            cpu.setZeroFlag((newVal & 0xFF) == 0);
            
            
            boolean originalSignsSame = ((accum ^ val) & 0x80) == 0;
            boolean newSignsDifferent = ((accum ^ newVal) & 0x80) != 0;
            boolean bothPositive = ((accum & 0x80) == 0) && ((val & 0x80) == 0);
            boolean bothNegative = ((accum & 0x80) != 0) && ((val & 0x80) != 0);
            
            //    cpu.setOverflowFlag((bothPositive && ((newVal & 0x80) != 0))
            //                        || (bothNegative && ((newVal & 0x80) == 0)));
            cpu.setOverflowFlag(originalSignsSame && newSignsDifferent);
            //cpu.setOverflowFlag(((! originalSignsDifferent) && (((accum & 0x80) == 0 && (newVal & 0x80) != 0) || ((accum & 0x80) != 0 && (newVal & 0x80) == 0))));
            
            
            cpu.setSignFlag((newVal & 0x80) != 0);
            
            //cpu.setCarryFlag((newVal & 0x100) != 0);
            cpu.setCarryFlag(newVal > 0xFF);
        }
        
        cpu.writeRegister(RegisterType.accumulator,newVal & 0xFF);
        
        return cycles;
        
    }
}

package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

/**
 * Add memory to accumulator with carry
 */
public class ADC_Instruction extends Instruction
{

    int cycles;
    final boolean useDecimalMode;
    ADC_Instruction(AddressingMode mode){
        this(mode, true);
    }
    ADC_Instruction(AddressingMode mode, boolean useDecimal){
	super(mode,"ADC");
	
    useDecimalMode = useDecimal;
	switch (mode){
	case Immediate:cycles = 2; break;
	case ZeroPageAbsolute: cycles = 3; break;
	case ZeroPageIndexedX: cycles = 4; break;
	case Absolute: cycles = 4; break;
	case IndexedX: cycles = 4; break;
	case IndexedY: cycles = 4; break; // supposed to add 1 if boundary crossed
	case PreIndexedIndirect: cycles = 6; break;
	case PostIndexedIndirect: cycles = 5; break; // supposed to add 1 if boundary crossed
	default: throw new IllegalArgumentException("AddressMode not supported: " + mode);
	}
    }

    public int execute(int[] operands, Memory memory, CPU cpu){
	int val = toInt(operands);
	if (getAddressingMode() != AddressingMode.Immediate){
	    val = memory.read(val);
	}
    val = val & 0xFF;
	int accum = 0xFF & cpu.readRegister(RegisterType.accumulator);
	
    int newVal = 0;
    if (useDecimalMode && cpu.getDecimalFlag()){
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

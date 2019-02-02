package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

/**
 * Subtract memory from accumulator with borrow
 */
public class SBC_Instruction extends Instruction
{
    int cycles;
    final boolean useDecimalMode; // whether or not we do BCD logic
    SBC_Instruction(AddressingMode mode){
        this(mode,true);
    }

    public SBC_Instruction(AddressingMode mode, boolean useDecimal){
        super(mode, "SBC");
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
        if (useDecimalMode && cpu.getDecimalFlag()){
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

            //System.out.println("Result = " + Integer.toHexString(newVal));

            
        }
        else {
            cpu.setCarryFlag(newVal < 256);
        }
        cpu.writeRegister(RegisterType.accumulator,newVal & 0xFF);
        
    
        return cycles;
    }
}

package org.fortytwo.common.cpu;

import org.fortytwo.common.memory.Memory;

// Ands then then RORs
public class ARR_Instruction extends Instruction
{
    ARR_Instruction(AddressingMode mode){
        super(mode, "ARR");

    }

    public int execute(int[] operands, Memory memory, CPU cpu){
        int val = toInt(operands);
        int accum = 0xFF & cpu.readRegister(RegisterType.accumulator);
        accum = 0xFF & (accum &  val); // AND
        int originalAccum = accum;
        // tests xoring of 6 & 7
        cpu.setOverflowFlag((((accum & 0xC0) == 0x80) || ((accum & 0xC0) == 0x40))); // 6502Extras.txt
        if (cpu.getCarryFlag()){
            accum |= 0x100; // bring over the previous carry
        }
        //        cpu.setCarryFlag((accum & 0x01) != 0); // bit 0 is lost
        boolean originalCarry = cpu.getCarryFlag();
        
        cpu.setCarryFlag((accum & 0x80) != 0); // copy bit 7 into carry
        accum = accum >> 1; // LS
        
        cpu.setZeroFlag((accum & 0xFF) == 0);
        cpu.setSignFlag((accum & 0x80) != 0);
        if (cpu.getDecimalFlag()){
            cpu.setCarryFlag((!originalCarry) && (accum & 0x80) != 0); // copy bit 7 into carry            
            /*            if ((accum & 0xf) > 9){
              accum += 0x56;
              cpu.setCarryFlag(((!originalCarry) && (accum & 0x80) != 0) || ((accum & 0x100) != 0)); // copy bit 7 into carry            
            }
            else if (accum == 0x33 || accum == 0x44 || accum == 0x55 || accum == 0x66 || accum == 0x77){
                accum += 0x66;
                cpu.setCarryFlag((!originalCarry) && (accum & 0x80) != 0); // copy bit 7 into carry            

            }
            else if (!originalCarry && (accum & 0xf0) > 0x90){
                accum += 0x66;
                cpu.setCarryFlag((!originalCarry) && (accum & 0x80) != 0); // copy bit 7 into carry            
            }
            */
            if (accum != 0){
                if ((((originalAccum & 0x0f) + (val & 0x0f) + (originalCarry ? 1 : 0))) > 9){
                    accum += 6;
                }
                
                if ((((originalAccum & 0x0f) + (val & 0x0f) + (originalCarry ? 1 : 0))) >= 16) {            
                    accum += 0x50;
                }
                else if (accum > 99){
                    accum += 0x60;
                }
            }
            //}

        }

        cpu.writeRegister(RegisterType.accumulator, 0xFF & accum);


        return 2;
    }
}

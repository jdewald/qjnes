package org.fortytwo.nes.model.mappers;

import org.fortytwo.common.memory.ROM;
import org.fortytwo.c64.model.memory.MemoryPPU;

/**
 * Best documentation found at: http://www.tripoint.org/kevtris/mappers/mmc1/index.html
 */
public class MMC1Mapper extends Mapper 
{
    protected boolean charInvert = false;

    private int shiftRegister = 0;
    private int shiftIndex = 0;
    private int[] register = new int[4];

    public static final int PRG_SWAPPING = 0x40;
    
    private int swapSize = 0;
    protected boolean swappable_8000 = false;
    protected boolean swappable_A000 = false;
    protected boolean swappable_C000 = false;
    protected boolean swappable_E000 = false;

    protected int mode = 0;
    protected int mirroringType = 0;
    public MMC1Mapper(ROM programROM, ROM charROM){
        super(programROM, charROM);
    }

    
    // we actually deal in 8K chunks so we need to
    // map both 0x8000 and 0xA000
    public void write(int location, int value){
        //        logger.info("Write of " + Integer.toHexString(value) + " to " + Integer.toHexString(location + 0x8000));

        if ((value & 0x80) != 0){
            shiftRegister = 0;
            shiftIndex = 0;
            resetRegister(location);
        }
        else {
            if ((value & 0x01) != 0){
                shiftRegister |= (1 << shiftIndex);
            }
            shiftIndex++;

            if (shiftIndex == 5){
                storeRegisterValue(location);
                shiftRegister = 0;
                shiftIndex = 0;
            }
        }

    }

    private void storeRegisterValue(int location){
        int value = shiftRegister;
        // PRG SWAPPING
        if (location < 0x2000){ // 0x8000
            register[0] = value;
            
            //            logger.info("8000 Value is: " + Integer.toHexString(register[0]));
            
            mirroringType = register[0] & 0x3;
            if (mirroringType == 0 || mirroringType == 1){
                //ppuMemory.setMirroringType(MemoryPPU.SINGLE_SCREEN_MIRRORING);
                throw new RuntimeException("Single screen mirroring?");
            }
            else if (mirroringType == 2){
                ppuMemory.setMirroringType(MemoryPPU.VERTICAL_MIRRORING);
            }
            else if (mirroringType == 3){
                ppuMemory.setMirroringType(MemoryPPU.HORIZONTAL_MIRRORING);
            }

            if ((register[0] & 0x2) != 0){
                swappable_8000 = true;
                swappable_C000 = false;
            }
            else {
                swappable_8000 = false;
                swappable_C000 = true;
            }
            
            if ((register[0] & 0x4) != 0){
                swapSize = 16384;
                logger.info("Using 16K PRG ROM");
            }
            else {
                swapSize = 32768;
                logger.info("Using 32K PRG ROM");
            }

        }
        else if (location < 0x4000){ // 0xA000
            register[1] = value;
            //            logger.info("A000 Value is: " + Integer.toHexString(register[1]));
        }
        else if  (location < 0x6000){ // 0xC000
            register[2] = value;
            //logger.info("C000 Value is: " + Integer.toHexString(register[2]));
        }
        else if (location < 0x10000){ // 0xE000
            register[3] = value;
            //logger.info("E000 Value is: " + Integer.toHexString(register[3]));

            if (swapSize == 16384){ // remember we use 8K chunks
                int bankValue = value & 0xF;
                if (swappable_8000){
                    //                          logger.info("Swapping in at 8000");
                    start_8000 = swapSize * bankValue;
                    start_A000 = start_8000 + CHUNK_SIZE;
                }
                else if (swappable_C000){
                    logger.info("Swapping in at C000");
                    start_C000 = swapSize * bankValue;
                    start_E000 = start_C000 + CHUNK_SIZE;
                }
                else {
                    throw new RuntimeException("Neither 8000 or C000 is swappable!");
                }
            }
            else {
                int bankValue = (value & 0xF) >> 1;
                logger.info("Swapping in 32K: " + bankValue);

                start_8000 = 32768 * bankValue;
                start_A000 = start_8000 + CHUNK_SIZE;
                start_C000 = start_A000 + CHUNK_SIZE;
                start_E000 = start_C000 + CHUNK_SIZE;
            }
        }
        else {
            throw new RuntimeException("Don't know how to handle write to " + Integer.toHexString(location + 0x8000));
        }        
    }
    
    private void resetRegister(int location){
        if (location == 0) { register[0] = 0; }
        else if (location == 0x2000) { register[1] = 0; }
        else if (location == 0x4000) { register[2] = 0; }
        else if (location == 0x6000) { register[3] = 0; }
    }
    private void handleBankSwitching(int val){
        //        if (mode >=0 && mode < 6)logger.info("Bank switch: " + mode + " " + val);
        switch (mode){
        case 0: 
            chr_start_0000 = val * 1024;
            chr_start_0400 = (val + 1) * 1024;
            
            //ppuMemory.setPatternROM(0x0, charROM, val * 1024, 2048);
            break;
        case 1:
            chr_start_0800 = val * 1024;
            chr_start_0C00 = (val + 1) * 1024;
            //            ppuMemory.setPatternROM(0x800, charROM, val * 1024, 2048);
            break;
        case 2:
            chr_start_1000 = val * 1024;
            //            ppuMemory.setPatternROM(0x1000,charROM,val * 1024, 1024);
            break;            
        case 3:
            chr_start_1400 = val * 1024;
            //            ppuMemory.setPatternROM(0x1400,charROM,val * 1024, 1024);
            break;
        case 4:
            chr_start_1800 = val * 1024;            
            //            ppuMemory.setPatternROM(0x1800,charROM,val * 1024, 1024);
            break;
        case 5:{
            chr_start_1C00 = val * 1024;
            //            ppuMemory.setPatternROM(0x1C00,charROM,val * 1024, 1024);
            break;
        }
        case 6: {
            if (swappable_8000){
                start_8000 = val * CHUNK_SIZE;
            }
            else if (swappable_C000){
                start_C000 = val * CHUNK_SIZE;
            }
            break;
        }
        case 7: {
            start_A000 = val * CHUNK_SIZE;
            break;
        }
        default: {
            throw new RuntimeException("Don't know how to handle mode " + mode + " with value " + val);
        }
        }
    }


}

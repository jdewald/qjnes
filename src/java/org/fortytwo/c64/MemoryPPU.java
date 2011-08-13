package org.fortytwo.c64;

import org.fortytwo.c64.memory.Memory;
import org.fortytwo.c64.memory.ROM;
import org.fortytwo.c64.memory.RAM;

import java.util.logging.Logger;

public class MemoryPPU implements Memory
{
    protected Logger logger = Logger.getLogger(this.getClass().getName());
    public static final int PATTERN_ROM_START = 0x0000;
    public static final int PATTERN_ROM_END = 0x2000;

    public static final int SINGLE_SCREEN_MIRRORING = 2;
    public static final int VERTICAL_MIRRORING = 1; // actually 1, but doesn't work with metalgear..
    public static final int HORIZONTAL_MIRRORING = 0; // actually 0
    public static final int RAM_START = 0x2000;
    public static final int RAM_END = 0x3F00;
    public static final int RAM_SIZE = 0x800;
    public static final int VIRTUAL_RAM_SIZE = 0x1000; // can do mirroring of the name and attribute tables
    
    public static final int PALETTE_START = 0x3F00;
    //public static final int PALETTE_END = 0x3F10; // end of BACKGROUND
    //public static final int PALETTE_END = 0x3F20; // this is the SPRITE end, gets mirrored to 0x4000
    public static final int PALETTE_END = 0x4000;
    //    private final byte[] ramData;

    private RAM ram = null;
    private ROM patternROM = null;
    private RAM paletteRAM = null;
    private int mirroringType = VERTICAL_MIRRORING;
    private boolean shouldLog = false;

    public MemoryPPU(ROM patternROM_, int mirroringType_){
        this.ram = new RAM(16384);
        this.patternROM = patternROM_;
        //        setPatternROM(0, patternROM_, 0, 8192);
        //byte patternData[] = new byte[8192];
        //System.arraycopy(patternData, 0, patternROM_.getRaw(), 0, 8192);
        
        //this.patternROM = new ROM("blah",patternData);
        this.mirroringType = mirroringType_;
        //this.patternROM = new ROM("blah", new byte[0]);

        System.out.println("Mirroring Type = " + mirroringType);
        //        int size = patternROM.size();
        /*  for (int i = 0; i < size; i++){
            //   write(i, patternROM.read(i));
        }
        */
        this.paletteRAM = new RAM(32); // 16
    }

    
    public void setPatternROM(ROM patternROM_){
        patternROM = patternROM_;
    }

    public void setPatternROM(int start, ROM patternROM_, int romStart, int romLength){
        System.arraycopy(patternROM_.getRaw(), romStart, ram.getRaw(), start, romLength);
        //System.arraycopy(patternROM.getRaw(), start, patternROM_.getRaw(), romStart, romLength);
        
    }

    public void setMirroringType(int mirroringType)
    {
        this.mirroringType = mirroringType;
        System.out.println("Mirroring Type = " + mirroringType);
    }
    public int read(int address){
        address = address & 0xFFFF;
        address = address % 0x4000;
        if (address >= PATTERN_ROM_START && address < PATTERN_ROM_END){

            if (patternROM.size() > 0){
                return patternROM.read(address - PATTERN_ROM_START);                

            }
            else {
                
                return ram.read(address - PATTERN_ROM_START);                
            }
        }
        else if (address >= RAM_START && address < RAM_END){
            address -= RAM_START;
            // TODO: move this to method
            address = translateNameTableAddress(address);
            //            address = address % RAM_SIZE;

            return ram.read(RAM_START + address); // mirroring of 0x2000 - 0x27FF through 0x2800 - 0x2EFF
        }
        //else if (address >= PALETTE_START && address < (PALETTE_END * 2)){
        else if (address >= PALETTE_START && address < PALETTE_END ){
            address -= PALETTE_START;
            address = address % paletteRAM.size();

            if ((address % 4) == 0){ // mirroring of 0x3F00
                address = 0;
            }
            return paletteRAM.read(address);
        }
        else {
            throw new RuntimeException("Invalid read attempt: " + Integer.toHexString(address));
        }
    }

    public void write(int address, int value){
        address = address % 0x4000;
        if (address >= PATTERN_ROM_START && address < PATTERN_ROM_END) {
            //System.out.println("Writing to: " + Integer.toHexString(address) + "," + Integer.toHexString(value));
            ram.write(address - PATTERN_ROM_START, value);
        }
        else if (address >= RAM_START && address < RAM_END){
            address -= RAM_START;
            address = translateNameTableAddress(address);
            //            address = address % RAM_SIZE;
            ram.write(RAM_START + address,value); // mirroring of 0x2000 - 0x27FF through 0x2800 - 0x2EFF
        }
        //else if (address >= PALETTE_START && address < (PALETTE_END * 2)){
        else if (address >= PALETTE_START && address < PALETTE_END ){
            //            logger.info("Writing to palette: " + Integer.toHexString(address) + " value = " + Integer.toHexString(value));
            address -= PALETTE_START;
            address = address % paletteRAM.size();
            paletteRAM.write(address,value);
        }
        else {
            throw new RuntimeException("Invalid write attempt: " + Integer.toHexString(address));
        }
    }

    public void write(int address, int[] source){
        for (int i = 0; i < source.length; i++){
            write(address + i, source[i]);
        }
    }

    public void read(int address, int[] target){
        for (int i = 0; i < target.length; i++){
            target[i] = read(address + i);
        }
    }

    public void writeWord(int address, int value){
        throw new RuntimeException("Not implemented");
    }

    public int readWord(int address){
        throw new RuntimeException("Not implemented");
    }

    public void dump(int start, int end, String filename){
        throw new RuntimeException("Not implemented");
    }

    /**
     * Apply mirroring
     */
    private int translateNameTableAddress(int address){
        if (mirroringType == HORIZONTAL_MIRRORING){
            if (address < 0x800){
                address = address % 0x400; // use first name table
            }
            else { // use second physical name/attribute table

                address = 0x400 + (address % 0x400);

            }
        }
        else if (mirroringType == VERTICAL_MIRRORING) {
            if (address >= 0x800 && address < 0xC00){
                //                logger.info("Address = " + Integer.toHexString(address));
                //logger.info("New Address: " + Integer.toHexString(address % 0x400));
            }
            if (address < 0x400 || (address >= 0x800 && address < 0xC00)){ // 1st physical name table
                address = address % 0x400;

            }
            else { // move to 2nd physical name table
                address = 0x400 + (address % 0x400);
            }
        }        
        else if (mirroringType == SINGLE_SCREEN_MIRRORING){
            address = address & 0x400;
        }
        return address;
    }
    public void disableLogging(){
        shouldLog = false;
    }

    public void enableLogging(){
        shouldLog = true;
    }

    public void enableLogging(String subsystem){
        enableLogging();
    }

    public void disableLogging(String subsystem){
        disableLogging();
    }
}

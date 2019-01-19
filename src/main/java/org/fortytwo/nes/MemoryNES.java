package org.fortytwo.nes;

import org.fortytwo.common.memory.Memory;
import org.fortytwo.common.memory.MemoryHandler;
import org.fortytwo.common.memory.RAM;
import org.fortytwo.common.io.IODevice;

import java.io.IOException;

import java.util.logging.Logger;

/**
 * Represents the Main Memory for the Commodore 64
 * This handles ROM as well as special I/O sections
 * TODO: Make this be an interface so that it can be mocked as necessary
 */
public class MemoryNES implements Memory
{
    private RAM ram = null;

    private final byte[] ramData;
    private final byte[] programData;
    private final MemoryHandler ppu;
	private final MemoryHandler apu;
    private final IODevice controller1;
    private final Memory cartridge; 
    private boolean shouldLog = false;

    public static final int RAM_START = 0;
    public static final int RAM_END = 0x2000;
    private final int RAM_SIZE;
    public static final int CHAR_ROM_SIZE = 8192;
    public static final int CHAR_ROM_START = 0x8000;
    public static final int CHAR_ROM_END = 0xBFFF;

    public final int PROGRAM_ROM_START;
    public static final int PROGRAM_ROM_END = 0xFFFF;

    public static final int PPU_START = 0x2000;
    public static final int PPU_END = 0x3FFF;
    public static final int PPU_SIZE = 0x8; // mirrors every 8 bytes

    public static final int SAVE_START = 0x6000;
    public static final int SAVE_END = 0x7FFF;

    public static final int SOUND_START = 0x4000;
    public static final int SOUND_END = 0x4017; // actually a gap at 4016

    public static final int EXPANSION_START = 0x4020;
    public static final int EXPANSION_END = 0x5FFF;
    public static final int PPU_SPRITE_DMA_ADDRESS = 0x4014;
    public static final int SOUND_SWITCH = 0x4015;
    public static final int JOYSTICK1 = 0x4016;
    public static final int JOYSTICK2 = 0x4017;

    public static final int LOW_FREQ_TIMER_CONTROL = 0x4017; // write-only
    public int characterROMStart;
    /**
     * Intercept LOAD command
     */
    public static final int LOAD_RAM_ADDRESS = 0xE175;

    private Logger logger;

    private long readCount = 0;
    private long totalReadTime = 0;

    private int prevIOWrite = 0xFF;
    private static int iolatch = 0x17;

    // for mapper 1
    // should create a Cartridge interface which
    // has implementations for each mapper
    private int firstProgramROM = 0;
    private int lastProgramROM = 7;

    RAM saved;
    RAM expansion;
    //    public MemoryNES(ROM programROM, int mapper, RAM ram, MemoryHandler ppu, IODevice controller1){
    /** cartridge should really be MemoryHandler, not Memory... I have way too many memory-like interfaces **/
    public MemoryNES(Memory cartridge, RAM saved, RAM ram, MemoryHandler ppu, MemoryHandler apu, IODevice controller1){
        logger = Logger.getLogger(this.getClass().getName());
        this.cartridge = cartridge;
        this.programData = null;
        this.saved = saved;
        //this.saved = new RAM(0x2000);
        this.expansion = new RAM(0x2000); // well not really, but whateverg
        /*      
        this.programData = programROM.getRaw();
        */
        PROGRAM_ROM_START = 0x8000;
        //        this.characterData = characterROM.getRaw();
        this.ramData = ram.getRaw();
        this.ppu = ppu;
		this.apu = apu;
        this.controller1 = controller1;
        RAM_SIZE = ramData.length;
    }


    public void enableLogging(){
        shouldLog = true;
        ppu.enableLogging();
    }
    public void disableLogging(){
        shouldLog = false;
        ppu.disableLogging();
    }
    
    public void enableLogging(String subsystem){
        if (subsystem.indexOf("ppu") != -1){
            ppu.enableLogging();
        }
    }

    public void disableLogging(String subsystem){
        if (subsystem.indexOf("ppu") != -1){
            ppu.disableLogging();
        }
    }
    public void dump(int start, int end, String filename) throws IOException{
        ram.dump(start,end,filename);
    }
    
    public void read(int location, int[] target){
        for (int i = 0; i < target.length; i++){
            target[i] = read(location+i);
        }
    }
    
    public int read(int location){
        int val = 0;
        /*	if (location == LOAD_RAM_ADDRESS) {
            val = 0x60; // RTS
            }
            else {
        */
        val = readInternal(location,1);
	    if (shouldLog){
            System.out.println("Read " + Integer.toHexString(val) + " from " + Integer.toHexString(location));
	    }
	    //}
        return val;
    }
    
    public int readWord(int location){
        if (location == ramData.length - 1){
            return 0;
        }
        int val = 0xFFFF & (readInternal(location,1) | (0xFF00 & (readInternal(location+1,1) << 8)));
        //int val =  readInternal(location, 2);
        if (shouldLog){
            System.out.println("ReadW " + Integer.toHexString(val) + " from " + Integer.toHexString(location));
        }
        return val;
    }
    
    /**
     * Returns count bytes from location (assumes data is LSB)
     */
    private int readInternal(int location,int count){
        if (location < 0 /*|| location >= ramData.length*/){
            return 0;
        }
        //if (shouldLog && logger.isLoggable(Level.FINE)){
        //          System.out.println("READ from " + Integer.toHexString(location));
        //  System.out.println("RAM STATUS: " + Integer.toHexString(ddrStatus));
        //}
        if (location >= PROGRAM_ROM_START && location <= PROGRAM_ROM_END){
            location = location - PROGRAM_ROM_START;
            if (count == 1){
                //  return 0xFF & programData[location];
                return 0xFF & cartridge.read(location);
            }
            else {
                //                return 0xFF & programData[location] | ((0xFF & programData[location+ 1]) << 8);
                return 0xFF & cartridge.read(location) | ((0xFF & cartridge.read(location + 1)) << 8);
            }
        }
        else if (location >= SAVE_START && location <= SAVE_END){
        	//logger.info("Reading from SAVE: " + Integer.toHexString(location));
            return saved.read(location - SAVE_START);
        }
        else if (location >= EXPANSION_START && location <= EXPANSION_END){
            return expansion.read(location - EXPANSION_START);
        }
        else if (location >= PPU_START && location <= PPU_END){
            return ppu.read((location - PPU_START) % PPU_SIZE);
        }
        else if (location == JOYSTICK1){
            //logger.info("Reading from joystick");
            return controller1.read();
        }
        else if (location == JOYSTICK2){
            //return 0x42; // <== Super Mario Bros doesn't like this
            return 0x40; // not connected
        }
        else if (location >= SOUND_START && location <= SOUND_END){
			return apu.read(location - SOUND_START);
        }
        else if (location == SOUND_SWITCH){ // ignore
            return 0;
        }
        else if (location >= RAM_START && location < RAM_END){
            location = location % RAM_SIZE;
            if ( count == 1){
                //            if (location >= 0xD000 && location <= 0xD004){
                //  System.out.println("Reading from RAM gs/ex" + gameStatus +"/" + exromStatus);
                //            }
                //return 0xFF & ram.read(location);
                return 0xFF & ramData[location];
            }
            else {
                //	    return (0xFF & ram.read(location)) | ((0xFF & ram.read(location + 1)) << 8);
                return 0xFF & ramData[location] | ((0xFF & ramData[location + 1]) << 8);
            }
        }
        else {
            throw new RuntimeException("Invalid read attempt: " + Integer.toHexString(location));
        }
	
    }
    
    public void write(int location, int[] data){
        for (int i = 0; i < data.length; i++){
            write(location + i, data[i]);
        }
    }

    public void write(int location, int val){
        //if (location > 0x7FF){
        if (shouldLog)
            System.out.println("WRITE " + Integer.toHexString(val) + " to " + Integer.toHexString(location));
        
        if (location >= PROGRAM_ROM_START && location <= PROGRAM_ROM_END){
            cartridge.write(location - PROGRAM_ROM_START, val);
        }
        else if (location >= SAVE_START && location <= SAVE_END){
        	//System.out.println("WRITE " + Integer.toHexString(val) + " to " + Integer.toHexString(location));
            saved.write(location - SAVE_START, val);
        }
        else if (location >= EXPANSION_START && location <= EXPANSION_END){
            expansion.write(location - EXPANSION_START, val);
        }
        else if (location >= RAM_START && location < RAM_END){
            ramData[location % RAM_SIZE] = (byte)(val & 0xFF);
        }
        else if (location >= PPU_START && location <= PPU_END){
            ppu.write((location - PPU_START) % PPU_SIZE, val);
        }
        else if (location == PPU_SPRITE_DMA_ADDRESS){ /* Direct DMA access to copy into sprite memory */
            if (shouldLog){
                logger.info("Doing DMA copy of sprite data");
            }
            int spriteData[] = new int[256];
            read(0x100 * val, spriteData);
            ppu.write(3,0); // sets sprite address
            //            ppu.setSpriteMemoryAddress(0);
            for (int i = 0; i < spriteData.length; i++){
                ppu.write(4,spriteData[i]);
            }
        }
        else if (location == JOYSTICK1){
            controller1.write(val);
        }
        else if (location >= SOUND_START && location <= SOUND_END){
			apu.write(location - SOUND_START, val);
        }
        else if (location == SOUND_SWITCH){
        }
        else if (location == LOW_FREQ_TIMER_CONTROL){ // ignore
            //            logger.info("Wrote " + Integer.toHexString(val) + " to " + Integer.toHexString(location));
        }
        else if (location == 0x4025){
            logger.info("Wrote to 4025");
        }
        else {
            throw new RuntimeException("Invalid write attempt at: " + Integer.toHexString(location));
        }
    }

    public void writeWord(int location, int val){
        //	ram.write(location, val & 0xFF);
        //	ram.write(location + 1, (val & 0xFF00) >> 8);
        ramData[location] = (byte)(val & 0xFF);
        ramData[location + 1] = (byte)((val & 0xFF00) >> 8);

    }

}



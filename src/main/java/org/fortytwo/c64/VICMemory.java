package org.fortytwo.c64;

import org.fortytwo.common.exceptions.VicAttemptingToWriteException;
import org.fortytwo.common.memory.BaseMemory;
import org.fortytwo.common.memory.ROM;
import org.fortytwo.common.memory.RAM;

import java.io.IOException;

import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Represents the Main Memory for the Commodore 64
 * This handles ROM as well as special I/O sections
 * TODO: Make this be an interface so that it can be mocked as necessary
 */
public class VICMemory extends BaseMemory
{
    
    private CIA cia = null;
    private RAM ram = null;
    private ROM charROM = null;
    private RAM colorRAM = null;

    private int offset = 0;

    private HashMap<Integer,Integer> prev = new HashMap<Integer,Integer>();

    public static final int RAM_SIZE = 65535;
    
    public static final int CHAR_ROM_START = 0x1000;
    public static final int CHAR_ROM_END = 0x1FFF;
    public static final int CHAR_ROM_SIZE = 0x1000;

    private Logger logger;

    private static int[] bankOffset = new int[] {0xC000, 0x8000, 0x4000, 0x0000 };

    private long readCount = 0;
    private long totalReadTime = 0;

    private final byte[] ramData;
    private final byte[] colorRAMData;
    private final byte[] charData;

    public VICMemory(RAM ram, CIA cia, ROM charROM, RAM colorRAM){
        //	this();
        //this.ram = ram;
        disableLogging();
        this.ramData = ram.getRaw();
        this.cia = cia;
        this.charROM = charROM;
        this.charData = charROM.getRaw();
        //	this.colorRAM = colorRAM;
        this.colorRAMData = colorRAM.getRaw();
        logger = Logger.getLogger(this.getClass().getName());
        int value = cia.readDirect(0);
        //cia.write(0,value & 0);
    }
    public void enableLogging(){
        shouldLog = true;
    }

    public void disableLogging(){
        shouldLog = false;
    }

    public void read(int location, int[] target){
        int bankSelector = cia.readDirect(00) & 0x3;
        if (bankOffset[bankSelector] != offset){
            offset = bankOffset[bankSelector];
            //            System.out.println("VICII Bank offset: " + Integer.toHexString(offset));
        }
        if ((location >= CHAR_ROM_START && location <= CHAR_ROM_END) && (bankSelector == 3 || bankSelector == 1) ){
            //            return 0xFF & charROM.read(location - CHAR_ROM_START);
            for (int i = 0; i < target.length; i++){
                target[i] = charData[(location - CHAR_ROM_START) + i] | ((0xF & colorRAMData[(location + i) & 0x3FF]) << 8);
            }
        }
        else {
            for (int i = 0; i < target.length; i++){
                target[i] = ramData[location + offset + i] | ((0xF & colorRAMData[(location + i) & 0x3FF]) << 8);
            }
        }


        //            target[i] = read(location+i);
        //}
    }

    public int read(int location){
        int val = readInternal(location,1);
        //	int colorMemoryValue = (0xF & colorRAM.read(location & 0x3FF)) << 8;
        // grab the bottom 4 bytes of the color ram data (top 4 is meaningless)
        int colorMemoryValue = (0xF & colorRAMData[location & 0x3FF]) << 8;
        return val | colorMemoryValue;

    }

    public int readWord(int location){
        int val =  readInternal(location, 2);
        //if (shouldLog && logger.isLoggable(Level.FINE)){
	    //System.out.println("VICMEMORY: ReadW " + Integer.toHexString(val) + " from " + Integer.toHexString(location));
	    //}
        return val;
    }

    public void dump(int start, int end, String filename) throws IOException{
        ram.dump(start,end,filename);
    }
    /**
     * Returns count bytes from location (assumes data is LSB)
     */
    private int readInternal(int location,int count){
        int bankSelector = cia.readDirect(00) & 0x3;
	
        // used to have a compare here, but there's no need for
        // it since we're still looking at the value
        if (bankOffset[bankSelector] != offset){
            offset = bankOffset[bankSelector];
            //            System.out.println("VICII Bank offset: " + Integer.toHexString(offset));
        }
        if ((location >= CHAR_ROM_START && location <= CHAR_ROM_END) && (bankSelector == 3 || bankSelector == 1) ){
            //            return 0xFF & charROM.read(location - CHAR_ROM_START);
            return 0xFF & charData[location - CHAR_ROM_START];
        }
        else {
            return 0xFF & ramData[location + offset];
        }
	
    }
    
    public void write(int location, int val){
        throw new VicAttemptingToWriteException();
    }

    public void write(int location, int[] val){
        throw new VicAttemptingToWriteException();
    }
    public void writeWord(int location, int val){
        //	logger.fine("WRITEW " + Integer.toHexString(val) + " to " + Integer.toHexString(location));
        //	RAM[location] = (byte)(val & 0xFF);
        //RAM[location + 1] = (byte)((val & 0xFF00) >> 8);
        throw new VicAttemptingToWriteException();
    }

}

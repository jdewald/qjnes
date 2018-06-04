package org.fortytwo.c64;

import org.fortytwo.c64.io.Joystick;
import org.fortytwo.c64.io.Keyboard;
import org.fortytwo.c64.memory.BaseMemory;
import org.fortytwo.c64.memory.RAM;
import org.fortytwo.c64.memory.ROM;

import java.io.IOException;
import org.fortytwo.c64.video.VICII;

import java.util.logging.Logger;

/**
 * Represents the Main Memory for the Commodore 64
 * This handles ROM as well as special I/O sections
 * TODO: Make this be an interface so that it can be mocked as necessary
 */
public class Memory6502 extends BaseMemory
{
    
    private RAM ram = null;
    private RAM colorRAM = null;
    private ROM basicROM = null;
    private ROM kernalROM = null;
    private final ROM cartridgeROM;
    private final byte[] kernalData;
    private final byte[] basicData;
    private final byte[] charData;
    private  byte[] cartridgeData = null;
    private ROM charROM = null;
    private VICII vic = null;
    private CIA cia1 = null;
    private CIA cia2 = null;
    private Keyboard keyboard;
    private Joystick joystick;


    private final byte[] ramData;
    public static final int RAM_SIZE = 65535;


    public static final int KERNAL_ROM_SIZE = 8192;
    public static final int KERNAL_START = 0xE000;
    public static final int KERNAL_END = 0xFFFF;

    public static final int BASIC_ROM_SIZE = 8192;
    public static final int BASIC_START = 0xA000;
    public static final int BASIC_END = 0xBFFF;

    public static final int CHAR_ROM_SIZE = 4096;
    public static final int CHAR_ROM_START = 0xD000;
    public static final int CHAR_ROM_END = 0xDFFF;

    public static final int SID_SIZE = 0x400;
    public static final int SID_START = 0xD400;
    public static final int SID_END = 0xD7FF;

    public static final int CARTRIDGE_ROM_SIZE = 4096;
    public static final int CARTRIDGE_ROM_START = 0x8000;
    public static final int CARTRIDGE_ROM_END = 0x9FFF;

    private static final int LORAM_FLAG = 0x1;
    private static final int HIRAM_FLAG = 0x2;
    private static final int CHAR_FLAG = 0x4;

    public static final int VIC_START = 0xD000;
    public static final int VIC_END = 0xD3FF;

    public static final int COLOR_RAM_START = 0xD800;
    public static final int COLOR_RAM_END = 0xDBFF;

    public static final int CIA_1_START = 0xDC00;
    public static final int CIA_1_END = 0xDCFF;

    public static final int CIA_2_START = 0xDD00;
    public static final int CIA_2_END = 0xDDFF;


    private int cartridgeROMStart = CARTRIDGE_ROM_START;    
    private int cartridgeROMEnd = CARTRIDGE_ROM_END;
    /**
     * Intercept LOAD command
     */
    public static final int LOAD_RAM_ADDRESS = 0xE175;

    private Logger logger;

    private long readCount = 0;
    private long totalReadTime = 0;

    private int gameStatus = 1;
    private int exromStatus = 1;
    private int prevIOWrite = 0xFF;
    private static int iolatch = 0x17;
    public Memory6502(ROM kernalROM, ROM basicROM, ROM charROM, ROM cartridgeROM, int cartROMStart, RAM ram, RAM colorRAM, VICII vic, CIA cia1, CIA cia2){
        disableLogging();
        logger = Logger.getLogger(this.getClass().getName());
        //	this.kernalROM = kernalROM;
        this.kernalData = kernalROM.getRaw();
        this.basicROM = basicROM;
        this.basicData = basicROM.getRaw();
        this.charROM = charROM;
        this.charData = charROM.getRaw();
        this.cartridgeROM = cartridgeROM;
        if (cartridgeROM != null){
            this.cartridgeData = cartridgeROM.getRaw();
            this.cartridgeROMStart = cartROMStart;
            this.cartridgeROMEnd = (cartROMStart + cartridgeData.length) - 1;
        }
        this.ram = ram;
        this.ramData = ram.getRaw();
        this.colorRAM = colorRAM;
        this.vic = vic;
        this.cia1 = cia1;
        this.cia2 = cia2;
        ramData[0] = (byte)0xEF;
        ramData[1] = 0x17;
        //ram.write(1,7);
    }

    public void setGameStatus(int status){
        this.gameStatus = status;
    }

    public void setExromStatus(int status){
        this.exromStatus = status;
    }

    public void enableLogging(){
        shouldLog = true;
    }
    public void disableLogging(){
        shouldLog = false;
    }

    public void dump(int start, int end, String filename) throws IOException{
        ram.dump(start,end,filename);
    }
    /*    public void registerRead(MemoryHandler handler, int start, int end){
          MemoryHandlerBean registration = new MemoryHandlerBean(handler, start, end);
          readHandlers.add(registration);
          }
          
          public void registerWrite(MemoryHandler handler, int start, int end){
          MemoryHandlerBean registration = new MemoryHandlerBean(handler, start, end);
          writeHandlers.add(registration);
          }
          
    */
    
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
        /*
        if (location == 0xA000){
            System.out.println(Integer.toHexString(location) + " " + Integer.toHexString(val) + " ram = " + ramData[0xA000] + " basic = " + Integer.toHexString(basicData[0]) + " 0 = " + ramData[0] + " 1 = " + ramData[1]);
        } 
        else if (location == 0xD000){
            System.out.println(Integer.toHexString(location) + " " + Integer.toHexString(val) + " ram = " + ramData[0xD000] + " vic = " + Integer.toHexString(kernalData[0])+ " 0 = " + ramData[0] + " 1 = " + ramData[1]);
}
        else if (location == 0xE000){
            System.out.println(Integer.toHexString(location) + " " + Integer.toHexString(val) + " ram = " + ramData[0xE000] + " kernal = " + Integer.toHexString(kernalData[0])+ " 0 = " + ramData[0] + " 1 = " + ramData[1]);
        }
        */
        return val;
    }
    
    public int readWord(int location){
        if (location == ramData.length - 1){
            return 0;
        }
        int val = 0xFFFF & (readInternal(location,1) | (0xFF00 & (readInternal(location+1,1) << 8)));
        //int val =  readInternal(location, 2);
        //	if (shouldLog){
	    //System.out.println("ReadW " + Integer.toHexString(val) + " from " + Integer.toHexString(location));
        //}
        return val;
    }
    
    /**
     * Returns count bytes from location (assumes data is LSB)
     */
    private int readInternal(int location,int count){
        if (location < 0 || location >= ramData.length){
            return 0;
        }
        int ddrStatus = 0xFF & ramData[1];
        
        
        int ddr = 0xFF & ramData[0];
      
        //int ddrStatus = (((~ddr) & 0x17) | (ramData[1] & ddr));
        if (location == 1){
            return ddrStatus;
        }
        //if (shouldLog && logger.isLoggable(Level.FINE)){
        //  System.out.println("READ from " + Integer.toHexString(location));
        //  System.out.println("RAM STATUS: " + Integer.toHexString(ddrStatus));
        //}
        if ((! (((ddrStatus & 0x3) == 0) && gameStatus == 1)) && location >= CHAR_ROM_START && location <= CHAR_ROM_END){
            if ( (ddrStatus & CHAR_FLAG) != 0){ // use I/O
                if (location >= VIC_START && location <= VIC_END){
                    //                    if (location >= 0xD000 && location <= 0xD004){
                    //  System.out.println("Reading from VIC gs/es " + gameStatus + "/" + exromStatus);
                    // }
                    //if (shouldLog){
                    //System.out.println("Reading from VIC");
                    //}
                    return 0xFF & vic.read(location - VIC_START);
                }
                else if (location >= COLOR_RAM_START && location <= COLOR_RAM_END){
                    //if (shouldLog){
                    //System.out.println("Reading from Color RAM");
                    //}
                    return 0xFF & colorRAM.read(location - COLOR_RAM_START);
                }
                else if (location >= CIA_1_START && location <= CIA_1_END){
                    //if (shouldLog){
                    //System.out.println("Reading from CIA 1");
                    //}
                    return 0xFF & cia1.read(location - CIA_1_START);
                }
                else if (location >= CIA_2_START && location <= CIA_2_END){
                    //if (shouldLog){
                    //System.out.println("Reading from CIA 2");
                    //}
                    return 0xFF & cia2.read(location - CIA_2_START);
                }
                else {
                    if ( count == 1){
                        //return 0xFF & ram.read(location);
                        return 0xFF & ramData[location];
                    }
                    else {
                        //	    return (0xFF & ram.read(location)) | ((0xFF & ram.read(location + 1)) << 8);
                        return 0xFF & ramData[location] | ((0xFF & ramData[location + 1]) << 8);
                    }
                    //                    return 0x00; // most likely SID
                    //                    throw new RuntimeException("Don't know how to handle read to: " + Integer.toHexString(location));
                }
            }
            else {
                //if (shouldLog && logger.isLoggable(Level.FINE))
                //		  System.out.println("Reading from CHARACTER MEMORY");
                if (count == 1){
                    //                    if (location >= 0xD000 && location <= 0xD004){
                    //  System.out.println("Reading from CHAR ROM");
                    //}
                    return 0xFF & charROM.read(location - CHAR_ROM_START);
                }
                else {
                    return (0xFF & charROM.read(location - CHAR_ROM_START)) | ((0xFF & charROM.read((location - CHAR_ROM_START)+1)) << 8);
                }
            }
        }
        else if (location >= BASIC_START && location <= BASIC_END){
            if (((ddrStatus & LORAM_FLAG) != 0) && (ddrStatus & HIRAM_FLAG) != 0 && gameStatus == 1){ // use BASIC
                return 0xFF & basicData[location - BASIC_START];
            }
            else if (gameStatus == 0 && (ddrStatus & HIRAM_FLAG) != 0 && (location >= cartridgeROMStart && location <= (cartridgeROMEnd))){
                return 0xFF & cartridgeROM.read(location - cartridgeROMStart);
            }
            else {
                return 0xFF & ramData[location];
            }
        }
        else if ((! (((ddrStatus & 0x3) == 0) && gameStatus == 1)) &&  location >= KERNAL_START && location <= KERNAL_END && ((ddrStatus & HIRAM_FLAG) != 0)){ // use KERNAL
            //if (shouldLog && logger.isLoggable(Level.FINE))
            //System.out.println("Reading from KERNAL MEMORY: " + Integer.toHexString(location - KERNAL_START));
            if (count == 1){
                //return 0xFF & kernalROM.read(location-KERNAL_START);
                return 0xFF & kernalData[location - KERNAL_START];
                
            }
            else {
                //				return (0xFF & kernalROM.read((location-KERNAL_START))) | ((0xFF & kernalROM.read((location - KERNAL_START) + 1)) << 8);
                return (0xFF & kernalData[((location-KERNAL_START))] | ((0xFF & kernalData[(location - KERNAL_START) + 1])) << 8);
            }
        }
        else if (cartridgeROM != null && location >= cartridgeROMStart && location <= cartridgeROMEnd){
            //if (shouldLog && logger.isLoggable(Level.FINE))
            //            System.out.println("Reading from Cartridge ROM: " + Integer.toHexString(location - CARTRIDGE_ROM_START));
            if (count == 1){
                //return 0xFF & kernalROM.read(location-KERNAL_START);
                return 0xFF & cartridgeData[location - cartridgeROMStart];
                
            }
            else {
                //				return (0xFF & kernalROM.read((location-KERNAL_START))) | ((0xFF & kernalROM.read((location - KERNAL_START) + 1)) << 8);
                return (0xFF & cartridgeData[((location-cartridgeROMStart))] | ((0xFF & cartridgeData[(location - cartridgeROMStart) + 1])) << 8);
            }   
        }
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
    
    public void write(int location, int[] data){
        for (int i = 0; i < data.length; i++){
            write(location + i, data[i]);
        }
    }
    public void write(int location, int val){
        //if (location > 0x7FF){
        if (shouldLog)
            System.out.println("WRITE " + Integer.toHexString(val) + " to " + Integer.toHexString(location));
        /*
          for (MemoryHandlerBean bean : writeHandlers){
          if (location >= bean.start && location <= bean.end){
          bean.handler.write(location,val);
          return;
          }
          }
        */
	    //}
        //	int ddrStatus = 0xFF & ram.read(1);
        //        int ddrStatus = 0xFF & ramData[1];
        int ddr = 0xFF & ramData[0];
        // int ddrStatus = (((~ddr) & 0x17) | (ramData[1] & ddr));        
                int ddrStatus = 0xFF & ramData[1];
        if (location >= CHAR_ROM_START && location <= CHAR_ROM_END && (! (((ddrStatus & 0x3) == 0) && gameStatus == 1)) && ((ddrStatus & CHAR_FLAG) != 0)){ // use I/O        
                //        if ((ddrStatus & CHAR_FLAG) != 0){ // use I/O
            if (location >= VIC_START && location <= VIC_END){
                vic.write(location - VIC_START,0xFF & val);
            }
            else if (location >= COLOR_RAM_START && location <= COLOR_RAM_END){

                colorRAM.write(location - COLOR_RAM_START, 0xFF & val);
            }
            else if (location >= CIA_1_START && location <= CIA_1_END){
                cia1.write(location - CIA_1_START, 0xFF & val);
            }
            else if (location >= CIA_2_START && location <= CIA_2_END){
                cia2.write(location - CIA_2_START, 0xFF & val);
            }
            else if (location >= SID_START && location <= SID_END){
                ramData[location] = (byte)(0xFF & val);
                // ignore
            }
            else if (location < ramData.length){
                if (location >= 0xD000 && location <= 0xD004){
                    System.out.println("Writing to RAM (IO)");
                }
                //ram.write(location,0xFF & val);
                ramData[location] = (byte)(0xFF & val);
            }
        }
        else if (location < ramData.length){
            if (location == 0){
                //System.out.println("Writing to DDR: " + Integer.toHexString(val));
                // 0011 0100 (34)
                // 0000 0010 (2)
                // 0001 0111 (17)
                // 0001 0101 (15)
                // 1111 1101 (~2)
                // 0001 0101 (17 & ~2)
                //System.out.println("Previous IO: " + Integer.toHexString(ramData[1]));
                ramData[0] = (byte)val;
                //                if (val == 0){
                //  ramData[1] = (byte)0x17;
                //}
                if (val != 0x2f){
                    ramData[1] = (byte)((~val & 0x17) | (val & prevIOWrite)); /**
                                                                               * Basically the previous 
                                                                               */
                    //                    System.out.println("New IO val: " + Integer.toHexString(ramData[1]));
                    //ramData[1] = (byte)(0xEF & (writeMask) | ((0xFF & ~ddr) & ramData[1]));
                }
                //                ramData[1] = (byte)(((~val) & 0x17) | (ramData[1] & val));

                //ramData[1] = (byte)(val & iolatch);
                //ramData[1] = (byte)(0xFF & ramData[1] & ~val);
                //iolatch = 0x17;
                //ramData[1] = (byte)(0xFF & ramData[1] & (~val));
                if (val == 0){
                    //                    ramData[1] = (byte)0x17; 
                    /**
                                              * turns them all into input, which means that the banking ones
                                              * go back to default and cassette data to 1
                                              * rest go to 0
                                              */
                }
                //                ramData[1] = (byte)(((val) & 0x17));// | (val & 0x34));
                //System.out.println("IO = " + Integer.toHexString(ramData[1]));
                /**
                   0010 1111 (2f)                   
                   0011 0100 (34)

                   STY $00
                   0000 0000 (00)
                   0001 0111 (17)

                   0000 0010 (02)
                   0001 0111 (17)
                   0011 0111 (37)

                   0000 0010 ^ 0000 0000 = 0000 0010
                   1111 1101 | 0000 0000 = 1111 1101
                   0001 0111 & 1111 1101 = 0001 0101

                   0000 0010 ^ 0011 0111 = 0011 0101
                   1111 1101 | 0011 0111 = 1111 1111
                   0001 0111 & 1111 1111 = 0001

                   1111 1111 | 0000 0000 = 1111 1111
                   0001 0111 & 1111 1111 = 0001 0111
                   0000 0000
                   0001 0101
                   0000 0000
                   0001 0111
               
                 */
            }
            if (location == 1){
                int oldVal = ramData[1];
                ddr = ramData[0];
                int defaultVal = 0x17;
               
                int writeMask = ddr & val;
                //                writeMask |= (~ddr & defaultVal); 
                /* anything we don't
                   have control over
                   maintains the default;
                */
                if (ddr == 0 && val == 0){
                    ramData[1] = (byte)defaultVal;
                }
                else {
                    ramData[1] = (byte)(0xFF & (writeMask) | ((0xFF & ~ddr) & ramData[1]));
                }
                                                        
                if (prevIOWrite != val){
                    prevIOWrite = val;
                    //                    System.out.println("Writing to I/O: " + Integer.toHexString(val) + "/" + Integer.toHexString(ramData[location]));
                }
                
                //ramData[location] = (byte)(ramData[0] & val);
            }
            else {
                if (location >= 0xD000 && location <= 0xD004){
                    //                    System.out.println("Writing to RAM");
                }
                
                ramData[location] = (byte)(0xFF & val);
            }
            //ram.write(location,0xFF & val);
        }
    }

    public void writeWord(int location, int val){
        //	ram.write(location, val & 0xFF);
        //	ram.write(location + 1, (val & 0xFF00) >> 8);
        ramData[location] = (byte)(val & 0xFF);
        ramData[location + 1] = (byte)((val & 0xFF00) >> 8);

    }

    public ROM getCharROM() {
        return charROM;
    }

    public RAM getRam() {
        return ram;
    }

    public RAM getColorRAM() {
        return colorRAM;
    }

    public CIA getCia2() {
        return cia2;
    }
}



package org.fortytwo.c64.video;

import org.fortytwo.c64.cpu.CycleObserver;
import org.fortytwo.c64.cpu.CPU;
import org.fortytwo.c64.memory.MemoryHandler;
import org.fortytwo.c64.memory.Memory;

import java.util.Arrays;
import java.util.logging.Logger;

import java.awt.image.BufferedImage;

import java.awt.Color;

import javax.swing.JFrame;
/**
 * Representation of the VIC-II
 *
 * The VIC-II's dot clock is actually fed into the real 6510 by dividing it by 8
 * So 8.18 Mhz / 8 = 1.023 Mhz
 */
public class VICII implements MemoryHandler, CycleObserver
{
    protected Logger logger;

    private static final int VIDEO_RAM_SIZE = 0x3FF;
    private static final int NUM_REGISTERS = 47;
    // make these enums that dirctly get you a color?
    private static final int COLOR_BLACK = 0;
    private static final int COLOR_WHITE = 1;
    private static final int COLOR_RED = 2;
    private static final int COLOR_CYAN = 3;
    private static final int COLOR_PURPLE = 4;
    private static final int COLOR_GREEN = 5;
    private static final int COLOR_BLUE = 6;
    private static final int COLOR_YELLOW = 7;
    private static final int COLOR_ORANGE = 8;
    private static final int COLOR_BROWN = 9;
    private static final int COLOR_LIGHT_RED = 10;
    private static final int COLOR_DARK_GREY = 11;
    private static final int COLOR_MEDIUM_GREY = 12;
    private static final int COLOR_LIGHT_GREEN = 13;
    private static final int COLOR_LIGHT_BLUE = 14;
    private static final int COLOR_LIGHT_GREY = 15;

    private static final int SPRITE_0_X = 0x00;
    private static final int SPRITE_0_Y = 0x01;
    private static final int SPRITE_1_X = 0x02;
    private static final int SPRITE_1_Y = 0x03;
    private static final int SPRITE_2_X = 0x04;
    private static final int SPRITE_2_Y = 0x05;
    private static final int SPRITE_3_X = 0x06;
    private static final int SPRITE_3_Y = 0x07;
    private static final int SPRITE_4_X = 0x08;
    private static final int SPRITE_4_Y = 0x09;
    private static final int SPRITE_5_X = 0x0A;
    private static final int SPRITE_5_Y = 0x0B;
    private static final int SPRITE_6_X = 0x0C;
    private static final int SPRITE_6_Y = 0x0D;
    private static final int SPRITE_7_X = 0x0E;
    private static final int SPRITE_7_Y = 0x0F;
    private static final int SPRITE_X_MSBS = 0x10;

    private static final int CONTROL_REGISTER_1 = 0x11;
    private static final int RASTER_INTERRUPT_LINE = 0x12;
    
    private static final int LIGHT_PEN_X = 0x13;
    private static final int LIGHT_PEN_Y = 0x14;
    
    private static final int SPRITE_ENABLED = 0x15;
    private static final int CONTROL_REGISTER_2 = 0x16;
    private static final int SPRITE_Y_EXPANSION = 0x17;
    private static final int MEMORY_SETUP = 0x18;
    private static final int INTERRUPT_STATUS = 0x19;
    private static final int INTERRUPT_CONTROL = 0x1A;
    
    private static final int SPRITE_PRIORITY = 0x1B;
    private static final int SPRITE_MULTICOLOR = 0x1C;
    private static final int SPRITE_X_EXPANSION = 0x1D;
    private static final int COLLISION_SPRITE_DATA = 0x1E;
    private static final int COLLISION_SPRITE_SPRITE = 0x1F;

   private static final int BORDER_COLOR = 0x20;
    
    private static final int BACKGROUND_COLOR_0 = 0x21;
    private static final int BACKGROUND_COLOR_1 = 0x22;
    private static final int BACKGROUND_COLOR_2 = 0x23;
    private static final int BACKGROUND_COLOR_3 = 0x24;

    //** sprites
    private static final int SPRITE_0_ENABLED = 0x1; // SPRITE_ENABLED area
    private static final int SPRITE_1_ENABLED = 0x2;
    private static final int SPRITE_2_ENABLED = 0x4;
    private static final int SPRITE_3_ENABLED = 0x8;
    private static final int SPRITE_4_ENABLED = 0x10;
    private static final int SPRITE_5_ENABLED = 0x20;
    private static final int SPRITE_6_ENABLED = 0x40;
    private static final int SPRITE_7_ENABLED = 0x80;

    private static final int SPRITE_MULTICOLOR_0 = 0x25;
    private static final int SPRITE_MULTICOLOR_1 = 0x26;
    private static final int SPRITE_0_COLOR = 0x27;
    private static final int SPRITE_1_COLOR = 0x28;
    private static final int SPRITE_2_COLOR = 0x29;
    private static final int SPRITE_3_COLOR = 0x2A;
    private static final int SPRITE_4_COLOR = 0x2B;
    private static final int SPRITE_5_COLOR = 0x2C;
    private static final int SPRITE_6_COLOR = 0x2D;
    private static final int SPRITE_7_COLOR = 0x2E;

    /** CONTROL REGISTER BITS
     */
    private static final int CR1_RASTER_HIGH = 0x80;

    private static final int CR1_ECM = 0x40;
    private static final int CR1_BMM = 0x20;
    private static final int CR1_DEN = 0x10;
    private static final int CR1_RSEL = 0x08;
    private static final int CR1_YSCROLL = 0x07;

    private static final int CR2_MCM = 0x10;
    private static final int CR2_CSEL = 0x08;
    private static final int CR2_XSCROLL = 0x07;
    private int[] videoRAM = new int[VIDEO_RAM_SIZE];

    private int[] screen = new int[320 * 200];

    private boolean collisionDetected = false;
    Screen videoScreen = null;
    JFrame videoFrame;
    private Memory memory;
    
    public static final int NTSC_SCAN_LINES = 262;
    public static final int NTSC_VISIBLE_START_LINE = 50; // actually depends on RSEL and is about visible border
    public static final int NTSC_VISIBLE_END_LINE = 250;
    //public static final int NTSC_VISIBLE_SCAN_LINES = 200;
    public static final int NTSC_VISIBLE_SCAN_LINES = 234;
    public static final int CHARACTERS_PER_LINE = 40;
    public static final int START_X = 24;
    public static final int END_X = 343;
    public static final int PAL_SCAN_LINES = 312;
    
    public static final int DOT_CLOCK_SPEED = 8180000; // 8.18 Mhz

    //    private static final int PAL_CYCLES_PER_LINE = 65; // where does this come from? (8180000 / 8) / 16421
    private static final int NTSC_CYCLES_PER_LINE = 64; // comes from vic-ii.txt
    private static final int PIXELS_PER_DOT_CLOCK_CYCLE = 8;
    private int currentScanLine = 0;
    private int cyclesUntilNextLine = 0;

    private static final int[] screenOffsets = new int[] {0x0000,0x0400,0x0800,0x0C00,
                                                          0x1000,0x1400,0x1800,0x1C00,
                                                          0x2000,0x2400,0x2800,0x2C00,
                                                          0x3000,0x3400,0x3800,0x3C00};
    private static final int[] charOffsets = new int[] { 0x0000,0x0800,0x1000,0x1800,
                                                         0x2000,0x2800,0x3000,0x3800};

    private static final int[] bitmapOffsets = new int[] {0x0000, 0x2000};

    int screenMemoryStart = 0;
    int charMemoryStart = 0;
    int bitmapMemoryStart = 0;
    boolean displayEnabled = true;
    int mcm = 0;
    int ecm = 0;
    int bmm = 0;
    int rsel = 0;
    int csel = 0;
    int yscroll = 0;
    int xscroll = 0;
    //    private Color screenColor;
    private int screenRGB;
    private int spriteMulticolorRGB1 = 0;
    private int spriteMulticolorRGB2 = 0;
    private static final int NUM_SPRITES = 8;

    private boolean spriteEnabled[] = new boolean[NUM_SPRITES];
    private int spriteX[] = new int[NUM_SPRITES];
    private int spriteY[] = new int[NUM_SPRITES];
    
    private static final int SPRITE_POINTER_OFFSET = 0x3F8; /* offset from screenMemory */
    private static final int SPRITE_ADDRESS_MULTIPLIER = 64; 

    // working variables
    private int interruptRasterLine = -1;
    

    private long frameStartTimeMs = 0;
    
    private boolean rasterInterruptEnabled = false;
    private boolean collisionInterruptEnabled = false;

    public VICII(){
        logger = Logger.getLogger(this.getClass().getName());
        cyclesUntilNextLine = NTSC_CYCLES_PER_LINE;
	
        currentScanLine = 0;

        charMemoryStart = charOffsets[(videoRAM[MEMORY_SETUP] & 0x0E) >> 1];
        bitmapMemoryStart = bitmapOffsets[(videoRAM[MEMORY_SETUP] & 0x8) >> 3];
        screenMemoryStart = screenOffsets[(videoRAM[MEMORY_SETUP] & 0xF0) >> 4];

        screenRGB = Color.red.getRGB();
    }
    
    public void setMemory(Memory memory){
        this.memory = memory;
    }

    public void setScreen(Screen screen){
        this.videoScreen = screen;
    }

    public void enableLogging(){
    }

    public void disableLogging(){
    }
    // The or's are because some bits are always 1 upon reading
    public int read(int address){
        address = address % NUM_REGISTERS;
        switch (address){
        case INTERRUPT_STATUS:
            return 0xFF & (0x70 | videoRAM[INTERRUPT_STATUS]);
        case CONTROL_REGISTER_1:
            return ((currentScanLine & 0x100) >> 1) | videoRAM[address];
        case CONTROL_REGISTER_2:
            return 0xFF & (0xC0 | videoRAM[address]);
        case RASTER_INTERRUPT_LINE:
            return currentScanLine & 0xFF;
        case INTERRUPT_CONTROL:
            return 0xFF & (0xF0 | videoRAM[address]);
        case SPRITE_5_X:
        case SPRITE_6_X:
            System.out.println("Reading from sprite x");
        default:
            return videoRAM[address];
        }
    }

    public void write(int address, int value){
        //System.out.println("[---------- VIC -----------------]");
        //        System.out.println("VICII: Write " + Integer.toHexString(value) + " to " + Integer.toHexString(address));
        address = address % NUM_REGISTERS;
        switch (address){
        case SPRITE_0_X:
        case SPRITE_1_X:
        case SPRITE_2_X:
        case SPRITE_3_X:
        case SPRITE_4_X:
        case SPRITE_5_X:
        case SPRITE_6_X:
        case SPRITE_7_X:
            {
                videoRAM[address] = 0xFF & value;
                int spriteNum = (address - SPRITE_0_X) / 2;
                int oldX = spriteX[spriteNum];
                // take the current value along with the MSB value
                int spriteMSB = videoRAM[SPRITE_X_MSBS] & (1 << spriteNum);
                spriteX[spriteNum] = ((0xFF & value) | (spriteMSB != 0 ? 0x100 : 0));
                if (spriteNum == 5 && oldX < 500 && Math.abs(oldX - spriteX[spriteNum]) > 4){
                    //                    System.out.println("modified sprite X: old = " + oldX + "new = " + spriteX[spriteNum]);
                }

                //                System.out.println("1Sprite " + spriteNum + " X = " + spriteX[spriteNum]);
                break;
            }
        case SPRITE_0_Y:
        case SPRITE_1_Y:
        case SPRITE_2_Y:
        case SPRITE_3_Y:
        case SPRITE_4_Y:
        case SPRITE_5_Y:
        case SPRITE_6_Y:
        case SPRITE_7_Y:
            {
                int spriteNum = (address - SPRITE_0_Y) / 2;
                spriteY[spriteNum] = value; // +8
                videoRAM[address] = value;
                //videoRAM[address] = value;

                break;

            }
        case SPRITE_X_MSBS:
            {
                videoRAM[address] = 0xFF & value;
                for (int spriteNum = 0; spriteNum < NUM_SPRITES; spriteNum++){
                    int spriteMSB = videoRAM[SPRITE_X_MSBS] & (1 << spriteNum);
                    spriteX[spriteNum] = (videoRAM[SPRITE_0_X + (spriteNum * 2)] | (spriteMSB != 0 ? 0x100 : 0)); 
                }
                break;
            }
        case LIGHT_PEN_X:
        case LIGHT_PEN_Y:
            {
                videoRAM[address] = value & 0xFF;
                break;
            }
        case INTERRUPT_STATUS:
            {
                //System.out.println("interrupt status before: " + Integer.toHexString(videoRAM[INTERRUPT_STATUS]));
                videoRAM[INTERRUPT_STATUS] &= (~value); // clears any bits that are already set
                //System.out.println("interrupt status after " + Integer.toHexString(value) + ": " + Integer.toHexString(videoRAM[INTERRUPT_STATUS]));
                break;
            }
        case RASTER_INTERRUPT_LINE:{

            videoRAM[address] = value & 0xFF;
            //int oldRasterLine = interruptRasterLine;
            interruptRasterLine = 0xFF & videoRAM[RASTER_INTERRUPT_LINE] | ((videoRAM[CONTROL_REGISTER_1] & 0x80) << 1);
            //if (oldRasterLine != interruptRasterLine){
                //                                System.out.println("VICII: Interrupt raster line = " + interruptRasterLine);
            //}
            //            logger.fine("Interrupt raster line = " + interruptRasterLine);
            break;
        }
        case INTERRUPT_CONTROL: {
            //            System.out.println("Interrupt control: " + Integer.toHexString(value));
            videoRAM[address] = value & 0xFF;
            rasterInterruptEnabled = ((value & 0x1) != 0);
            collisionInterruptEnabled = ((value & 0x4) != 0);
            
            //            System.out.println("VICII: Interrupts enabled: " + rasterInterruptEnabled);
            //            System.out.println("VICII: Collisions enabled: " + collisionInterruptEnabled);
            //            System.out.println("VICII: Data collisions: " + ((value & 0x2) != 0));
            break;
        }
        case MEMORY_SETUP: {
            videoRAM[MEMORY_SETUP] = 0xFF & value;
            int oldCharStart = charMemoryStart;
            charMemoryStart = charOffsets[(videoRAM[MEMORY_SETUP] & 0x0E) >> 1];
            if (oldCharStart != charMemoryStart){
                //                System.out.println("VICII: new char start: " + Integer.toHexString(charMemoryStart));
            }
            bitmapMemoryStart = bitmapOffsets[(videoRAM[MEMORY_SETUP] & 0x08) >> 3];
            int oldScreenStart = screenMemoryStart;
            screenMemoryStart = screenOffsets[(videoRAM[MEMORY_SETUP] & 0xF0) >> 4];
            if (oldScreenStart != screenMemoryStart){

                //                System.out.println("VICII: new screen start: " + Integer.toHexString(screenMemoryStart));
            }
            break;
        }
        case SPRITE_ENABLED:
            {
                videoRAM[SPRITE_ENABLED] = value;
                //             System.out.println("VICII: Sprite enabling: " + Integer.toHexString(value));
                for (int i = 0; i < NUM_SPRITES; i++){
                    spriteEnabled[i] = ((value & (1 << i)) != 0);
                    
                }
                        
                break;
            }
        case SPRITE_PRIORITY:
        case SPRITE_MULTICOLOR:
            {
                //                System.out.println("Sprite Multicolor: " + value);
                videoRAM[address] = value;
                break;
            }
        case SPRITE_MULTICOLOR_0:
            {
                videoRAM[address] = value;
                spriteMulticolorRGB1 = getRGB(value);
                break;
            }
        case SPRITE_MULTICOLOR_1:
            {
                videoRAM[address] = value;
                spriteMulticolorRGB2 = getRGB(value);
                break;
            }
        case SPRITE_X_EXPANSION:
            {
                //System.out.println("X expansion: " + value);
                videoRAM[address] = value;
                break;
            }
        case SPRITE_Y_EXPANSION:
            {
                //System.out.println("Y expansion" + value);
                videoRAM[address] = value;
                break;
            }
        case COLLISION_SPRITE_DATA:
        case COLLISION_SPRITE_SPRITE:
            {
                videoRAM[address] = 0xFF & value;
                break;
            }
        case BORDER_COLOR:
            {
                videoRAM[BORDER_COLOR] = 0xFF & value;
                break;
            }
        case BACKGROUND_COLOR_0:{
            videoRAM[BACKGROUND_COLOR_0] = 0xFF & value;
            
            screenRGB = getRGB(value);
            break;
        }
        case BACKGROUND_COLOR_1:{
            videoRAM[BACKGROUND_COLOR_1] = 0xFF & value;
            break;
        }
        case BACKGROUND_COLOR_2:{
            videoRAM[BACKGROUND_COLOR_2] = 0xFF & value;
            break;
        }
        case BACKGROUND_COLOR_3:{
            videoRAM[BACKGROUND_COLOR_3] = 0xFF & value;
            break;
        }
        case SPRITE_0_COLOR:
        case SPRITE_1_COLOR:
        case SPRITE_2_COLOR:
        case SPRITE_3_COLOR:
        case SPRITE_4_COLOR:
        case SPRITE_5_COLOR:
        case SPRITE_6_COLOR:
        case SPRITE_7_COLOR:{
            videoRAM[address] = 0xFF & value;
            break;
        }
            
        case CONTROL_REGISTER_1:{
            //            System.out.println("VICII Wrote to CR1: " + Integer.toHexString(value));
            //System.out.println("VICII CR DEN: " + (CR1_DEN & value));
            ecm = ((CR1_ECM & value)) >> 6;
            bmm = ((CR1_BMM & value)) >> 5;
            rsel = ((CR1_RSEL & value)) >> 3;
            int oldYScroll = yscroll;
            yscroll = (CR1_YSCROLL & value);
            
            if (yscroll != oldYScroll){
                System.out.println("yscroll = " + yscroll);
            }
            //            System.out.println("VICII CR ECM: " + ecm);
            //            System.out.println("VICII CR BMM: " + bmm);
            //System.out.println("VICII RSEL: " + rsel);
            displayEnabled = ((CR1_DEN & value) != 0);
            //System.out.println("VICII CR MCM: " + (CR2_MCM & videoRAM[CONTROL_REGISTER_2]));
            videoRAM[address] = 0xFF & value;
            int oldRasterLine = interruptRasterLine;
            interruptRasterLine = 0xFF & videoRAM[RASTER_INTERRUPT_LINE] | ((videoRAM[CONTROL_REGISTER_1 ] & 0x80) << 1);
            if (oldRasterLine != interruptRasterLine){
                  System.out.println("VICII: Interrupt raster line = " + interruptRasterLine);
            }

            break;
        }
        case CONTROL_REGISTER_2:{
            System.out.println("VICII Wrote to CR2: " + Integer.toHexString(value));
            //            System.out.println("VICII CR ECM: " + (CR1_ECM & videoRAM[CONTROL_REGISTER_1]));
            //System.out.println("VICII CR BMM: " + (CR1_BMM & videoRAM[CONTROL_REGISTER_1]));
            int prevMCM = mcm;
            mcm = ((CR2_MCM & value) >> 4);
            if (mcm != prevMCM){
                //                System.out.println("VICII CR MCM: " + mcm);
            }
            csel = ((CR2_CSEL & value) >> 3);
            int oldscroll = xscroll;
            xscroll = ((CR2_XSCROLL & value));
            if (oldscroll != xscroll){
                System.out.println(xscroll);
            }
            //System.out.println("csel = " + csel);
            videoRAM[address] = 0xFF & value;
            break;
        }
        default:{
            throw new RuntimeException("Don't know how to handle write to: " + Integer.toHexString(address));
        }

        }

    }
    

    /**
     * Returns the number of cycles expected until an interrupt
     */
    //    private boolean drawThisFrame = true;
    int[] screenLine = new int[CHARACTERS_PER_LINE]; // 40 characters per line    
    private int frameCounter = 0; // basically a frame rate throttle
    public int tick(int cycles, CPU cpu){
        if (frameStartTimeMs == 0){
            frameStartTimeMs = System.currentTimeMillis();
        }
        cyclesUntilNextLine -= cycles;
        boolean interrupted = false;
        if (cyclesUntilNextLine <= 0){
            int overflow = 0;
            if (cyclesUntilNextLine < 0){
                overflow = -cyclesUntilNextLine;

            }


            /**
             * Displays at 25/30 fps instead of 50
             */
            if (currentScanLine == NTSC_VISIBLE_END_LINE){
                
                frameCounter++;
                //drawRasterLine();
                //drawScreen();
                repaint();
                if (frameCounter == 1000){
                    long frameEndTimeMs = System.currentTimeMillis();
                    

                    long totalTime = frameEndTimeMs - frameStartTimeMs;
                    System.out.println("FPS: " + ((double)frameCounter / ((double)totalTime/1000.0)));
                    frameStartTimeMs = frameEndTimeMs;
                    frameCounter = 0;
                }
                //                videoScreen.repaint();
                //drawThisFrame = false;

            }
            
            currentScanLine = (currentScanLine + 1) % NTSC_SCAN_LINES;
            
            if (displayEnabled && currentScanLine >= NTSC_VISIBLE_START_LINE && currentScanLine <= NTSC_VISIBLE_END_LINE){
                drawLine(currentScanLine, screenLine);
                drawSprites(currentScanLine);

            }
           

            /*            else {
                drawThisFrame = true;
            }
            */
            cyclesUntilNextLine = NTSC_CYCLES_PER_LINE - overflow;
            

            //logger.fine("Current Scan Line: " + currentScanLine);
            if (rasterInterruptEnabled && currentScanLine == interruptRasterLine){
                videoRAM[INTERRUPT_STATUS] |= 0x1; // set the interrupt
                //System.out.println("VICII: Scan Line interrupt!");
                cpu.handleInterrupt();
                return 0;
                //interrupted = true;
            }
        }
        else if (collisionInterruptEnabled && collisionDetected){
            collisionDetected= false;
            cpu.handleInterrupt();
            return 0;
        }
        return cyclesUntilNextLine;
    }

    public int untilReady(int cycles){
        cyclesUntilNextLine -= cycles;
        return cyclesUntilNextLine;
    }



    private int[][] oldRgbLine = new int[(NTSC_VISIBLE_END_LINE - NTSC_VISIBLE_START_LINE)+1][(END_X - START_X) + 1];
    //    private int[] oldScreenMemory = new int[CHARACTERS_PER_LINE * (NTSC_VISIBLE_END_LINE - NTSC_VISIBLE_START_LINE)];
    /**
     * Redraws the entire screen
     */
    private void drawScreen(){
        
        if (displayEnabled){

            //            int screenRGB = screenColor.getRGB(); // default background colo

            for (int line = NTSC_VISIBLE_START_LINE; line <= NTSC_VISIBLE_END_LINE; line++){
                drawLine(line, screenLine);

            }
        }
    }

    int rgbLine[] = new int[(END_X - START_X) + 1];   
    protected void drawLine(int line, int[] screenLine){

        int row = (line - NTSC_VISIBLE_START_LINE) >> 3; // divides by eight
        //        System.out.println("Row = " + row);
        int rowCounter = (line - NTSC_VISIBLE_START_LINE) % 8;
        int col = 0;
	    
        // this is reading from the video matrix
        read((screenMemoryStart) + (row * CHARACTERS_PER_LINE),screenLine); // pre-c-access
        int lastChar = -1;
        int charLine = -1;
        int prevAddress = 0;
        for (int curX = START_X; curX <= END_X; curX += 8, col++){

            int memoryValue = screenLine[col]; // c-access
            if (bmm == 0){ // text mode - use character generator           
                if (ecm == 1 && mcm == 1){ // Invalid Text Mode - everything is black
                    for (int i = 0; i < 8; i++){
                        rgbLine[(curX - START_X) + i] = 0x000000;
                    }
                    continue;
                }
                else if (ecm == 1){
                    throw new RuntimeException("ECM not supported");
                }
                //		int memoryValue = memory.read((screenMemoryStart) + (row * CHARACTERS_PER_LINE) + col);		

                int characterVal = 0xFF & memoryValue;
                    
                if (characterVal != lastChar){
                    // CB13 CB12 CB11 D7 D6 D5 D4 D3 D2 D1 D0 RC2 RC1 RC0
                    int charAddress = charMemoryStart + (characterVal * 8) + rowCounter;
                    //System.out.println("VICII char value: " + characterVal);
                    charLine = 0xFF & memory.read(charAddress); // g-access
                    lastChar = characterVal;
                }
                
                int foregroundRGB = 0;
                int colorValue = (0xF00 & memoryValue) >> 8;
                
                if (mcm == 0 || ((colorValue & 0x8) == 0)){ // bit 11 of what came of the "c-access"
                    if (mcm == 1){
                        colorValue = colorValue & 0x7F;
                    }
                    
                    foregroundRGB = getRGB(colorValue);
                    setCharacterRGBLine(charLine, screenRGB, foregroundRGB, curX, rgbLine);
                    
                } 
                else { // multicolor = yes
                    colorValue = colorValue & 0x7F;
                    foregroundRGB = getRGB(colorValue);
                    setMulticolorCharacterRGBLine(charLine, foregroundRGB, curX, rgbLine);
                }
            }
            else { // bitmap mode
                if (ecm == 1){
                    throw new RuntimeException("ECM not supported");
                }
                // 8x8 blocks on screen are 8 consecutive bytes
                // 12345678 12345678 12345678 .....
                // 90123456 90123456 90123456
                // CB13 VC9 VC8 VC7 VC6 VC5 VC4 VC3 VC2 VC1 RC2 RC1 RC0
                
                // CHARACTERS_PER_LINE * 8 = 320
                // g-access
                // the * 8 offsets it against the fact that there are 8 consecutive bytes
                // the rowCounter chooses the actual byte
                int bitmapAddress = bitmapMemoryStart + (row * 8 * CHARACTERS_PER_LINE) + (col * 8) + rowCounter;
                //                System.out.println(bitmapAddress - prevAddress);
                
                int bitmapValue = memory.read(bitmapAddress);
                if (mcm == 0){
                    int foregroundRGB = getRGB((memoryValue & 0xF0) >> 4);
                    int backgroundRGB = getRGB((memoryValue & 0x0F));
                    setStandardBitmapRGBLine(foregroundRGB,backgroundRGB, bitmapValue, curX, rgbLine);
                }
                else {
                    
                    int background1RGB = getRGB((memoryValue & 0xF0) >> 4); // 01
                    int background2RGB = getRGB((memoryValue & 0x0F)); // 10
                    int foregroundRGB = getRGB((memoryValue & 0xF00) >> 8); // 11

                    setMulticolorBitmapRGBLine(foregroundRGB, background1RGB, background2RGB, bitmapValue, curX, rgbLine);
                }

                
            }
            if ((curX - START_X) < 320){
                //                rgbLine[curX - START_X] = 0xFFFFFF;
            }
        }

        videoScreen.setLine(START_X,line,rgbLine);
        
    }

    protected void setStandardBitmapRGBLine(int foregroundRGB, int backgroundRGB, int bitmapValue, int curX, int[] rgbLine){
        int colorRGB = 0;
        int prevPixel = -1;
        int prevColorRGB = -1;
        for (int i = 0; i < 8; i++){
            int pixelValue = (bitmapValue >> i) & 0x1;
            
            if (pixelValue == prevPixel){
                colorRGB = prevColorRGB;
            }
            else {
                if (pixelValue == 0){ // background
                    colorRGB = backgroundRGB;
                }
                else if (pixelValue == 1){
                    colorRGB = foregroundRGB;
                }
                prevPixel = pixelValue;
                prevColorRGB = colorRGB;
            }
            
            rgbLine[(curX+(7 -i))-START_X] = colorRGB;
        }        
    }
    protected void setMulticolorBitmapRGBLine(int foregroundRGB, int background1RGB, int background2RGB, int bitmapValue, int curX, int[] rgbLine){
        int colorRGB = 0;
        int prevPixel = -1;
        int prevColorRGB = -1;
        
        for (int i = 0; i <= 6; i+=2){
            int pixelValue = (bitmapValue >> i) & 3;
            
            if (pixelValue == prevPixel){
                colorRGB = prevColorRGB;
            }
            else {

                if (pixelValue == 0){ // background
                    colorRGB = getRGB(videoRAM[BACKGROUND_COLOR_0] & 0xF);
                }
                else if (pixelValue == 1){
                    colorRGB = background1RGB;
                }
                else if (pixelValue == 2){
                    colorRGB = background2RGB;
                }
                else if (pixelValue == 3){
                    colorRGB = foregroundRGB;
                }
                prevPixel = pixelValue;
                prevColorRGB = colorRGB;
            }

            rgbLine[(curX+(6- i))-START_X] = colorRGB;
            rgbLine[(curX+(6 - i) + 1)-START_X] = colorRGB;
        }
    }

    protected void setMulticolorCharacterRGBLine(int charLine, int foregroundRGB, int curX, int[] rgbLine){
        int colorRGB = 0;
                
        int prevPixel = -1;
        int prevColorRGB = -1;
        for (int i = 0; i <= 6; i+=2){
            int pixelValue = (charLine >> i) & 3;
            if (pixelValue == prevPixel){
                //                            System.out.println("VICII pixel value: " + pixelValue);
                colorRGB = prevColorRGB;
            }
            else {
                if (pixelValue == 0){
                    colorRGB = getRGB(videoRAM[BACKGROUND_COLOR_0] & 0xF);
                }
                else if (pixelValue == 1){
                    colorRGB = getRGB(videoRAM[BACKGROUND_COLOR_1] & 0xF);
                }
                else if (pixelValue == 2){
                    colorRGB = getRGB(videoRAM[BACKGROUND_COLOR_2] & 0xF);
                }
                else if (pixelValue == 3){
                    colorRGB = foregroundRGB;
                }
                prevPixel = pixelValue;
                prevColorRGB = colorRGB;
            }    
            rgbLine[(curX+(6-i))-START_X] = colorRGB;
            rgbLine[(curX+(6-i)+1)-START_X] = colorRGB;
            
        }
        
    }

    protected void setCharacterRGBLine(int charLine, int screenRGB, int foregroundRGB, int curX, int[] rgbLine){
        //foregroundRGB = foreground.getRGB();
        if (charLine == 0){
            rgbLine[curX-START_X] = screenRGB;
            rgbLine[curX+1-START_X] = screenRGB;
            rgbLine[curX+2-START_X] = screenRGB;
            rgbLine[curX+3-START_X] = screenRGB;
            rgbLine[curX+4-START_X] = screenRGB;
            rgbLine[curX+5-START_X] = screenRGB;
            rgbLine[curX+6-START_X] = screenRGB;
            rgbLine[curX+7-START_X] = screenRGB;
        }
        else {
            rgbLine[curX-START_X] = (charLine & 0x80) != 0 ? foregroundRGB : screenRGB;
            rgbLine[curX+1-START_X] = (charLine & 0x40) != 0 ? foregroundRGB : screenRGB;
            rgbLine[curX+2-START_X] = (charLine & 0x20) != 0 ? foregroundRGB : screenRGB;
            rgbLine[curX+3-START_X] = (charLine & 0x10) != 0 ? foregroundRGB : screenRGB;
            rgbLine[curX+4-START_X] = (charLine & 0x08) != 0 ? foregroundRGB : screenRGB;
            rgbLine[curX+5-START_X] = (charLine & 0x04) != 0 ? foregroundRGB : screenRGB;
            rgbLine[curX+6-START_X] = (charLine & 0x02) != 0 ? foregroundRGB : screenRGB;
            rgbLine[curX+7-START_X] = (charLine & 0x01) != 0 ? foregroundRGB : screenRGB;
        }
        
    }

    // right not supporting the ability to move sprites after their
    // display line has already been drawn
    private int spriteData[] = new int[63];    
    private void drawSprites(int line){
        //        System.out.println("line = " + line);
        //        int screenRGB = screenColor.getRGB(); // default background color

        // check for potential collisions
        
        if (collisionInterruptEnabled){
            for (int i = 0; i < NUM_SPRITES; i++){
                for (int j = 0; j < NUM_SPRITES; j++){
                    if (i != j && spriteEnabled[i] && spriteEnabled[j]){
                        if (spriteX[i] >= spriteX[j] && spriteX[i] <= (spriteX[j] + 24)){
                            if (spriteY[i] >= spriteY[j] && spriteY[i] <= (spriteY[j] + 21)){
                                System.out.println("Collision!!: " + i + " " + j);
                                collisionDetected= true;
                                videoRAM[COLLISION_SPRITE_SPRITE] |= i;
                                videoRAM[COLLISION_SPRITE_SPRITE] |= j;
                                videoRAM[INTERRUPT_STATUS] |= 0x4; // set the interrupt
                            }
                        }
                    }
                }
            }
        }
        
        for (int sprite = (NUM_SPRITES) - 1; sprite >= 0; sprite--){

            //            spriteX[i] = START_X + (i * 24);
            //spriteY[sprite] = NTSC_VISIBLE_START_LINE + 40;
            if (spriteEnabled[sprite]){
                //   System.out.println("Enabled " + i + spriteEnabled[sprite]);
            
                if (((spriteY[sprite] <= line) && (spriteY[sprite]+21) > line) && spriteX[sprite] >= 24 && spriteX[sprite] <= 343){
                //System.out.println("Sprite " + sprite);
                //                Color spriteColor = getColor(videoRAM[SPRITE_0_COLOR + i]);
                int spriteRGB = getRGB(videoRAM[SPRITE_0_COLOR + sprite]);
                //                System.out.println("Sprite " + i + " color = " + spriteColor);
                int spriteBank = 0xFF & memory.read(screenMemoryStart + SPRITE_POINTER_OFFSET + sprite); // "p-access"
                int spriteStartAddress = (spriteBank * SPRITE_ADDRESS_MULTIPLIER);
               
                memory.read(spriteStartAddress,spriteData); // "s-access"
                //                System.out.println("Start Address: " + spriteStartAddress);
                int y = line - spriteY[sprite];
                //int y = spriteY[sprite] - line;
                //System.out.println("SpriteNum = " + sprite);
                //                System.out.println(line + " " + spriteY[sprite] + " " + y);
                boolean isMulticolor = ((videoRAM[SPRITE_MULTICOLOR] & (1 << sprite)) != 0);
                //for (int y = 0; y < 21; y++){
                
                /*                if ((y % 8) == 0){
                    for (int x = 0; x < 24; x++){
                        //                        videoScreen.setPixel(spriteX[sprite]+x,spriteY[sprite]+y,0xFFFFFF);
                    }
                }
                else {
                */
                    for (int x = 0; x < 24; x+=8){
                        int spriteLine = spriteData[(y*3)+(x/8)];

                        if (! isMulticolor){
                            //      System.out.println("SpriteData: " + Integer.toHexString(spriteLine));
                            
                            if ((spriteLine & 0x80) != 0){
                                videoScreen.setPixel(spriteX[sprite]+x,spriteY[sprite]+y,spriteRGB);
                            }
                            if ((spriteLine & 0x40) != 0){ 
                                videoScreen.setPixel(spriteX[sprite]+x+1,spriteY[sprite]+y,spriteRGB);
                            }
                            if ((spriteLine & 0x20) != 0){
                                videoScreen.setPixel(spriteX[sprite]+x+2,spriteY[sprite]+y,spriteRGB);
                            }
                            if ((spriteLine & 0x10) != 0){
                                videoScreen.setPixel(spriteX[sprite]+x+3,spriteY[sprite]+y,spriteRGB);
                            }
                            if ((spriteLine & 0x08) != 0){
                                videoScreen.setPixel(spriteX[sprite]+x+4,spriteY[sprite]+y,spriteRGB);
                            }
                            if ((spriteLine & 0x04) != 0){
                                videoScreen.setPixel(spriteX[sprite]+x+5,spriteY[sprite]+y,spriteRGB);
                            }
                            if ((spriteLine & 0x02) != 0){
                                videoScreen.setPixel(spriteX[sprite]+x+6,spriteY[sprite]+y,spriteRGB);
                            }
                            if ((spriteLine & 0x01) != 0){
                                videoScreen.setPixel(spriteX[sprite]+x+7,spriteY[sprite]+y,spriteRGB);
                            }
                        }
                        else { // multicolor
                            //System.out.println("Using multicolor: " + sprite);
                            for (int i = 0; i <= 6; i+=2){
                                int pixelValue = (spriteLine >> i) & 3;                 
                                if (pixelValue == 1){
                                    videoScreen.setPixel(spriteX[sprite]+x+(6-i),spriteY[sprite]+y,spriteMulticolorRGB1);
                                    videoScreen.setPixel(spriteX[sprite]+x+(6-i)+1,spriteY[sprite]+y,spriteMulticolorRGB1);
                                }
                                else if (pixelValue == 2){
                                    videoScreen.setPixel(spriteX[sprite]+x+(6-i),spriteY[sprite]+y,spriteRGB);
                                    videoScreen.setPixel(spriteX[sprite]+x+(6-i)+1,spriteY[sprite]+y,spriteRGB);
                                }
                                else if (pixelValue == 3){
                                    videoScreen.setPixel(spriteX[sprite]+x+(6-i),spriteY[sprite]+y,spriteMulticolorRGB2);
                                    videoScreen.setPixel(spriteX[sprite]+x+(6-i)+1,spriteY[sprite]+y,spriteMulticolorRGB2);
                                }
                            } // internal x counter
                        } // multicolor
                        //                        videoScreen.setPixel(spriteX[sprite]+x,spriteY[sprite]+y,0xFFFFFF);
                    } // x counter
                    //                    videoScreen.setPixel(spriteX[sprite]+23,spriteY[sprite]+y,0xFFFFFF);
                    //} // check y
                    //} // y counter
                    //
                } // should show
            } // sprite enabled
        } // sprite counter
                                         
    }

    /**
     * Returns the RGB values for the given Commodore palette color
     */
    private int getRGB(int commodoreColor){
        switch (commodoreColor){
        case COLOR_BLACK: return 0;
        case COLOR_WHITE: return 0xFFFFFF;
        case COLOR_RED: return 0xFF1111;
        case COLOR_CYAN: return 0xF5FF82;
        case COLOR_PURPLE: return 0xDC16FE;
        case COLOR_GREEN: return 0x6CFF29;
        case COLOR_BLUE: return 0x5406FE;
        case COLOR_YELLOW: return 0xF2FF09;
        case COLOR_ORANGE: return 0xFFB413;
        case COLOR_BROWN: return 0xA3730C;
        case COLOR_LIGHT_RED: return 0xFB7B65;
        case COLOR_DARK_GREY: return 0x686664;
        case COLOR_MEDIUM_GREY: return 0x9B9897;
        case COLOR_LIGHT_GREEN: return 0x6FFD8B;
        case COLOR_LIGHT_BLUE: return 0x3BEBFC;
        case COLOR_LIGHT_GREY: return 0xD1D0CF;
        default: return 0x00;
        }
    }

    /**
     * Returns the AWT Color object for the given Commodore palette color
     */
    private Color getColor(int value){
        Color color = null;
        switch (value){
        case COLOR_BLACK: color = Color.black;break;
        case COLOR_WHITE: color = Color.white;break;
        case COLOR_RED: color = Color.red;break;
        case COLOR_CYAN: color = Color.pink;break;
        case COLOR_PURPLE: color = Color.magenta;break;
        case COLOR_GREEN: color = Color.green;break;
        case COLOR_BLUE: color = Color.blue;break;
        case COLOR_YELLOW: color = Color.yellow;break;
        case COLOR_ORANGE: color = Color.orange;break;
        case COLOR_BROWN: color = Color.orange;break; // what's the RGB for this?
        case COLOR_LIGHT_RED: color = Color.pink;break;
        case COLOR_DARK_GREY: color = Color.darkGray;break;
        case COLOR_MEDIUM_GREY: color = Color.lightGray;break;
        case COLOR_LIGHT_GREEN: color = Color.green;break; // RGB?
        case COLOR_LIGHT_BLUE: color = Color.cyan;break; 
        case COLOR_LIGHT_GREY: color = Color.lightGray; break;
        default: throw new RuntimeException("Invalid color value: " + value);
        }
        return color;
    }

    protected void read(int address, int[] target){
        memory.read(address,target);
    }

    protected void repaint(){
        videoScreen.repaint();
    }


}


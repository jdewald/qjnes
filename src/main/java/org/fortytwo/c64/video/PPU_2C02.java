package org.fortytwo.c64.video;

import org.fortytwo.common.cpu.CPU;
import org.fortytwo.common.cpu.CycleObserver;
import org.fortytwo.common.memory.Memory;
import org.fortytwo.common.memory.MemoryHandler;

import java.util.logging.Logger;

public class PPU_2C02 implements MemoryHandler, CycleObserver
{
    private Logger logger = Logger.getLogger(this.getClass().getName());

    public static final int NUM_REGISTERS = 8;

    //public static final int FIRST_VISIBLE_SCANLINE = 8;
    public static final int PATTERN_OFFSET = 1;
    public static final int FIRST_VISIBLE_SCANLINE = 20;
    //public static final int FIRST_VISIBLE_SCANLINE = 19;
    public static final int LAST_VISIBLE_SCANLINE = FIRST_VISIBLE_SCANLINE + 240; // 242
    public static final int SCANLINES_PER_FRAME = 262;
    public static final int PPU_CYCLES_PER_LINE = 341; // PPU is 3 times faster than CPU
    public static final int CPU_CYCLES_PER_LINE = 113; // 341/3=113.66. Every 3 cpu cycles, we need to add 2 more to get to the correct PPU count
    public static final int PPU_CYCLES_PER_FRAME = SCANLINES_PER_FRAME * PPU_CYCLES_PER_LINE;

    public static final int SPRITE_COUNT = 64;
    public static final int BYTES_PER_SPRITE = 4;
    public static final int HORIZONTAL_TILE_COUNT = 32;

    public static final int CONTROL_REGISTER_1 = 0x00;
    public static final int CONTROL_REGISTER_2 = 0x01;
    public static final int STATUS_REGISTER = 0x02;
    public static final int SPRITE_MEMORY_ADDRESS = 0x03;
    public static final int SPRITE_MEMORY_DATA = 0x04;
    public static final int SCROLL_OFFSET = 0x05; /*
                                                     write #1 sets fineHoriztonal latch and horizontalTile latch
                                                     write #2 sets fineVertical latch and verticalTile latch
                                                  */
    public static final int PPU_MEMORY_ADDRESS = 0x06; // First write upper byte, then lower
    public static final int PPU_MEMORY_DATA = 0x07; // After setting address, read/write here

    /** Control Register 1 bits **/
    public static final int CR1_NAME_TABLE = 0x3; // 0 = 0x2000, 1 = 0x2400, 2 = 0x2800, 3 = 0x2C00
    public static final int CR1_VERTICAL_WRITE = 0x04; // 1 == ppu address goes by 32
    public static final int CR1_SPRITE_PATTERN_ADDRESS = 0x08; // 0 = 0x0000, 1 = 0x1000
    public static final int CR1_SCREEN_PATTERN_ADDRESS = 0x10; // 0 = 0x0000, 1 = 0x1000 "playfield pattern selection"
    public static final int CR1_SPRITE_SIZE = 0x20; // 0 = 8x8, 1 = 8x16
    public static final int CR1_INTERRUPT_ON_HIT = 0x40;
    public static final int CR1_INTERRUPT_ON_VBLANK = 0x80; // 1 == generate interrupt

    /** Control Register 2 bits **/
    public static final int CR2_UNKNOWN = 0x01;
    public static final int CR2_IMAGE_MASK = 0x02; // 0 = don't show left 8 columns
    public static final int CR2_SPRITE_MASK = 0x04; // 0 = don't show sprites in left 8 columns
    public static final int CR2_SCREEN_SWITCH = 0x08; // 1 = show, 0 = blank
    public static final int CR2_SPRITE_SWITCH = 0x10; // 1 = show sprites, 0 = hide

    public static final int STATUS_MAX_SPRITES = 0x20;
    public static final int STATUS_SPRITE0_HIT = 0x40;
    public static final int STATUS_VBLANK = 0x80;

    private int[] nameTableAddresses = new int[] {0x2000,0x2400,0x2800,0x2C00};

    private int[] registers_r = new int[NUM_REGISTERS];
    private int[] registers_w = new int[NUM_REGISTERS];

    private int[] spriteAttributeMemory = new int[SPRITE_COUNT * BYTES_PER_SPRITE];
    private int spriteMemoryCounter = 0;
    private Sprite[] bufferedSprites = new Sprite[SPRITE_COUNT]; // technically we're only supposed to display 8

    private int ppuMemoryAddress = 0;
    private boolean lastPPUAddressWasHigh = false; // Last write to 0x2006 was the High Byte
    private boolean isFirstWriteToScroll = true;

    private int ppuCyclesUntilEndOfFrame;
    private int ppuCyclesUntilEndOfLine;
    private int scanLine = 0;

    private int horizontalTileCounter = 0;
    private int verticalTileCounter = 0;
    private int horizontalNameCounter = 0;
    private int verticalNameCounter = 0;
    private int fineVerticalCounter = 0;
    private int fineHorizontalCounter = 0; // when is this used?


    private int horizontalTileLatch = 0;
    private int verticalTileLatch = 0;
    private int horizontalNameLatch = 0;
    private int verticalNameLatch = 0;
    private int fineVerticalLatch = 0;

    private int attributeShift = 0;
    private int playfieldSelector = 0;

    private boolean doubleSizeSprites; // 8x16
    private int previousSpriteCount = 0;
    public static final int PALETTE_ADDRESS = 0x3F10;
    public static final int BACKGROUND_PALETTE_ADDRESS = 0x3F00;
    private boolean displayEnabled = false;
    private boolean spritesEnabled = false;
    private Memory memory = null;
    private LineObserver lineObserver = null;
    private Screen videoScreen;
    private Integer[] pixelBuffer = new Integer[262];
    private boolean shouldLog = false;

    private long lastFrameTime = 0;
    private long frameCounter = 0;
    private int sprite0Line = 0;
    private static final long FRAME_SYNC = 5;
    private static final long FRAMES_PER_SEC = 60;
    private static final long MS_PER_FRAME = 1000 / FRAMES_PER_SEC;

    private boolean usingVROM = true;
    public PPU_2C02(){
        ppuCyclesUntilEndOfFrame = PPU_CYCLES_PER_FRAME;
        ppuCyclesUntilEndOfLine = PPU_CYCLES_PER_LINE;

        videoScreen = null;
        //        registers_r[STATUS_REGISTER] = STATUS_VBLANK; // since we start out less than 20
        registers_w[CONTROL_REGISTER_1] = CR1_INTERRUPT_ON_VBLANK;

    }

    public void setLineObserver(LineObserver observer){
        this.lineObserver = observer;
    }

    public void setMemory(Memory memory){
        this.memory = memory;
    }

    public void setScreen(Screen screen){
        this.videoScreen = screen;
    }

    public int read(int address){
        //        logger.info("Read from: " + Integer.toHexString(address));
        switch (address){
            /*
        case 0: {
            return registers_w[CONTROL_REGISTER_1];
        }
        case 3: {
            return spriteMemoryCounter;
        }
        case 5:{
            return registers_w[SCROLL_OFFSET];
        }
        case 6:{
            return horizontalTileCounter | (verticalTileCounter << 5) | (horizontalNameCounter << 10) | (verticalNameCounter << 11);
        }
            */
            case 0: {
                return scanLine;
                // hack
            }
            case 1: {
                return registers_w[CONTROL_REGISTER_2];
            }
            case STATUS_REGISTER:{ // 2
                int returnVal = registers_r[STATUS_REGISTER];
                registers_r[STATUS_REGISTER] = getBitsUnset(registers_r[STATUS_REGISTER], STATUS_VBLANK); //| STATUS_SPRITE0_HIT);
                isFirstWriteToScroll = true;
                lastPPUAddressWasHigh = false;
                //logger.info("Status is: " + Integer.toHexString(returnVal));
                return returnVal;
            }
            case SPRITE_MEMORY_DATA:{ // 4
                return readFromSpriteMemory();
            }
            case PPU_MEMORY_DATA:{ // 7
                int returnVal = 0xFF & registers_r[PPU_MEMORY_DATA]; // buffered read

                //int returnVal = 0xFF & memory.read(ppuMemoryAddress);
                int readAddress = horizontalTileCounter
                        | (verticalTileCounter << 5)
                        | (horizontalNameCounter << 10)
                        | (verticalNameCounter << 11)
                        | ((fineVerticalCounter & 0x3) << 12);

                if (readAddress == 0x3F10 || readAddress == 0x3F14 || readAddress == 0x3F18 || readAddress == 0x3F1C) {
                    readAddress = readAddress & 0x3F0F;
                }
                registers_r[PPU_MEMORY_DATA] = 0xFF & memory.read(readAddress);
                //            registers_r[PPU_MEMORY_DATA] = 0xFF & memory.read(ppuMemoryAddress);
                incrementCounters();
            /*
            if ((registers_w[CONTROL_REGISTER_1] & CR1_VERTICAL_WRITE) == 0){
                ppuMemoryAddress++;
            }
            else {
                ppuMemoryAddress  = (ppuMemoryAddress + 32);
            }
            */
                return returnVal;
            }

            default: throw new RuntimeException("Invalid read attempt: " + Integer.toHexString(address));
        }
    }

    public void write(int address, int value){
        //        logger.info("Write " + Integer.toHexString(value) + " to " + Integer.toHexString(address));

        switch (address){
            case CONTROL_REGISTER_1:{ // 0
                //logger.info("Write " + Integer.toHexString(value) + " to " + Integer.toHexString(address));
                registers_w[CONTROL_REGISTER_1] = value & 0xFF;
                if ((value & CR1_SPRITE_SIZE) != 0){
                    doubleSizeSprites = true;
                }
                else {
                    doubleSizeSprites = false;
                }
                /**
                 * creates a 0-3 index into the name table address array
                 * by setting which way we go horizontally and vertically
                 */
                int oldH = horizontalNameLatch;
                int oldV = verticalNameLatch;
                horizontalNameLatch = value & 0x1;
                if (shouldLog && oldH != horizontalNameLatch){
                    logger.info("Set H to " + horizontalNameLatch);
                }
                verticalNameLatch = (value & 0x2) >> 1;
                if (shouldLog && oldV != verticalNameLatch){
                    logger.info("Set V to " + verticalNameLatch);
                }
                playfieldSelector = (value & CR1_SCREEN_PATTERN_ADDRESS) >> 4;
                break;
            }
            case CONTROL_REGISTER_2:{ // 1
                //  logger.info("CR2: " + Integer.toHexString(value));
                registers_w[CONTROL_REGISTER_2] = value & 0xFF;
                displayEnabled = (registers_w[CONTROL_REGISTER_2] & CR2_SCREEN_SWITCH) != 0;
                spritesEnabled = (registers_w[CONTROL_REGISTER_2] & CR2_SPRITE_SWITCH) != 0;


                break;
            }
            case 2: {
                break;
            }
            case SPRITE_MEMORY_ADDRESS:{ // 3
                setSpriteMemoryAddress(value);
                break;
            }
            case SPRITE_MEMORY_DATA:{ // 4
                writeToSpriteMemory(value);
                break;
            }
            case SCROLL_OFFSET:{ // 5
                if (shouldLog)
                    logger.info("Wrote to scroll offset: " + value);
                registers_w[SCROLL_OFFSET] = value;
                if (isFirstWriteToScroll){
                    horizontalTileLatch = (value & 0xF8) >> 3;
                    fineHorizontalCounter = value & 0x7; // immediate effect... but what's it used for?
                    isFirstWriteToScroll = false;
                }
                else {
                    verticalTileLatch = (value & 0xF8) >> 3;
                    fineVerticalLatch  = value & 0x7;
                    isFirstWriteToScroll = true;
                }
                break;
            }
            case PPU_MEMORY_ADDRESS:{ /** 6 - write high byte, then low byte */
                if (lastPPUAddressWasHigh){
                    lastPPUAddressWasHigh = false;


                    //                System.out.println("Scanline = " + scanLine);
                    horizontalTileLatch = value & 0x1F;
                    verticalTileLatch |= ((value & 0xE0) >> 5); // gets top 2 bits from first write

                    loadAddressFromLatches();
                    //                System.out.println("PPU Address: " + Integer.toHexString(ppuMemoryAddress));
                    //                ppuMemoryAddress = ((registers_w[address] & 0xFF) << 8) | (value & 0xFF);
                    //                System.out.println("PPU Address after real load: " + Integer.toHexString(ppuMemoryAddress));


                }
                else { // this is the 1st write
                    verticalTileLatch = (value & 0x3) << 3; // the upper 2 bits of latch
                    horizontalNameLatch = (value & 0x4) >> 2;
                    verticalNameLatch = (value & 0x8) >> 3;
                    fineVerticalLatch = (value & 0x30) >> 4;
                    lastPPUAddressWasHigh = true;
                }
                registers_w[address] = value & 0xFF;
                break;
            }
            case PPU_MEMORY_DATA: { // 7
                //            System.out.println("PPU MEMORY WRITE: " + Integer.toHexString(ppuMemoryAddress) + "," + Integer.toHexString(value));
                //memory.write(ppuMemoryAddress, value & 0xFF);
                int writeAddress = horizontalTileCounter
                        | (verticalTileCounter << 5)
                        | (horizontalNameCounter << 10)
                        | (verticalNameCounter << 11)
                        | ((fineVerticalCounter & 0x3) << 12);
                if (writeAddress == 0x3F10 ||
                        writeAddress == 0x3F14 ||
                        writeAddress == 0x3F18 ||
                        writeAddress == 0x3F1C) {
                    writeAddress = writeAddress & 0x3F0F;
                }
                memory.write(writeAddress, value & 0xFF);
            /*
            if (ppuMemoryAddress < 0x3F00){
                memory.write(ppuMemoryAddress, value & 0xFF);
            }
            else {
                memory.write(ppuMemoryAddress - 0x3F00, value & 0xFF);
            }
            */
                incrementCounters();
            /*
            if ((registers_w[CONTROL_REGISTER_1] & CR1_VERTICAL_WRITE) == 0){
                ppuMemoryAddress++;
            }
            else {
                ppuMemoryAddress += 32;
            }
            */
                //            if (ppuMemoryAddress >= ram.size()) { ppuMemoryAddress = 0; }
                break;
            }

            default: throw new RuntimeException("Invalid write attempt: " + Integer.toHexString(address));
        }
    }

    public void setSpriteMemoryAddress(int address){
        if (address > 0xFF){
            throw new RuntimeException("Invalid sprite memory address: " + Integer.toHexString(address));
        }

        spriteMemoryCounter = address;
    }

    public int readFromSpriteMemory(){
        int returnVal = spriteAttributeMemory[spriteMemoryCounter];
//        spriteMemoryCounter = (spriteMemoryCounter + 1) % 256;

        return returnVal;

    }

    public void writeToSpriteMemory(int value){
        spriteAttributeMemory[spriteMemoryCounter] = value & 0xFF;
        if (value != 0){
            //            logger.info("Sprite data value: " + Integer.toHexString(value));
        }
        spriteMemoryCounter = (spriteMemoryCounter + 1) % 256;
    }

    public int tick(int cycles, CPU cpu){
        ppuCyclesUntilEndOfFrame -= (cycles * 3);
        ppuCyclesUntilEndOfLine -= (cycles * 3);

        if (ppuCyclesUntilEndOfLine <= 0){
            if (scanLine == 0) {
                sprite0Line = 0;
            }
            scanLine++;
            //            ppuMemoryAddress += 256; // handle the fact that we're not actually drawing
            if (scanLine == (LAST_VISIBLE_SCANLINE + 1)){
                registers_r[STATUS_REGISTER] |= STATUS_VBLANK;
                //logger.info("Status register: " + Integer.toHexString(registers_r[STATUS_REGISTER]));
                if ((registers_w[CONTROL_REGISTER_1] & CR1_INTERRUPT_ON_VBLANK) != 0){
                    cpu.handleNMI((ppuCyclesUntilEndOfLine/3));
                }

            } else if (scanLine == (FIRST_VISIBLE_SCANLINE - 1)) {
                // clear VBlank flag and Sprite 0
                registers_r[STATUS_REGISTER] = getBitsUnset(registers_r[STATUS_REGISTER],STATUS_VBLANK | STATUS_SPRITE0_HIT);
            } else if (scanLine == FIRST_VISIBLE_SCANLINE){
                if (displayEnabled) {
                    loadAddressFromLatches();
                }
            }
            else if (scanLine == SCANLINES_PER_FRAME){
                if (ppuCyclesUntilEndOfFrame > 0){
                    throw new RuntimeException("Reached last line, but not end of frame: " + ppuCyclesUntilEndOfFrame);
                }
                videoScreen.repaint();

                frameCounter++;

                if (frameCounter >= FRAME_SYNC){
                    long now = System.currentTimeMillis();
                    if (lastFrameTime == 0){
                        lastFrameTime = now;
                    } else if (now - lastFrameTime < (frameCounter * MS_PER_FRAME)){
                        try {
                            Thread.sleep(((frameCounter * MS_PER_FRAME) - (now - lastFrameTime)));
                        } catch (Throwable t){
                            t.printStackTrace();
                        }
                        //System.out.println("Frame time: " + (now - lastFrameTime) + " / " + MS_PER_FRAME);
                        lastFrameTime = now;
                        frameCounter = 0;
                    }
                }
                scanLine = 0;

            }

            if (displayEnabled && scanLine >= FIRST_VISIBLE_SCANLINE && scanLine <= LAST_VISIBLE_SCANLINE){
                drawRasterLine(scanLine);

                if (lineObserver != null){
                    lineObserver.notifyLine();
                }

                //                videoScreen.repaint();
            }

            ppuCyclesUntilEndOfLine = PPU_CYCLES_PER_LINE + ppuCyclesUntilEndOfLine;



        }

        if (ppuCyclesUntilEndOfFrame <= 0){
            ppuCyclesUntilEndOfFrame = PPU_CYCLES_PER_FRAME + ppuCyclesUntilEndOfFrame; // handle underflow            scanLine = 0;
        }

        //        return ppuCyclesUntilEndOfFrame;
        if (scanLine < (FIRST_VISIBLE_SCANLINE + 1) || scanLine >= LAST_VISIBLE_SCANLINE) {
            return 3;
        } else {
            return ppuCyclesUntilEndOfLine;
        }
        //return 1;
    }

    public void enableLogging(){
        shouldLog = true;
        memory.enableLogging();
    }

    public void disableLogging(){
        shouldLog = false;
        memory.disableLogging();
    }
    private void incrementCounters(){

        if ((registers_w[CONTROL_REGISTER_1] & CR1_VERTICAL_WRITE) == 0){
            horizontalTileCounter++;
            if (horizontalTileCounter == 32){
                verticalTileCounter++;
                horizontalTileCounter = 0;
            }
        }
        else {
            verticalTileCounter++;
        }
        if (verticalTileCounter == 32){
            horizontalNameCounter++;
            verticalTileCounter = 0;
        }
        if (horizontalNameCounter >= 2){
            verticalNameCounter++;
            horizontalNameCounter = 0;
        }
        if (verticalNameCounter >= 2){
            fineVerticalCounter++;
            verticalNameCounter = 0;
        }
        if (fineVerticalCounter == 8){
            fineVerticalCounter = 0;
        }
    }


    private void loadAddressFromLatches(){
        /*        ppuMemoryAddress = horizontalTileLatch
            | (verticalTileLatch << 5)
            | (horizontalNameLatch << 10)
            | (verticalNameLatch << 11)
            | ((fineVerticalLatch & 3)<< 12);
        */
        horizontalTileCounter = horizontalTileLatch;
        verticalTileCounter = verticalTileLatch;
        horizontalNameCounter = horizontalNameLatch; // single bit
        verticalNameCounter = verticalNameLatch; // single bit
        fineVerticalCounter = fineVerticalLatch;
    }

    private void drawRasterLine(int line){
        //        System.out.println(ppuMemoryAddress);
        int nameTableAddress = nameTableAddresses[registers_w[CONTROL_REGISTER_1] & CR1_NAME_TABLE];
        //int attributeTableAddress = nameTableAddress + 0x3C0;
        //logger.info("Name Table Address: " + Integer.toHexString(nameTableAddress));
        //        int screenPatternAddress = ((registers_w[CONTROL_REGISTER_1] & CR1_SCREEN_PATTERN_ADDRESS) != 0) ? 0x1000 : 0x0000;
        int x = 0;
        /// technically the previous line will have grabbed the first 2 tiles
        // for 32 tiles
        horizontalTileCounter = horizontalTileLatch;
        horizontalNameCounter = horizontalNameLatch;

        // Pattern Table is 16 consecutive bytes for each tile where the first 8
        // controls bit 0 of color and the second 8 controls bit 1
        for (int tile = 0; tile < HORIZONTAL_TILE_COUNT; tile++){
            // fetch 1 name table byte
            // every entry represents an 8x8 block

            int yOffset = (line - FIRST_VISIBLE_SCANLINE) / 8;
            int offset = yOffset * HORIZONTAL_TILE_COUNT + tile;
            //int nameAddress = nameTableAddress + offset;



            //            ppuMemoryAddress = ppuMemoryAddress & (~0x7FFF);
            //ppuMemoryAddress = ppuMemoryAddress | (nameAddress - 0x2000);
            /* TODO: This should be calculated
               during a read of 0x2006 to simulate
               drawing in real time rather than
               line by line
            */
            //ppuMemoryAddress = ppuMemoryAddress | (yOffset << 12);

            int address = horizontalTileCounter | (verticalTileCounter << 5) | (horizontalNameCounter << 10) | (verticalNameCounter << 11);
            int nameData = memory.read(0x2000 + address); // the name counters will handle the actual lookup of which name table we're using
            // every entry represents a 4x4 block of tiles

            // only need to address 64 bits of data (2^6)
            // every block covers 8 lines, so we have 32 lines for each byte in the attribute table
            // every attribute by covers a 4x4 block of sprites so we don't care about the lower two bits
            // of the horizontal and vertical counters
            int attributeTableAddress = (horizontalTileCounter >> 2) | ((verticalTileCounter >> 2) << 3) | (horizontalNameCounter << 10) | (verticalNameCounter << 11);

            int attributeData = memory.read(0x23C0 + attributeTableAddress);
            //ppuMemoryAddress++;
            int upperBits = 0;
            // now select which of the attribute "quadrants" we're in:
            // 33 22 11 00 corresponding to:
            // 00 00 11 11
            // 00 00 11 11
            // 22 22 33 33
            // 22 22 33 33

            int bit1 = horizontalTileCounter & 0x2;
            int bit2 = verticalTileCounter & 0x2;
            int shift = bit1 | (bit2 << 1);
            upperBits = (attributeData >> shift) & 0x3;
            drawBackgroundTile(nameData, upperBits, x, line);

            x += 8;

            horizontalTileCounter++;

            if (horizontalTileCounter == 32){
                horizontalNameCounter++;
                if (horizontalNameCounter == 2){
                    horizontalNameCounter = 0;
                }
                horizontalTileCounter = 0;
            }

        }
        // for 64 sprites

        //
        if (spritesEnabled){
            drawSprites(line);
            updateSpriteBuffer(line);
        }

        // implements an internal "vertical scroll counter"
        fineVerticalCounter++;
        if (fineVerticalCounter == 8){
            verticalTileCounter++;
            fineVerticalCounter = 0;

            if (verticalTileCounter == 30){
                verticalNameCounter++;
                verticalTileCounter = 0;
                if (verticalNameCounter >= 2){
                    verticalNameCounter = 0;
                }
            }

        }

    }

    private int getBitsUnset(int original, int mask){
        return original & (~mask);
    }

    private void updateSpriteBuffer(int line){
        registers_r[STATUS_REGISTER] = getBitsUnset(registers_r[STATUS_REGISTER], STATUS_MAX_SPRITES);
        int spriteCount = 0;
        for (int sprite = 0; sprite < SPRITE_COUNT; sprite++){
            //   determine if Y coord in range (for line + 1)
            int yCoord = spriteAttributeMemory[sprite * 4] + 1;
            int diff = ((line - FIRST_VISIBLE_SCANLINE) - yCoord);
            //if ((line - FIRST_VISIBLE_SCANLINE) >= yCoord && (line - FIRST_VISIBLE_SCANLINE) <= (yCoord + 8)){
            if ((diff >= 0 && diff <= 7) || (doubleSizeSprites && (diff >= 0 && diff <= 15))){
                int tile = spriteAttributeMemory[(sprite * 4) + 1];
                int colorInfo = spriteAttributeMemory[(sprite *4) + 2];
                int spriteX = spriteAttributeMemory[(sprite * 4) + 3];
                bufferedSprites[spriteCount] = new Sprite(sprite, tile, colorInfo,(yCoord +1) + FIRST_VISIBLE_SCANLINE,spriteX);
                spriteCount++;

                if (spriteCount == 9 ){
                    spriteCount = 8;
                    registers_r[STATUS_REGISTER] |= STATUS_MAX_SPRITES;
                    break;
                }

                //                drawSpriteTile(tile,colorInfo, spriteX,line);
            }
            //   if sprite count > 8 mark SPRITE_OVERFLOW on 0x02 and ignore
        }
        previousSpriteCount = spriteCount;

    }
    private void drawSprites(int line){
        if (previousSpriteCount > 0){
            for (int c = 0; c < pixelBuffer.length; c++) {
                pixelBuffer[c] = 0;
            }
            //for (int i = previousSpriteCount - 1; i > 0; i--){
            for (int i = 0 ; i < previousSpriteCount; i++){
                Sprite sprite = bufferedSprites[i];
                if (drawSpriteTile(sprite.tile,sprite.colorInfo,sprite.spriteX,line,line-sprite.spriteY,sprite.spriteNum == 0) &&
                        (registers_r[STATUS_REGISTER] & STATUS_SPRITE0_HIT) == 0){
                    registers_r[STATUS_REGISTER] |= STATUS_SPRITE0_HIT;
                    sprite0Line = line;
                }
            }

            int transparentValue = memory.read(BACKGROUND_PALETTE_ADDRESS);
            int transparentColor = getNESColor(transparentValue);
            for (int x = 0; x < pixelBuffer.length; x++) {
                if (pixelBuffer[x] != 0 && (pixelBuffer[x] & 0xFF) != transparentValue) {
                    if (videoScreen.getPixel(x, line) == transparentColor ||
                            (pixelBuffer[x] >> 8) == 0) {
                        videoScreen.setPixel(x, line, getNESColor(pixelBuffer[x] & 0xFF));
                    }
                }

            }
        }

    }
    private boolean drawSpriteTile(int tileNum, int attributes, int x, int line, int offset, boolean isSprite0){
        boolean sprite0Triggered = false;
        int spritePatternAddress = 0;

        int paletteUpper = attributes & 0x3;
        int priority = attributes & 0x20;
        boolean reverse = (0x40 & attributes) != 0;
        //boolean flipVertical = (0x80 & attributes) != 0;
        boolean flipVertical = false;

        if (flipVertical  ) { System.out.println("flip vertical: " + tileNum + " offset = " + offset); }
        if (! doubleSizeSprites){ // 8x8
            spritePatternAddress = ((registers_w[CONTROL_REGISTER_1] & CR1_SPRITE_PATTERN_ADDRESS) != 0) ? 0x1000 : 0x0000;
        }
        else { // 8x16
            /**
             * Basically the tile index tells us which pattern table it comes from
             * And then, apparently, we take the MSB of the offset ("range") and use it
             * as the LSB of the tile
             */
            if ((tileNum & 0x1) == 0){
                spritePatternAddress = 0x0000;
            }
            else {
                spritePatternAddress = 0x1000;
            }

            tileNum = (tileNum & 0xFE) | (offset >> 3);

        }
        offset = offset & 7;
        int y = line;

        // Tiles are laid out as two image planes each representing
        // one bit of the color, they each have 8 bytes, so 2x8 = 16
        int patternBitmap1 = 0;
        int patternBitmap2 = 0;
        if (! flipVertical){
            patternBitmap1 = memory.read(spritePatternAddress+(tileNum*16)+ offset);
            patternBitmap2 = memory.read(spritePatternAddress+(tileNum*16)+ offset + 8);
        }
        else {
            patternBitmap1 = memory.read(spritePatternAddress+(tileNum*16)+ (7 - offset));
            patternBitmap2 = memory.read(spritePatternAddress+(tileNum*16)+ (7 - offset) + 8);
        }

        if (! reverse){
            x += 7;
        }

        // we actually draw from the end of it
        // which is why we add for !reverse
        int transparentColor = memory.read(BACKGROUND_PALETTE_ADDRESS);
        int _tColor = getNESColor(transparentColor);
        for (int i = 0; i < 8; i++){
            if (x < 256){
                int bit1 = (patternBitmap1 >> i) & 0x01;
                int bit2 = ((patternBitmap2 >> i) & 0x01) << 1;
                int pixelValue = ((bit1) | (bit2)) & 0x3;
                pixelValue |= (paletteUpper << 2);

                int paletteValue = memory.read((PALETTE_ADDRESS + pixelValue) & 0x3F1F);
                int color = getNESColor(paletteValue);


                if (pixelValue != 0 && isSprite0 && videoScreen.getPixel(x,y) != _tColor){
                    sprite0Triggered = true;
                }
                // If this pixel has FRONT priority (0), then it will go in front of background
                // If this pixel has BACK priority (1), then background will go over it
                // Additionally, if we see a BACK priority sprite pixel before a FRONT priority one and
                // 		it isn't transparent, then it will block the transparent one
                if (paletteValue != transparentColor){
                    if (x >= 8 || ((registers_w[CONTROL_REGISTER_2] & CR2_SPRITE_MASK) != 0)) {
                        if (pixelBuffer[x] == 0 || (pixelBuffer[x] & 0xFF) == transparentColor) {
                            pixelBuffer[x] = paletteValue | (priority << 8);
                        }
                    }

					/*
					if (videoScreen.getPixel(x,y) == _tColor){
						if (x >= 8 || ((registers_w[CONTROL_REGISTER_2] & CR2_SPRITE_MASK) != 0)){
							videoScreen.setPixel(x,y,color);
						}
					}
					*/
                }
                if (reverse){
                    x++;
                }
                else {
                    x--;
                };
            }
        }
        return sprite0Triggered;
    }

    private void drawBackgroundTile(int tileNum, int paletteUpper, int x, int line){
        if (x >= 8 || (registers_w[CONTROL_REGISTER_2] & CR2_IMAGE_MASK) != 0){
            int screenPatternAddress = ((registers_w[CONTROL_REGISTER_1] & CR1_SCREEN_PATTERN_ADDRESS) != 0) ? 0x1000 : 0x0000;
            //        drawTile(tileNum, paletteUpper, screenPatternAddress, x, line, (line - FIRST_VISIBLE_SCANLINE) % 8 == 0);
            int y = line;

            // Tiles are laid out as two image planes each representing
            // one bit of the color, they each have 8 bytes, so 2x8 = 16
            int patternBitmap1 = 0;
            int patternBitmap2 = 0;
            int address = screenPatternAddress + (fineVerticalCounter | (tileNum << 4));

            patternBitmap1 = memory.read(address);
            patternBitmap2 = memory.read(address + 8);
            //        patternBitmap1 = memory.read(screenPatternAddress+(tileNum*16)+ ((y - FIRST_VISIBLE_SCANLINE) % 8));
            //patternBitmap2 = memory.read(screenPatternAddress+(tileNum*16)+ ((y - FIRST_VISIBLE_SCANLINE) % 8) + 8);
            x += 7;

            for (int i = 0; i < 8; i++){
                if (x < 256){
                    int bit1 = (patternBitmap1 >> i) & 0x01;
                    int bit2 = ((patternBitmap2 >> i) & 0x01) << 1;
                    int pixelValue = ((bit1) | (bit2)) & 0x3;
                    pixelValue |= (paletteUpper << 2);

                    int paletteValue = memory.read((BACKGROUND_PALETTE_ADDRESS + pixelValue) & 0x3F0F);
                    int color = getNESColor(( i == 0 || (line % 7 == 0)) ? paletteValue /*0xFFFFFF*/ : paletteValue);

                    videoScreen.setPixel(x,y,color);

                    x--;
                }
            }
        }
    }


    private void drawTile(int tileNum, int attributes, int patternAddress, int x, int y, boolean firstOrLast ){
        int paletteUpper = attributes & 0x3;
        boolean reverse = (0x40 & attributes) != 0;
        boolean flipVertical = (0x80 & attributes) != 0;
        // Tiles are laid out as two image planes each representing
        // one bit of the color, they each have 8 bytes, so 2x8 = 16
        int patternBitmap1 = 0;
        int patternBitmap2 = 0;
        if (! flipVertical){
            patternBitmap1 = memory.read(patternAddress+(tileNum*16)+ ((y - PATTERN_OFFSET) % 8));
            patternBitmap2 = memory.read(patternAddress+(tileNum*16)+ ((y - PATTERN_OFFSET) % 8) + 8);
        }
        else {
            patternBitmap1 = memory.read(patternAddress+(tileNum*16)+ (7 - ((y - PATTERN_OFFSET) % 8)));
            patternBitmap2 = memory.read(patternAddress+(tileNum*16)+ (7 - ((y - PATTERN_OFFSET) % 8)) + 8);
        }
        //int patternBitmap1 = memory.read(patternAddress + (tileNum * 16) + ((y % 8) * 2));
        //int patternBitmap2 = memory.read(patternAddress + (tileNum * 16) + ((y % 8) * 2) + 1);

        if (! reverse){
            x += 7;
        }

        for (int i = 0; i < 8; i++){
            if (x < 256){
                int bit1 = (patternBitmap1 >> i) & 0x01;
                int bit2 = ((patternBitmap2 >> i) & 0x01) << 1;
                int pixelValue = ((bit1) | (bit2)) & 0x3;
                pixelValue |= (paletteUpper << 2);

                int paletteValue = memory.read(PALETTE_ADDRESS + pixelValue);
                int color = getNESColor(paletteValue);

                //                if (paletteValue != 0){
                videoScreen.setPixel(x,y,getNESColor(paletteValue));
                //}
                if (reverse){
                    x++;
                }
                else {
                    x--;
                };
            }
        }

    }

    private int getNESColor(int index){
        switch (index){
            case 0: return 0x525252; // 0xFFFFFF; // 0x525252
            case 1: return 0x000080;
            case 2: return 0x08008A;
            case 3: return 0x2C007E;
            case 4: return 0x4A004E;
            case 5: return 0x500006;
            case 6: return 0x440000;
            case 7: return 0x260800;
            case 8: return 0xA2000A;
            case 9: return 0x002E00;
            case 10: return 0x003200;
            case 11: return 0x00260A;
            case 12: return 0x001C48;
            case 13: return 0x000000;
            case 14: return 0x000000;
            case 15: return 0x000000;

            case 16: return 0xA4A4A4;
            case 17: return 0x0038CE;
            case 18: return 0x3416EC;
            case 19: return 0x5E04DC;
            case 20: return 0x8C00B0;
            case 21: return 0x9A004C;
            case 22: return 0x901800;
            case 23: return 0x703600;
            case 24: return 0x4C5400;
            case 25: return 0x0E6C00;
            case 26: return 0x007400;
            case 27: return 0x006C2C;
            case 28: return 0x005E84;
            case 29: return 0x000000;
            case 30: return 0x000000;
            case 31: return 0x000000;

            case 32: return 0xFFFFFF;
            case 33: return 0x4C9CFF;
            case 34: return 0x7C78FF;
            case 35: return 0xA664FF;
            case 36: return 0xDA5AFF;
            case 37: return 0xF054C0;
            case 38: return 0xF06A56;
            case 39: return 0xD68610;
            case 40: return 0xBAA400;
            case 41: return 0x76C000;
            case 42: return 0x46CC1A;
            case 43: return 0x2EC866;
            case 44: return 0x34C2BE;
            case 45: return 0x3A3A3A;
            case 46: return 0x000000;
            case 47: return 0x000000;

            case 48: return 0xFFFFFF;
            case 49: return 0xB6DAFF;
            case 50: return 0xC8CAFF;
            case 51: return 0xDAC2FF;
            case 52: return 0xF0BEFF;
            case 53: return 0xFCBCEE;
            case 54: return 0xFAC2C0;
            case 55: return 0xF2CCA2;
            case 56: return 0xE6DA92;
            case 57: return 0xCCE68E;
            case 58: return 0xB8EEA2;
            case 59: return 0xAEEABE;
            case 60: return 0xAEE8E2;
            case 61: return 0xB0B0B0;
            case 62: return 0x000000; // 0x000000
            case 63: return 0x000000; // 0x000000
        }
        return -1;
    }

    class Sprite
    {
        public int tile;
        public int colorInfo;
        public int spriteX;
        public int spriteY;
        public int spriteNum;
        Sprite(int spriteNum, int tile, int colorInfo, int spriteY, int spriteX){
            this.spriteNum = spriteNum;
            this.tile = tile;
            this.colorInfo = colorInfo;
            this.spriteX = spriteX;
            this.spriteY = spriteY;
        }
    }

}
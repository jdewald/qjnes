package org.fortytwo.nes.mappers;

import org.fortytwo.common.memory.ROM;
import org.fortytwo.c64.MemoryPPU;
import org.fortytwo.c64.video.LineObserver; // hmm...

/**
 * mapper 4
Mapper 4: MMC3 - PRG/8K, VROM/2K/1K, VT, SRAM, IRQ
--------------------------------------------------

A great majority of newer NES games (early 90's) use this mapper, both U.S. and
Japanese. Among the better-known MMC3 titles are Super Mario Bros. 2 and 3,
Mega Man 3, 4, 5, and 6, and Crystalis.
  8000h  Index/Control (5bit)
         Bit7   CHR Address Select  (0=Normal, 1=Address Areas XOR 1000h)
         Bit6   PRG Register 6 Area (0=8000h-9FFFh, 1=C000h-DFFFh)
         Bit2-0 Command Number
           0 - Select 2x1K VROM at PPU 0000h-07FFh (or 1000h-17FFh, if Bit7=1)
           1 - Select 2x1K VROM at PPU 0800h-0FFFh (or 1800h-1FFFh, if Bit7=1)
           2 - Select 1K VROM at PPU 1000h-13FFh   (or 0000h-03FFh, if Bit7=1)
           3 - Select 1K VROM at PPU 1400h-17FFh   (or 0400h-07FFh, if Bit7=1)
           4 - Select 1K VROM at PPU 1800h-1BFFh   (or 0800h-0BFFh, if Bit7=1)
           5 - Select 1K VROM at PPU 1C00h-1FFFh   (or 0C00h-0FFFh, if Bit7=1)
           6 - Select 8K ROM at 8000h-9FFFh (or C000h-DFFFh, if Bit6=1)
           7 - Select 8K ROM at A000h-BFFFh
         N/A - Fixed  8K ROM at C000h-DFFFh (or 8000h-9FFFh, if Bit6=1)
         N/A - Fixed  8K ROM at E000h-FFFFh (always last 8K bank)
  8001h  Data Register    (Indexed via Port 8000h)
  A000h  Mirroring Select (Bit0: 0=Vertical, 1=Horizontal Mirroring)
  A001h  SaveRAM Toggle   (Bit7: 0=Disable 6000h-7FFFh, 1=Enable 6000h-7FFFh)
  C000h  IRQ Counter Register - The IRQ countdown value is stored here.
  C001h  IRQ Latch Register - A temporary value is stored here.
  E000h  IRQ Control Register 0
          Any value written here will disable IRQ's and copy the
          latch register to the actual IRQ counter register.
  E001h  IRQ Control Register 1 - Any value written here will enable IRQ's.
The fixed PRG banks are always the LAST two 8K banks in the cart.
On carts with VROM, the first 8K of VROM is swapped into PPU $0000 on reset.
On carts without VROM, as always, there is 8K of VRAM at PPU $0000.

The IRQ counter is decremented each scanline, based on PPU address line A13
which toggles between Pattern Tables (LOW) and Name Tables (HIGH) 42 times per
scanline. The counter is paused during VBlank, which allows to use the same
settings for PAL and NTSC timings. Note that the counter gets clocked, even
during VBlank, when toggling A13 a bunch of times via Port 2006h.

Multicarts with MMC3 and additional Game-Select Ports
--> Mapper 44: 7-in-1 MMC3 Port A001h
--> Mapper 45: X-in-1 MMC3 Port 6000hx4
--> Mapper 47: 2-in-1 MMC3 Port 6000h
--> Mapper 49: 4-in-1 MMC3 Port 6xxxh
--> Mapper 52: 7-in-1 MMC3 Port 6800h with SRAM

Low G Man                       128k PRG / 128k CHR     H       MMC3 (4)
 */
public class MMC3Mapper extends Mapper implements LineObserver
{
    //    protected boolean charInvert = false;

    protected boolean irqFlag = false;
    protected boolean irqEnabled = false;
    protected int irqLatch = 0;
    protected int irqCounter = 0;

    public static final int PRG_SWAPPING = 0x40;

    protected boolean swappable_8000 = false;
    protected boolean swappable_A000 = false;
    protected boolean swappable_C000 = false;
    protected boolean swappable_E000 = false;

    protected int mode = 0;
    protected int mirroringType = 0;
    protected boolean usingCharRAM = false;

    // TODO: The TQROM can actually have 8K of RAM, not 2K
    // so the bank switching must affect it
    public MMC3Mapper(ROM programROM, ROM charROM){
        super(programROM, charROM);
        super.charROM = new MMC3_119Handler(charROM);
    }

    
    /*
	@Override
	public ROM getCharacterROM() {
		return new MMC3_119Handler(super.getCharacterROM());
	}*/


//http://wiki.nesdev.com/w/index.php/TQROM
	class MMC3_119Handler extends ROM {
		ROM mapped;
		public MMC3_119Handler(String name, byte[] data) {
			super(name, data);
			// TODO Auto-generated constructor stub
		}
		
		public MMC3_119Handler(ROM mapped){
			
			super("",new byte[1]);
			this.mapped = mapped;
		}

		@Override
		public int read(int address) {
			if (usingCharRAM){
				return saved.read(address % 0x2000);
			} else {
				return mapped.read(address);
			}
		}
    	
		
    }
    // we actually deal in 8K chunks so we need to
    // map both 0x8000 and 0xA000
    public void write(int location, int value){
        //logger.info("Write of " + Integer.toHexString(value) + " to " + Integer.toHexString(location));
    	//7c  = 0111 1100
    	//7e  = 0111 1110
        // PRG SWAPPING
        if (location == 0){ // 0x8000
        	//logger.info("Write of " + Integer.toHexString(value) + " to " + Integer.toHexString(location));
            if ((value & PRG_SWAPPING) == 0){
                swappable_8000 = true;
                swappable_A000 = true;
                swappable_C000 = false;

                start_C000 = (chunks - 2) * CHUNK_SIZE; // 2nd to last
            }
            else {
                swappable_8000 = false;
                swappable_A000 = true;
                swappable_C000 = true;
                
                start_8000 = (chunks - 2) * CHUNK_SIZE; // 2nd to last
            }

            // MODE
            mode = value & 0x7;

            charInvert = ((value & 0x80) != 0);

            if (charInvert){
                                throw new RuntimeException("Char invert is true!");
            }
            
        }
        else if (location == 1){
        	if ((value & 0x70) != 0){
        		//logger.info("mode = " + mode + " / Write of " + Integer.toHexString(value) + " to " + Integer.toHexString(location));
        	}
        	handleBankSwitching(value);
        }
        else if (location == 0x2000){ // 0xA000
        	//ppuMemory.setMirroringType(MemoryPPU.HORIZONTAL_MIRRORING);
            mirroringType = value & 0x1;
            //System.out.println("Raw mirror type: " + value);
            //ppuMemory.setMirroringType(mirroringType);
            if (mirroringType == 0) {  ppuMemory.setMirroringType(MemoryPPU.VERTICAL_MIRRORING); }
            else if (mirroringType == 1) {  ppuMemory.setMirroringType(MemoryPPU.HORIZONTAL_MIRRORING); }

        }
        else if (location == 0x2001) { // 0xA001
            logger.info("WRAM: " + Integer.toHexString(value));

        }
        else if (location == 0x4000){ // 0xC000 - IRQ reload - latch
            irqLatch = value;
            //            logger.info("IRQ latch " + value);
        }
        else if (location == 0x4001) { // 0xC001 - IRQ counter clear
            //            logger.info("IRQ clear: counter is " + irqCounter);
            irqCounter = 0;

        }
        else if (location == 0x6000) { // 0xE000
            // there seems to be disagreement in the various docs about whether
            // this actually loads the latch
            irqEnabled = false; 
            irqFlag = false;
            irqCounter = irqLatch;
        }
        else if (location == 0x6001){ // 0xE001
            irqEnabled = true;
        }
        else {
            throw new RuntimeException("Don't know how to handle write to " + Integer.toHexString(location + 0x8000));
        }
    }

    private void handleBankSwitching(int val){
    	// when in TQROM mode, this is val & 0x3f, and we enable usingCharRAM 
        int charValue = (val ) * 1024;
        //int charValue = (val & 0x3f) * 1024;
        // technicall this is just for the TQROM
        //usingCharRAM = (val & 0x40) != 0;
        
        //        if (mode >=0 && mode < 6)logger.info("Bank switch: " + mode + " " + val);
        switch (mode){
        case 0: 
            chr_start_0000 = charValue;
            chr_start_0400 = chr_start_0000 + 1024;

            //ppuMemory.setPatternROM(0x0, charROM, val * 1024, 2048);
            break;
        case 1:
                chr_start_0800 = charValue;
                chr_start_0C00 = chr_start_0800 + 1024;

            //            ppuMemory.setPatternROM(0x800, charROM, val * 1024, 2048);
            break;
        case 2:
            chr_start_1000 = charValue;

            //            ppuMemory.setPatternROM(0x1000,charROM,val * 1024, 1024);
            break;            
        case 3:
            chr_start_1400 = charValue;
            //            ppuMemory.setPatternROM(0x1400,charROM,val * 1024, 1024);
            break;
        case 4:
            chr_start_1800 = charValue;            
            //            ppuMemory.setPatternROM(0x1800,charROM,val * 1024, 1024);
            break;
        case 5:{
            chr_start_1C00 = charValue;

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

    /*
     * should probably be done by just intercepting memory calls
     */
    public void notifyLine(){
        if (irqCounter == 0){ // 0 from previous
            irqCounter = irqLatch;
        }

        if (((ppu.read(1) & 0x08) != 0)|| ((ppu.read(1) & 0x10) != 0)){        
            irqCounter--;
            //        logger.info("IRQ counter = " + irqCounter);        
            if (irqCounter == 0){
                if (irqEnabled){ 
                    irqFlag = true;
                    
                    cpu.handleInterrupt();
                }
            }
        }


    }
}

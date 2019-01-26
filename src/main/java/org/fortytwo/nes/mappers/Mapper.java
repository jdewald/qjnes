package org.fortytwo.nes.mappers;

import org.fortytwo.common.memory.BaseMemory;
import org.fortytwo.common.memory.RAM;
import org.fortytwo.common.memory.ROM;
import org.fortytwo.c64.MemoryPPU;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.fortytwo.common.cpu.CPU;
import org.fortytwo.c64.video.PPU_2C02;

/**
 * Base for doing memory mapping
 */
public class Mapper extends BaseMemory {
    protected Logger logger = Logger.getLogger(this.getClass().getName());
    protected ROM programROM;
    protected ROM charROM;
    protected RAM saved;
    protected MemoryPPU ppuMemory; // hmm...
    protected boolean charInvert = false;
    // 8K chunks
    protected int start_8000;
    protected int start_A000;
    protected int start_C000;
    protected int start_E000;

    // 1K chunks for CHR
    protected int chr_start_0000;
    protected int chr_start_0400;
    protected int chr_start_0800;
    protected int chr_start_0C00;
    protected int chr_start_1000;
    protected int chr_start_1400;
    protected int chr_start_1800;
    protected int chr_start_1C00;

    public static final int CHUNK_SIZE = 8192;
    protected int chunks;
    protected ROM mappedCharROM;

    protected CPU cpu = null;
    protected PPU_2C02 ppu = null;

    public static Mapper getMapper(int mapperId, ROM programROM, ROM charROM) {

        Map<Integer, Mapper> mappers = Map.of(
                0, new Mapper(programROM, charROM)
                , 1, new MMC1Mapper(programROM, charROM)
                , 2, new UnromMapper(programROM, charROM)
                , 3, new CnromMapper(programROM, charROM)
                , 4, new MMC3Mapper(programROM, charROM)
                , 7, new AxROMMapper(programROM, charROM));

        Optional<Mapper> mapper = Optional.ofNullable(mappers.get(mapperId));

        if (mapper.isPresent())
            return mapper.get();
        else
            throw new RuntimeException("Don't know how to handle mapper type: " + mapperId);
    }

    public void setCPU(CPU cpu) {
        this.cpu = cpu;
    }

    public void setMemoryPPU(MemoryPPU ppuMemory_) {
        this.ppuMemory = ppuMemory_;
    }

    public void setPPU(PPU_2C02 ppu_) {
        this.ppu = ppu_;
    }

    /**
     * Defaults to using first and last bits
     */
    public Mapper(ROM programROM, ROM charROM) {
        super();
        this.programROM = programROM;
        this.charROM = charROM;
        this.saved = new RAM(0x2000);
        chunks = programROM.size() / CHUNK_SIZE; // how many 8K chunks do we have
        logger.info("Have " + chunks + " chunks");
        // default to using last 16 at 0xC000
        start_E000 = (chunks - 1) * CHUNK_SIZE;
        logger.info("E000 starts at " + Integer.toHexString(start_E000));
        start_C000 = start_E000 - CHUNK_SIZE;

        // and first 16 at 0x8000
        start_8000 = 0;
        start_A000 = CHUNK_SIZE;

        // 1K chunks
        chr_start_0000 = 0;
        chr_start_0400 = 0x400;
        chr_start_0800 = 0x800;
        chr_start_0C00 = 0xC00;
        chr_start_1000 = 0x1000;
        chr_start_1400 = 0x1400;
        chr_start_1800 = 0x1800;
        chr_start_1C00 = 0x1C00;
    }

    public RAM getSaved() {
        return saved;
    }

    public ROM getCharacterROM() {
        if (mappedCharROM == null) {
            mappedCharROM = new MappedCharROM();
        }
        return mappedCharROM;
    }


    public int read(int location) {
        if (location >= 0 && location < 0x2000) {
            return programROM.read(start_8000 + location);
        } else if (location >= 0x2000 && location < 0x4000) {
            return programROM.read(start_A000 + (location - 0x2000));
        } else if (location >= 0x4000 && location < 0x6000) {
            return programROM.read(start_C000 + (location - 0x4000));
        } else if (location >= 0x6000 && location < 0x8000) {
            return programROM.read(start_E000 + (location - 0x6000));
        } else {
            throw new RuntimeException("Invalid read location: " + Integer.toHexString(location));
        }
    }

    public void write(int location, int value) {
        logger.info("Location = " + Integer.toHexString(location) + " value = " + Integer.toHexString(value));
        throw new RuntimeException("Write not supported!");
        //        programROM.write(location, value);
        /*
        if (programROM.getName().indexOf("GENIE") != -1){
            if (location >= 0x4000){
                programROM.getRaw()[location - 16384] = (byte)(0xFF & value);
            }
            else {
                programROM.getRaw()[location] = (byte)(0xFF & value);
            }
        }
        else {

        }
        */
    }

    class MappedCharROM extends ROM {
        MappedCharROM() {
            super("MAPPED", new byte[0]);
            logger.info("Created mapper");
        }

        public int size() {
            return charROM.size() > 0 ? 8192 : 0;
        }

        public int read(int address) {
            if (charInvert) {
                address ^= 0x1000;
            } // this really should be handled by the MMC3 mapper...
            if (address >= 0 && address < 0x400) {
                return charROM.read(chr_start_0000 + address);
            } else if (address >= 0x400 && address < 0x800) {
                address -= 0x400;
                return charROM.read(chr_start_0400 + address);
            } else if (address >= 0x800 && address < 0xC00) {
                address -= 0x800;
                return charROM.read(chr_start_0800 + address);
            } else if (address >= 0xC00 && address < 0x1000) {
                address -= 0xC00;
                return charROM.read(chr_start_0C00 + address);
            } else if (address >= 0x1000 && address < 0x1400) {
                address -= 0x1000;
                return charROM.read(chr_start_1000 + address);
            } else if (address >= 0x1400 && address < 0x1800) {
                address -= 0x1400;
                return charROM.read(chr_start_1400 + address);
            } else if (address >= 0x1800 && address < 0x1C00) {
                address -= 0x1800;
                return charROM.read(chr_start_1800 + address);
            } else if (address >= 0x1C00 && address < 0x2000) {
                address -= 0x1C00;
                return charROM.read(chr_start_1C00 + address);
            } else {
                throw new RuntimeException("Can only read up to 8K!");
            }
        }
    }
}

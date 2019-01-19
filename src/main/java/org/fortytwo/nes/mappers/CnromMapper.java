package org.fortytwo.nes.mappers;

import org.fortytwo.common.memory.ROM;

/**
 * writing to any location brings in the selected 16K bank
 */
public class CnromMapper extends Mapper {
    public CnromMapper(ROM programROM, ROM charROM) {
        super(programROM, charROM);
    }

    // we actually deal in 8K chunks so we need to
    // map both 0x8000 and 0xA000
    public void write(int location, int value) {
        logger.info("Block = " + value);
        chr_start_0000 = value * CHUNK_SIZE;
        chr_start_0400 = chr_start_0000 + 1024;
        chr_start_0800 = chr_start_0400 + 1024;
        chr_start_0C00 = chr_start_0800 + 1024;
        chr_start_1000 = chr_start_0C00 + 1024;
        chr_start_1400 = chr_start_1000 + 1024;
        chr_start_1800 = chr_start_1400 + 1024;
        chr_start_1C00 = chr_start_1800 + 1024;
    }
}

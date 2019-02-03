package org.fortytwo.nes.model.mappers;

import org.fortytwo.common.memory.ROM;
/**
 * writing to any location brings in the selected 16K bank
 */
public class UnromMapper extends Mapper
{
    public UnromMapper(ROM programROM, ROM charROM){
        super(programROM, charROM);
    }

    // we actually deal in 8K chunks so we need to
    // map both 0x8000 and 0xA000
    public void write(int location, int value){
        start_8000 = (CHUNK_SIZE * 2) * value;
        start_A000 = start_8000 + CHUNK_SIZE;
    }
}

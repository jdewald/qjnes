package org.fortytwo.c64.mappers;

import org.fortytwo.c64.memory.ROM;


public class AxROMMapper extends Mapper
{

	protected int SWAP_SIZE = 32 * 1024;

	public AxROMMapper(ROM programROM, ROM charROM) {
		super(programROM, charROM);
	}

	
	public void write(int location, int value) {
		int bankValue = value & 0x7; 

		start_8000 = SWAP_SIZE * bankValue;
		start_A000 = start_8000 + CHUNK_SIZE;
		start_C000 = start_A000 + CHUNK_SIZE;
		start_E000 = start_C000 + CHUNK_SIZE;
	}
}


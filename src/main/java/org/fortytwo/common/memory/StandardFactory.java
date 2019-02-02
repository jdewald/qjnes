package org.fortytwo.common.memory;

import org.fortytwo.c64.model.CIA;
import org.fortytwo.c64.model.memory.Memory6502;
import org.fortytwo.c64.model.video.VICII;

import java.io.File;
import java.io.IOException;

public class StandardFactory {

    private final String ROOT_FOLDER = "C:\\Users\\KDRZAZGA\\Documents\\programming\\qjnes\\";

    public Memory6502 createStandardMemory6502(VICII vic, CIA cia1, ROM cartridgeROM) throws IOException {
        int romStart;
        var kernalROM = createKernalRom();
        var basicROM = createBasicRom();
        var charROM = createCharRom();

        var cia2 = new CIA("CIA2");

        romStart = 0;
        var ram = new RAM(65536);
        var colorRAM = new RAM(0x400);
        /**
         * Create the view that the 6502 sees
         */
        // cartridge should actually use a different view of memory (separate "emulator" mode)
        return new Memory6502(kernalROM,basicROM,charROM,cartridgeROM, romStart, ram,colorRAM, vic,cia1,cia2);
    }

    private ROM createCharRom() throws IOException {
        return new ROM("char", new File(ROOT_FOLDER + "roms/characters.901225-01.bin"));
    }

    private ROM createBasicRom() throws IOException {
        return new ROM("basic", new File(ROOT_FOLDER + "roms/basic.901226-01.bin"));
    }

    private ROM createKernalRom() throws IOException {
        return new ROM("kernal",new File(ROOT_FOLDER + "roms/kernal.901227-03.bin"));
    }
}

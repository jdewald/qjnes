package org.fortytwo.nes.mappers;

import org.fortytwo.common.memory.Memory;
import org.fortytwo.common.memory.BaseMemory;
import org.fortytwo.common.memory.ROM;
import org.fortytwo.common.memory.RAM;
import org.fortytwo.common.cpu.CPU;

import org.fortytwo.nes.util.NESFile;

import java.io.File;

public class GameGenie extends BaseMemory {
    ROM genieROM;
    ROM charROM;
    RAM genieRAM;

    ROM cartridgeCharROM;
    Memory cartridgeMemory;


    CPU cpu;

    // right now only support a single memory address
    int checkAddress = 0;
    int patchValue = 0;
    int checkValue = 0;
    int type = 0; // enabled, disabled, checked?
    boolean passThrough = false;
    boolean enabled = true;

    public GameGenie(CPU cpu, Mapper cartridge) {
        this.cpu = cpu;
        try {
            var genieFile = new NESFile(new File("GENIE.NES"));

            genieROM = genieFile.getProgramROM();
            charROM = genieFile.getCharROM();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        this.cartridgeMemory = (Memory) cartridge;
        this.cartridgeCharROM = cartridge.getCharacterROM();
        genieRAM = new RAM(32768);
    }

    public ROM getCharacterROM() {
        return new GenieCharROM();
    }

    public int read(int location) {
        if (passThrough || !enabled) {
            int realValue = cartridgeMemory.read(location);
            if (location == checkAddress && realValue == checkValue) {
                return patchValue;
            } else {
                return realValue;
            }
        } else {
            return genieROM.read(location % 16384);
        }

    }

    public void write(int location, int value) {
        if (!passThrough) {
            genieRAM.write(location, value);
            System.out.println("Location = " + Integer.toHexString(location) + " val=" + Integer.toHexString(value));
            if (location == 0 && value == 0) {
                // this seems to mean that we're good to go
                passThrough = true;
                enabled = (type != 0x71);
                if (enabled) {
                    System.out.println("Address: " + Integer.toHexString(checkAddress) + " patch = " + Integer.toHexString(patchValue) + " check = " + Integer.toHexString(checkValue));
                }
                cpu.restart();
            } else if (location == 4) {
                patchValue = value;
            } else if (location == 3) {
                checkValue = value;
            } else if (location == 2) {
                checkAddress &= 0xFF00;
                checkAddress |= (0xFF & value);
            } else if (location == 1) {
                checkAddress &= 0xFF;
                checkAddress |= ((0xFF & value) << 8);

            } else if (location == 0) {
                type = value;
            }

        } else {
            cartridgeMemory.write(location, value);
        }
    }

    class GenieCharROM extends ROM {
        GenieCharROM() {
            super("GENIE", new byte[0]);
        }

        public int size() {
            return 8192;
        }

        public int read(int address) {
            if (passThrough) {
                return cartridgeCharROM.read(address);
            } else {
                return charROM.read(address);
            }
        }
    }
}

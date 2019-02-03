package org.fortytwo.c64;

import org.fortytwo.c64.model.Emulator;

public class Commodore64 {
    public static void main(String[] args) {
        try {
            final var emulator = Emulator.createMos6502Emulator(args);
            emulator.run();

        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            Runtime.getRuntime().exit(0);
        }
    }
}

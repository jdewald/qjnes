package org.fortytwo.c64.memory;

import org.fortytwo.c64.Emulator;
import org.fortytwo.c64.cpu.Instruction;
import org.fortytwo.c64.cpu.MOS6502Emulator;
import org.fortytwo.c64.cpu.ROR_Instruction;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ROL_InstructionTest {
    private MOS6502Emulator emulator;

    @Before
    public void init() {
        try {
            emulator = Emulator.createMos6502Emulator(new String[0]);
            Emulator.enableSilentRun();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void RORByteRotationTest() {
        ROR_Instruction rorInstruction = new ROR_Instruction(Instruction.AddressingMode.Absolute);

        int address = 0xd020;
        int value = 0b00100100;

        Memory memory = emulator.getMemory();

        rorInstruction.execute(new int[]{address, value}, memory, emulator);

        assertEquals(0b01001000, memory.read(address));

    }
}

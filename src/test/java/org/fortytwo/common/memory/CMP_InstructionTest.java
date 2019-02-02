package org.fortytwo.common.memory;

import org.fortytwo.c64.model.Emulator;
import org.fortytwo.common.cpu.CMP_Instruction;
import org.fortytwo.common.cpu.Instruction;
import org.fortytwo.common.cpu.MOS6502Emulator;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class CMP_InstructionTest {

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
    public void CMPSizeTest() {
        var cmpInstruction = new CMP_Instruction(Instruction.AddressingMode.Absolute);

        int address = 0xd020;
        int value = 0x00;

        cmpInstruction.execute(new int[]{address, value}, emulator.getMemory(), emulator);

        assertEquals(2, cmpInstruction.getAddressingMode().getByteCount());

    }

}

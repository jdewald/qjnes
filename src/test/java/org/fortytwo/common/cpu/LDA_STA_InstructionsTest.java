package org.fortytwo.common.cpu;

import org.fortytwo.c64.model.Emulator;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class LDA_STA_InstructionsTest {

    private MOS6502Emulator emulator;

    LDA_Instruction ldaInstruction;
    STA_Instruction staInstruction;

    @Before
    public void init() {
        try {
            emulator = Emulator.createMos6502Emulator(new String[0]);
            Emulator.enableSilentRun();
            ldaInstruction = new LDA_Instruction(Instruction.AddressingMode.Immediate);
            staInstruction = new STA_Instruction(Instruction.AddressingMode.Absolute);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testStroringDataInMemory()
    {
        ldaInstruction.execute(new int[]{0x6f}, emulator.getMemory(), emulator);
        staInstruction.execute(new int[]{0xc000}, emulator.getMemory(), emulator);

        assertEquals(0x6f, emulator.getMemory().read(0xc000));
    }

}

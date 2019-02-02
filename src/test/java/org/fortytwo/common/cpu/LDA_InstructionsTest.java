package org.fortytwo.common.cpu;

import org.fortytwo.c64.model.Emulator;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class LDA_InstructionsTest {

    private MOS6502Emulator emulator;

    LDA_Instruction ldaInstruction;

    @Before
    public void init() {
        try {
            emulator = Emulator.createMos6502Emulator(new String[0]);
            Emulator.enableSilentRun();
            ldaInstruction = new LDA_Instruction(Instruction.AddressingMode.Immediate);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testStroringDataInMemory()
    {
        ldaInstruction.execute(new int[]{0xab}, emulator.getMemory(), emulator);

        assertEquals(0xab, emulator.readRegister(RegisterType.accumulator));
    }

}

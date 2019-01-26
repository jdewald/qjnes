package org.fortytwo.common.cpu;

import org.fortytwo.c64.Emulator;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class PHA_PLA_InstructionsTest {

    private MOS6502Emulator emulator;

    LDA_Instruction ldaInstruction;
    PHA_Instruction phaInstruction;
    PLA_Instruction plaInstruction;

    @Before
    public void init() {
        try {
            emulator = Emulator.createMos6502Emulator(new String[0]);
            Emulator.enableSilentRun();
            ldaInstruction = new LDA_Instruction(Instruction.AddressingMode.Immediate);
            phaInstruction = new PHA_Instruction(Instruction.AddressingMode.Immediate);
            plaInstruction = new PLA_Instruction(Instruction.AddressingMode.Immediate);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPushingAndPullingAccumulatorOnStack()
    {
        ldaInstruction.execute(new int[]{0x8a}, emulator.getMemory(), emulator);
        phaInstruction.execute(new int[0], emulator.getMemory(), emulator);
        ldaInstruction.execute(new int[]{0xb9}, emulator.getMemory(), emulator);
        assertEquals(0xb9, emulator.readRegister(RegisterType.accumulator));

        plaInstruction.execute(new int[0], emulator.getMemory(), emulator);
        assertEquals(0x8a, emulator.readRegister(RegisterType.accumulator));
    }

}

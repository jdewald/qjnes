package org.fortytwo.common.cpu;

import org.fortytwo.c64.Emulator;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class SEI_InstructionTest {

    private MOS6502Emulator emulator;
    private SEI_Instruction seiInstruction;

    @Before
    public void init() {
        try {
            emulator = Emulator.createMos6502Emulator(new String[0]);
            Emulator.enableSilentRun();
            seiInstruction = new SEI_Instruction(Instruction.AddressingMode.Absolute);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testInterruptDisabling(){
        seiInstruction.execute(new int[0], emulator.getMemory(), emulator);
        assertTrue(emulator.getInterruptsDisabled());
    }

}

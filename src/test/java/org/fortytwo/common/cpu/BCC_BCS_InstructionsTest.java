package org.fortytwo.common.cpu;

import org.fortytwo.c64.model.Emulator;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class BCC_BCS_InstructionsTest {

    private MOS6502Emulator emulator;

    private BCC_Instruction bccInstruction;
    private BCS_Instruction bcsInstruction;
    private SEC_Instruction secInstruction;
    private CLC_Instruction clcInstruction;

    @Before
    public void init() {
        try {
            emulator = Emulator.createMos6502Emulator(new String[0]);
            Emulator.enableSilentRun();
            bccInstruction = new BCC_Instruction(Instruction.AddressingMode.Relative);
            bcsInstruction = new BCS_Instruction(Instruction.AddressingMode.Relative);
            secInstruction = new SEC_Instruction(Instruction.AddressingMode.Relative);
            clcInstruction = new CLC_Instruction(Instruction.AddressingMode.Implied);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testConditionalJumpOnCarryFlagClear()
    {
        clcInstruction.execute(new int[0], emulator.getMemory(), emulator);
        bcsInstruction.execute(new int[]{2000}, emulator.getMemory(), emulator);
        bccInstruction.execute(new int[]{1000}, emulator.getMemory(), emulator);
        assertEquals(1000, emulator.readRegister(RegisterType.programCounter));
    }

    @Test
    public void testConditionalJumpOnCarryFlagSet()
    {
        secInstruction.execute(new int[0], emulator.getMemory(), emulator);
        bcsInstruction.execute(new int[]{2000}, emulator.getMemory(), emulator);
        bccInstruction.execute(new int[]{1000}, emulator.getMemory(), emulator);
        assertEquals(2000, emulator.readRegister(RegisterType.programCounter));
    }

}

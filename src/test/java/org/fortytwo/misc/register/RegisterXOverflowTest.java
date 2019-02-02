package org.fortytwo.misc.register;

import org.fortytwo.c64.model.Emulator;
import org.fortytwo.common.cpu.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.fortytwo.common.cpu.Instruction.AddressingMode.Immediate;
import static org.fortytwo.common.cpu.Instruction.AddressingMode.Implied;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RegisterXOverflowTest {

    private MOS6502Emulator emulator;

    LDX_Instruction ldxInstruction;
    INX_Instruction inxInstruction;

    @Before
    public void init() {
        try {
            emulator = Emulator.createMos6502Emulator(new String[0]);
            Emulator.enableSilentRun();
            ldxInstruction = new LDX_Instruction(Immediate);
            inxInstruction = new INX_Instruction(Implied);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRegisterXOverflow(){
        ldxInstruction.execute(new int[]{0b11111111}, emulator.getMemory(), emulator);
        inxInstruction.execute(new int[0], emulator.getMemory(), emulator);

        assertEquals(0b00000000, emulator.readRegister(RegisterType.X));
        assertTrue(emulator.getZeroFlag());
        assertTrue(emulator.getOverflowFlag());
    }


}

package org.fortytwo.c64.memory;

import org.fortytwo.c64.Emulator;
import org.fortytwo.c64.cpu.Instruction;
import org.fortytwo.c64.cpu.MOS6502Emulator;
import org.fortytwo.c64.cpu.ROL_Instruction;
import org.fortytwo.c64.cpu.RegisterType;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class ROL_InstructionTest {

    private MOS6502Emulator emulator;
    private ROL_Instruction rolInstruction;

    @Before
    public void init() {
        try {
            emulator = Emulator.createMos6502Emulator(new String[0]);
            Emulator.enableSilentRun();
            rolInstruction = new ROL_Instruction(Instruction.AddressingMode.Absolute);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testROLByteRotation() {
        int address = 0xd020;
        int value = 0b00100100;

        Memory memory = emulator.getMemory();
        rolInstruction.execute(new int[]{address, value}, memory, emulator);

        assertEquals(0b01001000, memory.read(address));
    }

    @Test
    public void testROLByteRotationWithCarry(){
        int address = 0xcafe;
        int value = 0b00100101;

        int statusRegBeforeRotation = emulator.readRegister(RegisterType.status);
        Memory memory = emulator.getMemory();

        rolInstruction.execute(new int[]{address, value}, memory, emulator);

        int statusRegAfterRotation = emulator.readRegister(RegisterType.status);

        assertNotEquals(statusRegBeforeRotation, statusRegAfterRotation);
        assertFalse(emulator.getCarryFlag());
    }

}

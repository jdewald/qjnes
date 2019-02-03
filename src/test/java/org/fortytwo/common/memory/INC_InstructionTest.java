package org.fortytwo.common.memory;

import org.fortytwo.c64.model.Emulator;
import org.fortytwo.common.cpu.INC_Instruction;
import org.fortytwo.common.cpu.Instruction;
import org.fortytwo.common.cpu.MOS6502Emulator;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class INC_InstructionTest {

    private MOS6502Emulator emulator;
    private INC_Instruction incInstruction;

    @Before
    public void init() {
        try {
            emulator = Emulator.createMos6502Emulator(new String[0]);
            Emulator.enableSilentRun();
            incInstruction = new INC_Instruction(Instruction.AddressingMode.Absolute);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCellValueIncrementation(){

        int location = 0xa000;
        int value = 0xfe;

        var memory = emulator.getMemory();

        memory.write(location, value);

        assertEquals(0xfe, memory.read(location));

        incInstruction.execute(new int[]{location}, memory, emulator);

        assertEquals(0xff, memory.read(location));
    }

}

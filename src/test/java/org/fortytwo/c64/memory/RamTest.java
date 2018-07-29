package org.fortytwo.c64.memory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RamTest {

    @Test
    public void testReadWrite(){
        RAM ram = new RAM(64 * 1024);

        ram.write(0xd020, 2);
        ram.write(53281, 4);

        assertEquals(2, ram.read(0xd020));
        assertEquals(4, ram.read(53281));
    }
}

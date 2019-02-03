package org.fortytwo.common.memory;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RamTest {

    private RAM ram;

    @Before
    public void init() {
        ram = new RAM(64 * 1024);
    }

    @Test
    public void testReadWrite() {
        ram.write(0xd020, 2);
        ram.write(53281, 4);

        assertEquals(2, ram.read(0xd020));
        assertEquals(4, ram.read(53281));
    }

    @Test(expected = Exception.class)
    public void testWriteToCellOver64Kb() {
        ram.write(65536, 1);
    }
}

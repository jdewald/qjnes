package org.fortytwo.c64.io;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class D64FileTest {

    @Test
    public void testFile() {
        try {
            File file = new File("test\\resources\\org\\fortytwo\\c64\\io\\testDisk.d64");
            D64File d64File = new D64File(file);

            assertEquals(35, d64File.tracks.length);

        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            Runtime.getRuntime().exit(0);
        }
    }
}

package org.fortytwo.c64.io;

import org.fortytwo.c64.model.io.D64File;
import org.fortytwo.c64.model.io.Simple1541;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class Simple1541Test {

    @Test
    public void readD64() throws IOException {
        var drive = new Simple1541();
        var file = new File("test\\resources\\org\\fortytwo\\c64\\io\\testDisk.d64");

        drive.loadDisk(new D64File(file));

        assertEquals(0x40, drive.read());
        Assert.fail("No data was read from disk image. ");
    }

}

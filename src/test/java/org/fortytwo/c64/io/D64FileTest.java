package org.fortytwo.c64.io;

import org.fortytwo.c64.model.io.D64File;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class D64FileTest {

    @Test
    public void testDiskImage() {
        try {
            var file = new File("test\\resources\\org\\fortytwo\\c64\\io\\testDisk.d64");
            var d64File = new D64File(file);

            assertEquals(35, d64File.tracks.length);//diskette has 35 tracks
            assertEquals(21, d64File.getTrack(1).sectors.size());//tracks 1-17 have 21 sectors each
            assertEquals(19, d64File.getTrack(18).sectors.size());//tracks 18-30 have 19
            assertEquals(17, d64File.getTrack(31).sectors.size());//and tracks 17-35 17 sectors
            assertEquals(256, d64File.getTrack(1).sectors.get(20).data.length);// each sector contains 256 bytes of data

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}

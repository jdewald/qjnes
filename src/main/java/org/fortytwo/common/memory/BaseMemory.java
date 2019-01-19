package org.fortytwo.common.memory;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.File;


/**
 * Represents the Main Memory for the Commodore 64
 * This handles ROM as well as special I/O sections
 * TODO: Make this be an interface so that it can be mocked as necessary
 */
public abstract class BaseMemory implements Memory {

    protected boolean shouldLog = false;

    public BaseMemory() {
        shouldLog = false;
    }

    public void enableLogging() {
        shouldLog = true;
    }

    public void disableLogging() {
        shouldLog = false;
    }

    public void enableLogging(String subsystem) {
        enableLogging();
    }

    public void disableLogging(String subsystem) {
        disableLogging();
    }

    public void dump(int start, int end, String filename) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(new File(filename));
        int[] data = new int[end - start];
        read(start, data);
        //        fileOut.write(data);
        fileOut.close();
    }

    public abstract void write(int location, int val);

    public abstract int read(int location);

    public void read(int location, int[] target) {
        for (int i = 0; i < target.length; i++) {
            target[i] = read(location + i);
        }
    }


    public int readWord(int location) {
        return 0xFFFF & (read(location) | (0xFF00 & (read(location + 1) << 8)));
    }

    public void write(int location, int[] data) {
        for (int i = 0; i < data.length; i++) {
            write(location + i, data[i]);
        }
    }

    public void writeWord(int location, int val) {
        write(location, (byte) (val & 0xFF));
        write(location + 1, (byte) ((val & 0xFF00) >> 8));

    }

}



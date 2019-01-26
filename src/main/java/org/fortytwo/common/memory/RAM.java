package org.fortytwo.common.memory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class RAM {
    byte data[];

    public RAM(int size) {
        data = new byte[size];
    }

    public byte[] getRaw() {
        return data;
    }

    public int read(int address) {
        return 0xFF & data[address];
    }

    public void dump(int start, int end, String filename) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(new File(filename));
        fileOut.write(data, start, end - start);
        fileOut.close();
    }

    public void write(int address, int value) {
        data[address] = (byte) (0xFF & value);
    }

    public int size() {
        return data.length;
    }
}

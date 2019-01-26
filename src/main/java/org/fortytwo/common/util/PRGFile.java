package org.fortytwo.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PRGFile {
    int[] data;
    int startAddress;
    String filename;

    private long _totalSize;
    public static final int HEADER_LENGTH = 26;

    public PRGFile(File file) throws IOException {
        var fileIn = new FileInputStream(file);

        _totalSize = file.length();
        parse(fileIn);
    }

    private void parse(InputStream in) throws IOException {
        in.skip(8); // "C64File\0"
        byte[] name = new byte[16];
        in.read(name);
        filename = new String(name);
        in.skip(1); // 00
        in.skip(1); // REL

        startAddress = in.read() | (in.read() << 8);
        long dataLen = _totalSize - HEADER_LENGTH - 2;
        data = new int[(int) dataLen];
        for (int i = 0; i < dataLen; i++) {
            data[i] = in.read();
        }
    }

    public String getFilename() {
        return filename;
    }

    public int getStartAddress() {
        return startAddress;
    }

    public int[] getData() {
        return data;
    }

    public static void main(String[] args) {
        try {
            var prg = new PRGFile(new File(args[0]));
            System.out.println(prg.getFilename());
            System.out.println(Integer.toHexString(prg.getStartAddress()));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}

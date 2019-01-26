package org.fortytwo.common.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.DataInputStream;

/**
 * Reads the .crt format
 * Document came from: http://ist.uwaterloo.ca/%7Eschepers/formats/CRT.TXT
 */
public class CRTFile {
    public static final int SIGNATURE_LENGTH = 16;

    protected int version = 0; // 2
    protected int type = 0; // 2
    protected int exromStatus = 0; // 0 = inactive, 1 = active
    protected int gameStatus = 0; // 0 = inactive, 1 = active
    protected String name = "";

    protected CHIPData chipData = null;

    public CRTFile(File crtFile) throws IOException {
        this(new FileInputStream(crtFile));
    }

    public CRTFile(InputStream in) throws IOException {
        DataInputStream dataIn = new DataInputStream(in);
        byte[] signature = new byte[SIGNATURE_LENGTH];
        dataIn.readFully(signature);

        int headerLength = dataIn.readInt();
        version = dataIn.readUnsignedShort();
        type = dataIn.readUnsignedShort();
        exromStatus = dataIn.readUnsignedByte();
        gameStatus = dataIn.readUnsignedByte();

        // reserved
        dataIn.skip(6);
        byte[] nameBytes = new byte[32];
        dataIn.readFully(nameBytes);
        name = new String(nameBytes);

        chipData = new CHIPData(dataIn);

    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public int getExromStatus() {
        return exromStatus;
    }

    public int getGameStatus() {
        return gameStatus;
    }

    public CHIPData getCHIPData() {
        return chipData;
    }

    public static void main(String[] args) {
        File file = new File(args[0]);

        try {
            CRTFile crt = new CRTFile(file);
            System.out.println("Name: " + crt.getName());
            System.out.println("Type: " + crt.getType());
            System.out.println("EXROM: " + crt.getExromStatus());
            System.out.println("GAME: " + crt.getGameStatus());
            System.out.println("CHIP Length: " + crt.getCHIPData().getROMData().length);
            System.out.println("Start: " + Integer.toHexString(crt.getCHIPData().getStartAddress()));
            System.out.println("Chip Type: " + crt.getCHIPData().getType());
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            Runtime.getRuntime().exit(0);
        }
    }

    /**
     * Stored in MSB for int values
     */
    public class CHIPData {
        private int length = 0;
        private byte[] romData = null;
        private int bankNumber = 0;
        private int startAddress = 0;
        private int chipType = 0;
        public static final int HEADER_LENGTH = 12;

        public CHIPData(InputStream in) throws IOException {
            DataInputStream dataIn = new DataInputStream(in);
            in.skip(4); // CHIP signature
            length = dataIn.readInt();

            chipType = dataIn.readUnsignedShort();
            bankNumber = dataIn.readUnsignedShort();
            startAddress = dataIn.readUnsignedShort();
            int romLength = dataIn.readUnsignedShort();
            System.out.println("Length: " + romLength);
            romData = new byte[romLength];
            dataIn.readFully(romData);


        }

        public byte[] getROMData() {
            return romData;
        }

        public int getBankNumber() {
            return bankNumber;
        }

        public int getStartAddress() {
            return startAddress;
        }

        public int getType() {
            return chipType;
        }

    }
}

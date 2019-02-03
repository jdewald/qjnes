package org.fortytwo.nes.model.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;

import org.fortytwo.common.memory.ROM;

public class NESFile 
{
    public static final int SIGNATURE_LENGTH = 4;

    protected String signature = "";
    protected int sixteenCount = 0; // number of 16K blocks
    protected int eightCount = 0; // number of 8K blocks;
    protected int flags = 1; /**  1 byte - 
          bit 0     1 for vertical mirroring, 0 for horizontal mirroring
          bit 1     1 for battery-backed RAM at $6000-$7FFF
          bit 2     1 for a 512-byte trainer at $7000-$71FF
          bit 3     1 for a four-screen VRAM layout 
                    This is only available with certain types of mappers,
                    for example type #1 (BoulderDash) and type #5
                    (Castlevania3).
          bit 4-7   Type of ROM Mapper
                     0 - None
                     1 - Megaman2,Bomberman2,Destiny,etc.
                     2 - Castlevania,LifeForce,etc.
                     3 - QBert,PipeDream,Cybernoid,etc.
                     4 - SilverSurfer,SuperContra,Immortal,etc.
                     5 - Castlevania3
                     6 - F4xxx carts off FFE CDROM (experimental)
                     8 - F3xxx carts off FFE CDROM (experimental)
                    11 - CrystalMines,TaginDragon,etc. (experimental)
                    15 - 100-in-1 cartridge
                             */
    byte[] reserved = new byte[9];
    byte[] programData = null;
    byte[] charData = null;
    
    String name;
    public NESFile(File nesFile) throws IOException {
        this(new FileInputStream(nesFile), nesFile.getName());
    }

    public NESFile(InputStream in, String name) throws IOException {
        var dataIn = new DataInputStream(in);
        this.name = name;
        var sig = new byte[SIGNATURE_LENGTH];
        dataIn.readFully(sig);

        signature = new String(sig);
        sixteenCount = dataIn.readByte();
        programData = new byte[16384 * sixteenCount];
        eightCount = dataIn.readByte();
        charData = new byte[8192 * eightCount];
        flags = dataIn.readUnsignedByte();
        dataIn.readFully(reserved);
        
        dataIn.readFully(programData);
        dataIn.readFully(charData);

        //        programROM = new ROM("program", programData);
        //charRom = new ROM("char", charData);

        //        mapper = Mapper.getMapper(getMapper());
    }

    public ROM getProgramROM(){
        //return mapper.getProgramData();
        return new ROM(name,programData);
    }

    public ROM getCharROM(){
        //return mapper.getCharacterROM();
        return new ROM("character",charData);
    }

    /**
     * 0 = Vertical, 1 = Horiztonal
     */
    public int getMirroringType(){
        return flags & 0x1;
    }

    public boolean isFourScreenLayout(){
        return ((flags & 0x8) != 0);
    }
    public int getMapper(){
        System.out.println(reserved[0]);
        return (0xF0 & flags) >> 4;
    }
    public String toString(){
        var buffer = new StringBuilder();
        buffer.append(signature).append(",");
        buffer.append(sixteenCount).append(",");
        buffer.append(eightCount).append(",");
        buffer.append(Integer.toHexString(flags));
        buffer.append(", Reserved:");
        for (int i : reserved){
        	buffer.append(Integer.toHexString(i)).append(" ");
        }

        return buffer.toString();
    }
    public static void main(String[] args){
        var file = new File(args[0]);

        try {
            var nes = new NESFile(file);

            System.out.println(nes);
        }
        catch (Throwable t){
            t.printStackTrace();
        }
    }
}

package org.fortytwo.c64.memory;

import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;

// represents read-only memory of some sort
public class ROM {
    
    private byte data[];
    private String name;

    public ROM(String name, File romFile) throws IOException{
        this.name = name;
        data = new byte[(int)romFile.length()];
        FileInputStream fileIn = new FileInputStream(romFile);
        fileIn.read(data);
        
        fileIn.close();
        
    }
    
    public ROM(String name, byte[] data){
        this.data = data;
        this.name = name;
    }
    /**
     * This is bad...by trying to see if it speeds things up
     */
    public byte[] getRaw(){
        return data;
    }

    public int read(int address){
        return 0xFF & data[address];
    }

    public String getName(){
        return name;
    }
    public int size(){
        return data.length;
    }

}

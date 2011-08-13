package org.fortytwo.c64.memory;

import java.io.IOException;
public interface Memory
{
    void enableLogging();
    void disableLogging();
    void enableLogging(String subsystem); // enable logging for particular place
    void disableLogging(String subsystem); 
    void read(int location, int[] target);
    int read(int location);
    int readWord(int location);
    void write(int location, int val);
    void write(int location, int[] data);
    void writeWord(int location, int val);
    void dump(int start, int end, String filename) throws IOException;
}



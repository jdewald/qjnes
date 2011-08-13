package org.fortytwo.c64.memory;

/*
 * Anything that can "intercept" a particular read
 * Should the 'Memory' address just be an extension of this?
 */
public interface MemoryHandler
{
    int read(int address);
    void write(int address, int value);

    void enableLogging();
    void disableLogging();
}

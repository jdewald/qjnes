package org.fortytwo.c64.io;

/**
 * Umm..and I/O device (keyboard, cassette, etc)
 * Will be fed to a CIA
 */
public interface IODevice
{
    void write(int val); 
    int read();
}

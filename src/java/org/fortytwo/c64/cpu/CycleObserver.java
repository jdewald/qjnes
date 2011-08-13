package org.fortytwo.c64.cpu;

public interface CycleObserver
{
    /**
     * This interface represents something that is listening for cycles
     * @return true if an interrupt has occurred
     */
    public int tick(int cycles, CPU cpu); 
}

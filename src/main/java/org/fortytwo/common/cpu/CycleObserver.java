package org.fortytwo.common.cpu;

public interface CycleObserver
{
    /**
     * This interface represents something that is listening for cycles
     * @return true if an interrupt has occurred
     */
    int tick(int cycles, CPU cpu);
}

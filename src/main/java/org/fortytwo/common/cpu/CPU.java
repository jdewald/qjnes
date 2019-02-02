package org.fortytwo.common.cpu;


import org.fortytwo.common.memory.Memory;

public interface CPU
{
    int readRegister(int register);
    void writeRegister(int register, int value);
    void setMemory(Memory java);
    void registerCycleObserver(CycleObserver observer);
    void setBreak(int address);

    void handleInterrupt();
    
    // are these actually needed... ?
    boolean getCarryFlag();
    void setCarryFlag(boolean carry);
    boolean getSignFlag();
    void setSignFlag(boolean sign);
    boolean getOverflowFlag();
    void setOverflowFlag(boolean overflow);
    boolean getDecimalFlag();
    void setDecimalFlag(boolean decimal);
    boolean getZeroFlag();
    void setZeroFlag(boolean zero);
    void setInterruptsDisabled(boolean disabled);
    boolean getInterruptsDisabled();
    void setBreakFlag(boolean breakFlag);
    boolean getBreakFlag();

    void pushStack(byte val);
    int popStack();

    void handleNMI(int cycleOffset);
    void restart();

    // most important!
    void run();
}

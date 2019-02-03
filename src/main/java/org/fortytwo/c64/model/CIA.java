package org.fortytwo.c64.model;

import org.fortytwo.common.cpu.CycleObserver;
import org.fortytwo.common.cpu.CPU;
import org.fortytwo.common.memory.MemoryHandler;

import java.util.logging.Logger;
import java.util.logging.Level;

import org.fortytwo.common.io.IODevice;

public class CIA implements MemoryHandler, CycleObserver {
    private Logger logger;
    private int[] registers_r; // read
    private int[] registers_w; // write

    public static final int NUM_REGISTERS = 0x10;//16 - repeats

    public static final int PORT_A = 0; /* (CIA 2) VIC bank (bits 0 and 1)
                                           0 = $C000 - $FFFF
                                           1 = $8000 - $BFFF
                                           2 = $4000 - $7FFF
                                           3 = $0000 - $3FFF
                                        */
    public static final int PORT_B = 1; // key pressed, port 1 joystick
    public static final int PORT_A_DIRECTION = 2; // 0 in bit means read, 1 means read/write
    public static final int PORT_B_DIRECTION = 3;

    public static final int TIMER_A = 4; // read = current, write = set (2 bytes)
    public static final int TIMER_A_HIGH = 5;
    public static final int TIMER_B = 6; // ditto (2 bytes)
    public static final int TIMER_B_HIGH = 7;

    public static final int TOD_TENTH_SECONDS = 8; // 1/10s of second (read), set alarm (write)
    public static final int TOD_SECONDS = 9;
    public static final int TOD_MINUTES = 0xA;
    public static final int TOD_HOURS = 0xB;

    public static final int SERIAL_SHIFT = 0xC;

    public static final int INTERRUPT_CONTROL = 0xD; // bit 7 = interrupt generated

    public static final int TIMER_A_CONTROL = 0xE;
    public static final int TIMER_B_CONTROL = 0xF;

    // TIMER A CONTROL BITS
    public static final int TIMER_A_STARTSTOP = 0x01;
    public static final int TIMER_A_INDICATEUNDERFLOW = 0x02;
    public static final int TIMER_A_INVERT6 = 0x4;
    public static final int TIMER_A_UNDERFLOW_STARTSTOP = 0x8;
    public static final int TIMER_A_LOAD_START = 0x10;
    public static final int TIMER_A_COUNT_BEHAVIOR = 0x20; // 0 = count system cycles, 1 = positive edges on CNT pin
    public static final int TIMER_A_SERIAL_DIRECTION = 0x40; // 0 = input, 1 = output
    public static final int TIMER_A_TOD_SPEED = 0x80; // 0 = 60hz, 1 = 50hz

    // TIMER B CONTROL BITS
    public static final int TIMER_B_STARTSTOP = 0x01;
    public static final int TIMER_B_INDICATEUNDERFLOW = 0x02;
    public static final int TIMER_B_INVERT6 = 0x4;
    public static final int TIMER_B_UNDERFLOW_STARTSTOP = 0x8;
    public static final int TIMER_B_LOAD_START = 0x10;
    public static final int TIMER_B_COUNT_BEHAVIOR = 0x60; // 0 = count system cycles, 1 = positive edges on CNT pin,2=timerA to 0,3 = timer A 0's when CNT pulses are present
    public static final int TIMER_B_TOD_ALARM_TOD = 0x80; // 0 = clock , 1 = tod


    // INTERRUPT CONTROL BITS
    public static final int TIMER_A_UNDERFLOW_INTERRUPTS = 0x01;
    public static final int TIMER_B_UNDERFLOW_INTERRUPTS = 0x02;
    public static final int TOD_INTERRUPTS = 0x04;
    public static final int INTERRUPT_OCCURRED = 0x80; /* writing to this bit will cause other bits
                                                          in this byte to be set. Setting to 0
                                                          will cause them to be cleared */
    private boolean timerAEnabled = false;
    private boolean timerBEnabled = false;

    private int timerACounter = 0;
    private int timerBCounter = 0;

    private boolean timerAInterruptsEnabled = false;
    private boolean timerBInterruptsEnabled = false;
    private boolean todInterruptsEnabled = false;

    private boolean timerARestart = true;
    private boolean timerBRestart = true;

    private String name;

    private IODevice keyboard = null;
    private IODevice joystick1 = null;

    public CIA(String name) {
        registers_r = new int[NUM_REGISTERS];
        registers_w = new int[NUM_REGISTERS];
        logger = Logger.getLogger(this.getClass().getName());
        this.name = name;
        registers_w[PORT_A] = 0xFF; // actually, we can just return the inverse of the bits that are set

    }

    public CIA(String name, Logger logger) {
        this(name);
        this.logger = logger;
    }

    public void disableLogging() {
    }

    public void enableLogging() {
    }

    public void setKeyboard(IODevice keyboard) {
        this.keyboard = keyboard;
    }

    public void setJoystick1(IODevice joystick) {
        this.joystick1 = joystick;
    }

    /*    public void setIODevice1(IODevice device){
        this.device1 = device;
    }
    /*
    public void setIODevice2(IODevice device){
        this.device2 = device;
        }
    */
    /* this is really for the VIC for now... */
    public int readDirect(int address) {
        return registers_w[address % NUM_REGISTERS];
    }

    public int read(int address) {
        if (logger.isLoggable(Level.FINER))
            System.out.println(name + " Reading from: " + Integer.toHexString(address));

        address = address % NUM_REGISTERS;
        switch (address) {
            case PORT_A:
                //	    System.out.println(name + "Reading from PORT A");
                // TODO: another way to "wire" up these ports


                if ("CIA2".equals(name)) {
                    //System.out.println("CIA2 PORT_A direction:" + Integer.toHexString(registers_w[PORT_A_DIRECTION]));
                    int direction = registers_w[PORT_A_DIRECTION];
                
                
                /*                if (device1 != null){
                    return device1.read() | (registers_w[PORT_A] & 0x3);
                    //return (device1.read() & 0xFC) | (registers_w[PORT_A] & 0x3);
                    //return ((registers_w[PORT_A] & direction) | (device1.read() & ~direction));
                    //return (0xFC & device1.read()) | (registers_w[PORT_A] & 0x3); // VIC is in first 2 bits
                }
                else {
                */
                    //return direction & registers_w[PORT_A];
                    return registers_w[PORT_A];
                    //                return (registers_w[address] & 0x3);
                    //}
                } else if ("CIA1".equals(name)) {
                    return 0xFF;

                    //                if (device2 != null && registers_w[PORT_A_DIRECTION] == 0x00){
                    //                    return device2.read();
                    //                }

                }
                //return registers_w[PORT_A];


            case PORT_A_DIRECTION:
                return registers_w[PORT_A_DIRECTION];
            case PORT_B_DIRECTION:
                return registers_w[PORT_B_DIRECTION];
            case PORT_B:

                if (keyboard != null || joystick1 != null) {
                    int returnVal = 0xFF;
                    //                System.out.println(name + "Going to read from device...");
                    if (keyboard != null) {
                        returnVal &= keyboard.read();
                    }
                    if (joystick1 != null) {
                        returnVal &= joystick1.read();
                    }
                    return returnVal;
                } else {
                    return registers_r[PORT_B];
                }
            case TIMER_A:
                //            System.out.println(name + ":Reading timer A " + timerACounter);
                return timerACounter & 0xFF;
            case TIMER_A + 1:
                return (timerACounter & 0xFF00) >> 8;
            case TIMER_B:
                System.out.println(name + ":Reading timer B" + timerBCounter);
                return timerBCounter & 0xFF;
            case TIMER_B + 1:
                return (timerBCounter & 0xFF00) >> 8;
            case INTERRUPT_CONTROL: // reading this clears it
                int returnVal = registers_r[address];
                registers_r[address] = 0;
                return returnVal;
            case TIMER_A_CONTROL:
            case TIMER_B_CONTROL:
                return registers_w[address];
            case TOD_TENTH_SECONDS:
            case TOD_SECONDS:
            case TOD_MINUTES:
            case TOD_HOURS:
                System.out.println("Reading TOD");
                return registers_w[address];
            default:
                throw new RuntimeException(name + ":Not sure what happens when " + Integer.toHexString(address) + " is read");
        }
    }

    public void write(int address, int value) {
        //        System.out.println("[--------------" + name + "-----------------]");
        //	if (logger.isLoggable(Level.FINE))
        //        System.out.println(name + ": Writing to: " + Integer.toHexString(address) + "," + Integer.toHexString(value & 0xFF));
        address = (address) % NUM_REGISTERS;

        switch (address) {
            case PORT_A: // should actually be checking the direction...
            {
                // the low 2 bits of CIA2 here are stored directly
                // but we'll store the whole thing and write the
                // whole thing
                registers_w[PORT_A] = value;
                //registers_r[address] = value & 0x3;
                if ("CIA2".equals(name)) {
                    int direction = registers_w[PORT_A_DIRECTION];
                    //		    System.out.println("CIA2: Writing " + Integer.toHexString(value));
                    // confused... so this is from http://e32frodo.sourceforge.net/doxygen/html/CIA_8cpp-source.html
                    value = (registers_w[PORT_A] | ~direction);
                    //		    System.out.println("CIA2: Value with Direction " + Integer.toHexString(value));
                    //		    if (device1 != null){
                    //device1.write(value); // let's us keep the input values
                    //}
                } else if (keyboard != null) {
                    keyboard.write((value & registers_w[PORT_A_DIRECTION]));
                    //                    System.out.println(name + ": Writing to device: " + Integer.toHexString(value));
                }

                return;

            }
            case PORT_B: {
                registers_w[address] = value;
                return;
            }
            case PORT_A_DIRECTION: {
                registers_w[address] = value;
                /*                if ("CIA2".equals(name)){
                    if (device1 != null){
                        device1.write(registers_w[PORT_A] | ~value);
                    }
                }
                */
                //		System.out.println(name + ":Set direction of Port A to: " + Integer.toHexString(value));
                return;
            }
            case PORT_B_DIRECTION: {
                registers_w[address] = value;
                //System.out.println("Set direction of Port B to: " + Integer.toHexString(value));
                return;
            }
            case TOD_TENTH_SECONDS: {
                registers_w[address] = value;
                System.out.println("Set tenth seconds to: " + (value & 0xF));
                return;
            }
            case TOD_SECONDS: {
                registers_w[address] = value;
                System.out.println("Set seconds to: " + ((0x70 & value) >> 4) + (0xF & value));
                return;
            }
            case TOD_MINUTES: {
                registers_w[address] = value;
                System.out.println("Set minutes to: " + ((0x70 & value) >> 4) + (0xF & value));
                return;
            }
            case TOD_HOURS: {
                registers_w[address] = value;
                System.out.println("Set hours to: " + ((0x01 & value) >> 4) + (0xF & value));

                return;
            }
            case SERIAL_SHIFT: {
                registers_w[address] = value;
                return;
            }
            case INTERRUPT_CONTROL: // 0D
            {
                System.out.println(name + ": Modifying Interrupt Values");
                registers_w[address] = value;

                boolean shouldClear = ((value & INTERRUPT_OCCURRED) == 0);
                if ((value & TIMER_A_UNDERFLOW_INTERRUPTS) != 0) {
                    timerAInterruptsEnabled = (!shouldClear);
                    if (shouldClear) {
                        registers_w[address] &= (~TIMER_A_UNDERFLOW_INTERRUPTS);
                    }
                }
                if ((value & TIMER_B_UNDERFLOW_INTERRUPTS) != 0) {
                    timerBInterruptsEnabled = (!shouldClear);
                    if (shouldClear) {
                        registers_w[address] &= (~TIMER_B_UNDERFLOW_INTERRUPTS);
                    }
                }
                /*              if ((value & TOD_INTERRUPTS) != 0){
                    todInterruptsEnabled = (! shouldClear);
                    if (shouldClear){
                        registers_w[address] &= (~TOD_INTERRUPTS);
                    }
                }
                */
            }
            break;
            case TIMER_A:
            case TIMER_A_HIGH:
            case TIMER_B:
            case TIMER_B_HIGH: {
                registers_w[address] = value;
                break;
            }
            case TIMER_A_CONTROL: // 0E
            {
                registers_w[address] = value;
                System.out.println(name + ": TIMER A Controls: " + Integer.toHexString(value));
                timerAEnabled = (value & TIMER_A_STARTSTOP) != 0;

                if ((value & TIMER_A_LOAD_START) != 0) {
                    timerACounter = (0xFF & registers_w[TIMER_A]) | ((0xFF & registers_w[TIMER_A + 1]) << 8);
                    System.out.println(name + ":Timer A: " + timerACounter);

                }
                timerARestart = (value & TIMER_A_UNDERFLOW_STARTSTOP) == 0;

                if ((value & TIMER_A_COUNT_BEHAVIOR) != 0) {
                    //                    throw new RuntimeException("Don't know how to deal with CNT behavior");
                }
                System.out.println(name + ": Timer A Restarts: " + timerARestart);
                break;

            }
            case TIMER_B_CONTROL: // 0F
            {
                registers_w[address] = value;
                System.out.println(name + ": TIMER B Controls " + Integer.toHexString(value));
                timerBEnabled = (value & TIMER_B_STARTSTOP) != 0;

                if ((value & TIMER_B_LOAD_START) != 0) {
                    timerBCounter = (0xFF & registers_w[TIMER_B]) | ((0xFF & registers_w[TIMER_B + 1]) << 8);

                }
                timerBRestart = (value & TIMER_B_UNDERFLOW_STARTSTOP) == 0;

                if (name.equals("CIA2")) {
                    System.out.println((registers_w[TIMER_B_CONTROL] & TIMER_B_COUNT_BEHAVIOR) >> 5);
                }

                System.out.println(name + ": Timer B Restarts: " + timerBRestart);

            }
            break;
            default:
                throw new RuntimeException(name + ":Don't know how to handle write to: " + address);
        }
        
        /*
        System.out.println(name + ": Timer A: " + timerACounter);
        System.out.println(name + ": TIMER B: " + timerBCounter);
        System.out.println(name + ":Timer A enabled: " + timerAEnabled);
        System.out.println(name + ":Timer B enabled: " + timerBEnabled);
        System.out.println(name + ": Timer A Interrupts Enabled: " + timerAInterruptsEnabled);
        System.out.println(name +": Timer B Interrupts Enabled: " + timerBInterruptsEnabled);
        */
        //        System.out.println("[-------------------------------------------]");
    }

    public boolean ready(int cycles) {
        boolean ready = false;
        if (timerAEnabled && timerACounter > 0) {
            ready = (timerACounter <= cycles);
        }

        if (timerBEnabled && timerBCounter > 0) {
            if (!ready) {
                ready = (timerBCounter <= cycles);
            }
        }

        return ready;
    }

    public int tick(int cycles, CPU cpu) {
        if (timerAEnabled || timerBEnabled) {
            boolean expired = false;
            if (timerAEnabled && timerACounter > 0) { // it could be enabled but not actually running
                timerACounter -= cycles;
                if (timerACounter <= 0) {
                    if (name.equals("CIA2")) {
                        System.out.println((registers_w[TIMER_B_CONTROL] & TIMER_B_COUNT_BEHAVIOR) >> 5);
                        if (((registers_w[TIMER_B_CONTROL] & TIMER_B_COUNT_BEHAVIOR) >> 5) == 2) {
                            System.out.println("underflow!");
                            if (timerBEnabled && timerBCounter > 0) {
                                timerBCounter--;
                            }
                        }
                    }
                    int overflow = -timerACounter;
                    //		    System.out.println(name + ": TimerA has expired!(" + timerACounter + ")");
                    registers_r[INTERRUPT_CONTROL] |= TIMER_A_UNDERFLOW_INTERRUPTS;
                    expired = true;
                    timerACounter = (0xFF & registers_w[TIMER_A]) | ((0xFF & registers_w[TIMER_A + 1]) << 8);
                    if (!timerARestart) {
                        timerAEnabled = false;
                    } else {
                        timerACounter -= overflow;
                    }
                }
                //	    System.out.println("Timer A: " + timerACounter);    
            }

            if (timerBEnabled && timerBCounter > 0) {
                if (name.equals("CIA2")) {
                    if (((registers_w[TIMER_B_CONTROL] & TIMER_B_COUNT_BEHAVIOR) >> 5) == 0) {
                        timerBCounter -= cycles;
                    }
                }
                if (timerBCounter <= 0) {
                    int overflow = 0 - timerBCounter;
                    //		    System.out.println(name + ": TimerB has expired!" + timerBCounter);
                    registers_r[INTERRUPT_CONTROL] |= TIMER_B_UNDERFLOW_INTERRUPTS;
                    expired = true;
                    timerBCounter = (0xFF & registers_w[TIMER_B]) | ((0xFF & registers_w[TIMER_B + 1]) << 8);
                    if (timerBRestart) {
                        timerBCounter -= overflow;
                    } else {
                        timerBEnabled = false;
                    }
                }
            }

            if (expired && (timerAInterruptsEnabled || timerBInterruptsEnabled)) {
                registers_r[INTERRUPT_CONTROL] |= INTERRUPT_OCCURRED;

                cpu.handleInterrupt();
            }

            if (timerACounter > 0 && timerBCounter > 0) {
                return timerACounter < timerBCounter ? timerACounter : timerBCounter; // even if it's not actually going to fire
            } else if (timerACounter > 0) {
                return timerACounter;
            } else if (timerBCounter > 0) {
                return timerBCounter;
            }
        }
        return 100; // figure that the timer will be set long enough that we can go for a bit
    }

    public IODevice getKeyboard() {
        return keyboard;
    }

    public IODevice getJoystick1() {
        return joystick1;
    }
}

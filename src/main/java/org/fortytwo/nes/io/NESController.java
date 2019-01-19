package org.fortytwo.nes.io;

import org.fortytwo.common.io.IODevice;

import java.util.HashSet;
import java.util.Set;

public abstract class NESController implements IODevice {
    public static final int DPAD_RIGHT = 8;
    public static final int DPAD_LEFT = 7;
    public static final int DPAD_DOWN = 6;
    public static final int DPAD_UP = 5;
    public static final int START = 4;
    public static final int SELECT = 3;
    public static final int BUTTON_B = 2;
    public static final int BUTTON_A = 1;
    public static final int NO_BUTTON = 0;

    protected int scanPosition = 0;
    private int button = NO_BUTTON;

    protected Set<Integer> pressed = new HashSet<Integer>();
    private int lastWrite = -1;
    private static final int BUTTON_PRESSED = 0x01;
    private static final int UNKNOWN = 0x40;
    public NESController(){
    }

    public void write(int val){
        if ((lastWrite & BUTTON_PRESSED) != 0 && (val & BUTTON_PRESSED) == 0){
            scanPosition = 1;
        }
        lastWrite = val;
    }

    public int read(){

        int returnVal = UNKNOWN;
        if (pressed.contains(scanPosition)){
            //== button){
            //            System.out.println("Button = " + button);

            returnVal =  UNKNOWN | BUTTON_PRESSED;
        }

        if ((lastWrite & BUTTON_PRESSED)== 0){
            scanPosition++;
        }
        return returnVal;
    }
}

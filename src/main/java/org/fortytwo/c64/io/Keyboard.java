package org.fortytwo.c64.io;

import org.fortytwo.common.cpu.CPU;
import org.fortytwo.common.io.IODevice;

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

import java.util.LinkedList;
import java.util.Queue;

import static java.awt.event.KeyEvent.*;

/**
 * Yeah, it's a keyboard
 */
public class Keyboard implements IODevice, KeyListener {
    //    Scanner input;

    Queue<KeyBean> keyQueue;

    /*    String keyColumn[] = new String[] {"-\n<7135d",
          "3WA4ZSEs",
          "5RD6CFTX",
          "7YG8BHUV",
          "9IJ0MKON",
          "+PL-.;@,",
          "pa:hs=u/",
          "1<c2 cQr"};
    */
    static final int NUM_COLUMN = 8;
    static final int NUM_ROW = 8;
    int keyColumn[] = new int[]{VK_INSERT, VK_ENTER, VK_RIGHT, VK_F7, VK_F1, VK_F3, VK_F5, VK_DOWN,
            VK_3, VK_W, VK_A, VK_4, VK_Z, VK_S, VK_E, VK_SHIFT,
            VK_5, VK_R, VK_D, VK_6, VK_C, VK_F, VK_T, VK_X,
            VK_7, VK_Y, VK_G, VK_8, VK_B, VK_H, VK_U, VK_V,
            VK_9, VK_I, VK_J, VK_0, VK_M, VK_K, VK_O, VK_N,
            VK_PLUS, VK_P, VK_L, VK_MINUS, VK_PERIOD, VK_COLON, VK_AT, VK_COMMA,
            CHAR_UNDEFINED, VK_ASTERISK, VK_SEMICOLON, VK_HOME, VK_CAPS_LOCK, VK_EQUALS, VK_UP, VK_SLASH,
            VK_1, VK_LEFT, VK_CONTROL, VK_2, VK_SPACE, VK_ALT, VK_Q, VK_PAUSE};

    // the usage of this stuff is actually confusing...needs to be more unified
    int shiftedKeyColumn[] = new int[]{VK_INSERT, VK_ENTER, VK_LEFT, VK_F7, VK_F1, VK_F3, VK_F5, VK_UP,
            VK_NUMBER_SIGN, VK_W, VK_A, VK_4, VK_Z, VK_S, VK_E, VK_SHIFT,
            VK_5, VK_R, VK_D, VK_6, VK_C, VK_F, VK_T, VK_X,
            VK_7, VK_Y, VK_G, VK_8, VK_B, VK_H, VK_U, VK_V,
            VK_9, VK_I, VK_J, VK_0, VK_M, VK_K, VK_O, VK_N,
            VK_PLUS, VK_P, VK_L, VK_MINUS, VK_PERIOD, VK_COLON, VK_AT, VK_COMMA,
            CHAR_UNDEFINED, VK_ASTERISK, VK_SEMICOLON, VK_HOME, VK_CAPS_LOCK, VK_EQUALS, VK_UP, VK_SLASH,
            VK_EXCLAMATION_MARK, VK_LEFT, VK_CONTROL, VK_QUOTE /* for double quote */, VK_SPACE, VK_ALT, VK_Q, VK_TAB};
    static final int SHIFT_ROW = 1;
    static final int COMMODORE_ROW = 7;
    static final int SHIFT_CODE = 128;
    static final int COMMODORE_CODE = 32;

    public static final int NO_KEYPRESS = 0xFF;


    public static final int ROW_0 = 1;
    public static final int ROW_1 = 2;
    public static final int ROW_2 = 4;
    public static final int ROW_3 = 8;
    public static final int ROW_4 = 16;
    public static final int ROW_5 = 32;
    public static final int ROW_6 = 64;
    public static final int ROW_7 = 128;

    public static final int ALL_ROWS = 0xFF;
    public static final int NO_ROWS = 0x00;
    private int row = 0;
    private boolean startEnterLoop = false;
    private int readCount = 0;
    private KeyBean enterKey = null;

    private CPU cpu;

    public Keyboard() {
        //	input = new Scanner(System.in);
        this(null);
    }

    public Keyboard(CPU cpu) {
        this.cpu = cpu;
        keyQueue = new LinkedList<KeyBean>();
    }

    /**
     * Called when the C64 wants to poll for a keypress and to select
     * which column to scan
     */
    public void write(int rowSelector) {
        //        System.out.println("Keyboard: wrote " + rowSelector);
        int selector = (~rowSelector) & 0xFF;
        switch (selector) {
            case NO_ROWS: {
                nextKey = null;
                row = -1;
                break;
            }
            case ROW_0:
                row = 0;
                break;
            case ROW_1:
                row = 1;
                break;
            case ROW_2:
                row = 2;
                break;
            case ROW_3:
                row = 3;
                break;
            case ROW_4:
                row = 4;
                break;
            case ROW_5:
                row = 5;
                break;
            case ROW_6:
                row = 6;
                break;
            case ROW_7:
                row = 7;
                break;
            case ALL_ROWS: {
                nextKey = null;
                row = ALL_ROWS;
                if (keyQueue.peek() != null) {
                    nextKey = keyQueue.remove();
                }
                break;
            }
        }
    }

    //    int nextChar = -1;
    KeyBean nextKey = null;
    int charColumn = -1;

    boolean inShiftMode = false;
    boolean inCommodoreMode = false;

    public int read() {
        int keyVal = NO_KEYPRESS;
        //        if (row == NO_ROWS){
        //    
        //}
        if (row == ALL_ROWS) {
            if (nextKey != null) {
                keyVal = 0;
            } else {
                keyVal = NO_KEYPRESS;
            }
        } else if (nextKey != null) {
            keyVal = NO_KEYPRESS;
            if (nextKey.shifted && row == SHIFT_ROW) {
                keyVal &= (~SHIFT_CODE);
                System.out.println("Shifted = true");
            }
            if (nextKey.commodore && row == COMMODORE_ROW) {
                keyVal &= (~COMMODORE_CODE);
            }
            //	    System.out.println("KEYBOARD:next col = " + nextKey.col + " col = " + column);
            if (nextKey.row == row) {
                //            System.out.println("KEYBOARD:original code = " + Integer.toHexString(nextKey.keyCode));
                keyVal &= (~nextKey.keyCode);
            }
            //            System.out.println("KEYBOARD: row = " + row + " output keyval = " + Integer.toHexString(keyVal));        

        }

        return keyVal;

    }


    private int beingPressed = 0;
    long pressTime = -1;

    // KeyListner
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == VK_PAUSE && cpu != null) {
            cpu.handleNMI(0);
            return;
        }
        if (code != VK_LEFT && code != VK_RIGHT && code != VK_UP && code != VK_DOWN && code != VK_CONTROL) {
            //        System.out.println("Code = " + code);
            beingPressed = code;
            pressTime = System.currentTimeMillis();
            if (code == KeyEvent.VK_SHIFT) {
                inShiftMode = true;
            } else if (code == KeyEvent.VK_ALT) {
                inCommodoreMode = true;
            } else {
                boolean found = false;
                int col = 0;
                int row = 0;
                //	System.out.println("Typed: " + c);
                boolean c64ShiftMode = inShiftMode;
                //        boolean c64CommodoreMode = false;
                for (col = 0; col < NUM_COLUMN && !found; col++) {
                    for (row = 0; row < NUM_ROW && !found; row++) {
                        //                System.out.println("Code: " + code + " keyColumn = " + keyColumn[(col * NUM_ROW) + row]);
                        //System.out.println("Code: " + code + " shiftedKeyColumn = " + shiftedKeyColumn[(col * NUM_ROW) + row]);

                        // hack?
                        if (inShiftMode && code == VK_EQUALS) {
                            code = VK_PLUS;
                        }
                        ;
                        if (inShiftMode && shiftedKeyColumn[(row * NUM_COLUMN) + col] == code) {
                            found = true;
                            c64ShiftMode = true;
                        } else if (keyColumn[(row * NUM_COLUMN) + col] == code) {
                            found = true;
                            c64ShiftMode = false;
                            //          if (code == VK_F1) {c64CommodoreMode = true; }
                        }
                        if (found) {
                            System.out.println("Col = " + col + " row = " + row);
                            KeyBean bean = new KeyBean(row, 1 << col, (char) code, c64ShiftMode, inCommodoreMode);
                            keyQueue.offer(bean);

                        }

                    }
                }

                if (!found) {
                    throw new RuntimeException("What did you hit? shifted = " + inShiftMode + " code = " + code);
                }
            }
        }
    }

    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        beingPressed = 0;
        pressTime = -1;
        if (code == KeyEvent.VK_SHIFT) {
            inShiftMode = false;
        } else if (code == KeyEvent.VK_ALT) {
            inCommodoreMode = false;
        }
    }

    public void keyTyped(KeyEvent e) {
        /*
          int c = e.getKeyChar();
          int code = e.getKeyCode();
          boolean found = false;
          int col = 0;
          int row = 0;
          //	System.out.println("Typed: " + c);
          for ( ; col < NUM_COLUMN; col++){
          for ( ; row < NUM_ROW; row++){
          if (! inShiftMode){
          if (keyColumn[(col * NUM_ROW) + row] == code){
          found = true;
          break;
          }
			
          }
          else {
          if (shiftedKeyColumn[(col * NUM_ROW) + row] == code){
          found = true;
          break;
          }
          }
          }
          }

          if (found){
          KeyBean bean = new KeyBean(col, 1 << row, (char)c,inShiftMode);
          keyQueue.offer(bean);
          }
          else {
          throw new RuntimeException("What did you type: shifted = " + inShiftMode + " char = " + (char)c );
          }
        */

    }

    public static void main(String[] args) {
        try {
            Keyboard keyboard = new Keyboard();

            while (true) {
                int val = keyboard.read();
                if (val != NO_KEYPRESS) {
                    System.out.println("key = " + val);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Runtime.getRuntime().exit(0);
        }
    }

    private class KeyBean {
        public int row;
        public int keyCode; //what we're going to send on a read()
        public char c;
        public boolean shifted;
        public boolean commodore;

        KeyBean(int row, int keyCode, char c, boolean shifted, boolean commodore) {
            this.row = row;
            this.keyCode = keyCode;
            this.c = c;
            this.shifted = shifted;
            this.commodore = commodore;
        }
    }
}

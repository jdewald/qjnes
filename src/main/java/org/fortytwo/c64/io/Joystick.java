package org.fortytwo.c64.io;

import org.fortytwo.common.io.IODevice;

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

import java.util.LinkedList;
import java.util.Queue;

import static java.awt.event.KeyEvent.*;
/**
 * Yeah, it's a keyboard
 */
public class Joystick implements IODevice,KeyListener
{
    //    Scanner input;

    Queue<KeyBean> keyQueue;
    public Joystick(){
	//	input = new Scanner(System.in);
        keyQueue = new LinkedList<KeyBean>();
    }

    
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
    int keyColumn[] = new int[] {VK_INSERT,VK_ENTER,VK_RIGHT,VK_F7,VK_F1,VK_F3,VK_F5,VK_DOWN,
                                 VK_3,VK_W,VK_A,VK_4,VK_Z,VK_S,VK_E,VK_SHIFT,
                                 VK_5,VK_R,VK_D,VK_6,VK_C,VK_F,VK_T,VK_X,
                                 VK_7,VK_Y,VK_G,VK_8,VK_B,VK_H,VK_U,VK_V,
                                 VK_9,VK_I,VK_J,VK_0,VK_M,VK_K,VK_O,VK_N,
                                 VK_PLUS,VK_P,VK_L,VK_MINUS,VK_PERIOD,VK_COLON,VK_AT,VK_COMMA,
                                 CHAR_UNDEFINED,VK_ASTERISK,VK_SEMICOLON,VK_HOME,VK_CAPS_LOCK,VK_EQUALS,VK_UP,VK_SLASH,
                                 VK_1,VK_LEFT,VK_CONTROL,VK_2,VK_SPACE,VK_ALT,VK_Q,VK_TAB};
    
   int shiftedKeyColumn[] = new int[] {VK_INSERT,VK_ENTER,VK_LEFT,VK_F7,VK_F1,VK_F3,VK_F5,VK_UP,
                                       VK_NUMBER_SIGN,VK_W,VK_A,VK_4,VK_Z,VK_S,VK_E,VK_SHIFT,
                                       VK_5,VK_R,VK_D,VK_6,VK_C,VK_F,VK_T,VK_X,
                                       VK_7,VK_Y,VK_G,VK_8,VK_B,VK_H,VK_U,VK_V,
                                       VK_9,VK_I,VK_J,VK_0,VK_M,VK_K,VK_O,VK_N,
                                       VK_PLUS,VK_P,VK_L,VK_MINUS,VK_PERIOD,VK_COLON,VK_AT,VK_COMMA,
                                       CHAR_UNDEFINED,VK_ASTERISK,VK_SEMICOLON,VK_HOME,VK_CAPS_LOCK,VK_EQUALS,VK_UP,VK_SLASH,
                                       VK_EXCLAMATION_MARK,VK_LEFT,VK_CONTROL,VK_QUOTE /* for double quote */,VK_SPACE,VK_ALT,VK_Q,VK_TAB};
    static final int SHIFT_COLUMN = 1;
    static final int COMMODORE_COLUMN = 7;
    static final int SHIFT_CODE = 128;
    static final int COMMODORE_CODE = 32;

    public static final int NO_KEYPRESS = 0xFF;

    public static final int COLUMN_0 = 1;
    public static final int COLUMN_1 = 2;
    public static final int COLUMN_2 = 4;
    public static final int COLUMN_3 = 8;
    public static final int COLUMN_4 = 16;
    public static final int COLUMN_5 = 32;
    public static final int COLUMN_6 = 64;
    public static final int COLUMN_7 = 128;
    
    public static final int ALL_COLUMNS = 0xFF;
    private int column = 0;
    public void write(int columnSelector){
        // nop
    }
    
    
    //    int nextChar = -1;
    KeyBean nextKey = null;
    int charColumn = -1;

    boolean inShiftMode = false;
    boolean inCommodoreMode = false;
    public int read(){
        //System.out.println("JOYSTICK: read");
        //        return ~4; // should be left

        if (nextKey == null && keyQueue.peek() != null){
            nextKey = keyQueue.remove();
        }
        if (nextKey != null ){
            //  System.out.println("JOYSTICK: returning " + nextKey.value);
            int value = nextKey.value;
            nextKey = null;
            //            System.out.println("Returning " + Integer.toHexString(~value));
            return ~value;
        }
        else {
            return 0xFF;
        }
        
	}

    // KeyListner
    public void keyPressed(KeyEvent e){
        int code = e.getKeyCode();
        //        System.out.println("JOYSTICK: recevied: " + code);
        KeyBean bean = null;
        switch (code){
        case VK_LEFT: { // left
            bean = new KeyBean(code,4);
            break;
        }
        case VK_RIGHT: { // right
            bean = new KeyBean(code,8);
            break;
        }
        case VK_UP: { // up
            bean = new KeyBean(code,1);
            break;
        }
        case VK_DOWN: { // down
            bean = new KeyBean(code,2);
            break;
        }
        case VK_Q: { // up left
            bean = new KeyBean(code,5);
            break;
        }
        case VK_E: { // up right
            bean = new KeyBean(code,9);
            break;
        }
            /*
        case VK_C: { // down right
            bean = new KeyBean(code,10);
            break;
        }
        case VK_Z: { // down left
            bean = new KeyBean(code, 6);
            break;
        }
            */
        case VK_CONTROL: {
            bean = new KeyBean(code, 16);
            break;
        }
            
        }
        if (bean != null){
            keyQueue.offer(bean);
        }

    }

    public void keyReleased(KeyEvent e){
    }

    public void keyTyped(KeyEvent e){
    }
    
    public static void main(String[] args){
        try {
            Joystick keyboard = new Joystick();
            
            while (true){
                int val = keyboard.read();
                if (val != NO_KEYPRESS){
                    System.out.println("key = " + val);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            Runtime.getRuntime().exit(0);
        }
    }
    
    private class KeyBean {
        public int keyCode; 
        public int value; // what we're oing to send on a read()
        
        KeyBean(int keyCode, int value){
            this.keyCode = keyCode;
            this.value = value;
        }
    }
}

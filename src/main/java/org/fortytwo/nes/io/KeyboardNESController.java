package org.fortytwo.nes.io;

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.*;

public class KeyboardNESController extends NESController implements KeyListener {
    private int button = NO_BUTTON;


    public KeyboardNESController(){
        super();
    }

    public void keyPressed(KeyEvent e){


        int code = e.getKeyCode();
        
        int button = getNintendoKey(code);
        if (button != NO_BUTTON){
            pressed.add(button);
        }
    }

    public void keyReleased(KeyEvent e){
        int code = e.getKeyCode();
        
        int button = getNintendoKey(code);
        if (button != NO_BUTTON){
            pressed.remove(button);
        }
        //        button = NO_BUTTON;
    }

    public void keyTyped(KeyEvent e){
        // ignore
    }

    public int getNintendoKey(int code){
        int button = NO_BUTTON;
        switch (code){
            //        case VK_LEFT: { // left
        case VK_A:{
            button = DPAD_LEFT;
            break;
        }
            //        case VK_RIGHT: { // right
        case VK_D:{
            button = DPAD_RIGHT;
            break;
        }
            //        case VK_UP: { // up
        case VK_W: {
            button = DPAD_UP;
            break;
        }
            //        case VK_DOWN: { // down
        case VK_S: {
            button = DPAD_DOWN;
            break;
        }
        case VK_ENTER: { // 
            button = START;
            break;
        }
            //        case VK_SPACE: { //
        case VK_TAB:{
            button = SELECT;
            break;
        }
            //        case VK_A: { //
        case VK_SPACE:{
            button = BUTTON_A;
            break;
        }
            //        case VK_B: { // 
        case VK_SHIFT: {
            button = BUTTON_B;
            break;
        }
            
        }
        return button;
    }
}

package org.fortytwo.c64.io;

import org.lwjgl.input.Controllers;
import org.lwjgl.input.Controller;

import java.util.HashSet;
/**
 * Starts up a thread and notifies listeners as if keys were pressed
 */
public class GamepadController extends NESController implements IODevice
{
    Controller controller;

    public GamepadController(){
        //pressed = new HashSet<Integer>();
	
        initializeController();
    }

    
    public void write(int val){
        Controllers.clearEvents();
        controller.poll();
        super.write(val);

        if (scanPosition == 1){
            updatePressed();
        }
    }

    private void updatePressed(){
		try {
		    pressed.clear();

            //		    controller.poll();
/*			for (int b = 0; b < controller.getButtonCount(); b++) {
				if (controller.isButtonPressed( b )) {
					System.out.println("" + b + " is pressed");
				}
			}
			*/

			if (controller.getName().equals("Twin USB Joystick")) {
				if (controller.isButtonPressed(1) || controller.isButtonPressed(2)){ // 'A' - Circle
					pressed.add(BUTTON_A);
				}

				if (controller.isButtonPressed(3)){ // 'B' - square
					pressed.add(BUTTON_B);
				}

				if (controller.isButtonPressed(8)){ // select
					pressed.add(SELECT);
				}
				if (controller.isButtonPressed(9)){ // start
					pressed.add(START);
				}
				float povY = controller.getPovY();
				if (povY - 0.00001f > 0.0){ // down
					pressed.add(DPAD_DOWN);
				}
				else if (povY + 0.00001f < 0.0){ // up
					pressed.add(DPAD_UP);
				}

				float povX = controller.getPovX();
				if (povX - 0.00001f > 0.0) { // 
					pressed.add(DPAD_RIGHT);
				}
				else if (povX + 0.00001f < 0.0){
					pressed.add(DPAD_LEFT);
				}
			} else if (controller.getName().startsWith("Wiimote")) {
				if (controller.isButtonPressed(0)) {
					pressed.add(DPAD_DOWN);
				} else if (controller.isButtonPressed(1)) {
					pressed.add(DPAD_UP);
				} else if (controller.isButtonPressed(2)) {
					pressed.add(DPAD_LEFT);
				} else if (controller.isButtonPressed(3)) {
					pressed.add(DPAD_RIGHT);
				} 
				if (controller.isButtonPressed(10)) { // labelled '2'
					pressed.add(BUTTON_A);
				} 
				if (controller.isButtonPressed(9)) { // labelled '1'
					pressed.add(BUTTON_B);
				} 
				if (controller.isButtonPressed(4)) { // big 'A' 
					pressed.add(START);
				} 
				if (controller.isButtonPressed(5)) { // trigger 
					pressed.add(SELECT);
				}






			}
		    
            //		    pressed.clear();
            //		    pressed.addAll(newPressed);
            //		    newPressed.clear();
		}
		catch (Throwable t){
		    t.printStackTrace();
		}        
    }
    private void initializeController(){
	try {
	    Controllers.create();
	    
	    int n = Controllers.getControllerCount();
		System.out.println("Found " + n + " controllers");
	    
	    for (int i = 0; i < n; i++){
		Controller temp = Controllers.getController(i);
		
		if (temp.getName().equals("Twin USB Joystick")){
		    controller = Controllers.getController(i + 1); /* damn adapter returns them both as the same name */
		    break;
	    } else {
			System.out.println(temp.getName());
			controller = Controllers.getController(i);

			break;
		}
		}
	}
	catch (Throwable t){
	    t.printStackTrace();
	}
    }

    
}

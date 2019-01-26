package org.fortytwo.c64.io;

import org.fortytwo.common.io.IODevice;

/**
 * Basically a LOAD only version that doesn't
 * actually handle most of what the 1541 does
 */
public class Simple1541 implements IODevice
{
    private D64File disk = null;
    public static final int DIRECTORY_TRACK_NUMBER = 18;
    
    private int lastVal = 0;
    /** pins **/
    public static final int BIT_INPUT = 0x80;
    public static final int BIT_CLOCK_IN = 0x40;
    public static final int BIT_OUTPUT = 0x20;
    public static final int BIT_CLOCK_OUT = 0x10;
    public static final int BIT_ATN_OUT = 0x08;

    public static final int TALK = 0x40;
    public static final int LISTEN = 0x20;
    public static final int DATA = 0x60;
    public static final int OPEN = 0xf0;
    public static final int CLOSE = 0xe0;

    private int returnVal;
    private int counter;

    private int ATN = 0;
    private int CLK_IN = 0;
    private int IN = 0;
    private int OUT = 0;
    private int CLK_OUT = 0;

    private boolean listenMode = false;

    public Simple1541(){
	returnVal = 0;
	counter = 0;
    }

    public void loadDisk(D64File diskFile){
	this.disk = diskFile;
    }

    //** IODevice
    public void write(int val){
	System.out.println("1541: write: " + Integer.toHexString(val));
	System.out.println("BIT: " + ((val & BIT_OUTPUT) >> 5));
	int newCLK =  ((val & BIT_CLOCK_OUT) >> 4);
	System.out.println("CLOCK: " + newCLK);
	
	int newATN = ((val & BIT_ATN_OUT) >> 3);
	System.out.println("ATN: " + newATN);
	if (newATN != ATN){
	    System.out.println("LISTEN");
	    ATN = newATN;
	    listenMode = true;
	}
	if (newCLK != CLK_IN){
	    System.out.println("CLK Changed");
	    CLK_IN = newCLK;
	}

	lastVal = val;
    }

    public int read(){
	//	System.out.println("1541: read");
	//	return lastVal;
	//	return 0x3 & lastVal;
	if (listenMode){
	    return 0x40;
	}
	else {
	    counter++;
	    if (counter == 8){
		//	    System.out.println("Counter is 0, return 0");
		counter = 0;
		return 0;
	    }
	    else {
		return 0x40 | (0x80 & (returnVal << counter));
	    }
	}
    }
    
}

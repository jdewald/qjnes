package org.fortytwo.c64.cpu;

public class InstructionException extends RuntimeException{
    public InstructionException(){
	super();
    }
    public InstructionException(String reason){
	super(reason);
    }
}

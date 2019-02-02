package org.fortytwo.common.cpu;

public class InstructionException extends RuntimeException{
    public InstructionException(){
	super();
    }
    public InstructionException(String reason){
	super(reason);
    }
}

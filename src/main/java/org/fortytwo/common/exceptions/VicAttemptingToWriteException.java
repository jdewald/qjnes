package org.fortytwo.common.exceptions;

public class VicAttemptingToWriteException extends RuntimeException {

    public VicAttemptingToWriteException(){
        super("VIC attempted to write!");
    }
}

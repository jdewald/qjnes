package org.fortytwo.common.exceptions;

public class InstructionException extends RuntimeException {

    public InstructionException() {
        super();
    }

    public InstructionException(String reason) {
        super(reason);
    }
}

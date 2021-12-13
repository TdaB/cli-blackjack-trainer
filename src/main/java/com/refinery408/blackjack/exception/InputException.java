package com.refinery408.blackjack.exception;


public class InputException extends Exception {
    public InputException(String msg) {
        super(msg);
    }

    public InputException(String message, Throwable cause) {
        super(message, cause);
    }

    public InputException(Throwable cause) {
        super(cause);
    }
}

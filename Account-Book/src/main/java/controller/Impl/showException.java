package controller.Impl;

public class showException extends Exception {
    public showException(String message) {
        super(message);
    }

    public showException(String message, Throwable cause) {
        super(message, cause);
    }
}

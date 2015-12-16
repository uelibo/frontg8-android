package ch.frontg8.lib.message;

public class InvalidMessageException extends Throwable {
    public InvalidMessageException(String s) {
        super(s);
    }

    public InvalidMessageException() {
        super();
    }
}

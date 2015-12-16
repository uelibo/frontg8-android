package ch.frontg8.lib.crypto;

public class KeyNotFoundException extends Exception {
    public KeyNotFoundException(String alias) {
        super("Key not found: " + alias);
    }
}

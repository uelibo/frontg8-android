package ch.frontg8.lib.crypto;

/**
 * Created by tstauber on 07.11.15.
 */
public class KeyNotFoundError extends Error {
    public KeyNotFoundError(String s) {
        super(s);
    }
}

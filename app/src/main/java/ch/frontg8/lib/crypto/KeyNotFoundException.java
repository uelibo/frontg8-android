package ch.frontg8.lib.crypto;

/**
 * Created by tstauber on 07.11.15.
 */
public class KeyNotFoundException extends Exception {
    public KeyNotFoundException(String alias){
        super("Key not found: " + alias);
    }

    public KeyNotFoundException(){
        super("Key not found!");
    }
}

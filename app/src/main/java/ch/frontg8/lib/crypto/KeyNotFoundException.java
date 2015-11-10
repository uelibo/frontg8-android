package ch.frontg8.lib.crypto;

import java.security.Key;

/**
 * Created by tstauber on 07.11.15.
 */
public class KeyNotFoundException extends Exception {
    public KeyNotFoundException(String s){
        super(s);
    }
    public KeyNotFoundException(){
        super();
    }
}

package ch.frontg8.lib.crypto;

/**
 * Created by tstauber on 10.11.15.
 */
public class MyKeysNotFoundException extends KeyNotFoundException{

    public MyKeysNotFoundException(String s) {
        super(s);
    }
    public MyKeysNotFoundException() {
        super();
    }
}

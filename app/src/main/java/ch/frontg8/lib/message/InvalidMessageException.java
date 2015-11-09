package ch.frontg8.lib.message;

/**
 * Created by tstauber on 09.11.15.
 */
public class InvalidMessageException extends Throwable {
    public InvalidMessageException(String s){
        super(s);
    }
    public InvalidMessageException(){
        super();
    }
}

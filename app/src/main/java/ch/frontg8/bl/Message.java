package ch.frontg8.bl;

import java.io.Serializable;

public class Message implements Serializable {
    private String messagetext;

    public Message(String messagetext) {
        this.messagetext = messagetext;
    }

    public String getMessage() {
        return messagetext;
    }

}

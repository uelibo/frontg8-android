package ch.frontg8.bl;

import java.io.Serializable;

import ch.frontg8.lib.protobuf.Frontg8Client;

public class Message implements Serializable {
    private String messagetext;
    private String sessionID = "0";
    private long timestamp = 0;

    //Simple Constructor for Prototype
    public Message(String messagetext) {
        this.messagetext = messagetext;
    }

    public Message(Frontg8Client.Data dataMessage) {
        messagetext = dataMessage.getMessageData().toStringUtf8();
        sessionID = dataMessage.getSessionId().toStringUtf8();
        timestamp = dataMessage.getTimestamp();
    }

    public String getMessage() { return messagetext; }
    public String getSessionID() { return sessionID; }
    public long getTimestamp() { return timestamp; }

}

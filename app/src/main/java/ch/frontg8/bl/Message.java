package ch.frontg8.bl;

import java.io.Serializable;

import ch.frontg8.lib.protobuf.Frontg8Client;

public class Message implements Serializable, Filtertext {
    private String messagetext;
    private String sessionID = "0";
    private long timestamp = 0;
    private Frontg8Client.Encrypted encryptedMessage = null;

    public Message(Frontg8Client.Data dataMessage) { replaceMessage(dataMessage); }

    // Simple Constructor for Prototype
    public Message(String messagetext) { this.messagetext = messagetext; }

    // Constructor for encrypted Message
    public Message(Frontg8Client.Encrypted encryptedMessage) {
        this.messagetext = "encrypted Message";
        this.encryptedMessage = encryptedMessage;
    }

    public void replaceMessage(Frontg8Client.Data dataMessage) {
        messagetext = dataMessage.getMessageData().toStringUtf8();
        sessionID = dataMessage.getSessionId().toStringUtf8();
        timestamp = dataMessage.getTimestamp();
        encryptedMessage = null;
    }

    public String getMessage() { return messagetext; }
    public String getSessionID() { return sessionID; }
    public long getTimestamp() { return timestamp; }
    public String getFilterValue() { return this.messagetext; }
    public Frontg8Client.Encrypted getEncryptedMessage() { return this.encryptedMessage; }
    public boolean isEncrypted() { return encryptedMessage != null; }

}

package ch.frontg8.bl;

import java.io.Serializable;

import ch.frontg8.lib.message.MessageHelper;
import ch.frontg8.lib.protobuf.Frontg8Client;

public class Message implements Serializable, Filtertext {
    private Frontg8Client.Encrypted encryptedMessage = null;
    private Frontg8Client.Data dataMessage = null;

    // Please use this constructor!
    public Message(Frontg8Client.Data dataMessage) { replaceMessage(dataMessage); }

    // Simple Constructor for Prototype
    @Deprecated
    public Message(String messagetext) {
        byte[] sessionId = new byte[]{};
        int timestamp = 0;
        this.dataMessage = MessageHelper.buildDataMessage(messagetext.getBytes(), sessionId, timestamp);
    }

    // Constructor for encrypted Message
    @Deprecated
    public Message(Frontg8Client.Encrypted encryptedMessage) {
        this("encrypted Message");
        this.encryptedMessage = encryptedMessage;
    }

    public void replaceMessage(Frontg8Client.Data dataMessage) {
        this.dataMessage = dataMessage;
        encryptedMessage = null;
    }

    public String getMessage() { return dataMessage.getMessageData().toStringUtf8(); }
    public String getSessionID() { return dataMessage.getSessionId().toStringUtf8(); }
    public long getTimestamp() { return dataMessage.getTimestamp(); }
    public String getFilterValue() { return getMessage(); }
    public Frontg8Client.Encrypted getEncryptedMessage() { return this.encryptedMessage; }
    public boolean isEncrypted() { return encryptedMessage != null; }

}

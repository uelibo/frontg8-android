package ch.frontg8.bl;

import ch.frontg8.lib.message.MessageHelper;
import ch.frontg8.lib.protobuf.Frontg8Client;

public class Message implements Filtertext {
    private Frontg8Client.Data dataMessage = null;

    public Message(Frontg8Client.Data dataMessage) { replaceMessage(dataMessage); }

    // Simple Constructor for Prototype
    @Deprecated
    public Message(String messagetext) {
        byte[] sessionId = new byte[]{};
        int timestamp = 0;
        this.dataMessage = MessageHelper.buildDataMessage(messagetext.getBytes(), sessionId, timestamp);
    }

    public void replaceMessage(Frontg8Client.Data dataMessage) { this.dataMessage = dataMessage; }
    public String getMessage() { return dataMessage.getMessageData().toStringUtf8(); }
    public String getSessionID() { return dataMessage.getSessionId().toStringUtf8(); }
    public long getTimestamp() { return dataMessage.getTimestamp(); }
    public String getFilterValue() { return getMessage(); }
}

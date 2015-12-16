package ch.frontg8.lib.message;

class MessageType {
    public static final int Encrypted = 1;
    public static final int Blacklist = 2;
    public static final int Revocation = 4;
    public static final int Notification = 8;
    public static final int MessageRequest = 16;
}

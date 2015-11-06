package ch.frontg8.lib.message;

import com.google.protobuf.ByteString;

import java.math.BigInteger;

import ch.frontg8.lib.protobuf.Frontg8Client;

public class MessageHelper {

    public static String byteArrayAsHexString(byte[] byteArray){
        StringBuilder sb = new StringBuilder();
        for (byte b : byteArray) {
            sb.append(String.format("%02X ", b));
        }
        return(sb.toString());
    }

    public static Frontg8Client.MessageRequest buildMessageRequestMessage(String hash) {
        return Frontg8Client.MessageRequest
                .newBuilder()
                .setHash(ByteString.copyFromUtf8(hash))
                .build();
    }

    public static Frontg8Client.Data buildDataMessage(String message, String sessionId, int timestamp) {
        return Frontg8Client.Data
                .newBuilder()
                .setMessageData(ByteString.copyFromUtf8(message))
                .setSessionId(ByteString.copyFromUtf8(sessionId))
                .setTimestamp(0)
                .build();
    }

    public static Frontg8Client.Encrypted buildEncryptedMessage(ByteString data)  {
        return Frontg8Client.Encrypted
                .newBuilder()
                .setEncryptedData(data)
                .build();
    }

    private static byte[] sizeToByte(int size){
        return BigInteger.valueOf(size).toByteArray();
    }

    public static byte[] addMessageHeader(byte[] message, int messageType) {
        byte[] header = new byte[4];
        byte[] sizeArray = sizeToByte(message.length);

        header[2] = (byte) messageType;

        if (sizeArray.length == 2) {
            header[0] = sizeArray[1];
            header[1] = sizeArray[0];
        } else if (sizeArray.length == 1) {
            header[1] = sizeArray[0];
        } else {
            // TODO: raise new Exception();
        }

        byte[] fullMessage = new byte[header.length + message.length];
        System.arraycopy(header, 0, fullMessage, 0, header.length);
        System.arraycopy(message, 0, fullMessage, header.length, message.length);

        return fullMessage;
    }

}

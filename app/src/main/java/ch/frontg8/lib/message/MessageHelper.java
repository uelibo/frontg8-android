package ch.frontg8.lib.message;

import android.content.Context;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ch.frontg8.lib.connection.TlsClient;
import ch.frontg8.lib.protobuf.Frontg8Client;

import static ch.frontg8.lib.crypto.LibCrypto.decryptMSG;
import static ch.frontg8.lib.crypto.LibCrypto.encryptMSG;

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
        return buildDataMessage(ByteString.copyFromUtf8(message), ByteString.copyFromUtf8(sessionId), timestamp);
    }

    public static Frontg8Client.Data buildDataMessage(byte[] message, byte[] sessionId, int timestamp) {
        return buildDataMessage(ByteString.copyFrom(message), ByteString.copyFrom(sessionId), timestamp);
    }

    public static Frontg8Client.Data buildDataMessage(ByteString message, ByteString sessionId, int timestamp) {
        return Frontg8Client.Data
                .newBuilder()
                .setMessageData(message)
                .setSessionId(sessionId)
                .setTimestamp(0)
                .build();
    }

    public static byte[] buildEncryptedMessage(Frontg8Client.Data dataMessage){
        return buildEncryptedMessage(dataMessage.toByteString());
    }

    public static byte[] buildEncryptedMessage(ByteString encryptedData)  {
        return addMessageHeader(Frontg8Client.Encrypted.newBuilder().setEncryptedData(encryptedData).build().toByteArray(), MessageType.Encrypted);
    }

    /**
     * Builds a Encrypted message directly from a byte array containing the message.
     * @param plainData
     * @param sessionId
     * @param timestamp
     * @return
     */
    public static byte[] buildFullEncryptedMessage(byte[] plainData, byte[] sessionId, int timestamp, UUID uuid, Context context){
        Frontg8Client.Data dataMSG = buildDataMessage(plainData, sessionId, timestamp);
        byte[] encryptedDataMSG = encryptMSG(uuid,dataMSG.toByteArray(),context);
        return buildEncryptedMessage(ByteString.copyFrom(encryptedDataMSG));
    }

    public static List<Frontg8Client.Encrypted> getEncryptedMessagesFromNotification(Frontg8Client.Notification notification) {
        List<Frontg8Client.Encrypted> encrypted = new ArrayList<>();

        for (int i=0; i < notification.getCount(); i++) {
            encrypted.add(notification.getBundle(i));
        }
        return encrypted;
    }

    public static Frontg8Client.Data getDataMessage(ByteString data) throws InvalidMessageException {
        try {
            return Frontg8Client.Data.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new InvalidMessageException();
        }
    }

    public static Frontg8Client.Data getDataMessage(byte[] data) throws InvalidMessageException {
        //TODO: improve by length check
        return getDataMessage(ByteString.copyFrom(data));
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
            // TODO: raise Exception
        }

        byte[] fullMessage = new byte[header.length + message.length];
        System.arraycopy(header, 0, fullMessage, 0, header.length);
        System.arraycopy(message, 0, fullMessage, header.length, message.length);

        return fullMessage;
    }

    private static int getLengthFromHeader(byte[] header) {
        return (( header[0] < 0 ? 256+header[0]: header[0] ) << 8) + ( header[1] < 0 ? 256+header[1] : header[1] );
    }

    public static Frontg8Client.Notification getNotificationMessage(TlsClient tlsClient) {
        byte[] header = tlsClient.getBytes(4);
        int length = getLengthFromHeader(header);
        byte[] data = tlsClient.getBytes(length);

        Frontg8Client.Notification notification = null;

        try {
            notification = Frontg8Client.Notification.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        // TODO: raise Exception if notification = null
        return notification;
    }

    public static byte[] getDecryptedContent(Frontg8Client.Encrypted encryptedMSG, Context context) throws InvalidMessageException {
        byte[] decryptedBytes = decryptMSG((encryptedMSG.getEncryptedData()).toByteArray(), context);
        Frontg8Client.Data dataMSG = getDataMessage(decryptedBytes);
        return dataMSG.getMessageData().toByteArray();
    }



}

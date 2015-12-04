package ch.frontg8.lib.message;

import android.content.Context;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ch.frontg8.bl.Message;
import ch.frontg8.lib.connection.NotConnectedException;
import ch.frontg8.lib.connection.TlsClient;
import ch.frontg8.lib.crypto.KeyNotFoundException;
import ch.frontg8.lib.crypto.KeystoreHandler;
import ch.frontg8.lib.crypto.LibCrypto;
import ch.frontg8.lib.helper.Tuple;
import ch.frontg8.lib.protobuf.Frontg8Client;

public class MessageHelper {

    // Build messages from content

    public static byte[] buildMessageRequestMessage(byte[] hash) {
        return addMessageHeader(Frontg8Client.MessageRequest
                .newBuilder()
                .setHash(ByteString.copyFrom(hash))
                .build().toByteArray(), MessageType.MessageRequest);
    }

    public static Frontg8Client.Data buildDataMessage(byte[] message, byte[] sessionId, int timestamp) {
        return Frontg8Client.Data
                .newBuilder()
                .setMessageData(ByteString.copyFrom(message))
                .setSessionId(ByteString.copyFrom(sessionId))
                .setTimestamp(timestamp)
                .build();
    }

    public static byte[] buildEncryptedMessage(ByteString encryptedData) {
        return addMessageHeader(Frontg8Client.Encrypted.newBuilder()
                        .setEncryptedData(encryptedData)
                        .build()
                        .toByteArray(),
                MessageType.Encrypted);
    }

    public static byte[] encryptAndPutInEncrypted(Frontg8Client.Data dataMSG,
                                                  UUID uuid, KeystoreHandler ksHandler) {
        try {
            return buildEncryptedMessage(ByteString.copyFrom(
                    LibCrypto.encryptMSG(uuid, dataMSG.toByteArray(), ksHandler)
            ));
        } catch (KeyNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] putInDataEncryptAndPutInEncrypted(byte[] plainData, byte[] sessionId,
                                                           int timestamp, UUID uuid, KeystoreHandler ksHandler) {
        Frontg8Client.Data dataMSG = buildDataMessage(plainData, sessionId, timestamp);
        return encryptAndPutInEncrypted(dataMSG, uuid, ksHandler);
    }


    private static byte[] sizeToByte(int size) {
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

    // Extract content from messages

    public static List<Frontg8Client.Encrypted> getEncryptedMessagesFromNotification(Frontg8Client.Notification notification) {
        List<Frontg8Client.Encrypted> encrypted = new ArrayList<>();
        if (notification != null) {
            for (int i = 0; i < notification.getCount(); i++) {
                encrypted.add(notification.getBundle(i));
            }
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
        if ( data == null ) {
            return null;
        }
        return getDataMessage(ByteString.copyFrom(data));
    }

    public static Frontg8Client.Notification getNotificationMessage(byte[] data) {
        try {
            return Frontg8Client.Notification.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Could not construct msg");
        }
        // TODO: raise Exception if notification = null
    }

    public static Tuple<UUID, Frontg8Client.Data> getDecryptedContent(Frontg8Client.Encrypted encryptedMSG, KeystoreHandler ksHandler) throws InvalidMessageException {
        Tuple<UUID, byte[]> decrypted = LibCrypto.decryptMSG((encryptedMSG.getEncryptedData()).toByteArray(), ksHandler);
        return new Tuple<>(decrypted._1, getDataMessage(decrypted._2));
    }


    // Deprecated, remove as soon as possible

    @Deprecated
    public static String byteArrayAsHexString(byte[] byteArray) {
        StringBuilder sb = new StringBuilder();
        for (byte b : byteArray) {
            sb.append(String.format("%02X ", b));
        }
        return (sb.toString());
    }

    @Deprecated
    private static int getLengthFromHeader(byte[] header) {
        return ((header[0] < 0 ? 256 + header[0] : header[0]) << 8) + (header[1] < 0 ? 256 + header[1] : header[1]);
    }

    @Deprecated
    public static Frontg8Client.Notification getNotificationMessage(TlsClient tlsClient) {
        byte[] header;
        byte[] data = new byte[0];
        try {
            header = tlsClient.getBytes(4);
            int length = getLengthFromHeader(header);
            data = tlsClient.getBytes(length);
        } catch (NotConnectedException e) {
            e.printStackTrace();
        }

        Frontg8Client.Notification notification = null;

        try {
            notification = Frontg8Client.Notification.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        // TODO: raise Exception if notification = null
        return notification;
    }

}

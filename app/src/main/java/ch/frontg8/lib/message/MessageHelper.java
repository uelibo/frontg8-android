package ch.frontg8.lib.message;

import java.math.BigInteger;

public class MessageHelper {

    public static String byteArrayAsHexString(byte[] byteArray){
        StringBuilder sb = new StringBuilder();
        for (byte b : byteArray) {
            sb.append(String.format("%02X ", b));
        }
        return(sb.toString());
    }

    public static byte[] sizeToByte(int size){
        return BigInteger.valueOf(size).toByteArray();
    }

}

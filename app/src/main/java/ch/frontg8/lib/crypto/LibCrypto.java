package ch.frontg8.lib.crypto;

import android.content.Context;
import android.support.annotation.NonNull;

import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.macs.HMac;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.DecoderException;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import ch.frontg8.lib.helper.Tuple;

public class LibCrypto {
    private static final String BC = BouncyCastleProvider.PROVIDER_NAME;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final int KEYSIZE = 32;
    private static final int IVSIZE = 16;

//    private static KeystoreHandler ksHandler;


    // Encryption / Decryption

    @NonNull
    public static byte[] encryptMSG(UUID uuid, byte[] plainBytes, KeystoreHandler ksHandler) throws KeyNotFoundException {
        byte[] encryptedBytes;
        byte[] iv = genIV();
        IvParameterSpec ivspec = new IvParameterSpec(iv);

        Cipher cipher = getEncryptCipher(ksHandler.getSKC(uuid), ivspec);
        try {
            encryptedBytes = cipher.doFinal(plainBytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new Error(e.getMessage());
        }

        byte[] hmac = getHMAC(ksHandler.getSKS(uuid), encryptedBytes);
        return concat(iv, encryptedBytes, hmac);
        // TODO: what to do if uuid does not have a sessionkey?
    }

    @NonNull
    public static Tuple<UUID, byte[]> decryptMSG(byte[] encryptedMSG, KeystoreHandler ksHandler) {
        UUID decryptUUID = null;
        byte[] decodedBytes = null;
        byte[] iv = Arrays.copyOfRange(encryptedMSG, 0, IVSIZE);
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        byte[] encryptedBytes;
        byte[] hmacBytes;
        try {
            encryptedBytes = Arrays.copyOfRange(encryptedMSG, IVSIZE, (encryptedMSG.length - KEYSIZE));
            hmacBytes = Arrays.copyOfRange(encryptedMSG, (encryptedMSG.length - KEYSIZE), encryptedMSG.length);
        } catch (Throwable e) {
            System.err.println("Undecryptable MSG");
            return new Tuple<>(null, new byte[]{});
        }

        HashMap<String, SecretKey[]> sessionKeys = ksHandler.getBothKeyMap();

        for (String s : sessionKeys.keySet()) {

            if (verifyHMAC(sessionKeys.get(s)[0], encryptedBytes, hmacBytes)) {
                decryptUUID = UUID.fromString(s);
                decodedBytes = decrypt(encryptedBytes, sessionKeys.get(s)[1], ivspec);
                break;
            }
        }
        return new Tuple<>(decryptUUID, decodedBytes);
    }

    @NonNull
    public static Tuple<UUID, byte[]> decryptMSG(byte[] encryptedMSG, UUID uuid, KeystoreHandler ksHandler) {

        byte[] decodedBytes = null;
        byte[] iv = Arrays.copyOfRange(encryptedMSG, 0, IVSIZE);
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        byte[] encryptedBytes;
        byte[] hmacBytes;
        try {
            encryptedBytes = Arrays.copyOfRange(encryptedMSG, IVSIZE, (encryptedMSG.length - KEYSIZE));
            hmacBytes = Arrays.copyOfRange(encryptedMSG, (encryptedMSG.length - KEYSIZE), encryptedMSG.length);


            SecretKey sks = ksHandler.getSKS(uuid);
            SecretKey skc = ksHandler.getSKC(uuid);

            if (verifyHMAC(sks, encryptedBytes, hmacBytes)) {
                decodedBytes = decrypt(encryptedBytes, skc, ivspec);
            }
        } catch (KeyNotFoundException e) {
            System.err.println("Key not found");
            return new Tuple<>(null, new byte[]{});
        } catch (Throwable e) {
            System.err.println("Undecryptable MSG");
            return new Tuple<>(null, new byte[]{});
        }
        return new Tuple<>(uuid, decodedBytes);
    }


    // Crypto Helpers

    private static byte[] concat(byte[] a, byte[] b, byte[] c) {
        int aLen = a.length;
        int bLen = b.length;
        int cLen = c.length;
        byte[] d = new byte[aLen + bLen + cLen];
        System.arraycopy(a, 0, d, 0, aLen);
        System.arraycopy(b, 0, d, aLen, bLen);
        System.arraycopy(c, 0, d, aLen + bLen, cLen);
        return d;
    }

    private static byte[] genIV() {
        byte[] iv = new byte[IVSIZE];
        new Random().nextBytes(iv);
        return iv;
    }

    private static byte[] decrypt(byte[] encryptedBytes, SecretKey skc, IvParameterSpec ivspec) {
        byte[] decodedBytes = new byte[]{};
        try {
            Cipher cipher = getDecryptCipher(skc, ivspec);
            decodedBytes = cipher.doFinal(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decodedBytes;
    }

    private static Cipher getEncryptCipher(SecretKey skc, IvParameterSpec ivspec) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/CTR/NoPadding");
            SecureRandom random = new SecureRandom();
            cipher.init(Cipher.ENCRYPT_MODE, skc, ivspec, random);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipher;
    }

    private static Cipher getDecryptCipher(SecretKey skc, IvParameterSpec ivspec) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/CTR/NoPadding");
            SecureRandom random = new SecureRandom();
            cipher.init(Cipher.DECRYPT_MODE, skc, ivspec, random);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipher;
    }


    // HMAC Helpers

    private static boolean verifyHMAC(SecretKey sks, byte[] msg, byte[] hmac) {
        System.out.println("-----\n" + Base64.toBase64String(hmac));
        System.out.println(Base64.toBase64String(getHMAC(sks, msg)) + "\n+++++");
        return Arrays.areEqual(hmac, getHMAC(sks, msg));
    }

    private static byte[] getHMAC(SecretKey sks, byte[] msg) {
        HMac hmac = new HMac(new SHA256Digest());
        byte[] result = new byte[hmac.getMacSize()];
        KeyParameter kp = new KeyParameter(sks.getEncoded());
        hmac.init(kp);
        hmac.update(msg, 0, msg.length);
        hmac.doFinal(result, 0);
        return result;
    }


    // My Key Handling

    @NonNull
    public static PublicKey getMyPublicKey(KeystoreHandler ksHandler, Context context) {
        return ksHandler.getMyPublicKey(context);
    }

    @NonNull
    public static byte[] getMyPublicKeyBytes(KeystoreHandler ksHandler, Context context) {
        return ksHandler.getMyPublicKeyBytes(context);
    }

    public static void negotiateSessionKeys(@NonNull UUID uuid, byte[] pubKey, @NonNull KeystoreHandler ksHandler, @NonNull Context context) throws InvalidKeyException {
        negotiateSessionKeys(uuid, createPubKey(pubKey), ksHandler, context);
    }

    public static void negotiateSessionKeys(@NonNull UUID uuid, @NonNull String pubKey, @NonNull KeystoreHandler ksHandler, @NonNull Context context) throws InvalidKeyException {
        negotiateSessionKeys(uuid, createPubKey(pubKey.getBytes()), ksHandler, context);
    }

    public static void negotiateSessionKeys(@NonNull UUID uuid, @NonNull PublicKey pubKey, @NonNull KeystoreHandler ksHandler, @NonNull Context context) throws InvalidKeyException {
        byte[] sessionKey;
        sessionKey = ksHandler.negotiateSessionKeys(pubKey, context);

        // TODO: check for invalid Pubkey

        byte[] skcBytes = Arrays.copyOfRange(sessionKey, 0, KEYSIZE);
        byte[] sksBytes = Arrays.copyOfRange(sessionKey, sessionKey.length - KEYSIZE, sessionKey.length);

        SecretKey skc = new SecretKeySpec(skcBytes, 0, skcBytes.length, "AES");
        SecretKey sks = new SecretKeySpec(sksBytes, 0, sksBytes.length, "AES");

        ksHandler.removeSessionKeys(uuid);
        ksHandler.setSKC(uuid, skc, context);
        ksHandler.setSKS(uuid, sks, context);
    }

    private static PublicKey createPubKey(@NonNull byte[] pubKey) throws InvalidKeyException {
        try {
            return KeyFactory.getInstance("ECDSA", BC).generatePublic(new X509EncodedKeySpec(Base64.decode(pubKey)));
        } catch (Exception e) {
            e.printStackTrace();
            throw new InvalidKeyException();
        }
    }

    public static void generateNewKeys(@NonNull KeystoreHandler ksHandler, Context context) {
        ksHandler.genAndSetMyKeys(context);
    }


    // Session Key Handling

    public static boolean containsSKSandSKC(@NonNull UUID uuid, @NonNull KeystoreHandler ksHandler) {
        return ksHandler.containsSKSandSKC(uuid);
    }


    // Keystore Handling

    public static void setNewPassword(byte[] password) {
        //TODO: implement
    }


    // Execute before every public call

//    private static void loadKSH(Context context) {
//        if (ksHandler == null) {
//            ksHandler = new KeystoreHandler(context);
//        }
//    }
}

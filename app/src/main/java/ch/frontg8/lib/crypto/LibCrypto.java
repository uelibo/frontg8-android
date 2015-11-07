package ch.frontg8.lib.crypto;

import android.content.Context;

import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.macs.HMac;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.encoders.Base64;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static ch.frontg8.lib.crypto.LibCert.generateCertificate;
import static java.util.Collections.list;

public class LibCrypto {

    /*
    What must be public:
        Generate a new key and save it. ✓
        Encrypt byte[] ✓
        Decrypt byte[] ✓
        Get my pubkey  ✓
        Genereate new skc and sks ✓
        Set new Keystore-password
     */

    private static final String BC = BouncyCastleProvider.PROVIDER_NAME;
    private static final UUID MYUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private static final String SUFFIXPRIVATE = "pri";
    private static final String SUFFIXSESSIONKEYCRYPTO = "skc";
    private static final String SUFFIXSESSIONKEYSIGN = "sks";
    private static final int KEYSIZE = 32; //Bytes
    private static final int IVSIZE = 16;  //Bytes
    private static final String MYALIAS = MYUUID.toString() + SUFFIXPRIVATE;

    private static char[] ksPassword = "KEYSTORE PASSWORD".toCharArray(); //TODO: change to real pw
    private static String ksFileName = "src/main/res/raw/frontg8keystore.ks"; //TODO: make configurable

    private static char[] PASSWORD = ksPassword;

    private static KeyStore ks;

    static {
        Security.addProvider(new BouncyCastleProvider());
        initKeystore();
    }

    // Encryption / Decryption

    /**
     * @param uuid       The uuid of the user, for which the message should be encrypted
     * @param plainBytes The unencrypted message as byte array
     * @param context    The android context (Activity)
     * @return The message as an encrypted byte array
     */
    public static byte[] encryptMSG(UUID uuid, byte[] plainBytes, Context context) {
        loadKS(context);
        SecretKey skc = getSKC(uuid);
        byte[] encryptedBytes = new byte[]{};
        byte[] iv = genIV();
        IvParameterSpec ivspec = new IvParameterSpec(iv);

        Cipher cipher = getEncryptCipher(skc, ivspec);
        try {
            encryptedBytes = cipher.doFinal(plainBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] hmac = getHMAC(getSKS(uuid), encryptedBytes);

        return concat(iv, encryptedBytes, hmac);
        // TODO: what to do if uuid does not have a sessionkey?
    }

    /**
     * @param encryptedMSG The encrypted message as byte array
     * @param context      The android context (Activity)
     * @return The message as an unencrypted byte array
     */
    public static byte[] decryptMSG(byte[] encryptedMSG, Context context) {
        loadKS(context);
        byte[] decodedBytes = null;
        byte[] iv = Arrays.copyOfRange(encryptedMSG, 0, IVSIZE);
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        byte[] encryptedBytes = Arrays.copyOfRange(encryptedMSG, IVSIZE, (encryptedMSG.length - KEYSIZE));
        byte[] hmacBytes = Arrays.copyOfRange(encryptedMSG, (encryptedMSG.length - KEYSIZE), encryptedMSG.length);

        HashMap<String, SecretKey> skcs = getKeyList(getSKCAliasList());
        HashMap<String, SecretKey> skss = getKeyList(getSKSAliasList());

        //TODO: fertig mache (suffix entfernen)
        for (String s : skss.keySet()) {
            if (verifyHMAC(skss.get(s), encryptedBytes, hmacBytes)) {
                decodedBytes = decrypt(encryptedBytes, skcs.get(s.substring(0, s.length() - 3) + SUFFIXSESSIONKEYCRYPTO), ivspec); //TODO: create constant for length suffix
                break;
            }
        }
        return decodedBytes;
    }

    // Crypto helpers

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


    // Key handling

    /**
     * @param context The android context (Activity)
     * @return My public key.
     */
    public static PublicKey getMyPublicKey(Context context) {
        loadKS(context);
        final Certificate cert;
        try {
            cert = ks.getCertificate(MYALIAS);
            return cert.getPublicKey();
        } catch (KeyStoreException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static PrivateKey getMyPrivateKey() {
        return (PrivateKey) getKey(MYALIAS);
    }

    /**
     * @param uuid    The uuid of the user
     * @param pubKey  The public key from the user
     * @param context The android context (Activity)
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     */
    public static void negotiateSessionKeys(UUID uuid, PublicKey pubKey, Context context) throws NoSuchProviderException, NoSuchAlgorithmException {
        loadKS(context);
        byte[] sessionKey = negotiateSessionKey(pubKey);
        byte[] skcBytes = Arrays.copyOfRange(sessionKey, 0, KEYSIZE);
        byte[] sksBytes = Arrays.copyOfRange(sessionKey, sessionKey.length - KEYSIZE, sessionKey.length);

        SecretKey skc = new SecretKeySpec(skcBytes, 0, skcBytes.length, "AES");
        SecretKey sks = new SecretKeySpec(sksBytes, 0, sksBytes.length, "AES");

        removeKey(getSKCalias(uuid));
        setSecretKey(getSKCalias(uuid), skc, context);
        removeKey(getSKSalias(uuid));
        setSecretKey(getSKSalias(uuid), sks, context);
    }

    /**
     * @throws Exception
     */
    public static void generateNewKeys(Context context) throws Exception {
        loadKS(context);
        setMyKey(genECDHKeys(), context);
    }

    private static KeyPair genECDHKeys() throws Exception {
        ECGenParameterSpec ecParamSpec = new ECGenParameterSpec("secp521r1"); // TODO: Change curve to incompatible to pyg8
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDH", BC);
        kpg.initialize(ecParamSpec);
        return kpg.generateKeyPair();
    }

    private static byte[] negotiateSessionKey(PublicKey pubKey) {
        PrivateKey privKey = getMyPrivateKey();
        byte[] sessionKey = new byte[]{};
        try {
            KeyAgreement kA = KeyAgreement.getInstance("ECDH", BC);
            kA.init(privKey);
            kA.doPhase(pubKey, true);
            sessionKey = kA.generateSecret();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sessionKey;
    }

    private static void setSecretKey(String alias, SecretKey key, Context context) {
        try {
            ks.setKeyEntry(alias, key, PASSWORD, null);
            writeStore(context);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }

    private static Key removeKey(String alias) {
        Key key = getKey(alias);
        try {
            ks.deleteEntry(alias);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return key;
    }

    private static void setMyKey(KeyPair keyPair, Context context) {
        loadKS(context);
        X509Certificate certificate;
        try {
            certificate = generateCertificate(keyPair);
            Certificate[] certChain = {certificate};
            ks.setKeyEntry(MYUUID.toString() + SUFFIXPRIVATE, keyPair.getPrivate(), PASSWORD, certChain);
            writeStore(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Key checks

    public static boolean containsKey(String alias, Context context) {
        loadKS(context);
        boolean result = false;
        try {
            result = ks.containsAlias(alias);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean containsSKS(UUID uuid, Context context) {
        loadKS(context);
        return containsKey(getSKSalias(uuid), context);
    }

    public static boolean containsSKC(UUID uuid, Context context) {
        loadKS(context);
        return containsKey(getSKCalias(uuid), context);
    }

    public static boolean containsSKSandSKC(UUID uuid, Context context) {
        loadKS(context);
        boolean result1 = containsSKS(uuid, context);
        boolean result2 = containsSKC(uuid, context);
        return result1 && result2;
    }


    // Get keys

    private static SecretKey getSKC(UUID uuid) {
        return (SecretKey) getKey(getSKCalias(uuid));
    }

    private static SecretKey getSKS(UUID uuid) {
        return (SecretKey) getKey(getSKSalias(uuid));
    }

    private static String getSKCalias(UUID uuid) {
        return uuid.toString() + SUFFIXSESSIONKEYCRYPTO;
    }

    private static String getSKSalias(UUID uuid) {
        return uuid.toString() + SUFFIXSESSIONKEYSIGN;
    }

    private static Key getKey(String alias) {
        Key key = null;
        try {
            if (ks.isKeyEntry(alias)) {
                key = ks.getKey(alias, PASSWORD);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return key;
    }

    // Get key helpers

    private static ArrayList<String> getAliasList() throws KeyStoreException {
        return list(ks.aliases());
    }

    private static ArrayList<String> filter(ArrayList<String> coll, String suffix) {
        ArrayList<String> internColl = new ArrayList<>();
        for (String s : coll) {
            if (s.endsWith(suffix)) {
                internColl.add(s);
            }
        }
        return internColl;
    }

    private static ArrayList<String> getSKCAliasList() {
        ArrayList<String> skcAliasList = new ArrayList<>();
        try {
            skcAliasList = filter(getAliasList(), SUFFIXSESSIONKEYCRYPTO);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return skcAliasList;
    }

    private static ArrayList<String> getSKSAliasList() {
        ArrayList<String> sksAliasList = new ArrayList<>();
        try {
            sksAliasList = filter(getAliasList(), SUFFIXSESSIONKEYSIGN);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return sksAliasList;
    }

    private static HashMap<String, SecretKey> getKeyList(ArrayList<String> aliasList) {
        HashMap<String, SecretKey> keyMap = new HashMap<>();
        for (String alias : aliasList) {
            try {
                byte[] keyBytes = getKey(alias).getEncoded();
                SecretKey key = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
                keyMap.put(alias, key);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return keyMap;
    }

    // Keystore handling

    private static void initKeystore() {
        if (ks == null) {
            try {
                ks = KeyStore.getInstance("BKS", Security.getProvider(BC));
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }
        }
    }

    private static void loadKS(Context context) {
        if (ks == null) {
            initKeystore();
        }
        try {
            if (ks.containsAlias(MYALIAS)) {
                return;
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        try (InputStream is = context.openFileInput(ksFileName)) {
            ks.load(is, ksPassword);
        } catch (FileNotFoundException fnfe) {
            try {
                ks.load(null);
                try (OutputStream os = context.openFileOutput(ksFileName, Context.MODE_PRIVATE)) {
                    ks.store(os, ksPassword);
                } catch (KeyStoreException kse) {
                    kse.printStackTrace();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    private static void writeStore(Context context) {
        try {
            LibKeystore.writeStore(ksFileName, ksPassword, ks, context);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //helper for KeyFileGeneration
    static void setKeyfileName(String name) {
        ks = null;
        ksFileName = name;
    }
}

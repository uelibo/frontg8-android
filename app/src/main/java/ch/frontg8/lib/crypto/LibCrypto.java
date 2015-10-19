package ch.frontg8.lib.crypto;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
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
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.X509v1CertificateBuilder;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.macs.HMac;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;
import org.spongycastle.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static java.util.Collections.list;

public class LibCrypto {

    private static final String BC = BouncyCastleProvider.PROVIDER_NAME;
    private static final UUID MYUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private static final String SUFFIXPRIVATE = "pri";
    private static final String SUFFIXSESSIONKEYCRYPTO = "skc";
    private static final String SUFFIXSESSIONKEYSIGN = "sks";
    private static final int KEYSIZE = 32; //Bytes
    private static final int IVSIZE = 16;  //Bytes

    private static final String MYALIAS = MYUUID.toString() + SUFFIXPRIVATE;
    private static final char[] PASSWORD = "MyPassword".toCharArray();


    private static KeyStore ks;

    static {
        Security.addProvider(new BouncyCastleProvider());
        try {
            ks = KeyStore.getInstance("BKS", Security.getProvider(BC));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Encryption / Decryption

    public static byte[] encryptMSG(UUID uuid, byte[] plainBytes) {
        SecretKey skc = getSKC(uuid);
        byte[] encryptedBytes = new byte[]{};
        byte[] iv = genIV();
        IvParameterSpec ivspec = new IvParameterSpec(iv);

        Cipher cipher = getEncryptCipher(skc,ivspec);
        try {
            encryptedBytes = cipher.doFinal(plainBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] hmac = getHMAC(getSKS(uuid),encryptedBytes);

        return concat(iv, encryptedBytes, hmac);
        // TODO: what to do if uuid does not have a sessionkey?
    }

    private static byte[] concat(byte[] a, byte[] b, byte[] c) {
        int aLen = a.length;
        int bLen = b.length;
        int cLen = c.length;
        byte[] d= new byte[aLen+bLen+cLen];
        System.arraycopy(a, 0, d, 0, aLen);
        System.arraycopy(b, 0, d, aLen, bLen);
        System.arraycopy(c, 0, d, aLen+bLen, cLen);
        return d;
    }

    private static byte[] genIV(){
        byte[] iv = new byte[IVSIZE];
        new Random().nextBytes(iv);
        return iv;
    }

    public static byte[] decryptMSG(byte[] encryptedMSG) {
        byte[] decodedBytes = null;

        byte[] iv = Arrays.copyOfRange(encryptedMSG, 0, IVSIZE);
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        byte[] encryptedBytes = Arrays.copyOfRange(encryptedMSG, IVSIZE, (encryptedMSG.length - KEYSIZE));
        byte[] hmacBytes = Arrays.copyOfRange(encryptedMSG, (encryptedMSG.length - KEYSIZE), encryptedMSG.length);

        ArrayList<SecretKey> skcs = getKeyList(getSKCAliasList());
        ArrayList<SecretKey> skss = getKeyList(getSKSAliasList());

        for (int i = 0; i < skcs.size(); i++) {
            if (verifyHMAC(skss.get(i), encryptedBytes, hmacBytes)){
                decodedBytes = decrypt(encryptedBytes, skcs.get(i), ivspec);
                break;
            }
        }
        return decodedBytes;
    }

    public static byte[] decrypt(byte[] encryptedBytes, SecretKey skc, IvParameterSpec ivspec) {
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

    public static boolean verifyHMAC(SecretKey sks, byte[] msg, byte[] hmac) {
        return Arrays.areEqual(hmac, getHMAC(sks, msg));
    }

    public static byte[] getHMAC(SecretKey sks, byte[] msg) {
        HMac hmac=new HMac(new SHA256Digest());
        byte[] result=new byte[hmac.getMacSize()];
        KeyParameter kp=new KeyParameter(sks.getEncoded());
        hmac.init(kp);
        hmac.update(msg,0,msg.length);
        hmac.doFinal(result, 0);
        return result;
    }


    // KeyHandling

    public static KeyPair genECDHKeys() throws InvalidAlgorithmParameterException, NoSuchProviderException, NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException {
        ECGenParameterSpec ecParamSpec = new ECGenParameterSpec("secp521r1"); // TODO: Change curve to incompatible to pyg8
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDH", BC);
        kpg.initialize(ecParamSpec);
        return kpg.generateKeyPair();
    }

    public static void genSKCandSKS(UUID uuid, PublicKey pubKey) throws NoSuchProviderException, NoSuchAlgorithmException {
        byte[] sessionKey = negotiateSessionKey(pubKey);
        byte[] skcBytes = Arrays.copyOfRange(sessionKey, 0, KEYSIZE);
        byte[] sksBytes = Arrays.copyOfRange(sessionKey, sessionKey.length - KEYSIZE, sessionKey.length);

        SecretKey skc = new SecretKeySpec(skcBytes, 0, skcBytes.length, "AES");
        SecretKey sks = new SecretKeySpec(sksBytes, 0, sksBytes.length, "AES");


        setSecretKey(getSKCalias(uuid), skc);
        setSecretKey(getSKSalias(uuid), sks);
    }

    private static byte[] negotiateSessionKey(PublicKey pubKey) {
        PrivateKey privKey = getMyPrivKey();
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

    private static void setSecretKey(String alias, SecretKey key) {
        try {
            ks.setEntry(alias, new KeyStore.SecretKeyEntry(key), new KeyStore.PasswordProtection(PASSWORD));
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }

    public static void setMyKey(KeyPair keyPair) {
        X509Certificate certificate;
        try {
            certificate = generateCertificate(keyPair);
            ks.load(null, null);
            Certificate[] certChain = {certificate};
            ks.setKeyEntry(MYUUID.toString() + SUFFIXPRIVATE, keyPair.getPrivate(), PASSWORD, certChain);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean containsKey(String alias) {
        boolean result = false;
        try {
            result = ks.containsAlias(alias);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean containsSKS(UUID uuid) {
        return containsKey(getSKSalias(uuid));
    }

    public static boolean containsSKC(UUID uuid) {
        return containsKey(getSKCalias(uuid));
    }

    public static boolean containsSKSandSKC(UUID uuid) {
        return containsSKS(uuid) && containsSKC(uuid);
    }


    private static PrivateKey getMyPrivKey() {
        return (PrivateKey) getKey(MYALIAS);
    }

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

    private static ArrayList<SecretKey> getKeyList(ArrayList<String> aliasList) {
        ArrayList<SecretKey> keyList = new ArrayList<>();
        for (String alias : aliasList) {
            try {
                byte[] keyBytes = getKey(alias).getEncoded();
                SecretKey key = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
                keyList.add(key);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return keyList;
    }


    // Certificate handling

    private static X509Certificate generateCertificate(KeyPair keyPair) throws OperatorCreationException, CertificateException {
        PrivateKey privKey = keyPair.getPrivate();
        PublicKey pubKey = keyPair.getPublic();

        ContentSigner sigGen = new JcaContentSignerBuilder("SHA256withECDSA").setProvider(BC).build(privKey);
        SubjectPublicKeyInfo subPubKeyInfo = new SubjectPublicKeyInfo(ASN1Sequence.getInstance(pubKey.getEncoded()));

        Date startDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        Date endDate = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000); //TODO: check alert

        X509v1CertificateBuilder v1CertGen = new X509v1CertificateBuilder(
                new X500Name("CN=frontg8"),
                BigInteger.ONE,
                startDate, endDate,
                new X500Name("CN=frontg8"),
                subPubKeyInfo);

        X509CertificateHolder certHolder = v1CertGen.build(sigGen);

        return new JcaX509CertificateConverter().setProvider(BC).getCertificate(certHolder);
    }
}

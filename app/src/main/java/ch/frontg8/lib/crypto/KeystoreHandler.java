package ch.frontg8.lib.crypto;

import android.content.Context;

import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.encoders.Base64;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class KeystoreHandler {
    private static final String PN = BouncyCastleProvider.PROVIDER_NAME;
    private static char[] ksPassword = "KEYSTORE PASSWORD".toCharArray(); //TODO: change to real pw
    private static String ksFileName = "frontg8keystore.ks"; //TODO: make configurable
    private static final UUID MYUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private static final int SUFFIXLENGTH = 3;
    private static final String SUFFIXPRIVATE = "pri";
    private static final String SUFFIXSESSIONKEYCRYPTO = "skc";
    private static final String SUFFIXSESSIONKEYSIGN = "sks";
    private static final String MYALIAS = MYUUID.toString() + SUFFIXPRIVATE;
    private static char[] PASSWORD = ksPassword;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private KeyStore ks;

    public KeystoreHandler(Context context) {
        try {
            this.ks = loadFromFile(context);
        } catch (KeyStoreException | IOException e) {
            e.printStackTrace();
        }
    }

    private KeyStore createKeystore() throws KeyStoreException {
        KeyStore ks = KeyStore.getInstance("BKS", Security.getProvider(PN));
        try {
            ks.load(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ks;
    }

    public KeyStore loadFromFile(Context context) throws KeyStoreException, IOException {
        KeyStore ks = createKeystore();
        try (InputStream is = context.openFileInput(ksFileName)) {
            ks.load(is, ksPassword);
        } catch (FileNotFoundException fnfe) {
            try {
                //TODO: create keys at first startup
                genAndSetMyKeys(context);
                writeStore(context);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return ks;
    }


    public void writeStore(Context context) throws KeyStoreException, IOException {
        try (OutputStream os = context.openFileOutput(ksFileName, Context.MODE_PRIVATE)) {
            ks.store(os, ksPassword);
        } catch (CertificateException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


    // Session Key Handling

    public SecretKey getSKC(UUID uuid) throws KeyNotFoundException {
        return (SecretKey) getKey(uuid, SUFFIXSESSIONKEYCRYPTO);
    }

    public SecretKey getSKS(UUID uuid) throws KeyNotFoundException {
        return (SecretKey) getKey(uuid, SUFFIXSESSIONKEYSIGN);
    }

    public void setSKS(UUID uuid, SecretKey key, Context context) {
        try {
            ks.setKeyEntry(uuid.toString() + SUFFIXSESSIONKEYSIGN, key, PASSWORD, null);
            writeStore(context);
        } catch (KeyStoreException | IOException e) {
            e.printStackTrace();
        }
    }

    public void setSKC(UUID uuid, SecretKey key, Context context) {
        try {
            ks.setKeyEntry(uuid.toString() + SUFFIXSESSIONKEYCRYPTO, key, PASSWORD, null);
            writeStore(context);
        } catch (KeyStoreException | IOException e) {
            e.printStackTrace();
        }
    }

    public void removeSessionKeys(UUID uuid) {
        try {
            ks.deleteEntry(uuid.toString() + SUFFIXSESSIONKEYSIGN);
            ks.deleteEntry(uuid.toString() + SUFFIXSESSIONKEYCRYPTO);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }

    public byte[] negotiateSessionKeys(PublicKey publicKey, Context context) throws KeyNotFoundException {
        PrivateKey privateKey = getMyPrivateKey(context);
        byte[] sessionKey = new byte[]{};
        try {
            KeyAgreement kA = KeyAgreement.getInstance("ECDH", PN);
            kA.init(privateKey);
            kA.doPhase(publicKey, true);
            sessionKey = kA.generateSecret();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sessionKey;
    }


    // My Key Handling

    private void setMyKey(KeyPair keyPair, Context context) {
        X509Certificate certificate;
        try {
            certificate = LibCert.generateCertificate(keyPair);
            Certificate[] certChain = {certificate};
            ks.setKeyEntry(MYUUID.toString() + SUFFIXPRIVATE, keyPair.getPrivate(), PASSWORD, certChain);
            writeStore(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void genAndSetMyKeys(Context context) {
        ECGenParameterSpec ecParamSpec = new ECGenParameterSpec("secp521r1"); // TODO: Change curve to incompatible to pyg8

        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDH", PN);
            kpg.initialize(ecParamSpec);
            setMyKey(kpg.generateKeyPair(), context);
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
            e.printStackTrace();
        }


    }

    public PublicKey getMyPublicKey(Context context) {
        try {
            Certificate cert = ks.getCertificate(MYALIAS);

            if (cert == null) {
                genAndSetMyKeys(context);
                cert = ks.getCertificate(MYALIAS);
                // throw new Error("There should always be my public key!");
            }
            return cert.getPublicKey();
        } catch (Exception e) {
            throw new Error("KS Should be initialized");
        }
    }

    public byte[] getMyPublicKeyBytes(Context context) {
        PublicKey pk = getMyPublicKey(context);
        return Base64.encode(pk.getEncoded());
    }

    private PrivateKey getMyPrivateKey(Context context) {
        try {
            return (PrivateKey) getKey(MYUUID, SUFFIXPRIVATE);
        } catch (KeyNotFoundException e) {
            genAndSetMyKeys(context);
            try {
                return (PrivateKey) getKey(MYUUID, SUFFIXPRIVATE);
            } catch (KeyNotFoundException e1) {
                throw new Error("There should always be my private key!");
            }
        }
    }


    private Key getKey(UUID uuid, String suffix) throws KeyNotFoundException {
        return getKey(uuid.toString(), suffix);
    }

    private Key getKey(String uuid, String suffix) throws KeyNotFoundException {
        String alias = uuid + suffix;
        try {
            if (ks.isKeyEntry(alias)) {
                return ks.getKey(alias, PASSWORD);
            } else {
                throw new KeyNotFoundException(alias);
            }
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new KeyNotFoundException(alias);
        }
    }


    // Get key helpers

    public HashMap<String, SecretKey[]> getBothKeyMap() {
        HashMap<String, SecretKey[]> bothAliasMap = new HashMap<>();
        try {
            for (String alias : getAliasList()) {
                if (alias.endsWith("sks")) {
                    String uuid = alias.substring(0, alias.length() - SUFFIXLENGTH);
                    bothAliasMap.put(uuid, new SecretKey[]{
                            createSecretKey(getKey(uuid, SUFFIXSESSIONKEYSIGN)),
                            createSecretKey(getKey(uuid, SUFFIXSESSIONKEYCRYPTO))
                    });
                }
            }
        } catch (KeyStoreException | KeyNotFoundException e) {
            e.printStackTrace();
        }
        return bothAliasMap;
    }

    private ArrayList<String> getAliasList() throws KeyStoreException {
        return Collections.list(ks.aliases());
    }

    private SecretKey createSecretKey(Key key) {
        byte[] keyBytes = key.getEncoded();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
    }

    // Static Methods

    public static KeyStore createFromCertificate(Certificate certificate) throws KeyStoreException {
        try {
            KeyStore ks = KeyStore.getInstance("BKS", Security.getProvider(PN));
            ks.load(null);
            ks.setCertificateEntry("ca", certificate);
            return ks;
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new Error(e.getMessage());
        }
    }

    //Delete


    public boolean containsKey(String alias, Context context) {
        boolean result = false;
        try {
            result = ks.containsAlias(alias);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean containsSKS(UUID uuid, Context context) {
        return containsKey(uuid.toString()+SUFFIXSESSIONKEYSIGN, context);
    }

    public boolean containsSKC(UUID uuid, Context context) {
        return containsKey(uuid.toString()+SUFFIXSESSIONKEYCRYPTO, context);
    }

    public boolean containsSKSandSKC(UUID uuid, Context context) {
        boolean result1 = containsSKS(uuid, context);
        boolean result2 = containsSKC(uuid, context);
        return result1 && result2;
    }


//private static void loadKSH(Context context) {
//    initKeystore();
//    try {
//        ks.containsAlias(MYALIAS);
//    } catch (KeyStoreException e1) {
//        try {
//            ks = loadFromFile(ksFileName, ksPassword, context);
//            if (!ks.containsAlias(MYALIAS)) {
//                generateNewKeys(context);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}


//
//    private static void writeStore(Context context) {
//        try {
//            KeystoreHandler.writeStore(ksFileName, ksPassword, ks, context);
//        } catch (KeyStoreException | IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    //helper for KeyFileGeneration
//    static void setKeyfileName(String name) {
//        ks = null;
//        ksFileName = name;
//    }

//    private static ArrayList<String> filter(ArrayList<String> coll, String suffix) {
//        ArrayList<String> internColl = new ArrayList<>();
//        for (String s : coll) {
//            if (s.endsWith(suffix)) {
//                internColl.add(s);
//            }
//        }
//        return internColl;
//    }
//
//    private static ArrayList<String> getSKCAliasList() {
//        ArrayList<String> skcAliasList = new ArrayList<>();
//        try {
//            skcAliasList = filter(getAliasList(), SUFFIXSESSIONKEYCRYPTO);
//        } catch (KeyStoreException e) {
//            e.printStackTrace();
//        }
//        return skcAliasList;
//    }
//
//    private static ArrayList<String> getSKSAliasList() {
//        ArrayList<String> sksAliasList = new ArrayList<>();
//        try {
//            sksAliasList = filter(getAliasList(), SUFFIXSESSIONKEYSIGN);
//        } catch (KeyStoreException e) {
//            e.printStackTrace();
//        }
//        return sksAliasList;
//    }

//    private static String getSKCalias(UUID uuid) {
//        return uuid.toString() + SUFFIXSESSIONKEYCRYPTO;
//    }
//
//    private static String getSKSalias(UUID uuid) {
//        return uuid.toString() + SUFFIXSESSIONKEYSIGN;
//    }

//    private static void initKeystore() {
//        if (ks == null) {
//            try {
//                ks = KeyStore.getInstance("BKS", Security.getProvider(BC));
//            } catch (KeyStoreException e) {
//                e.printStackTrace();
//            }
//        }
//    }

//    private String cutSuffix(String alias) {
//        return alias.substring(0, alias.length() - SUFFIXLENGTH);
//    }

//    private static HashMap<String, SecretKey> getKeyList(ArrayList<String> aliasList) {
//        HashMap<String, SecretKey> keyMap = new HashMap<>();
//        for (String alias : aliasList) {
//            try {
//                keyMap.put(alias, createSecretKey(getKey(alias)));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return keyMap;
//    }

//    private static String changeSuffixToSKC(String alias){
//        return cutSuffix(alias)+ SUFFIXSESSIONKEYCRYPTO;
//    }


}

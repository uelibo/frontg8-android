package ch.frontg8.lib.crypto;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;
import org.spongycastle.jce.provider.BouncyCastleProvider;

public class LibCrypto {
    private static final String BC = BouncyCastleProvider.PROVIDER_NAME;
    static {
        Security.addProvider(new BouncyCastleProvider());
    }


    public static KeyPair genECDHKeys() throws InvalidAlgorithmParameterException, NoSuchProviderException, NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException {

        KeyStore ks = KeyStore.getInstance("AndroidKeyStore", Security.getProvider(BC));

        ECGenParameterSpec ecParamSpec = new ECGenParameterSpec("secp224k1");
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDH", BC);
        kpg.initialize(ecParamSpec);

        KeyPair kp = kpg.generateKeyPair();
        PrivateKey privKey = kp.getPrivate();
        KeyStore.PrivateKeyEntry kspe = new KeyStore.PrivateKeyEntry(privKey, null);

        // ks.aliases(); get Enumnerator for available keys
        ks.setEntry("MyKey", kspe, null);

        return kp;
    }

}

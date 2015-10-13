package ch.frontg8.lib.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import org.spongycastle.jce.provider.BouncyCastleProvider;

public class LibCrypto {
    private static final String BC = BouncyCastleProvider.PROVIDER_NAME;
    static {
        Security.addProvider(new BouncyCastleProvider());
    }


    public static KeyPair genECDHKeys() throws InvalidAlgorithmParameterException, NoSuchProviderException, NoSuchAlgorithmException {

        ECGenParameterSpec ecParamSpec = new ECGenParameterSpec("secp224k1");
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDH", BC);
        kpg.initialize(ecParamSpec);

        KeyPair kp = kpg.generateKeyPair();

        return kp;
    }

}

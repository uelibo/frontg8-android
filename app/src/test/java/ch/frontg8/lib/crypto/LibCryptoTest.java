package ch.frontg8.lib.crypto;

import android.test.mock.MockContext;

import junit.framework.TestCase;

import org.junit.Test;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.encoders.Base64;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Random;
import java.util.UUID;

import static ch.frontg8.lib.crypto.LibCrypto.containsSKSandSKC;
import static ch.frontg8.lib.crypto.LibCrypto.decryptMSG;
import static ch.frontg8.lib.crypto.LibCrypto.encryptMSG;
import static ch.frontg8.lib.crypto.LibCrypto.generateNewKeys;
import static ch.frontg8.lib.crypto.LibCrypto.getMyPublicKey;
import static ch.frontg8.lib.crypto.LibCrypto.negotiateSessionKeys;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by tstauber on 13.10.15.
 */
public class LibCryptoTest extends TestCase {
    MockContext mc = new MyMockContext();

    @Test
    public void testGenECDHKeys() throws Exception {
        generateNewKeys(mc);
        PublicKey pk1 = getMyPublicKey(mc);
        generateNewKeys(mc);
        PublicKey pk2 = getMyPublicKey(mc);
        assertNotEquals(pk1, pk2);
    }

    @Test
    public void testNegotiateKeys() throws Exception {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID uuid3 = UUID.randomUUID();

        generateNewKeys(mc);

        negotiateSessionKeys(uuid1, genKeyPair().getPublic(), mc);
        negotiateSessionKeys(uuid2, genKeyPair().getPublic(), mc);
        negotiateSessionKeys(uuid3, genKeyPair().getPublic(), mc);

        assertTrue(containsSKSandSKC(uuid1, mc));
        assertTrue(containsSKSandSKC(uuid2, mc));
        assertTrue(containsSKSandSKC(uuid3, mc));
    }

    @Test
    public void testEncryptDecrypt() throws Exception {
        UUID uuid1 = UUID.randomUUID();

        generateNewKeys(mc);

        byte[] plaintext = new byte[1024];
        new Random().nextBytes(plaintext);

        KeyPair kp = genKeyPair();

        negotiateSessionKeys(uuid1, kp.getPublic(), mc);

        byte[] ciphertext = encryptMSG(uuid1, plaintext, mc);
        byte[] decryptedtext = decryptMSG(ciphertext, mc);

        String encoded = Base64.toBase64String(plaintext);
        System.out.println("Plain: " + encoded);

        String decrypted = Base64.toBase64String(decryptedtext);
        System.out.println("Decrypted: " + decrypted);

        assertTrue(encoded.equals(decrypted));
    }

    private KeyPair genKeyPair() {
        try {
            ECGenParameterSpec ecParamSpec = new ECGenParameterSpec("secp521r1");
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME);
            kpg.initialize(ecParamSpec);
            return kpg.generateKeyPair();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return null;
    }
}
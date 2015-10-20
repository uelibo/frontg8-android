package ch.frontg8.lib.crypto;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Base64;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Random;
import java.util.UUID;

import static ch.frontg8.lib.crypto.LibCrypto.containsKey;
import static ch.frontg8.lib.crypto.LibCrypto.containsSKSandSKC;
import static ch.frontg8.lib.crypto.LibCrypto.decryptMSG;
import static ch.frontg8.lib.crypto.LibCrypto.encryptMSG;
import static ch.frontg8.lib.crypto.LibCrypto.genECDHKeys;
import static ch.frontg8.lib.crypto.LibCrypto.genSKCandSKS;
import static ch.frontg8.lib.crypto.LibCrypto.setMyKey;
import static org.junit.Assert.*;

/**
 * Created by tstauber on 13.10.15.
 */
public class LibCryptoTest extends TestCase {

    @Before
    public void initialize() {
    }

    @Test
    public void testGenECDHKeys() throws Exception {
        KeyPair kp = genECDHKeys();
        KeyPair kp2 = genECDHKeys();
        assertNotEquals(kp, kp2);
    }

    @Test
    public void testSetMyKey() throws Exception {
        KeyPair kp = genECDHKeys();
        setMyKey(kp);
        String alias = "00000000-0000-0000-0000-000000000000pri";
        assertTrue(containsKey(alias));
    }



    @Test
    public void testNegotiateKeys() throws Exception{
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID uuid3 = UUID.randomUUID();

        KeyPair kp = genECDHKeys();
        setMyKey(kp);
        KeyPair kp1 = genECDHKeys();
        PublicKey pk1 = kp1.getPublic();
        KeyPair kp2 = genECDHKeys();
        PublicKey pk2 = kp2.getPublic();
        KeyPair kp3 = genECDHKeys();
        PublicKey pk3 = kp3.getPublic();

        genSKCandSKS(uuid1, pk1);
        genSKCandSKS(uuid2, pk2);
        genSKCandSKS(uuid3, pk3);

        assertTrue(containsSKSandSKC(uuid1));
        assertTrue(containsSKSandSKC(uuid2));
        assertTrue(containsSKSandSKC(uuid3));
    }

    @Test
    public void testEncryptDecrypt() throws Exception{
        UUID uuid1 = UUID.randomUUID();

        KeyPair kp = genECDHKeys();
        setMyKey(kp);
        KeyPair kp1 = genECDHKeys();
        PublicKey pk1 = kp1.getPublic();

        byte[] plaintext = new byte[1024];
        new Random().nextBytes(plaintext);

        genSKCandSKS(uuid1, pk1);

        byte[] ciphertext = encryptMSG(uuid1, plaintext);
        byte[] decryptedtext = decryptMSG(ciphertext);

        String encoded = Base64.toBase64String(plaintext);
        System.out.println("Plain: " + encoded);

        String decrypted = Base64.toBase64String(decryptedtext);
        System.out.println("Decrypted: " + decrypted);

        assertTrue(encoded.equals(decrypted));
    }
}
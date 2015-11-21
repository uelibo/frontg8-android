package ch.frontg8.lib.crypto;

import android.test.mock.MockContext;

import org.junit.Assert;
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

import static org.junit.Assert.assertNotEquals;


public class LibCryptoTest {
    private MockContext mc = new MyMockContext();

    @Test
    public void testGetKey() {
        System.out.println(new String(LibCrypto.getMyPublicKeyBytes(mc)));
    }


    @Test
    public void testContainsSKSandSKC() throws Exception {
        UUID uuid1 = UUID.randomUUID();
        LibCrypto.generateNewKeys(mc);
        LibCrypto.negotiateSessionKeys(uuid1, genKeyPair().getPublic(), mc);
        Assert.assertTrue(LibCrypto.containsSKSandSKC(uuid1, mc));
    }


    @Test
    public void testGenECDHKeys() throws Exception {
        LibCrypto.generateNewKeys(mc);
        PublicKey pk1 = LibCrypto.getMyPublicKey(mc);
        LibCrypto.generateNewKeys(mc);
        PublicKey pk2 = LibCrypto.getMyPublicKey(mc);
        assertNotEquals(pk1, pk2);
    }

    @Test
    public void testNegotiateKeys() throws Exception {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID uuid3 = UUID.randomUUID();

        LibCrypto.generateNewKeys(mc);

        LibCrypto.negotiateSessionKeys(uuid1, genKeyPair().getPublic(), mc);
        LibCrypto.negotiateSessionKeys(uuid2, genKeyPair().getPublic(), mc);
        LibCrypto.negotiateSessionKeys(uuid3, genKeyPair().getPublic(), mc);

        Assert.assertTrue(LibCrypto.containsSKSandSKC(uuid1, mc));
        Assert.assertTrue(LibCrypto.containsSKSandSKC(uuid2, mc));
        Assert.assertTrue(LibCrypto.containsSKSandSKC(uuid3, mc));
    }

    @Test
    public void testEncryptDecrypt() throws Exception {
        UUID uuid1 = UUID.randomUUID();

        LibCrypto.generateNewKeys(mc);

        byte[] plaintext = new byte[1024];
        new Random().nextBytes(plaintext);

        KeyPair kp = genKeyPair();

        LibCrypto.negotiateSessionKeys(uuid1, kp.getPublic(), mc);

        byte[] ciphertext = LibCrypto.encryptMSG(uuid1, plaintext, mc);
        byte[] decryptedtext = LibCrypto.decryptMSG(ciphertext, mc)._2;


        String encoded = Base64.toBase64String(plaintext);
        System.out.println("Plain: " + encoded);

        String decrypted = decryptedtext == null ? "null" : Base64.toBase64String(decryptedtext);
        System.out.println("Decrypted: " + decrypted);

        Assert.assertTrue(encoded.equals(decrypted));
    }

    private KeyPair genKeyPair() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        ECGenParameterSpec ecParamSpec = new ECGenParameterSpec("secp521r1");
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME);
        kpg.initialize(ecParamSpec);
        return kpg.generateKeyPair();
    }
}
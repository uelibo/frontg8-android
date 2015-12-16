package ch.frontg8.lib.crypto;

import android.test.mock.MockContext;

import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

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
    private KeystoreHandler ksh = new KeystoreHandler(mc);

    @Test
    public void testGetKey() {
        System.out.println(new String(LibCrypto.getMyPublicKeyBytes(ksh, mc)));
    }


    @Test
    public void testContainsSKSandSKC() throws Exception {
        UUID uuid1 = UUID.randomUUID();
        LibCrypto.generateNewKeys(ksh, mc);
        LibCrypto.negotiateSessionKeys(uuid1, genKeyPair().getPublic(), ksh, mc);
        Assert.assertTrue(LibCrypto.containsSKSandSKC(uuid1, ksh));
    }

    @Test
    public void testKDF3() {
        final int KEYSIZE = 32;
        String hexsting = "01bfeaec8ba4ad8ce9b4daaa70187e558553e41b2324df7750c8f0f1eca10b7f";
        byte[] sk = hexsting.getBytes();
        byte[] skcBytes = Arrays.copyOfRange(sk, 0, KEYSIZE);
        byte[] sksBytes = Arrays.copyOfRange(sk, sk.length - KEYSIZE, sk.length);
        byte[] derivedSkc = LibCrypto.kdf3SHA256(skcBytes, new byte[]{});
        byte[] derivedSks = LibCrypto.kdf3SHA256(sksBytes, new byte[]{});
        System.out.println("skc:\n" + new String(Hex.encode(derivedSkc)));
        System.out.println("sks:\n" + new String(Hex.encode(derivedSks)));
    }


    @Test
    public void testGenECDHKeys() throws Exception {
        LibCrypto.generateNewKeys(ksh, mc);
        PublicKey pk1 = LibCrypto.getMyPublicKey(ksh, mc);
        LibCrypto.generateNewKeys(ksh, mc);
        PublicKey pk2 = LibCrypto.getMyPublicKey(ksh, mc);
        assertNotEquals(pk1, pk2);
    }

    @Test
    public void testNegotiateKeys() throws Exception {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID uuid3 = UUID.randomUUID();

        LibCrypto.generateNewKeys(ksh, mc);

        LibCrypto.negotiateSessionKeys(uuid1, genKeyPair().getPublic(), ksh, mc);
        LibCrypto.negotiateSessionKeys(uuid2, genKeyPair().getPublic(), ksh, mc);
        LibCrypto.negotiateSessionKeys(uuid3, genKeyPair().getPublic(), ksh, mc);

        Assert.assertTrue(LibCrypto.containsSKSandSKC(uuid1, ksh));
        Assert.assertTrue(LibCrypto.containsSKSandSKC(uuid2, ksh));
        Assert.assertTrue(LibCrypto.containsSKSandSKC(uuid3, ksh));
    }

    @Test
    public void testEncryptDecrypt() throws Exception {
        UUID uuid1 = UUID.randomUUID();

        LibCrypto.generateNewKeys(ksh, mc);

        byte[] plaintext = new byte[1024];
        new Random().nextBytes(plaintext);

        KeyPair kp = genKeyPair();

        LibCrypto.negotiateSessionKeys(uuid1, kp.getPublic(), ksh, mc);

        byte[] ciphertext = LibCrypto.encryptMSG(uuid1, plaintext, ksh);
        byte[] decryptedtext = LibCrypto.decryptMSG(ciphertext, ksh)._2;


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
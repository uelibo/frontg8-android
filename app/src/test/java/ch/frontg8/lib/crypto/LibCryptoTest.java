package ch.frontg8.lib.crypto;

import junit.framework.TestCase;

import org.junit.Test;

import java.security.KeyPair;

import static ch.frontg8.lib.crypto.LibCrypto.genECDHKeys;
import static org.junit.Assert.*;

/**
 * Created by tstauber on 13.10.15.
 */
public class LibCryptoTest extends TestCase {

    @Test
    public void testGenECDHKeys() throws Exception {
        KeyPair kp = genECDHKeys();
        System.out.println(kp.getPublic() + " | " + kp.getPrivate());
    }
}
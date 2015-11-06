package ch.frontg8.lib.crypto;

import android.test.mock.MockContext;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.util.UUID;

import static ch.frontg8.lib.crypto.LibCrypto.*;
/**
 * Created by tstauber on 06.11.15.
 */
public class KeyFileGenerator {
    private static MockContext mc =new MyMockContext();

    public static void generateKeyStoreFiles(){
        UUID uuid1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID uuid2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
        
        
        setKeyfileName("KS01");
        try {
            generateNewKeys(mc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        PublicKey pubkey1 = getMyPublicKey(mc);
        
        setKeyfileName("KS02");
        try {
            generateNewKeys(mc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        PublicKey pubkey2 = getMyPublicKey(mc);
        try {
            negotiateSessionKeys(uuid1,pubkey1,mc);
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        setKeyfileName("KS01");
        try {
            negotiateSessionKeys(uuid2, pubkey2,mc);
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        generateKeyStoreFiles();
    }

}

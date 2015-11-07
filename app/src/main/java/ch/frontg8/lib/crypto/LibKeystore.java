package ch.frontg8.lib.crypto;

import android.content.Context;

import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

/**
 * Created by tstauber on 07.11.15.
 */
public class LibKeystore {
    private static final String BC = BouncyCastleProvider.PROVIDER_NAME;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static boolean isInitialized(KeyStore ks) {
        try {
            ks.store(null);
        } catch (KeyStoreException e) {
            return false;
        } catch (Exception e) {
            return true;
        }
        return false;
    }

    private static KeyStore createKeystore() throws KeyStoreException {
        KeyStore ks = KeyStore.getInstance("BKS", Security.getProvider(BC));
        try {
            ks.load(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ks;
    }

    public static KeyStore loadFromFile(String path, char[] password, Context context) throws KeyStoreException, IOException {
        KeyStore ks = createKeystore();
        try (InputStream is = context.getResources().openRawResource(context.getResources().getIdentifier("raw/" + path, "raw", context.getPackageName())) {
            ks.load(is, password);
        } catch (FileNotFoundException fnfe) {
            try {
                writeStore(path, password, ks, context);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return ks;
    }


    public static void writeStore(String path, char[] password, KeyStore ks, Context context) throws KeyStoreException, IOException {
        try (OutputStream os = context.openFileOutput(path,Context.MODE_PRIVATE)) {
            ks.store(os, password);
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static KeyStore createFromCertificate(Certificate certificate) {
        KeyStore ks = null;
        try {
            ks = createKeystore();
            ks.setCertificateEntry("ca", certificate);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return ks;
    }
}

package ch.frontg8.lib.crypto;

import android.content.Context;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class LibSSLContext {

    public static SSLContext getSSLContext(String certificatePath, Context context) {
        SSLContext sslContext = null;
        X509Certificate cert;
        try {
            cert = LibCert.loadX509CertificateFromFile(certificatePath, context);
            KeyStore ks = KeystoreHandler.createFromCertificate(cert);

            //TODO; refactor to trustStore instead of keystore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(ks);

            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sslContext;
    }

}

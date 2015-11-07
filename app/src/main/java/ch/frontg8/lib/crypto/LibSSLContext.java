package ch.frontg8.lib.crypto;

import android.content.Context;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import static ch.frontg8.lib.crypto.LibCert.loadX509CertificateFromFile;
import static ch.frontg8.lib.crypto.LibKeystore.createFromCertificate;

public class LibSSLContext {

    public static SSLContext getSSLContext(String certpath, Context context) {
        SSLContext sslContext = null;
        X509Certificate cert;
        try {
            cert = loadX509CertificateFromFile(certpath, context);
            KeyStore ks = createFromCertificate(cert);

            //TODO; refactor to truststore instead of keystore
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

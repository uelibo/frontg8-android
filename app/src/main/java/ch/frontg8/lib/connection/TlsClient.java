package ch.frontg8.lib.connection;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import ch.frontg8.lib.message.MessageHelper;

public class TlsClient {
    private String hostname;
    private int port;
    private Logger Log;
    private Context context;
    private SSLSocket socket = null;

    public TlsClient(String hostname, int port, Logger Log, Context context) {
        this.hostname = hostname;
        this.port = port;
        this.Log = Log;
        this.context = context;
    }

    public boolean isConnected() {
        if (socket == null) { return false; }
        else { return socket.isConnected(); }
    }

    public void connect() {
        SSLSocketFactory factory = HttpsURLConnection.getDefaultSSLSocketFactory();
        CertificateFactory cf = null;
        Certificate ca;
        InputStream caInput = null;
        SSLContext sslContext = null;

        try {
            cf = CertificateFactory.getInstance("X.509");

            InputStream ins = context.getResources().openRawResource(
                    context.getResources().getIdentifier("raw/root",
                            "raw", context.getPackageName()));

            caInput = new BufferedInputStream(ins);

            ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            Log.TRACE("ca=" + ((X509Certificate) ca).getSubjectDN());

            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);

        } catch (CertificateException e) {
            e.printStackTrace();
            Log.TRACE(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.TRACE(e.getMessage());
        } catch (KeyStoreException e) {
            e.printStackTrace();
            Log.TRACE(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.TRACE(e.getMessage());
        } catch (KeyManagementException e) {
            e.printStackTrace();
            Log.TRACE(e.getMessage());
        } finally {
            try {
                caInput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //SSLSocket socket = null;
        try {
            //socket = (SSLSocket) factory.createSocket(hostname, port);
            socket = (SSLSocket) sslContext.getSocketFactory().createSocket(hostname, port);
        } catch (UnknownHostException e) {
            Log.TRACE("factory.createSocket >> UnknownHostException");
        } catch (IOException e) {
            Log.TRACE("factory.createSocket >> IOException");
        }
        Log.TRACE("factory.createSocket >> successful");
        /**
         * Starts an SSL handshake on this connection. Common reasons include a
         * need to use new encryption keys, to change cipher suites, or to
         * initiate a new session. To force complete reauthentication, the
         * current session could be invalidated before starting this handshake.
         * If data has already been sent on the connection, it continues to flow
         * during this handshake. When the handshake completes, this will be
         * signaled with an event. This method is synchronous for the initial
         * handshake on a connection and returns when the negotiated handshake
         * is complete. Some protocols may not support multiple handshakes on an
         * existing socket and may throw an IOException.
         */
        try {
            socket.startHandshake();
        } catch (IOException e) {
            Log.TRACE("socket.startHandshake >> IOException");
            Log.TRACE(e.getMessage());

        }
        Log.TRACE("Handshaking Complete");

        /**
         * Retrieve the server's certificate chain
         *
         * Returns the identity of the peer which was established as part of
         * defining the session. Note: This method can be used only when using
         * certificate-based cipher suites; using it with non-certificate-based
         * cipher suites, such as Kerberos, will throw an
         * SSLPeerUnverifiedException.
         *
         *
         * Returns: an ordered array of peer certificates, with the peer's own
         * certificate first followed by any certificate authorities.
         */
        Certificate[] serverCerts = null;
        try {
            serverCerts = socket.getSession().getPeerCertificates();
        } catch (SSLPeerUnverifiedException e) {
            Log.TRACE(" socket.getSession().getPeerCertificates >> SSLPeerUnverifiedException");
        }
        Log.TRACE("Retreived Server's Certificate Chain");
        Log.TRACE(serverCerts.length + "Certifcates Found\n\n\n");
        for (int i = 0; i < serverCerts.length; i++) {
            Certificate myCert = serverCerts[i];
            Log.TRACE("====Certificate:" + (i + 1) + "====");
            Log.TRACE("-Public Key-\n" + myCert.getPublicKey());
            Log.TRACE("-Certificate Type-\n " + myCert.getType());
            Log.TRACE("");
        }
    }

    void sendBytes(byte[] packet) {
        Log.TRACE("sending packet ");
        try {
            socket.getOutputStream().write(packet);
            Log.TRACE("sending packet succeeded");
        } catch (IOException e1) {
            Log.TRACE("socket.getOutputStream().write >> IOException");
        }
    }

    byte[] getBytes(int length){
        Log.TRACE("recving packet");
        byte[] recv = new byte[length];
        try {
            int recvLen = socket.getInputStream().read(recv, 0, recv.length);
            Log.TRACE(MessageHelper.byteArrayAsHexString(recv));
            String str = new String(recv, 0, recvLen);
            Log.TRACE("recv packet = " + str);
        } catch (IOException e1) {
            Log.TRACE("socket.getInputStream().read >> IOException");
        }
        return recv;
    }

    void close(){
        try {
            Log.TRACE("closing socket");
            socket.close();
            Log.TRACE("socket closed");
        } catch (IOException e) {
            Log.TRACE("socket.close >> IOException");
        }
    }

}

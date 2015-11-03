package ch.frontg8.lib.connection;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import ch.frontg8.R;

public class TlsTest {
    Activity context;

    public TlsTest(Activity context) {
        this.context = context;
    }

    private void TRACE(final String log) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.w("MainActivity", log);
                TextView textViewLog = (TextView) context.findViewById(R.id.textViewLog);
                textViewLog.append(log + "\r\n");
            }
        });
    }

    public void RunTlsTest() {
        /**
         * 443 is the network port number used by the SSL https: URi scheme.
         */
        int port = 40001;
        String hostname = "redmine.frontg8.ch";
        SSLSocketFactory factory = HttpsURLConnection.getDefaultSSLSocketFactory();

        TRACE("Creating a SSL Socket For " + hostname + " on port " + port);

        SSLSocket socket = null;
        try {
            socket = (SSLSocket) factory.createSocket(hostname, port);
        } catch (UnknownHostException e) {
            TRACE("factory.createSocket >> UnknownHostException");
        } catch (IOException e) {
            TRACE("factory.createSocket >> IOException");
        }
        TRACE("factory.createSocket >> successful");
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
            TRACE("socket.startHandshake >> IOException");
            TRACE(e.getMessage());

        }
        TRACE("Handshaking Complete");

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
            TRACE(" socket.getSession().getPeerCertificates >> SSLPeerUnverifiedException");
        }
        TRACE("Retreived Server's Certificate Chain");
        TRACE(serverCerts.length + "Certifcates Found\n\n\n");
        for (int i = 0; i < serverCerts.length; i++) {
            Certificate myCert = serverCerts[i];
            TRACE("====Certificate:" + (i + 1) + "====");
            TRACE("-Public Key-\n" + myCert.getPublicKey());
            TRACE("-Certificate Type-\n " + myCert.getType());
            TRACE("");
        }

        // String packet = "GET / HTTP/1.1\r\n\r\n";
        // packet.getBytes()

        byte[] packet = new byte[] {
                (byte)0x00, 0x14, 0x01, 0x00, 0x0a, 0x12, 0x30, 0x30,
                (byte)0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30,
                (byte)0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30
        };


        //  0x00, 0x14, 0x01, 0x00, 0x0a, 0x12, 0x30, 0x30,
        //  0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30,
        //  0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30,


        TRACE("sending packet ");
        try {
            socket.getOutputStream().write(packet);
            TRACE("sending packet succeeded");
        } catch (IOException e1) {
            TRACE("socket.getOutputStream().write >> IOException");
        }
        TRACE("recving packet");
        byte[] recv = new byte[10000];
        try {
            int recvLen = socket.getInputStream().read(recv, 0, recv.length);
            String str = new String(recv, 0, recvLen);
            TRACE("recv packet = " + str);
        } catch (IOException e1) {
            TRACE("socket.getInputStream().read >> IOException");
        }

        try {
            TRACE("closing socket");
            socket.close();
            TRACE("socket closed");
        } catch (IOException e) {
            TRACE("socket.close >> IOException");
        }
    }
}

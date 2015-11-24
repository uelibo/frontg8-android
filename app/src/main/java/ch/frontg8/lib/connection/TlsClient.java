package ch.frontg8.lib.connection;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.security.cert.Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;

import ch.frontg8.lib.message.MessageHelper;

public class TlsClient {
    private String hostname;
    private int port;
    private Logger log;
    private SSLContext sslContext;
    private SSLSocket socket = null;

    public TlsClient(String hostname, int port, Logger Log, SSLContext sslContext) {
        this.hostname = hostname;
        this.port = port;
        this.log = Log;
        this.sslContext = sslContext;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    private boolean throwExceptionIfNotConnected() throws NotConnectedException {
        if (isConnected()) {
            return true;
        } else {
            throw new NotConnectedException("not Connected");
        }
    }

    public void connect() throws NotConnectedException {
        getSocket(sslContext);
        tlsHandshake();
        listCerts(); // TODO: remove debugstuff
    }

    private void getSocket(SSLContext sslContext) {
        try {
            socket = (SSLSocket) sslContext.getSocketFactory().createSocket(hostname, port);
        } catch (UnknownHostException e) {
            Log.e("TLS", "factory.createSocket >> UnknownHostException", e);
        } catch (IOException e) {
            Log.e("TLS", "factory.createSocket >> IOException", e);
        }
        Log.e("TLS", "factory.createSocket >> successful");
    }


    private void tlsHandshake() throws NotConnectedException {
        if (throwExceptionIfNotConnected()) {
            try {
                socket.startHandshake();
            } catch (IOException e) {
                log.TRACE("socket.startHandshake >> IOException");
                log.TRACE(e.getMessage());

            }
            log.TRACE("Handshaking Complete");
        }
    }

    private void listCerts() throws NotConnectedException {
        if (throwExceptionIfNotConnected()) {
            Certificate[] serverCerts;
            try {
                serverCerts = socket.getSession().getPeerCertificates();
                log.TRACE("Retreived Server's Certificate Chain");
                log.TRACE(serverCerts.length + "Certifcates Found\n\n\n");
                for (int i = 0; i < serverCerts.length; i++) {
                    Certificate myCert = serverCerts[i];
                    log.TRACE("====Certificate:" + (i + 1) + "====");
                    log.TRACE("-Public Key-\n" + myCert.getPublicKey());
                    log.TRACE("-Certificate Type-\n " + myCert.getType());
                    log.TRACE("");
                }
            } catch (SSLPeerUnverifiedException e) {
                log.TRACE(" socket.getSession().getPeerCertificates >> SSLPeerUnverifiedException");
            }
        }
    }

    public void sendBytes(byte[] packet) throws NotConnectedException {
        if (throwExceptionIfNotConnected()) {
            log.TRACE("sending packet ");
            try {
                socket.getOutputStream().write(packet);
                socket.getOutputStream().flush();
                log.TRACE("sending packet succeeded");
            } catch (IOException e1) {
                log.TRACE("socket.getOutputStream().write >> IOException");
            }
        }
    }

    public byte[] getBytes(int length) throws NotConnectedException {
        byte[] recv = new byte[length];
        if (throwExceptionIfNotConnected()) {
            log.TRACE("recving packet");
            try {
                int recvLen = socket.getInputStream().read(recv, 0, recv.length);
                log.TRACE(MessageHelper.byteArrayAsHexString(recv) + " (" + recvLen + ")");
            } catch (IOException e1) {
                log.TRACE("socket.getInputStream().read >> IOException");
            }
        }
        return recv;
    }

    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    public void close() {
        if (isConnected()) {
            try {
                log.TRACE("closing socket");
                socket.close();
                log.TRACE("socket closed");
            } catch (IOException e) {
                log.TRACE("socket.close >> IOException");
            }
        }
    }

}
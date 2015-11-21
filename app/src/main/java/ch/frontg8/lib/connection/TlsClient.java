package ch.frontg8.lib.connection;

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
    private Logger Log;
    private SSLContext sslContext;
    private SSLSocket socket = null;

    public TlsClient(String hostname, int port, Logger Log, SSLContext sslContext) {
        this.hostname = hostname;
        this.port = port;
        this.Log = Log;
        this.sslContext = sslContext;
    }

    public boolean isConnected() {
        if (socket == null) {
            return false;
        } else {
            return socket.isConnected();
        }
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
            Log.TRACE("factory.createSocket >> UnknownHostException");
        } catch (IOException e) {
            Log.TRACE("factory.createSocket >> IOException");
        }
        Log.TRACE("factory.createSocket >> successful");
    }


    private void tlsHandshake() throws NotConnectedException {
        if (throwExceptionIfNotConnected()) {
            try {
                socket.startHandshake();
            } catch (IOException e) {
                Log.TRACE("socket.startHandshake >> IOException");
                Log.TRACE(e.getMessage());

            }
            Log.TRACE("Handshaking Complete");
        }
    }

    private void listCerts() throws NotConnectedException {
        if (throwExceptionIfNotConnected()) {
            Certificate[] serverCerts = null;
            try {
                serverCerts = socket.getSession().getPeerCertificates();
                Log.TRACE("Retreived Server's Certificate Chain");
                Log.TRACE(serverCerts.length + "Certifcates Found\n\n\n");
                for (int i = 0; i < serverCerts.length; i++) {
                    Certificate myCert = serverCerts[i];
                    Log.TRACE("====Certificate:" + (i + 1) + "====");
                    Log.TRACE("-Public Key-\n" + myCert.getPublicKey());
                    Log.TRACE("-Certificate Type-\n " + myCert.getType());
                    Log.TRACE("");
                }
            } catch (SSLPeerUnverifiedException e) {
                Log.TRACE(" socket.getSession().getPeerCertificates >> SSLPeerUnverifiedException");
            }
        }
    }

    public void sendBytes(byte[] packet) throws NotConnectedException {
        if (throwExceptionIfNotConnected()) {
            Log.TRACE("sending packet ");
            try {
                socket.getOutputStream().write(packet);
                socket.getOutputStream().flush();
                Log.TRACE("sending packet succeeded");
            } catch (IOException e1) {
                Log.TRACE("socket.getOutputStream().write >> IOException");
            }
        }
    }

    public byte[] getBytes(int length) throws NotConnectedException {
        byte[] recv = new byte[length];
        if (throwExceptionIfNotConnected()) {
            Log.TRACE("recving packet");
            try {
                int recvLen = socket.getInputStream().read(recv, 0, recv.length);
                Log.TRACE(MessageHelper.byteArrayAsHexString(recv) + " (" + recvLen + ")");
            } catch (IOException e1) {
                Log.TRACE("socket.getInputStream().read >> IOException");
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
                Log.TRACE("closing socket");
                socket.close();
                Log.TRACE("socket closed");
            } catch (IOException e) {
                Log.TRACE("socket.close >> IOException");
            }
        }
    }

}
package ch.frontg8.lib.connection;

import android.util.Log;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

public class TlsClient {
    private final String hostname;
    private final int port;
    private final SSLContext sslContext;
    private SSLSocket socket = null;
    private int autoReconnects = 0;

    public TlsClient(String hostname, int port, SSLContext sslContext) {
        this.hostname = hostname;
        this.port = port;
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
    }

    private void getSocket(SSLContext sslContext) {
        try {
            socket = (SSLSocket) sslContext.getSocketFactory().createSocket(hostname, port);
        } catch (UnknownHostException e) {
            Log.e("TLS", "factory.createSocket >> UnknownHostException", e);
        } catch (IOException e) {
            Log.e("TLS", "factory.createSocket >> IOException", e);
        }
        Log.d("TLS", "factory.createSocket >> successful");
    }


    private void tlsHandshake() throws NotConnectedException {
        if (throwExceptionIfNotConnected()) {
            try {
                socket.setSoTimeout(10000);
                socket.startHandshake();
            } catch (IOException e) {
                throw new NotConnectedException("Could not complete Handshake");
            }
        }
    }

    public void sendBytes(byte[] packet) throws NotConnectedException {
        if (throwExceptionIfNotConnected()) {
            Log.v("TLS", "sending packet ");
            try {
                socket.getOutputStream().write(packet);
                socket.getOutputStream().flush();
                Log.v("TLS", "sending packet succeeded");
            } catch (IOException e1) {
                Log.v("TLS", "socket.getOutputStream().write >> IOException");
            }
        }
    }

    public byte[] getBytes(int length) throws NotConnectedException {
        byte[] received = new byte[length];
        Log.v("TLS", "receiving packet");
        if (throwExceptionIfNotConnected()) try {
            socket.getInputStream().read(received, 0, received.length);
        } catch (IOException e1) {
            //TODO reconnect socket
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.e("TLS", "socket.getInputStream().read >> IOException", e1);
        }
        boolean socketOk = false;
        for (byte b : received) {
            if (b != 0x00) {
                socketOk = true;
                break;
            }
        }

        if (!socketOk) {
            if (autoReconnects <= 5) {
                connect();
                autoReconnects++;
            } else {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            autoReconnects = 0;
        }
        return received;
    }

//
//    public InputStream getInputStream() throws IOException {
//        return socket.getInputStream();
//    }
//
//    public OutputStream getOutputStream() throws IOException {
//        return socket.getOutputStream();
//    }

    public void close() {
        if (isConnected()) {
            try {
                Log.v("TLS", "closing socket");
                socket.close();
                Log.v("TLS", "socket closed");
            } catch (IOException e) {
                Log.v("TLS", "socket.close >> IOException");
            }
        }
    }

}
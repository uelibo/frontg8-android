package ch.frontg8.lib.connection;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import javax.net.ssl.SSLSocketFactory;

public class SslClient {
    private static String host = "www.google.ch";
    private static String path = "/";
    private static int port = 443;
    private static int timeout = 10;
    private static SSLSocketFactory clientFactory =
            (SSLSocketFactory) SSLSocketFactory.getDefault();

    private Socket socket;
    private PrintWriter writer;

    public void connect() {
        InetSocketAddress address = new InetSocketAddress(host, port);
        socket = new Socket();
        OutputStream output;
        try {
            socket.setKeepAlive(true);
            socket.setSoTimeout(timeout * 1000);
            socket.connect(address);
            output = socket.getOutputStream();
            writer = new PrintWriter(output);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            DataInputStream input = new DataInputStream(socket.getInputStream());
            android.util.Log.d("SSLSocketTest",
                    "GET https://" + host + path + " HTTP/1.1");

            // Send a request
            writer.println("GET https://" + host + path + " HTTP/1.1\r");
            writer.println("Host: " + host + "\r");
            writer.println("Connection: " + "Close\r");
            writer.println("\r");
            writer.flush();

            int length = -1;
            boolean chunked = false;

            String line = input.readLine();

            if (line == null) {
                throw new IOException("No response from server");
            }

            // Consume the headers, check content length and encoding type
            while (line != null && line.length() != 0) {
                System.out.println(line);
                android.util.Log.d("SSLSocketTest", line);
                int dot = line.indexOf(':');
                if (dot != -1) {
                    String key = line.substring(0, dot).trim();
                    String value = line.substring(dot + 1).trim();

                    if ("Content-Length".equalsIgnoreCase(key)) {
                        length = Integer.valueOf(value);
                    } else if ("Transfer-Encoding".equalsIgnoreCase(key)) {
                        chunked = "Chunked".equalsIgnoreCase(value);
                    }

                }
                line = input.readLine();
            }

            // Consume the content itself
            if (chunked) {
                length = Integer.parseInt(input.readLine(), 16);
                while (length != 0) {
                    byte[] buffer = new byte[length];
                    input.readFully(buffer);
                    input.readLine();
                    length = Integer.parseInt(input.readLine(), 16);
                }
                input.readLine();
            } else {
                byte[] buffer = new byte[length];
                input.readFully(buffer);
            }

            // Sleep for the given number of seconds
            Thread.sleep(1 * 1000);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //input.close();
        }
    }

}

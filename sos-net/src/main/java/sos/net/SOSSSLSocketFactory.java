package sos.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** @author Ghassan Beydoun */
public class SOSSSLSocketFactory extends SSLSocketFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSSLSocketFactory.class);
    private SSLSocketFactory sslFactory;
    private String proxyHost;
    private int proxyPort = 3128;
    private boolean done = false;
    private final String SECURITY_PROTOCOL = "TLS";
    private String securityProtocol = SECURITY_PROTOCOL;

    public SOSSSLSocketFactory(String pproxyHost, int pproxyPort, String psecurityProtocol) {
        this();
        this.proxyHost = pproxyHost;
        this.proxyPort = pproxyPort;
        this.securityProtocol = psecurityProtocol;
    }

    public SOSSSLSocketFactory(String pproxyHost, int pproxyPort) {
        this();
        this.proxyHost = pproxyHost;
        this.proxyPort = pproxyPort;
    }

    public SOSSSLSocketFactory() {
        SSLContext sslcontext = null;
        try {
            sslcontext = SSLContext.getInstance(securityProtocol);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error(e.getMessage(), e);
        }
        if (sslcontext != null) {
            try {
                sslcontext.init(null, new TrustManager[] { new SOSTrustManager() }, new java.security.SecureRandom());
            } catch (KeyManagementException e) {
                LOGGER.error(e.getMessage(), e);
            }
            sslFactory = sslcontext.getSocketFactory();
        }
    }

    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return sslFactory.createSocket(host, port);
    }

    public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort) throws IOException, UnknownHostException {
        return sslFactory.createSocket(host, port, clientHost, clientPort);
    }

    public Socket createSocket(InetAddress host, int port) throws IOException {
        return createSocket(null, host.getHostName(), port, true);
    }

    public Socket createSocket(InetAddress address, int port, InetAddress clientAddress, int clientPort) throws IOException {
        return createSocket(null, address.getHostName(), port, true);
    }

    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
        if (proxyHost != null && !proxyHost.isEmpty()) {
            Socket tunnel = new Socket(proxyHost, proxyPort);
            doTunnelHandshake(tunnel, host, port);
            SSLSocket sslSocket = (SSLSocket) sslFactory.createSocket(tunnel, host, port, autoClose);
            sslSocket.addHandshakeCompletedListener(new HandshakeCompletedListener() {

                public void handshakeCompleted(HandshakeCompletedEvent event) {
                    done = true;
                }
            });
            if (!done) {
                sslSocket.startHandshake();
            }
            return sslSocket;
        } else {
            return sslFactory.createSocket(socket, host, port, autoClose);
        }
    }

    private void doTunnelHandshake(Socket tunnel, String host, int port) throws IOException {
        OutputStream out = tunnel.getOutputStream();
        String msg =
                "CONNECT " + host + ":" + port + " HTTP/1.0\n" + "\r\n\r\n";
        byte b[];
        try {
            b = msg.getBytes("ASCII7");
        } catch (UnsupportedEncodingException ignored) {
            b = msg.getBytes();
        }
        out.write(b);
        out.flush();
        byte reply[] = new byte[200];
        int replyLen = 0;
        int newlinesSeen = 0;
        boolean headerDone = false;
        InputStream in = tunnel.getInputStream();
        while (newlinesSeen < 2) {
            int i = in.read();
            if (i < 0) {
                throw new IOException("Unexpected EOF from proxy");
            }
            if (i == '\n') {
                headerDone = true;
                ++newlinesSeen;
            } else if (i != '\r') {
                newlinesSeen = 0;
                if (!headerDone && replyLen < reply.length) {
                    reply[replyLen++] = (byte) i;
                }
            }
        }
        String replyStr;
        try {
            replyStr = new String(reply, 0, replyLen, "ASCII7");
        } catch (UnsupportedEncodingException ignored) {
            replyStr = new String(reply, 0, replyLen);
        }
        if (replyStr.toLowerCase().indexOf("200 connection established") == -1) {
            throw new IOException("Unable to tunnel through " + proxyHost + ":" + proxyPort + ".  Proxy returns \"" + replyStr + "\"");
        }
    }

    public String[] getDefaultCipherSuites() {
        return sslFactory.getDefaultCipherSuites();
    }

    public String[] getSupportedCipherSuites() {
        return sslFactory.getSupportedCipherSuites();
    }

    public ServerSocket createServerSocket(int arg0) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public ServerSocket createServerSocket(int arg0, int arg1) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public ServerSocket createServerSocket(int arg0, int arg1, InetAddress arg2) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

}
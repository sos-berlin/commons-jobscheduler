package com.sos.VirtualFileSystem.TCPIP;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

/** @author KB */
public class SOSVfsTcpIpServer {

    private static final Logger LOGGER = Logger.getLogger(SOSVfsTcpIpServer.class);

    public SOSVfsTcpIpServer() {
    }

    public static void main(final String args[]) {
        ServerSocket listenSocket = null;
        try {
            int serverPort = 6880;
            listenSocket = new ServerSocket(serverPort);
            LOGGER.info("server start listening... ... ...");
            while (true) {
                Socket clientSocket = listenSocket.accept();
                Connection c = new Connection(clientSocket);
            }
        } catch (IOException e) {
            LOGGER.error("Listen: " + e.getMessage(), e);
        } finally {
            try {
                if (listenSocket != null) {
                    listenSocket.close();
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}

class Connection extends Thread {

    DataInputStream input;
    DataOutputStream output;
    Socket clientSocket;
    private static final Logger LOGGER = Logger.getLogger(Connection.class);

    public Connection(final Socket aClientSocket) {
        try {
            clientSocket = aClientSocket;
            input = new DataInputStream(clientSocket.getInputStream());
            output = new DataOutputStream(clientSocket.getOutputStream());
            this.start();
        } catch (IOException e) {
            LOGGER.error("Connection: " + e.getMessage(), e);
        }
    }

    @Override
    public void run() {
        try {
            FileWriter out = new FileWriter("test.txt");
            BufferedWriter bufWriter = new BufferedWriter(out);
            // Step 1 read length
            int nb = input.readInt();
            LOGGER.debug("Read Length" + nb);
            byte[] digit = new byte[nb];
            // Step 2 read byte
            LOGGER.debug("Writing.......");
            for (int i = 0; i < nb; i++)
                digit[i] = input.readByte();
            String st = new String(digit);
            bufWriter.append(st);
            bufWriter.close();
            LOGGER.debug("receive from : " + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + " message - " + st);
            // Step 1 send length
            output.writeInt(st.length());
            // Step 2 send length
            output.writeBytes(st);
        } catch (EOFException e) {
            LOGGER.error("EOF: " + e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error("IO: " + e.getMessage(), e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // close failed
            }
        }
    }

    private String readUntil0(final DataInputStream readSock) throws IOException {
        byte[] input1 = new byte[64];
        int k = -1;
        do {
            k++;
            input1[k] = readSock.readByte();
        } while (input1[k] != 0);
        k--;
        byte[] toConvert = new byte[k];
        for (int i = 0; i < k; i++)
            toConvert[i] = input1[i];
        return new String(toConvert);
    }

}

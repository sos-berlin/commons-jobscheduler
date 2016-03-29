package com.sos.VirtualFileSystem.TCPIP;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class SOSVfsTcpIp {

    public SOSVfsTcpIp() {
    }

    public static void main(final String args[]) {
        Socket s = null;
        try {
            int serverPort = 6880;
            String ip = "localhost";
            String data = "Hello, How are you?";
            s = new Socket(ip, serverPort);
            DataInputStream input = new DataInputStream(s.getInputStream());
            DataOutputStream output = new DataOutputStream(s.getOutputStream());
            // Step 1 send length
            System.out.println("Length" + data.length());
            output.writeInt(data.length());
            // Step 2 send length
            System.out.println("Writing.......");
            output.writeBytes(data);
            // UTF is a string encoding
            // Step 1 read length
            int nb = input.readInt();
            byte[] digit = new byte[nb];
            // Step 2 read byte
            for (int i = 0; i < nb; i++) {
                digit[i] = input.readByte();
            }
            String st = new String(digit);
            System.out.println("Received: " + st);
        } catch (UnknownHostException e) {
            System.out.println("Sock:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (IOException e) {
                    // close failed
                }
            }
        }
    }

}
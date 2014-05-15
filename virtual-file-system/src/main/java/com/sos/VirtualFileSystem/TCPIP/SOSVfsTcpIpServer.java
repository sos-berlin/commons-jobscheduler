/**
 *
 */
package com.sos.VirtualFileSystem.TCPIP;
// TCPServer.java
// A server program implementing TCP socket
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author KB
 *
 */
public class SOSVfsTcpIpServer {
	/**
	 *
	 */
	public SOSVfsTcpIpServer() {
	}

	public static void main(final String args[]) {
		ServerSocket listenSocket = null;
		try {
			int serverPort = 6880;
			listenSocket = new ServerSocket(serverPort);
			System.out.println("server start listening... ... ...");
			while (true) {
				Socket clientSocket = listenSocket.accept();
				Connection c = new Connection(clientSocket);
			}
		}
		catch (IOException e) {
			System.out.println("Listen :" + e.getMessage());
		}
		finally {
			try {
				listenSocket.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

class Connection extends Thread {
	DataInputStream		input;
	DataOutputStream	output;
	Socket				clientSocket;

	public Connection(final Socket aClientSocket) {
		try {
			clientSocket = aClientSocket;
			input = new DataInputStream(clientSocket.getInputStream());
			output = new DataOutputStream(clientSocket.getOutputStream());
			this.start();
		}
		catch (IOException e) {
			System.out.println("Connection:" + e.getMessage());
		}
	}

	@Override
	public void run() {
		try { // an echo server
			//  String data = input.readUTF();
			FileWriter out = new FileWriter("test.txt");
			BufferedWriter bufWriter = new BufferedWriter(out);
			//Step 1 read length
			int nb = input.readInt();
			System.out.println("Read Length" + nb);
			byte[] digit = new byte[nb];
			//Step 2 read byte
			System.out.println("Writing.......");
			for (int i = 0; i < nb; i++)
				digit[i] = input.readByte();
			String st = new String(digit);
			bufWriter.append(st);
			bufWriter.close();
			System.out.println("receive from : " + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + " message - " + st);
			//Step 1 send length
			output.writeInt(st.length());
			//Step 2 send length
			output.writeBytes(st); // UTF is a string encoding
			//  output.writeUTF(data);
		}
		catch (EOFException e) {
			System.out.println("EOF:" + e.getMessage());
		}
		catch (IOException e) {
			System.out.println("IO:" + e.getMessage());
		}
		finally {
			try {
				clientSocket.close();
			}
			catch (IOException e) {/*close failed*/
			}
		}
	}

	@SuppressWarnings("unused")
	private String readUntil0 (final DataInputStream readSock) throws IOException {
		   byte[] input1 = new byte[64];
		   int k = -1;
		   do {
			   k++;
			   input1 [k] = readSock.readByte();
			}  while(input1[k] != 0);
			k--;
			byte[] toConvert = new byte[k];
			for(int i = 0; i < k; i++)
			   toConvert[i] = input1[i];
			return new String(toConvert);
		}

}

package sos.net;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/** @author ghassan beydoun */
public class SOSSchedulerCommand {

    private String host = "localhost";
    private int port = 4444;
    private Socket socket = null;
    private int timeout = 5;
    private DataInputStream in = null;
    private PrintWriter out = null;

    public void setHost(final String host) {
        this.host = host;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    public int getPort() {
        return socket.getPort();
    }

    public void connect(final String host, final int port) throws Exception {
        if (host == null || host.isEmpty()) {
            throw new Exception("hostname missing.");
        }
        if (port == 0) {
            throw new Exception("port missing.");
        }
        socket = new Socket(host, port);
        in = new DataInputStream(socket.getInputStream());
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void connect() throws Exception {
        this.connect(host, port);
    }

    public void sendRequest(final String command) throws Exception {
        if (command.indexOf("<?xml") == 0) {
            out.print(command + "\r\n");
        } else {
            out.print("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>" + command + "\r\n");
        }
        out.flush();
    }

    public String getResponse() throws IOException, RuntimeException {
        int sec = 0;
        byte[] buffer = {};
        int bytesRead = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (in.available() == 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //
            }
            if (sec++ == timeout) {
                throw new RuntimeException("timeout reached");
            }
        }
        buffer = new byte[in.available()];
        while ((bytesRead = in.read(buffer)) > 0) {
            if (buffer[bytesRead - 1] == 0x00) {
                bytesRead--;
            }
            baos.write(buffer, 0, bytesRead);
            int avail = in.available();
            int trial = 0;
            if (avail == 0 && trial < 10) {
                try {
                    Thread.sleep(10);
                    avail = in.available();
                    trial++;
                } catch (InterruptedException e) {
                    //
                }
            }
            buffer = new byte[avail];
        }
        return baos.toString("ISO-8859-1");
    }

    public void disconnect() throws Exception {
        if (socket != null) {
            socket.close();
        }
        if (in != null) {
            in.close();
        }
        if (out != null) {
            out.close();
        }
    }

    public static void main(final String[] args) throws Exception {
        int argc = 0;
        int i = 0;
        final String CLASS_NAME = "sos.net.SOSSchedulerCommand";
        final String USAGE = "\nUSAGE: java " + CLASS_NAME + " [ -host <host> -port <port ] <xml-command>";
        boolean params = false;
        String host = null;
        String command = null;
        int port = 0;
        argc = args.length;
        if (argc == 5) {
            if ("-host".equals(args[0])) {
                i++;
                host = args[i++];
                if ("-port".equals(args[i++])) {
                    port = Integer.parseInt(args[i++]);
                } else {
                    System.out.println(USAGE);
                    System.exit(0);
                }
                command = args[i];
            } else {
                System.out.println(USAGE);
                System.exit(0);
            }
        } else if (argc == 1) {
            params = true;
        } else {
            System.out.println(USAGE);
            System.exit(0);
        }
        SOSSchedulerCommand socket = null;
        try {
            socket = new SOSSchedulerCommand();
            if (params) {
                socket.connect();
            } else {
                socket.connect(host, port);
            }
            socket.sendRequest(command);
            System.out.println(socket.getResponse());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(USAGE);
        } finally {
            if (socket != null) {
                socket.disconnect();
            }
            System.exit(0);
        }
    }

}
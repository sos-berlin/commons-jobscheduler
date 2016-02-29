package com.sos.scheduler.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import org.apache.log4j.Logger;

public class SchedulerSocket extends Socket {

    private static final Logger LOGGER = Logger.getLogger(SchedulerSocket.class);
    private BufferedReader in = null;
    private PrintWriter out = null;
    private SchedulerObjectFactoryOptions objOptions = null;
    private int timeout = 60;

    public SchedulerSocket() {
        super();
    }

    public SchedulerSocket(final SchedulerObjectFactoryOptions pobjOptions) throws Exception {
        super(pobjOptions.ServerName.Value(), pobjOptions.PortNumber.value());
        LOGGER.debug("super() is executed ...");
        objOptions = pobjOptions;
        LOGGER.debug("try to connect ...");
    }

    public void connect(final SchedulerObjectFactoryOptions pobjOptions) throws IOException {
        objOptions = pobjOptions;
        this.connect();
    }

    public void connect() {
        InetAddress addr;
        try {
            String strHost = objOptions.ServerName.Value();
            addr = InetAddress.getByName(strHost);
            int intPortnumber = objOptions.PortNumber.value();
            if (!objOptions.TransferMethod.isTcp()) {
                intPortnumber = objOptions.UDPPortNumber.value();
            }
            SocketAddress sockaddr = new InetSocketAddress(addr, intPortnumber);
            timeout = objOptions.TCPTimeoutValue.value();
            super.connect(sockaddr, timeout);
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public PrintWriter getPrintWriter() throws IOException {
        if (out == null) {
            out = new PrintWriter(super.getOutputStream(), true);
        }
        return out;
    }

    public BufferedReader getBufferedReader() throws IOException {
        if (in == null) {
            in = new BufferedReader(new InputStreamReader(super.getInputStream()));
        }
        return in;
    }

    public synchronized void doClose() throws IOException {
        if (getOutputStream() != null) {
            getOutputStream().flush();
        }
        super.close();
    }

    public void sendRequest(final String request) throws Exception {
        out = getPrintWriter();
        if (out != null) {
            if (request.indexOf("<?xml") == 0) {
                out.print(request + "\r\n");
            } else {
                out.print("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>" + request + "\r\n");
            }
            out.flush();
        }
    }

    public String getResponse() throws IOException, RuntimeException {
        int b;
        StringBuffer response = new StringBuffer();
        in = getBufferedReader();
        if (in != null) {
            while ((b = in.read()) != -1) {
                if (b == 0) {
                    break;
                }
                response.append((char) b);
            }
        }
        return response.toString();
    }

}

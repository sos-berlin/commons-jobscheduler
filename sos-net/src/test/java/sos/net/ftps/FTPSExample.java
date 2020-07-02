package sos.net.ftps;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author KB */
public final class FTPSExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(FTPSExample.class);
    public static final String USAGE = "Usage: ftp [-s] [-b] <hostname> <username> <password> <remote file> <local file>\n"
            + "\nDefault behavior is to download a file and use ASCII transfer mode.\n" + "\t-s store file on server (upload)\n"
            + "\t-b use binary transfer mode\n";

    public static final void main(String[] args) throws NoSuchAlgorithmException {
        int base = 0;
        boolean storeFile = false;
        boolean binaryTransfer = false;
        boolean error = false;
        String server;
        String username;
        String password;
        String remote;
        String local;
        String protocol = "TLS";
        FTPSClient ftps;
        for (base = 0; base < args.length; base++) {
            if (args[base].startsWith("-s")) {
                storeFile = true;
            } else if (args[base].startsWith("-b")) {
                binaryTransfer = true;
            } else {
                break;
            }
        }
        if ((args.length - base) != 5) {
            LOGGER.error(USAGE);
            System.exit(1);
        }
        server = args[base++];
        username = args[base++];
        password = args[base++];
        remote = args[base++];
        local = args[base];
        ftps = new FTPSClient(protocol);
        ftps.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        try {
            int reply;
            ftps.connect(server);
            LOGGER.debug("Connected to " + server + ".");
            reply = ftps.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftps.disconnect();
                LOGGER.error("FTP server refused connection.");
                System.exit(1);
            }
        } catch (IOException e) {
            if (ftps.isConnected()) {
                try {
                    ftps.disconnect();
                } catch (IOException f) {
                    // do nothing
                }
            }
            LOGGER.error("Could not connect to server. " + e.getMessage(), e);
            System.exit(1);
        }
        __main: try {
            if (!ftps.login(username, password)) {
                ftps.logout();
                error = true;
                break __main;
            }
            LOGGER.debug("Remote system is " + ftps.getSystemName());
            if (binaryTransfer) {
                ftps.setFileType(FTP.BINARY_FILE_TYPE);
            }
            ftps.enterLocalPassiveMode();
            if (storeFile) {
                InputStream input;
                input = new FileInputStream(local);
                ftps.storeFile(remote, input);
                input.close();
            } else {
                OutputStream output;
                output = new FileOutputStream(local);
                ftps.retrieveFile(remote, output);
                output.close();
            }
            ftps.logout();
        } catch (FTPConnectionClosedException e) {
            error = true;
            LOGGER.error("Server closed connection. " + e.getMessage(), e);
        } catch (IOException e) {
            error = true;
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (ftps.isConnected()) {
                try {
                    ftps.disconnect();
                } catch (IOException f) {
                    // do nothing
                }
            }
        }
        System.exit(error ? 1 : 0);
    }

}

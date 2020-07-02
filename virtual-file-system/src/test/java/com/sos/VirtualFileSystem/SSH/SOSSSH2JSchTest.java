package com.sos.VirtualFileSystem.SSH;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.sos.JSHelper.Options.SOSOptionAuthenticationMethod;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionUserName;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;

public class SOSSSH2JSchTest {

    private static final String HOST = "homer.sos";
    private static final String WINDOWS_HOST = "lutest.sos";
    private static final int PORT = 22;
    private static final String USERNAME = "test";
    private static final String PASSWD = "12345";
    private static final String AUTH_METHOD = "password";
    private static final String CONNECTION_TYPE_SHELL = "shell";
    private static final String CONNECTION_TYPE_EXECUTE = "exec";
    private static final int TIMEOUT = 3 * 1000;
    private String command = "echo **Hallo world!**";
    private static final Logger LOG = LoggerFactory.getLogger(SOSSSH2JSchTest.class);
    private SOSOptionUserName optionUserName;
    private SOSOptionPassword optionPassword;
    private SOSOptionAuthenticationMethod optionAuthenticationMethod;
    private SOSOptionInFileName optionAuthFileName;
    private Channel sshChannel = null;
    private ChannelExec executeChannel = null;
    private Session sshSession = null;
    private JSch secureChannel = null;

    private ISOSAuthenticationOptions authenticationOptions = new ISOSAuthenticationOptions() {

        @Override
        public void setUser(SOSOptionUserName user) {
            optionUserName = user;
        }

        @Override
        public void setPassword(SOSOptionPassword password) {
            optionPassword = password;
        }

        @Override
        public void setAuthMethod(SOSOptionAuthenticationMethod authMethod) {
            optionAuthenticationMethod = authMethod;
        }

        @Override
        public SOSOptionUserName getUser() {
            return optionUserName;
        }

        @Override
        public SOSOptionPassword getPassword() {
            return optionPassword;
        }

        @Override
        public SOSOptionAuthenticationMethod getAuthMethod() {
            return optionAuthenticationMethod;
        }

        @Override
        public SOSOptionInFileName getAuthFile() {
            return optionAuthFileName;
        }

        @Override
        public void setAuthFile(SOSOptionInFileName authFile) {
            optionAuthFileName = authFile;
        }

        @Override
        public SOSOptionPassword getPassphrase() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setPassphrase(SOSOptionPassword passphrase) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void setUseKeyAgent(SOSOptionBoolean keyAgent) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public SOSOptionBoolean isUseKeyAgent() {
            // TODO Auto-generated method stub
            return null;
        }
    };

    @Before
    public void setup() {
        authenticationOptions.setUser(new SOSOptionUserName(null, null, null, USERNAME, USERNAME, false));
        authenticationOptions.setPassword(new SOSOptionPassword(null, null, null, PASSWD, PASSWD, false));
        authenticationOptions.setAuthMethod(new SOSOptionAuthenticationMethod(null, null, null, AUTH_METHOD, AUTH_METHOD, false));
        secureChannel = new JSch();
    }

    @Test
    public void testSessionConnect() throws JSchException, RuntimeException {
        LOG.debug("Test testSessionConnect started!");
        sessionConnect();
        assertTrue(sshSession.isConnected());
        sshSession.disconnect();
        LOG.debug("Test testSessionConnect ended!");
    }

    @Test
    public void testSessionDisconnect() throws JSchException, RuntimeException {
        LOG.debug("Test testSessionDisconnect started!");
        sessionConnect();
        sshSession.disconnect();
        assertFalse(sshSession.isConnected());
        LOG.debug("Test testSessionDisconnect ended!");
    }

    @Test
    public void testShellChannelConnect() throws JSchException, RuntimeException {
        LOG.debug("Test testShellChannelConnect started!");
        sessionConnect();
        shellChannelConnect();
        assertTrue(sshChannel.isConnected());
        sshChannel.disconnect();
        sshSession.disconnect();
        LOG.debug("Test testShellChannelConnect ended!");
    }

    @Test
    public void testShellChannelDisconnect() throws JSchException, RuntimeException {
        LOG.debug("Test testShellChannelDisconnect started!");
        sessionConnect();
        shellChannelConnect();
        sshChannel.disconnect();
        sshSession.disconnect();
        assertFalse(sshChannel.isConnected());
        LOG.debug("Test testShellChannelDisconnect ended!");
    }

    @Test
    public void testExecuteChannelConnect() throws JSchException, RuntimeException {
        LOG.debug("Test testExecuteChannelConnect started!");
        sessionConnect();
        executeChannelConnect();
        assertTrue(executeChannel.isConnected());
        executeChannel.disconnect();
        sshSession.disconnect();
        LOG.debug("Test testExecuteChannelConnect ended!");
    }

    @Test
    public void testExecuteChannelDisconnect() throws JSchException, RuntimeException {
        LOG.debug("Test testExecuteChannelDisconnect started!");
        sessionConnect();
        executeChannelConnect();
        executeChannel.disconnect();
        sshSession.disconnect();
        assertFalse(executeChannel.isConnected());
        LOG.debug("Test testExecuteChannelDisconnect ended!");
    }

    @Test
    public void testExecuteCommand() throws JSchException, RuntimeException, IOException {
        LOG.debug("Test testExecuteCommand started!");
        sessionConnect();
        executeChannelWithCommandConnect();
        InputStream in = executeChannel.getInputStream();
        String output = "";
        int exitCode = -1;
        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }
                output += new String(tmp, 0, i);
            }
            if (executeChannel.isClosed()) {
                if (in.available() > 0) {
                    continue;
                }
                LOG.debug("exit-status: {}", exitCode = executeChannel.getExitStatus());
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ee) {
                //
            }
        }
        LOG.debug("output = {}", output);
        assertTrue(output.length() > 0);
        assertEquals("exit code as expected", 0, exitCode);
        assertEquals("output as expected", "**Hallo world!**\n", output);
        executeChannel.disconnect();
        sshSession.disconnect();
        LOG.debug("Test testExecuteCommand ended!");
    }

    @Test
    public void testExecuteCommandOnWindows() throws JSchException, RuntimeException, IOException {
        LOG.debug("Test testExecuteCommandOnWindows started!");
        sessionConnectWindows();
        executeChannelWithCommandConnect(command);
        InputStream in = executeChannel.getInputStream();
        String output = "";
        int exitCode = -1;
        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }
                output += new String(tmp, 0, i);
            }
            if (executeChannel.isClosed()) {
                if (in.available() > 0) {
                    continue;
                }
                LOG.debug("exit-status: {}", exitCode = executeChannel.getExitStatus());
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ee) {
                //
            }
        }
        LOG.debug("output = {}", output);
        assertTrue(output.length() > 0);
        assertEquals("exit code not as expected", 0, exitCode);
        assertEquals("output not as expected", "**Hallo world!**\r\n", output);
        executeChannel.disconnect();
        sshSession.disconnect();
        LOG.debug("Test testExecuteCommandOnWindows ended!");
    }

    private void sessionConnect() throws JSchException, RuntimeException {
        sshSession = secureChannel.getSession(authenticationOptions.getUser().getValue(), HOST, PORT);
        sshSession.setPassword(authenticationOptions.getPassword().getValue().toString());
        sshSession.setConfig("StrictHostKeyChecking", "no");
        sshSession.connect();
    }

    private void sessionConnectWindows() throws JSchException, RuntimeException {
        sshSession = secureChannel.getSession(authenticationOptions.getUser().getValue(), WINDOWS_HOST, PORT);
        sshSession.setPassword(authenticationOptions.getPassword().getValue().toString());
        sshSession.setConfig("StrictHostKeyChecking", "no");
        sshSession.connect();
    }

    private void shellChannelConnect() throws JSchException, RuntimeException {
        sshChannel = sshSession.openChannel(CONNECTION_TYPE_SHELL);
        sshChannel.connect(TIMEOUT);
    }

    private void executeChannelConnect() throws JSchException {
        executeChannel = (ChannelExec) sshSession.openChannel(CONNECTION_TYPE_EXECUTE);
        executeChannel.setInputStream(System.in);
        executeChannel.setOutputStream(System.out);
        executeChannel.setErrStream(System.err);
        executeChannel.connect(TIMEOUT);
    }

    private void executeChannelWithCommandConnect() throws JSchException {
        executeChannel = (ChannelExec) sshSession.openChannel(CONNECTION_TYPE_EXECUTE);
        executeChannel.setCommand(command);
        executeChannel.setInputStream(System.in);
        executeChannel.setOutputStream(System.out);
        executeChannel.setErrStream(System.err);
        executeChannel.connect(TIMEOUT);
    }

    private void executeChannelWithCommandConnect(String command) throws JSchException {
        executeChannel = (ChannelExec) sshSession.openChannel(CONNECTION_TYPE_EXECUTE);
        executeChannel.setCommand(command);
        executeChannel.setInputStream(System.in);
        executeChannel.setOutputStream(System.out);
        executeChannel.setErrStream(System.err);
        executeChannel.connect(TIMEOUT);
    }

}
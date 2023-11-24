package com.sos.vfs.smb.common;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.vfs.common.SOSCommonProvider;
import com.sos.vfs.common.SOSEnv;
import com.sos.vfs.common.SOSShell;
import com.sos.vfs.common.interfaces.ISOSProviderFile;
import com.sos.vfs.common.options.SOSProviderOptions;

import sos.util.SOSString;

public abstract class ASOSSMB extends SOSCommonProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ASOSSMB.class);

    private static final int DEFAULT_PORT = 445;

    private SOSShell shell = null;
    private boolean isConnected = false;
    private String domain = null;
    private String logPrefix = null;

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void connect(final SOSProviderOptions options) throws Exception {
        super.connect(options);

        try {
            doDisconnect();
        } catch (Throwable e) {
        }
        try {
            isConnected = false;
            domain = getProviderOptions().domain.getValue();
            port = getProviderOptions().port.isDirty() ? port : ASOSSMB.DEFAULT_PORT;
            setLogPrefix();

            doConnect();

            isConnected = true;
            reply = "OK";
            LOGGER.info(String.format("%s%s[connect]%s", getLogPrefix(), getLogProviderClass(), reply));
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Throwable ex) {
            throw new JobSchedulerException(ex);
        }
    }

    public void doConnect() throws Exception {

    }

    @Override
    public void disconnect() {
        reply = "OK";

        try {
            doDisconnect();
        } catch (Throwable e) {
            reply = e.toString();
        }
        if (isConnected) {
            LOGGER.info(String.format("%s%s[disconnect]%s", getLogPrefix(), getLogProviderClass(), reply));
        }
        isConnected = false;
    }

    public void doDisconnect() throws Exception {

    }

    @Override
    public ISOSProviderFile getFile(String fileName) {
        fileName = adjustFileSeparator(fileName);
        ISOSProviderFile file = new SOSSMBFile(fileName);
        file.setProvider(this);
        return file;
    }

    @Override
    public void executeCommand(final String cmd) throws Exception {
        executeCommand(cmd, null);
    }

    @Override
    public void executeCommand(final String cmd, SOSEnv env) throws Exception {
        if (shell == null) {
            shell = new SOSShell();
        }
        String command = cmd.trim();
        if (shell.isWindows()) {
            command = shell.replaceCommand4Windows(command);
        }
        int exitCode = shell.executeCommand(command, env);
        if (exitCode != 0) {
            boolean raiseException = true;
            if (getProviderOptions() != null) {
                raiseException = getProviderOptions().raiseExceptionOnError.value();
            }
            if (raiseException) {
                throw new JobSchedulerException(SOSVfs_E_191.params(exitCode + ""));
            } else {
                LOGGER.info(SOSVfs_D_151.params(command, SOSVfs_E_191.params(exitCode + "")));
            }
        }
    }

    public Properties getConfigFromFiles(Properties properties) {
        if (!SOSString.isEmpty(getProviderOptions().configuration_files.getValue())) {
            boolean isDebugEnabled = LOGGER.isDebugEnabled();
            String[] files = getProviderOptions().configuration_files.getValue().split(";");
            for (int i = 0; i < files.length; i++) {
                String file = files[i].trim();
                LOGGER.info(String.format("%s[setConfigFromFiles][%s]", getLogPrefix(), file));
                FileInputStream in = null;
                try {
                    in = new FileInputStream(file);
                    Properties p = new Properties();
                    p.load(in);
                    for (Entry<Object, Object> entry : p.entrySet()) {
                        String key = (String) entry.getKey();
                        String value = (String) entry.getValue();
                        if (key.startsWith(";")) {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("%s[setConfigFromFiles][%s][skip]%s=%s", getLogPrefix(), file, key, value));
                            }
                        } else {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("%s[setConfigFromFiles][%s]%s=%s", getLogPrefix(), file, key, value));
                            }
                            properties.put(key, value);
                        }
                    }
                } catch (Exception ex) {
                    LOGGER.warn(String.format("%s[setConfigFromFiles][%s][failed]%s", getLogPrefix(), new java.io.File(file).getAbsolutePath(), ex
                            .toString()));
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Exception ex) {
                            //
                        }
                    }
                }
            }
        }
        return properties;
    }

    public String getParent(String path) {
        String parent = "/";
        try {
            Path p = Paths.get(path).getParent();
            if (p != null) {
                parent = SOSCommonProvider.normalizePath(p.toString());
            }
        } catch (Exception e) {
            LOGGER.error(String.format("[%s][can't get parent path]%s", path, e.toString()), e);
        }
        return parent;
    }

    private void setLogPrefix() {
        StringBuilder sb = new StringBuilder("[smb]");
        if (!SOSString.isEmpty(domain)) {
            sb.append("[").append(domain).append("]");
        }
        sb.append("[").append(user).append("@").append(host).append(":").append(port).append("]");
        logPrefix = sb.toString();
    }

    public String getDomain() {
        return domain;
    }

    public String getLogPrefix() {
        return logPrefix;
    }

    private String getLogProviderClass() {
        return "[" + getClass().getSimpleName() + "]";
    }

}

package com.sos.vfs.sftp;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Pattern;

import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.vfs.common.SOSCommandResult;
import com.sos.vfs.common.SOSCommonProvider;
import com.sos.vfs.common.SOSEnv;
import com.sos.vfs.common.SOSFileEntry;
import com.sos.vfs.common.interfaces.ISOSProviderFile;
import com.sos.vfs.common.options.SOSBaseOptions;
import com.sos.vfs.common.options.SOSProviderOptions;
import com.sos.vfs.sftp.common.ISOSSFTP;
import com.sos.vfs.sftp.common.SOSSSHServerInfo;
import com.sos.vfs.sftp.jcraft.SOSSFTPJCraft;
import com.sos.vfs.sftp.sshj.SOSSFTPSSHJ;

import sos.util.SOSString;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSSFTP extends SOSCommonProvider implements ISOSSFTP {

    public enum SSHProvider {
        JCRAFT, SSHJ
    }

    private static final Pattern HAS_WINDOWS_OPENSSH_DRIVER_LETTER_SPECIFIER = Pattern.compile("^/[a-zA-Z]:");
    private ISOSSFTP provider;

    public SOSSFTP(SOSOptionString sshProviderOption) {
        setProvider(sshProviderOption);
    }

    private void setProvider(SOSOptionString sshProviderOption) {
        if (sshProviderOption == null) {
            SOSBaseOptions vfsOptions = new SOSBaseOptions();
            sshProviderOption = vfsOptions.ssh_provider;
        }
        String sshPovider = sshProviderOption.getValue().toUpperCase();
        if (SOSString.isEmpty(sshPovider)) {
            sshPovider = sshProviderOption.getDefaultValue().toUpperCase();
        }
        if (sshPovider.equals(SSHProvider.SSHJ.name())) {
            provider = new SOSSFTPSSHJ();
        } else {
            provider = new SOSSFTPJCraft();
        }
    }

    @Override
    public boolean isConnected() {
        return provider.isConnected();
    }

    @Override
    public void connect(final SOSProviderOptions options) throws Exception {
        provider.connect(options);
    }

    @Override
    public void disconnect() {
        provider.disconnect();
    }

    @Override
    public void mkdir(final String path) {
        provider.mkdir(path);
    }

    @Override
    public void rmdir(final String path) {
        provider.rmdir(path);
    }

    @Override
    public boolean fileExists(final String filename) {
        return provider.fileExists(filename);
    }

    @Override
    public boolean directoryExists(final String filename) {
        return provider.directoryExists(filename);
    }

    @Override
    public boolean isDirectory(final String filename) {
        return provider.isDirectory(filename);
    }

    @Override
    public long size(String filename) throws Exception {
        return provider.size(filename);
    }

    @Override
    public SOSFileEntry getFileEntry(String pathname) throws Exception {
        return provider.getFileEntry(pathname);
    }

    @Override
    public List<SOSFileEntry> listNames(String path, boolean checkIfExists, boolean checkIfIsDirectory) {
        return provider.listNames(path, checkIfExists, checkIfIsDirectory);
    }

    @Override
    public void delete(final String path, boolean checkIsDirectory) {
        provider.delete(path, checkIsDirectory);
    }

    @Override
    public void rename(String from, String to) {
        provider.rename(from, to);
    }

    @Override
    public void executeCommand(String cmd) {
        executeCommand(cmd, null);
    }

    @Override
    public void executeCommand(String cmd, SOSEnv env) {
        provider.executeCommand(cmd, env);
    }

    @Override
    public SOSCommandResult executeResultCommand(String cmd) throws Exception {
        SOSCommandResult result = provider.executeResultCommand(cmd);
        if (result.getException() != null) {
            throw new Exception(result.getException());
        }
        return result;
    }

    @Override
    public InputStream getInputStream(final String fileName) {
        return provider.getInputStream(fileName);
    }

    @Override
    public OutputStream getOutputStream(String fileName, boolean append, boolean resume) {
        return provider.getOutputStream(fileName, append, resume);
    }

    @Override
    public ISOSProviderFile getFile(String fileName) {
        return provider.getFile(fileName);
    }

    @Override
    public String getModificationDateTime(final String path) {
        return provider.getModificationDateTime(path);
    }

    @Override
    public long putFile(final String source, final String target) {
        return provider.putFile(source, target);
    }

    @Override
    public void putFile(File source, String target, int chmod) throws Exception {
        provider.putFile(source, target, chmod);
    }

    @Override
    public void get(String source, String target) {
        provider.get(source, target);
    }

    @Override
    public String getStdErr() {
        return provider.getStdErr();
    }

    @Override
    public void resetStdErr() {
        provider.resetStdErr();
    }

    @Override
    public String getStdOut() {
        return provider.getStdOut();
    }

    @Override
    public void resetStdOut() {
        provider.resetStdOut();
    }

    @Override
    public Integer getExitCode() {
        return provider.getExitCode();
    }

    @Override
    public String getExitSignal() {
        return provider.getExitSignal();
    }

    @Override
    public boolean isSimulateShell() {
        return provider.isSimulateShell();
    }

    @Override
    public void setSimulateShell(boolean val) {
        provider.setSimulateShell(val);
    }

    @Override
    public boolean isExecSessionExists() {
        return provider.isExecSessionExists();
    }

    @Override
    public boolean isExecSessionConnected() {
        return provider.isExecSessionConnected();
    }

    @Override
    public void execSessionSendSignalContinue() throws Exception {
        provider.execSessionSendSignalContinue();
    }

    @Override
    public SOSSSHServerInfo getSSHServerInfo() {
        return provider.getSSHServerInfo();
    }

    public static boolean hasWindowsOpenSSHDriverLetterSpecifier(String path) {
        return HAS_WINDOWS_OPENSSH_DRIVER_LETTER_SPECIFIER.matcher(path).find();
    }

    @Override
    public boolean isSFTP() {
        return true;
    }
}

package com.sos.vfs.local;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.vfs.common.interfaces.ISOSVirtualFile;
import com.sos.vfs.common.options.SOSProviderOptions;
import com.sos.vfs.common.SOSFileEntry;
import com.sos.vfs.common.SOSFileEntry.EntryType;
import com.sos.vfs.common.SOSCommonProvider;
import com.sos.vfs.common.SOSEnv;
import com.sos.vfs.shell.SOSShell;
import com.sos.i18n.annotation.I18NResourceBundle;

import sos.util.SOSFile;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSLocal extends SOSCommonProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSLocal.class);
    private SOSProviderOptions providerOptions = null;
    private SOSShell shell = null;

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void connect(final SOSProviderOptions options) throws Exception {
        providerOptions = options;
    }

    @Override
    public void delete(final String pathname, boolean checkIsDirectory) throws IOException {
        File file = new File(pathname);
        file.delete();
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
            if (providerOptions != null) {
                raiseException = providerOptions.raiseExceptionOnError.value();
            }
            if (raiseException) {
                throw new JobSchedulerException(SOSVfs_E_191.params(exitCode + ""));
            } else {
                LOGGER.info(SOSVfs_D_151.params(command, SOSVfs_E_191.params(exitCode + "")));
            }
        }
    }

    public SOSShell getShell() {
        if (shell == null) {
            shell = new SOSShell();
        }
        return shell;
    }

    @Override
    public ISOSVirtualFile getFileHandle(final String fileName) {
        SOSLocalFile file = new SOSLocalFile(fileName);
        file.setHandler(this);
        return file;
    }

    @Override
    public SOSFileEntry getFileEntry(final String pathname) throws Exception {
        File file = new File(pathname);
        if (file.exists() && file.isFile()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("[%s]found", pathname));
            }
            return getFileEntry(file);
        }
        return null;
    }

    private SOSFileEntry getFileEntry(File file) {
        SOSFileEntry entry = new SOSFileEntry(EntryType.FILESYSTEM);
        entry.setDirectory(file.isDirectory());
        entry.setFilename(file.getName());
        entry.setFilesize(file.length());
        entry.setLastModified(file.lastModified());
        entry.setParentPath(file.getParent());
        return entry;
    }

    @Override
    public List<SOSFileEntry> listNames(final String pathname, boolean checkIfExists, boolean checkIfIsDirectory) throws IOException {
        List<SOSFileEntry> result = new ArrayList<SOSFileEntry>();
        File dir = new File(pathname);
        if (checkIfExists && !dir.exists()) {
            return result;
        }
        if (checkIfIsDirectory && !dir.isDirectory()) {
            reply = "ls OK";
            return result;
        }

        File[] list = dir.listFiles();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(String.format("[%s][listFiles] %s files or folders", pathname, list.length));
        }
        for (File file : list) {
            result.add(getFileEntry(file));
        }
        return result;
    }

    @Override
    public List<SOSFileEntry> getFilelist(final String folder, final String regexp, final int flag, final boolean recursive, boolean checkIfExists,
            String integrityHashType) {
        List<SOSFileEntry> result = new ArrayList<SOSFileEntry>();
        try {
            Vector<File> list = SOSFile.getFolderlist(folder, regexp, flag, recursive);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("[%s][getFolderlist] %s files or folders", folder, list.size()));
            }
            for (File file : list) {
                if (file.isDirectory()) {
                    continue;
                }
                if (integrityHashType != null && file.getName().endsWith(integrityHashType)) {
                    continue;
                }
                result.add(getFileEntry(file));
            }
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
        return result;
    }

    @Override
    public List<SOSFileEntry> getFolderlist(final String folder, final String regexp, final int flag, final boolean recursive) {
        List<SOSFileEntry> result = new ArrayList<SOSFileEntry>();
        try {
            Vector<File> list = SOSFile.getFolderlist(folder, regexp, flag, recursive);
            for (File file : list) {
                if (file.isDirectory()) {
                    result.add(getFileEntry(file));
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
        return result;
    }

    @Override
    public OutputStream getOutputStream(final String fileName, boolean append, boolean resume) {
        return null;
    }

    @Override
    public boolean isDirectory(final String fileName) {
        return new File(fileName).isDirectory();
    }

    @Override
    public void mkdir(final String pathname) throws IOException {
        File dir = new File(pathname);
        if (!dir.exists()) {
            dir.mkdirs();
        } else {
            if (!dir.isDirectory()) {
                throw new JobSchedulerException(SOSVfs_E_277.params(pathname));
            }
        }
    }

    @Override
    public void rmdir(final String folderName) throws IOException {
        new File(folderName).delete();
    }

    @Override
    public void reconnect(SOSProviderOptions options) {
        //
    }

}
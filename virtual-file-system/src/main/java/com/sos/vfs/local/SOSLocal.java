package com.sos.vfs.local;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.vfs.common.SOSCommonProvider;
import com.sos.vfs.common.SOSEnv;
import com.sos.vfs.common.SOSFileEntry;
import com.sos.vfs.common.SOSFileEntry.EntryType;
import com.sos.vfs.common.SOSShell;
import com.sos.vfs.common.interfaces.ISOSProviderFile;
import com.sos.vfs.common.options.SOSProviderOptions;

import sos.util.SOSFile;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSLocal extends SOSCommonProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSLocal.class);
    private SOSProviderOptions providerOptions = null;
    private SOSShell shell = null;

    private int directoryFilesCount = 0;

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void connect(final SOSProviderOptions options) throws Exception {
        providerOptions = options;
    }

    @Override
    public void delete(final String pathname, boolean checkIsDirectory) {
        Path path = null;
        try {
            path = Paths.get(pathname).toAbsolutePath();
            Files.delete(path);
        } catch (Throwable ex) {
            reply = ex.toString();
            throw new JobSchedulerException("[delete]" + reply, ex);
        }
        reply = "rm OK";
        LOGGER.info(getHostID(SOSVfs_D_181.params("delete", path, getReplyString())));
    }

    @Override
    public void rename(String oldpath, String newpath) {
        Path source = null;
        Path dest = null;
        try {
            source = Paths.get(oldpath).toAbsolutePath();
            dest = Paths.get(newpath).toAbsolutePath();
            if (!Files.exists(dest)) {
                try {
                    Files.move(source, dest, StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(source, dest);
                }
            } else {
                Files.move(source, dest, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Throwable e) {
            reply = e.toString();
            throw new JobSchedulerException("[rename]" + reply, e);
        }
        reply = "mv OK";
        LOGGER.info(getHostID(SOSVfs_I_189.params(source, dest, getReplyString())));
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
    public ISOSProviderFile getFile(final String fileName) {
        SOSLocalFile file = new SOSLocalFile(fileName);
        file.setProvider(this);
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
        // entry.setLastModified(file.lastModified());
        entry.setParentPath(file.getParent());
        return entry;
    }

    @Override
    public List<SOSFileEntry> listNames(final String pathname, final int maxFiles, boolean checkIfExists, boolean checkIfIsDirectory)
            throws IOException {
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
    public List<SOSFileEntry> getFileList(String folder, int maxFiles, boolean recursive, Pattern fileNamePattern, Pattern excludedDirectoriesPattern,
            boolean checkIfExists, String integrityHashType, int recLevel) throws Exception {

        directoryFilesCount = 0;
        if (excludedDirectoriesPattern == null) {
            return getFilelistOldMethod(folder, maxFiles, fileNamePattern.pattern(), 0, recursive, checkIfExists, integrityHashType);
        }

        Path dir = Paths.get(folder).toAbsolutePath();
        if (checkIfExists && !Files.exists(dir)) {
            return new ArrayList<>();
        }

        if (recursive) {
            return getFileListRecursive(dir, maxFiles, fileNamePattern, excludedDirectoriesPattern, integrityHashType);
        } else {
            return getFileListNonRecursive(dir, maxFiles, fileNamePattern, integrityHashType);
        }
    }

    private List<SOSFileEntry> getFileListNonRecursive(Path folder, int maxFiles, Pattern fileNamePattern, String integrityHashType)
            throws IOException {

        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        boolean isTraceEnabled = LOGGER.isTraceEnabled();
        List<SOSFileEntry> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for (Path path : stream) {
                FileVisitResult fvr = checkMaxFiles(maxFiles);
                if (fvr != null) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][skip][preVisitDirectory][maxFiles=%s]exceeded", path, maxFiles));
                    }
                    return result;
                }
                if (!Files.isDirectory(path)) {
                    if (isTraceEnabled) {
                        LOGGER.trace(String.format("[%s]", path));
                    }
                    String fn = path.getFileName().toString();
                    boolean add = true;
                    if (integrityHashType != null && fn.endsWith(integrityHashType)) {
                        add = false;
                    }
                    if (add && fileNamePattern.matcher(fn).find()) {
                        result.add(getFileEntry(path.toFile()));
                        directoryFilesCount++;
                    }
                }
            }
        }
        return result;
    }

    private List<SOSFileEntry> getFileListRecursive(Path folder, int maxFiles, Pattern fileNamePattern, Pattern excludedDirectoriesPattern,
            String integrityHashType) throws IOException {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        boolean isTraceEnabled = LOGGER.isTraceEnabled();
        List<SOSFileEntry> result = new ArrayList<>();
        Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path file, BasicFileAttributes attrs) {
                FileVisitResult fvr = checkMaxFiles(maxFiles);
                if (fvr != null) {
                    LOGGER.info(String.format("[skip]maxFiles=%s exceeded", maxFiles));
                    return fvr;
                }

                if (excludedDirectoriesPattern != null) {
                    String path = SOSCommonProvider.normalizePath(file.toAbsolutePath().toString());
                    if (excludedDirectoriesPattern.matcher(path).find()) {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][preVisitDirectory][match][excludedDirectories=%s]", path, excludedDirectoriesPattern
                                    .pattern()));
                        }
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (!attrs.isDirectory()) {
                    FileVisitResult fvr = checkMaxFiles(maxFiles);
                    if (fvr != null) {
                        LOGGER.info(String.format("[skip]maxFiles=%s exceeded", maxFiles));
                        return fvr;
                    }
                    if (isTraceEnabled) {
                        LOGGER.trace(String.format("[%s][visitFile]", file));
                    }
                    String fn = file.getFileName().toString();
                    boolean add = true;
                    if (integrityHashType != null && fn.endsWith(integrityHashType)) {
                        add = false;
                    }
                    if (add && fileNamePattern.matcher(fn).find()) {
                        result.add(getFileEntry(file.toFile()));
                        directoryFilesCount++;
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }

    private FileVisitResult checkMaxFiles(int maxFiles) {
        return maxFiles > 0 && directoryFilesCount >= maxFiles ? FileVisitResult.TERMINATE : null;
    }

    private List<SOSFileEntry> getFilelistOldMethod(final String folder, final int maxFiles, final String fileNameRegExp, final int flag,
            final boolean recursive, boolean checkIfExists, String integrityHashType) {
        boolean isTraceEnabled = LOGGER.isTraceEnabled();
        List<SOSFileEntry> result = new ArrayList<SOSFileEntry>();
        try {
            Vector<File> list = SOSFile.getFolderlist(folder, fileNameRegExp, flag, recursive);
            if (isTraceEnabled) {
                LOGGER.trace(String.format("[%s][getFolderlist] %s files or folders", folder, list.size()));
            }
            for (File file : list) {
                FileVisitResult fvr = checkMaxFiles(maxFiles);
                if (fvr != null) {
                    LOGGER.info(String.format("[skip]maxFiles=%s exceeded", maxFiles));
                    return result;
                }

                if (file.isDirectory()) {
                    continue;
                }
                if (integrityHashType != null && file.getName().endsWith(integrityHashType)) {
                    continue;
                }
                result.add(getFileEntry(file));
                directoryFilesCount++;
            }
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
        return result;
    }

    /** used only by com.sos.scheduler.model.SchedulerHotFolder */
    @Override
    public List<SOSFileEntry> getSubFolders(String folder, final int maxFiles, boolean recursive, Pattern pattern, int recLevel) throws Exception {
        List<SOSFileEntry> result = new ArrayList<SOSFileEntry>();
        try {
            Vector<File> list = SOSFile.getFolderlist(folder, pattern.pattern(), 0, recursive);
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
    public boolean directoryExists(final String fileName) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s]directoryExists", fileName));
        }
        File f = new File(fileName);
        return f.isDirectory() && f.exists();
    }

    @Override
    public boolean fileExists(final String fileName) {
        return new File(fileName).exists();
    }

    @Override
    public void mkdir(final String pathname) throws IOException {
        File dir = new File(pathname);
        if (!dir.exists()) {
            Files.createDirectories(dir.toPath().toAbsolutePath());
        } else {
            if (!dir.isDirectory()) {
                throw new JobSchedulerException(SOSVfs_E_277.params(pathname));
            }
        }
    }

    @Override
    public void rmdir(final String folderName) throws IOException {
        try (Stream<Path> stream = Files.walk(Paths.get(folderName))) {
            for (Path p : stream.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
                Files.delete(p);
            }
        }
        reply = "rmdir OK";
        LOGGER.info(String.format("[rmdir][%s]%s", folderName, reply));
    }

}
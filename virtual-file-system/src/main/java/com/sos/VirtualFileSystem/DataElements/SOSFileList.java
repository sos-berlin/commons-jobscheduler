package com.sos.VirtualFileSystem.DataElements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.sos.JSHelper.DataElements.JSDataElementDate;
import com.sos.JSHelper.DataElements.JSDateFormat;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.VirtualFileSystem.DataElements.SOSFileListEntry.TransferStatus;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;
import com.sos.VirtualFileSystem.common.SOSVfsConstants;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSFileList extends SOSVfsMessageCodes {

    private static final Logger LOGGER = Logger.getLogger(SOSFileList.class);
    private static final Logger JADE_REPORT_LOGGER = Logger.getLogger(VFSFactory.getLoggerName());

    public ISOSVfsFileTransfer sourceFileTransfer = null;
    public ISOSVfsFileTransfer targetFileTransfer = null;

    private final JSDataElementDate transactionStart = new JSDataElementDate(now(), JSDateFormat.dfTIMESTAMPS);
    private final JSDataElementDate transactionEnd = new JSDataElementDate(now(), JSDateFormat.dfTIMESTAMPS);
    private final HashMap<String, String> subFolders = new HashMap<>();
    private SOSFTPOptions options = null;
    private Vector<SOSFileListEntry> fileListEntries = new Vector<>();
    private boolean transferCountersCounted = false;
    private ISOSVFSHandler handler = null;
    private boolean resultSetFileCreated = false;
    private long sumFileSizes = 0L;
    private long counterSuccessfulTransfers = 0;
    private long counterFailedTransfers = 0;
    private long counterSkippedTransfers = 0;
    private long counterSuccessZeroByteFiles = 0;// TransferZeroBytes=true
    private long counterAbortedZeroByteFiles = 0;// TransferZeroBytes=false|strict
    private long counterSkippedZeroByteFiles = 0;// TransferZeroBytes=relaxed
    private long counterBytesTransferred = 0;
    private long counterRecordsInResultSetFile = 0;

    public SOSFileList() {
        super(SOSVfsConstants.strBundleBaseName);
    }

    public SOSFileList(final ISOSVFSHandler handler) {
        this();
        setVFSHandler(handler);
    }

    public SOSFileList(final String[] fileList) {
        this();
        for (String name : fileList) {
            add(name);
        }
    }

    public SOSFileList(final Vector<File> fileList) {
        this();
        for (File file : fileList) {
            add(file.getAbsolutePath());
        }
    }

    public boolean add2SubFolders(final String name) {
        boolean result = false;
        String subFolder = getSubFolderList().get(name);
        if (subFolder == null) {
            result = true;
            getSubFolderList().put(name, "");
        }
        return result;
    }

    public long count() {
        return fileListEntries.size();
    }

    private void countStatus() {
        if (!transferCountersCounted) {
            transferCountersCounted = true;
            counterSuccessfulTransfers = 0;
            counterFailedTransfers = 0;
            counterSkippedTransfers = 0;
            counterBytesTransferred = 0;
            if (fileListEntries != null) {
                for (SOSFileListEntry entry : fileListEntries) {
                    if (entry == null) {
                        continue;
                    }
                    counterBytesTransferred += entry.getFileSize();
                    switch (entry.getTransferStatus()) {
                    case transferred:
                        counterSuccessfulTransfers++;
                        break;
                    case renamed:
                        counterSuccessfulTransfers++;
                        break;
                    case deleted:
                        counterSuccessfulTransfers++;
                        break;
                    case compressed:
                        counterSuccessfulTransfers++;
                        break;
                    case transfer_has_errors:
                        counterFailedTransfers++;
                        break;
                    case transfer_aborted:
                        counterFailedTransfers++;
                        break;
                    case setBack:
                        counterFailedTransfers++;
                        break;
                    case notOverwritten:
                        counterSkippedTransfers++;
                        break;
                    case waiting4transfer:
                        counterFailedTransfers++;
                        break;
                    case transfer_skipped:
                        counterSkippedTransfers++;
                        break;
                    default:
                        break;
                    }
                }
            }
        }
    }

    public long getSuccessfulTransfers() {
        countStatus();
        return counterSuccessfulTransfers;
    }

    public long getFailedTransfers() {
        countStatus();
        return counterFailedTransfers;
    }

    public long getSkippedTransfers() {
        countStatus();
        return counterSkippedTransfers;
    }

    public SOSTransferStateCounts countTransfers() {
        countStatus();
        SOSTransferStateCounts counter = new SOSTransferStateCounts();
        counter.setSkipped(counterSkippedTransfers);
        counter.setSuccess(counterSuccessfulTransfers);
        counter.setFailed(counterFailedTransfers);
        counter.setSuccessZeroBytes(counterSuccessZeroByteFiles);
        counter.setAbortedZeroBytes(counterAbortedZeroByteFiles);
        counter.setSkippedZeroBytes(counterSkippedZeroByteFiles);
        return counter;
    }

    public Vector<SOSFileListEntry> getList() {
        if (fileListEntries == null) {
            fileListEntries = new Vector<>();
        }
        return fileListEntries;
    }

    public void addAll(final SOSFileList fileList) {
        for (SOSFileListEntry entry : fileList.getList()) {
            add(entry.getSourceFileName());
        }
    }

    public void add(final String[] arr, final String subFolder) {
        add(arr, subFolder, false);
    }

    public void add(final String[] arr, final String subFolder, boolean withExistCheck) {
        if (arr == null) {
            return;
        }
        for (String name : arr) {
            try {
                if (withExistCheck && sourceFileTransfer.getFileHandle(name).fileExists()) {
                    add(name);
                } else if (!withExistCheck) {
                    add(name);
                }
            } catch (Exception e) {
                if (withExistCheck) {
                    throw new JobSchedulerException(e);
                }
            }
        }
    }

    public void addFileNames(final Vector<String> fileNames) {
        for (String name : fileNames) {
            add(name);
        }
    }

    public void addFiles(final Vector<File> files) {
        for (File file : files) {
            add(file.getAbsolutePath());
        }
    }

    public SOSFileListEntry add(final String localFileName) {
        if (fileListEntries == null) {
            fileListEntries = new Vector<SOSFileListEntry>();
        }
        SOSFileListEntry entry = find(localFileName);
        if (entry == null) {
            entry = new SOSFileListEntry(localFileName);
            entry.setVfsHandler((ISOSVfsFileTransfer) handler);
            entry.setFileList(this);
            fileListEntries.add(entry);
            entry.setOptions(options);
            if (options.skipTransfer.isFalse()) {
                entry.setStatus(SOSFileListEntry.TransferStatus.waiting4transfer);
            } else {
                entry.setStatus(SOSFileListEntry.TransferStatus.transfer_skipped);
            }
        }
        return entry;
    }

    public void clearFileList() {
        fileListEntries = new Vector<>();
    }

    public void resetTransferCountersCounted() {
        transferCountersCounted = false;
    }

    public void resetNoOfZeroByteSizeFiles() {
        counterSkippedZeroByteFiles = 0;
        counterSuccessZeroByteFiles = 0;
    }

    public SOSFileListEntry add(final SOSFileListEntry entry) {
        if (fileListEntries == null) {
            fileListEntries = new Vector<>();
        }
        SOSFileListEntry findEntry = find(entry.getSourceFilename());
        if (findEntry == null) {
            entry.setVfsHandler((ISOSVfsFileTransfer) handler);
            entry.setFileList(this);
            fileListEntries.add(entry);
            entry.setOptions(options);
            if (options.skipTransfer.isFalse()) {
                entry.setStatus(SOSFileListEntry.TransferStatus.waiting4transfer);
            } else {
                entry.setStatus(SOSFileListEntry.TransferStatus.transfer_skipped);
            }
        }
        return entry;
    }

    public SOSFileListEntry find(final String localFileName) {
        for (SOSFileListEntry entry : fileListEntries) {
            if (entry == null) {
                continue;
            }
            String name = entry.getSourceFileName();
            if (name == null) {
                continue;
            }
            if (fileNamesAreEqual(localFileName, name, true)) {
                return entry;
            }
        }
        return null;
    }

    public long getCounterSuccessZeroByteFiles() {
        return counterSuccessZeroByteFiles;
    }

    public long getCounterAbortedZeroByteFiles() {
        return counterAbortedZeroByteFiles;
    }

    public long getCounterSkippedZeroByteFiles() {
        return counterSkippedZeroByteFiles;
    }

    public void deleteSourceFiles() throws Exception {
        if (options.removeFiles.isTrue()) {
            boolean filesDeleted = false;
            for (SOSFileListEntry entry : fileListEntries) {
                if (entry.getTransferStatus().equals(TransferStatus.transferred)) {
                    if (!filesDeleted) {
                        filesDeleted = true;
                        LOGGER.debug(SOSVfs_D_208.get());
                    }
                    entry.deleteSourceFile();
                }
            }
        }
    }

    public void renameSourceFiles() {
        if (options.removeFiles.isFalse()) {
            for (SOSFileListEntry entry : fileListEntries) {
                entry.renameSourceFile();
            }
        }
    }

    public void logFileList() {
        if (fileListEntries != null && fileListEntries.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (SOSFileListEntry entry : fileListEntries) {
                sb.append("\n");
                sb.append(entry.getFileName4ResultList());
            }
            String msg = sb.toString();
            LOGGER.info(msg);
            JADE_REPORT_LOGGER.info(msg);
        }
    }

    public void createResultSetFile() {
        if (resultSetFileCreated) {
            return;
        }
        resultSetFileCreated = true;
        counterRecordsInResultSetFile = 0L;
        JSFile resultSetFile = null;
        try {
            if (options.resultSetFileName.isDirty() && options.resultSetFileName.isNotEmpty()) {
                resultSetFile = options.resultSetFileName.getJSFile();
                if ("getlist".equalsIgnoreCase(options.getDmzOption("operation")) && !options.getDmzOption("resultfile").isEmpty()) {
                    ISOSVirtualFile jumpResultSetFile = sourceFileTransfer.getFileHandle(options.getDmzOption("resultfile"));
                    if (jumpResultSetFile.fileExists()) {
                        counterRecordsInResultSetFile = writeResultSetFileFromJumpFile(resultSetFile, jumpResultSetFile);
                    }
                } else {
                    for (SOSFileListEntry objListItem : fileListEntries) {
                        String strFileName = objListItem.getFileName4ResultList();
                        resultSetFile.writeLine(strFileName);
                        counterRecordsInResultSetFile++;
                    }
                }
                if (counterRecordsInResultSetFile == 0) {
                    resultSetFile.writeLine("");
                }
                LOGGER.info(String.format("ResultSet to '%1$s' is written", resultSetFile.getCanonicalPath()));
            }
        } catch (Exception e) {
            String msg = "";
            if (resultSetFile != null) {
                try {
                    msg = " '" + resultSetFile.getCanonicalPath() + "'";
                } catch (Throwable ee) {
                }
            }
            throw new JobSchedulerException(String.format("Problems occured creating ResultSet file%s: %s", msg, e.toString()), e);
        } finally {
            if (resultSetFile != null) {
                try {
                    resultSetFile.close();
                } catch (Throwable e) {
                }
            }
        }
    }

    private long writeResultSetFileFromJumpFile(JSFile localResultSetFile, ISOSVirtualFile jumpResultSetFile) {
        byte[] buffer = new byte[options.bufferSize.value()];
        int bytesTransferred = 0;
        FileOutputStream fos = null;
        long countLines = 0L;
        try {
            fos = new FileOutputStream(localResultSetFile);
            while ((bytesTransferred = jumpResultSetFile.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesTransferred);
                countLines += new String(buffer).replaceAll("[^\n]*", "").length();
            }
        } catch (Exception e) {
            throw new JobSchedulerException(e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                //
            }
            try {
                jumpResultSetFile.closeInput();
            } catch (Exception e) {
                //
            }
        }
        return countLines;
    }

    public void renameTargetAndSourceFiles() throws Exception {
        final String methodName = "SOSFileList::renameAtomicTransferFiles";
        try {
            if (options.isAtomicTransfer() && options.transactional.isTrue()) {
                LOGGER.debug(SOSVfs_D_209.get());
                boolean skipRenameTarget = false;
                for (SOSFileListEntry entry : fileListEntries) {
                    if (!skipRenameTarget) {
                        entry.renameTargetFile();
                    }
                    entry.createTargetChecksumFile();
                    entry.renameSourceFile();
                    entry.executePostCommands();
                    if (options.cumulateFiles.isTrue()) {
                        skipRenameTarget = true;
                    }
                }
            } else {
                for (SOSFileListEntry entry : fileListEntries) {
                    entry.executePostCommands();
                }
            }
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_210.params(methodName, e.getMessage()), e);
        }
    }

    public void endTransaction() {
        transactionEnd.setParsePattern(JSDateFormat.dfTIMESTAMPS);
        transactionEnd.Value(now());
        // getJumpHistoryFile();
        // writeTransferHistory();
    }

    public void startTransaction() {
        transactionStart.setParsePattern(JSDateFormat.dfTIMESTAMPS);
        transactionStart.Value(now());
    }

    public void rollback() {
        String msg = null;
        if (options.transactional.value()) {
            msg = SOSVfs_I_211.get();
        } else {
            msg = "Rollback aborted files.";
        }
        LOGGER.info(msg);
        JADE_REPORT_LOGGER.info(msg);
        if (options.isAtomicTransfer()) {
            for (SOSFileListEntry entry : fileListEntries) {
                if (!options.transactional.value() && entry.getTransferStatus().equals(TransferStatus.transferred)) {
                    continue;
                }
                entry.setStatus(TransferStatus.transfer_aborted);
                String atomicFileName = entry.getTargetAtomicFileName();
                if (atomicFileName.isEmpty()) {
                    continue;
                }
                atomicFileName = makeFullPathName(options.targetDir.getValue(), entry.getTargetAtomicFileName());
                if (isNotEmpty(entry.getTargetAtomicFileName())) {
                    try {
                        ISOSVirtualFile atomicFile = targetFileTransfer.getFileHandle(atomicFileName);
                        if (atomicFile.fileExists()) {
                            atomicFile.delete();
                        }
                        String strT = SOSVfs_D_212.params(atomicFileName);
                        LOGGER.info(strT);
                        JADE_REPORT_LOGGER.info(strT);
                        entry.setTargetAtomicFileName(EMPTY_STRING);
                        entry.setStatus(TransferStatus.setBack);
                    } catch (Exception e) {
                        LOGGER.error(e.toString());
                    }
                }
                if (!entry.isTargetFileExists()) {
                    try {
                        String strTargetFilename = makeFullPathName(options.targetDir.getValue(), entry.getTargetFileName());
                        ISOSVirtualFile targetFile = targetFileTransfer.getFileHandle(strTargetFilename);
                        if (targetFile.fileExists()) {
                            targetFile.delete();
                        }
                        msg = SOSVfs_D_212.params(targetFile.getName());
                        LOGGER.info(msg);
                        JADE_REPORT_LOGGER.info(msg);
                        entry.setStatus(TransferStatus.setBack);
                    } catch (Exception e) {
                        LOGGER.error(e.toString());
                    }
                }
                if (entry.hasTargetChecksumFile()) {
                    try {
                        ISOSVirtualFile targetChecksumFile = entry.getTargetChecksumFile();
                        if (targetChecksumFile.fileExists()) {
                            targetChecksumFile.delete();
                        }
                        msg = SOSVfs_D_212.params(targetChecksumFile.getName());
                        LOGGER.info(msg);
                        JADE_REPORT_LOGGER.info(msg);
                    } catch (Exception e) {
                        LOGGER.error(e.toString());
                    }
                }
                entry.rollbackRenameSourceFile();
            }
        } else {
            for (SOSFileListEntry entry : fileListEntries) {
                if (entry.getTransferStatus().equals(TransferStatus.transferred)) {
                    continue;
                }
                entry.setStatus(TransferStatus.transfer_aborted);
            }
        }
        if (!options.transactional.value()) {
            try {
                deleteSourceFiles();
            } catch (Exception e) {
                LOGGER.error(e.toString());
            }
        }
        endTransaction();
    }

    public long size() {
        long size = 0;
        if (getList() != null) {
            size = getList().size();
        }
        return size;
    }

    private boolean fileNamesAreEqual(String filenameA, String filenameB, boolean caseSensitiv) {
        String a = filenameA.replaceAll("[\\\\/]+", "/");
        String b = filenameB.replaceAll("[\\\\/]+", "/");
        return caseSensitiv ? a.equals(b) : a.equalsIgnoreCase(b);
    }

    public void handleZeroByteFiles() {
        counterSuccessZeroByteFiles = 0;
        counterAbortedZeroByteFiles = 0;
        counterSkippedZeroByteFiles = 0;
        long total = size();
        if (total == 0) {
            return;
        }
        long emptyFiles = getList().stream().filter(e -> e.getFileSize() <= 0).count();

        switch (options.zeroByteTransfer.getEnum()) {
        case yes: // transfer zero byte files
            counterSuccessZeroByteFiles = emptyFiles;
            break;
        case no: // transfer only if least one is not a zero byte file
            if (emptyFiles == total) {
                counterAbortedZeroByteFiles = emptyFiles;
                for (SOSFileListEntry entry : getList()) {
                    entry.setTransferStatus(TransferStatus.transfer_aborted);
                }
                throw new JobSchedulerException(String.format("All %s files have zero byte size, transfer aborted", emptyFiles));
            } else {
                counterSuccessZeroByteFiles = emptyFiles;
            }
            break;
        case relaxed:// not transfer zero byte files
            if (emptyFiles > 0) {
                for (SOSFileListEntry entry : getList()) {
                    if (entry.getFileSize() <= 0) {
                        entry.setIgnoredDueToZerobyteConstraint();
                        counterSkippedZeroByteFiles++;
                    }
                }
            }
            break;
        case strict: // abort transfer if any zero byte file is found
            if (emptyFiles > 0) {
                counterAbortedZeroByteFiles = emptyFiles;
                for (SOSFileListEntry entry : getList()) {
                    entry.setTransferStatus(TransferStatus.transfer_aborted);
                }
                throw new JobSchedulerException(String.format("%s zero byte size file(s) detected", emptyFiles));
            }
        }
    }

    public boolean isEmpty() {
        return getList().isEmpty();
    }

    public long getSumFileSizes() {
        return sumFileSizes;
    }

    public void setSumFileSizes(long val) {
        sumFileSizes = val;
    }

    public void setVFSHandler(final ISOSVFSHandler val) {
        handler = val;
    }

    public ISOSVFSHandler getVFSHandler() {
        return handler;
    }

    public HashMap<String, String> getSubFolderList() {
        return subFolders;
    }

    public SOSFTPOptions getOptions() {
        return options;
    }

    public void setOptions(final SOSFTPOptions val) {
        options = val;
    }

    // currently not used
    public long getBytesTransferred() {
        return counterBytesTransferred;
    }

}
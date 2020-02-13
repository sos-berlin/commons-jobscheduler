package com.sos.VirtualFileSystem.DataElements;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionJadeOperation.enuJadeOperations;
import com.sos.JSHelper.interfaces.IJobSchedulerEventHandler;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.VirtualFileSystem.DataElements.SOSFileListEntry.TransferStatus;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;
import com.sos.VirtualFileSystem.common.SOSFileEntry;
import com.sos.VirtualFileSystem.common.SOSVfsConstants;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;
import com.sos.i18n.annotation.I18NResourceBundle;

import sos.util.SOSDate;
import sos.util.SOSString;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSFileList extends SOSVfsMessageCodes {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSFileList.class);
    private static final Logger JADE_REPORT_LOGGER = LoggerFactory.getLogger(VFSFactory.getLoggerName());

    public static int LOG_INFO_STEP = 1_000;

    public ISOSVfsFileTransfer sourceFileTransfer = null;
    public ISOSVfsFileTransfer targetFileTransfer = null;

    private final HashMap<String, String> subFolders = new HashMap<>();
    private SOSFTPOptions options = null;
    private IJobSchedulerEventHandler eventHandler = null;
    private List<SOSFileListEntry> fileListEntries = new Vector<>();
    private boolean transferCountersCounted = false;
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
    private String lastErrorMessage;
    private int connectionErrorRetryInterval = 0;// in seconds

    public SOSFileList(SOSFTPOptions opt, IJobSchedulerEventHandler handler) {
        super(SOSVfsConstants.strBundleBaseName);
        options = opt;
        eventHandler = handler;
        setConnectionErrorRetryInterval();
    }

    public void create(final List<SOSFileEntry> entries, int maxFiles) {
        fileListEntries.clear();
        if (maxFiles > entries.size()) {
            int i = 0;
            for (SOSFileEntry entry : entries) {
                this.add(entry);
                i++;
                if (i == maxFiles) {
                    break;
                }
            }
        } else {
            for (SOSFileEntry entry : entries) {
                this.add(entry);
            }
        }
    }

    public SOSFileListEntry add(final SOSFileEntry fileEntry) {
        if (fileListEntries == null) {
            fileListEntries = new Vector<SOSFileListEntry>();
        }
        SOSFileListEntry entry = new SOSFileListEntry(fileEntry);
        entry.setParent(this);
        fileListEntries.add(entry);
        if (options.skipTransfer.isFalse()) {
            entry.setStatus(SOSFileListEntry.TransferStatus.waiting4transfer);
        } else {
            entry.setStatus(SOSFileListEntry.TransferStatus.transfer_skipped);
        }

        return entry;
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
                    case moved:
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

    public List<SOSFileListEntry> getList() {
        if (fileListEntries == null) {
            fileListEntries = new ArrayList<SOSFileListEntry>();
        }
        return fileListEntries;
    }

    public void clearFileList() {
        fileListEntries = new ArrayList<SOSFileListEntry>();
    }

    public void resetTransferCountersCounted() {
        transferCountersCounted = false;
    }

    public void resetNoOfZeroByteSizeFiles() {
        counterSkippedZeroByteFiles = 0;
        counterSuccessZeroByteFiles = 0;
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
            if (options.transactional.isFalse() && options.operation.value().equals(enuJadeOperations.move)) {
                // already removed
                return;
            }

            String msg = "delete source files";
            LOGGER.info(msg);
            JADE_REPORT_LOGGER.info(msg);
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

    public void rollback() {
        if (options.isAtomicTransfer()) {
            String msg = "Rollback atomic transfer";
            LOGGER.info(msg);
            JADE_REPORT_LOGGER.info(msg);
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
                            atomicFile.delete(false);
                        }
                        msg = SOSVfs_D_212.params(atomicFileName);
                        LOGGER.info(msg);
                        JADE_REPORT_LOGGER.info(msg);
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
                            targetFile.delete(false);
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
                            targetChecksumFile.delete(false);
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
            String msg = "Set transfer status";
            LOGGER.info(msg);
            JADE_REPORT_LOGGER.info(msg);
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
    }

    public long size() {
        long size = 0;
        if (getList() != null) {
            size = getList().size();
        }
        return size;
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

    public HashMap<String, String> getSubFolderList() {
        return subFolders;
    }

    public SOSFTPOptions getOptions() {
        return options;
    }

    // currently not used
    public long getBytesTransferred() {
        return counterBytesTransferred;
    }

    public ISOSVfsFileTransfer getTargetClient() {
        return targetFileTransfer;
    }

    public ISOSVfsFileTransfer getSourceClient() {
        return sourceFileTransfer;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public void setLastErrorMessage(String val) {
        lastErrorMessage = val;
    }

    public IJobSchedulerEventHandler getEventHandler() {
        return eventHandler;
    }

    private void setConnectionErrorRetryInterval() {
        String val = options.connection_error_retry_interval.getValue();
        if (!SOSString.isEmpty(val)) {
            try {
                connectionErrorRetryInterval = SOSDate.resolveAge("s", val).intValue();
            } catch (Exception ex) {
                LOGGER.warn(String.format("[serConnectionErrorRetryInterval]%s", ex.toString()), ex);
            }
        }
    }

    public int getConnectionErrorRetryInterval() {
        return connectionErrorRetryInterval;
    }

}
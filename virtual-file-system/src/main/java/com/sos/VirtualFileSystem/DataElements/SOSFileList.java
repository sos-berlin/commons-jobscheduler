package com.sos.VirtualFileSystem.DataElements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.sos.JSHelper.DataElements.JSDataElementDate;
import com.sos.JSHelper.DataElements.JSDateFormat;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSCsvFile;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.VirtualFileSystem.DataElements.SOSFileListEntry.enuTransferStatus;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;
import com.sos.VirtualFileSystem.common.SOSVfsConstants;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author KB */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSFileList extends SOSVfsMessageCodes {

    private static final Logger LOGGER = Logger.getLogger(SOSFileList.class);
    private static final Logger JADE_REPORT_LOGGER = Logger.getLogger(VFSFactory.getLoggerName());
    private final String historyFields =
            "guid;mandator;transfer_end;pid;ppid;operation;localhost;localhost_ip;local_user;remote_host;remote_host_ip;remote_user;protocol;"
                    + "port;local_dir;remote_dir;local_filename;remote_filename;file_size;md5;status;last_error_message;log_filename";
    private final String newHistoryFields = "jump_host;jump_host_ip;jump_port;jump_protocol;jump_user;transfer_start;modification_date";
    private final JSDataElementDate dteTransactionStart = new JSDataElementDate(now(), JSDateFormat.dfTIMESTAMPS);
    private final JSDataElementDate dteTransactionEnd = new JSDataElementDate(now(), JSDateFormat.dfTIMESTAMPS);
    private final HashMap<String, String> objSubFolders = new HashMap<>();
    private SOSFTPOptions objOptions = null;
    private Vector<SOSFileListEntry> objFileListEntries = new Vector<>();
    private boolean transferCountersCounted = false;
    private SOSFileList objParent = null;
    private ISOSVFSHandler objVFS = null;
    private boolean flgHistoryFileAlreadyWritten = false;
    private boolean flgResultSetFileAlreadyCreated = false;
    public ISOSVfsFileTransfer objDataTargetClient = null;
    public ISOSVfsFileTransfer objDataSourceClient = null;
    public int lngNoOfZeroByteSizeFiles = 0;
    long lngSuccessfulTransfers = 0;
    long lngFailedTransfers = 0;
    long lngSkippedTransfers = 0;
    long lngNoOfTransferHistoryRecordsSent = 0;
    long lngNoOfHistoryFileRecordsWritten = 0;
    long lngNoOfRecordsInResultSetFile = 0;
    long lngNoOfBytesTransferred = 0;

    public SOSFileList() {
        super(SOSVfsConstants.strBundleBaseName);
    }

    public SOSFileList(final ISOSVFSHandler pobjVFS) {
        this();
        this.setVFSHandler(pobjVFS);
    }

    public SOSFileList(final String[] pstrFileList) {
        this();
        for (String strFileName : pstrFileList) {
            this.add(strFileName);
        }
    }

    public SOSFileList(final Vector<File> pvecFileList) {
        this();
        for (File fleFileName : pvecFileList) {
            this.add(fleFileName.getAbsolutePath());
        }
    }

    public void setVFSHandler(final ISOSVFSHandler pobjVFS) {
        objVFS = pobjVFS;
    }

    public ISOSVFSHandler getVFSHandler() {
        return objVFS;
    }

    public HashMap<String, String> getSubFolderList() {
        return objSubFolders;
    }

    public boolean add2SubFolders(final String pstrSubFolderName) {
        boolean flgR = false;
        String strT = getSubFolderList().get(pstrSubFolderName);
        if (strT == null) {
            flgR = true;
            getSubFolderList().put(pstrSubFolderName, "");
        }
        return flgR;
    }

    public void setParent(final SOSFileList pobjParent) {
        objParent = pobjParent;
    }

    public SOSFTPOptions getOptions() {
        return objOptions;
    }

    public void setOptions(final SOSFTPOptions pobjOptions) {
        objOptions = pobjOptions;
    }

    public long count() {
        return objFileListEntries.size();
    }

    private void countStatus() {
        if (!transferCountersCounted) {
            transferCountersCounted = true;
            lngSuccessfulTransfers = 0;
            lngFailedTransfers = 0;
            lngSkippedTransfers = 0;
            lngNoOfBytesTransferred = 0;
            if (objFileListEntries != null) {
                for (SOSFileListEntry objEntry : objFileListEntries) {
                    if (objEntry == null) {
                        continue;
                    }
                    lngNoOfBytesTransferred += objEntry.getFileSize();
                    switch (objEntry.getTransferStatus()) {
                    case transferred:
                        lngSuccessfulTransfers++;
                        break;
                    case renamed:
                        lngSuccessfulTransfers++;
                        break;
                    case deleted:
                        lngSuccessfulTransfers++;
                        break;
                    case compressed:
                        lngSuccessfulTransfers++;
                        break;
                    case transfer_has_errors:
                        lngFailedTransfers++;
                        break;
                    case transfer_aborted:
                        lngFailedTransfers++;
                        break;
                    case setBack:
                        lngFailedTransfers++;
                        break;
                    case notOverwritten:
                        lngSkippedTransfers++;
                        break;
                    case waiting4transfer:
                        lngFailedTransfers++;
                        break;
                    case transfer_skipped:
                        lngSkippedTransfers++;
                        break;
                    default:
                        break;
                    }
                }
            }
        }
    }

    public long getNoOfBytesTransferred() {
        return lngNoOfBytesTransferred;
    }

    public long getSuccessfulTransfers() {
        this.countStatus();
        return lngSuccessfulTransfers;
    }

    public long getFailedTransfers() {
        countStatus();
        return lngFailedTransfers;
    }

    public long getSkippedTransfers() {
        countStatus();
        return lngSkippedTransfers;
    }

    public SOSTransferStateCounts countTransfers() {
        countStatus();
        SOSTransferStateCounts counts = new SOSTransferStateCounts();
        counts.setSkippedTransfers(lngSkippedTransfers);
        counts.setSuccessTransfers(lngSuccessfulTransfers);
        counts.setFailedTransfers(lngFailedTransfers);
        counts.setZeroBytesTransfers(lngNoOfZeroByteSizeFiles);
        return counts;
    }

    public Vector<SOSFileListEntry> getList() {
        if (objFileListEntries == null) {
            objFileListEntries = new Vector<>();
        }
        return objFileListEntries;
    }

    public void addAll(final SOSFileList pobjFileList) {
        for (SOSFileListEntry objFile : pobjFileList.getList()) {
            this.add(objFile.getSourceFileName());
        }
    }

    public void add(final String[] pstrA, final String pstrFolderName) {
        add(pstrA, pstrFolderName, false);
    }

    public void add(final String[] pstrA, final String pstrFolderName, boolean withExistCheck) {
        if (pstrA == null) {
            return;
        }
        for (String strFileName : pstrA) {
            try {
                if (withExistCheck && objDataSourceClient.getFileHandle(strFileName).fileExists()) {
                    this.add(strFileName);
                } else if (!withExistCheck) {
                    this.add(strFileName);
                }
            } catch (Exception e) {
                if (withExistCheck) {
                    throw new JobSchedulerException(e);
                }
            }
        }
    }

    public void addFileNames(final Vector<String> pstrA) {
        for (String strFileName : pstrA) {
            this.add(strFileName);
        }
    }

    public void addFiles(final Vector<File> pfleA) {
        for (File fleFileName : pfleA) {
            this.add(fleFileName.getAbsolutePath());
        }
    }

    public SOSFileListEntry add(final String pstrLocalFileName) {
        if (objFileListEntries == null) {
            objFileListEntries = new Vector<SOSFileListEntry>();
        }
        SOSFileListEntry objEntry = this.find(pstrLocalFileName);
        if (objEntry == null) {
            objEntry = new SOSFileListEntry(pstrLocalFileName);
            objEntry.setVfsHandler((ISOSVfsFileTransfer) objVFS);
            objEntry.setParent(this);
            objFileListEntries.add(objEntry);
            objEntry.setOptions(objOptions);
            if (objOptions.skipTransfer.isFalse()) {
                objEntry.setStatus(SOSFileListEntry.enuTransferStatus.waiting4transfer);
            } else {
                objEntry.setStatus(SOSFileListEntry.enuTransferStatus.transfer_skipped);
            }
        }
        return objEntry;
    }

    public void clearFileList() {
        objFileListEntries = new Vector<>();
    }

    public SOSFileListEntry add(final SOSFileListEntry pobjFileListEntry) {
        if (objFileListEntries == null) {
            objFileListEntries = new Vector<>();
        }
        SOSFileListEntry objEntry = this.find(pobjFileListEntry.getSourceFilename());
        if (objEntry == null) {
            pobjFileListEntry.setVfsHandler((ISOSVfsFileTransfer) objVFS);
            pobjFileListEntry.setParent(this);
            objFileListEntries.add(pobjFileListEntry);
            pobjFileListEntry.setOptions(objOptions);
            if (objOptions.skipTransfer.isFalse()) {
                pobjFileListEntry.setStatus(SOSFileListEntry.enuTransferStatus.waiting4transfer);
            } else {
                pobjFileListEntry.setStatus(SOSFileListEntry.enuTransferStatus.transfer_skipped);
            }
        }
        return pobjFileListEntry;
    }

    public SOSFileListEntry find(final String pstrLocalFileName) {
        for (SOSFileListEntry objEntry : objFileListEntries) {
            if (objEntry == null) {
                continue;
            }
            String strT = objEntry.getSourceFileName();
            if (strT == null) {
                continue;
            }
            if (fileNamesAreEqual(pstrLocalFileName, strT, true)) {
                return objEntry;
            }
        }
        return null;
    }

    public int getZeroByteCount() {
        return lngNoOfZeroByteSizeFiles;
    }

    public void deleteSourceFiles() throws Exception {
        if (objOptions.removeFiles.isTrue()) {
            boolean filesDeleted = false;
            for (SOSFileListEntry entry : objFileListEntries) {
                if (entry.getTransferStatus() == enuTransferStatus.transferred) {
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
        if (objOptions.removeFiles.isFalse()) {
            for (SOSFileListEntry objListItem : objFileListEntries) {
                objListItem.renameSourceFile();
            }
        }
    }

    public void logFileList() {
        String strT = "";
        for (SOSFileListEntry objListItem : objFileListEntries) {
            String strFileName = objListItem.getFileName4ResultList();
            strT += "\n" + strFileName;
        }
        LOGGER.info(strT);
        JADE_REPORT_LOGGER.info(strT);
    }

    public void createResultSetFile() {
        if (flgResultSetFileAlreadyCreated) {
            return;
        }
        flgResultSetFileAlreadyCreated = true;
        lngNoOfRecordsInResultSetFile = 0L;
        try {
            if (objOptions.resultSetFileName.isDirty() && objOptions.resultSetFileName.isNotEmpty()) {
                JSFile resultSetFile = objOptions.resultSetFileName.getJSFile();
                if ("copyFromInternet".equals(objOptions.getDmzOption("operation")) && !objOptions.getDmzOption("resultfile").isEmpty()) {
                    ISOSVirtualFile jumpResultSetFile = objDataSourceClient.getFileHandle(objOptions.getDmzOption("resultfile"));
                    if (jumpResultSetFile.fileExists()) {
                        lngNoOfRecordsInResultSetFile = transferResultSetFile(resultSetFile, jumpResultSetFile);
                    }
                } else {
                    for (SOSFileListEntry objListItem : objFileListEntries) {
                        String strFileName = objListItem.getFileName4ResultList();
                        resultSetFile.writeLine(strFileName);
                        lngNoOfRecordsInResultSetFile++;
                    }
                }
                if (lngNoOfRecordsInResultSetFile == 0) {
                    resultSetFile.writeLine("");
                }
                resultSetFile.close();
                LOGGER.info(String.format("ResultSet to '%1$s' is written", resultSetFile.getAbsoluteFile()));
            }
        } catch (Exception e) {
            throw new JobSchedulerException("Problems occured creating ResultSetFile", e);
        }
    }

    private long transferResultSetFile(JSFile localResultSetFile, ISOSVirtualFile jumpResultSetFile) {
        byte[] buffer = new byte[objOptions.bufferSize.value()];
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
            if (objOptions.isAtomicTransfer() && objOptions.transactional.isTrue()) {
                LOGGER.debug(SOSVfs_D_209.get());
                boolean skipRenameTarget = false;
                for (SOSFileListEntry objListItem : objFileListEntries) {
                    if (!skipRenameTarget) {
                        objListItem.renameTargetFile();
                    }
                    objListItem.createChecksumFile();
                    objListItem.renameSourceFile();
                    objListItem.executePostCommands();
                    if (objOptions.cumulateFiles.isTrue()) {
                        skipRenameTarget = true;
                    }
                }
            } else {
                for (SOSFileListEntry objListItem : objFileListEntries) {
                    objListItem.executePostCommands();
                }
            }
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_210.params(methodName, e.getMessage()), e);
        }
    }

    public void endTransaction() {
        dteTransactionEnd.setParsePattern(JSDateFormat.dfTIMESTAMPS);
        dteTransactionEnd.Value(now());
        getJumpHistoryFile();
        writeTransferHistory();
    }

    public void startTransaction() {
        dteTransactionStart.setParsePattern(JSDateFormat.dfTIMESTAMPS);
        dteTransactionStart.Value(now());
    }

    public void rollback() {
        String msg = null;
        if (objOptions.transactional.value()) {
            msg = SOSVfs_I_211.get();
        } else {
            msg = "Rollback aborted files.";
        }
        LOGGER.info(msg);
        JADE_REPORT_LOGGER.info(msg);
        if (objOptions.isAtomicTransfer()) {
            for (SOSFileListEntry entry : objFileListEntries) {
                if (!objOptions.transactional.value() && entry.getTransferStatus().equals(enuTransferStatus.transferred)) {
                    continue;
                }
                entry.setStatus(enuTransferStatus.transfer_aborted);
                String atomicFileName = entry.getAtomicFileName();
                if (atomicFileName.isEmpty()) {
                    continue;
                }
                atomicFileName = makeFullPathName(objOptions.targetDir.getValue(), entry.getAtomicFileName());
                if (isNotEmpty(entry.getAtomicFileName())) {
                    try {
                        ISOSVirtualFile atomicFile = objDataTargetClient.getFileHandle(atomicFileName);
                        if (atomicFile.fileExists()) {
                            atomicFile.delete();
                        }
                        String strT = SOSVfs_D_212.params(atomicFileName);
                        LOGGER.debug(strT);
                        JADE_REPORT_LOGGER.info(strT);
                        entry.setAtomicFileName(EMPTY_STRING);
                        entry.setStatus(enuTransferStatus.setBack);
                    } catch (Exception e) {
                        LOGGER.error(e.toString());
                    }
                }
                if (!entry.isTargetFileAlreadyExists()) {
                    try {
                        String strTargetFilename = makeFullPathName(objOptions.targetDir.getValue(), entry.getTargetFileName());
                        ISOSVirtualFile targetFile = objDataTargetClient.getFileHandle(strTargetFilename);
                        if (targetFile.fileExists()) {
                            targetFile.delete();
                        }
                        msg = SOSVfs_D_212.params(targetFile.getName());
                        LOGGER.debug(msg);
                        JADE_REPORT_LOGGER.info(msg);
                        entry.setStatus(enuTransferStatus.setBack);
                    } catch (Exception e) {
                        LOGGER.error(e.toString());
                    }
                }
                if (entry.hasChecksumFile()) {
                    try {
                        ISOSVirtualFile checksumFile = entry.getChecksumFile();
                        if (checksumFile.fileExists()) {
                            checksumFile.delete();
                        }
                        msg = SOSVfs_D_212.params(checksumFile.getName());
                        LOGGER.debug(msg);
                        JADE_REPORT_LOGGER.info(msg);
                    } catch (Exception e) {
                        LOGGER.error(e.toString());
                    }
                }
                entry.rollbackRenameSourceFile();
            }
        } else {
            for (SOSFileListEntry entry : objFileListEntries) {
                if (entry.getTransferStatus().equals(enuTransferStatus.transferred)) {
                    continue;
                }
                entry.setStatus(enuTransferStatus.transfer_aborted);
            }
        }
        if (!objOptions.transactional.value()) {
            try {
                deleteSourceFiles();
            } catch (Exception e) {
                LOGGER.error(e.toString());
            }
        }
        this.endTransaction();
    }

    public void writeTransferHistory() {
        if (flgHistoryFileAlreadyWritten) {
            return;
        }
        flgHistoryFileAlreadyWritten = true;
        dteTransactionEnd.setFormatString(JSDateFormat.dfTIMESTAMPS);
        dteTransactionStart.setFormatString(JSDateFormat.dfTIMESTAMPS);
        long duration = dteTransactionEnd.getDateObject().getTime() - dteTransactionStart.getDateObject().getTime();
        String operation = objOptions.operation.getValue();
        String msg = SOSVfs_D_213.params(dteTransactionStart.getFormattedValue(), dteTransactionEnd.getFormattedValue(), duration, operation);
        LOGGER.debug(msg);
        JADE_REPORT_LOGGER.info(msg);
        String historyFileName = objOptions.historyFileName.getValue();
        JSFile historyFile = null;
        try {
            if (objOptions.historyFileName.isDirty()) {
                historyFile = objOptions.historyFileName.getJSFile();
                if (objOptions.historyFileAppendMode.isTrue()) {
                    historyFile.setAppendMode(true);
                    if (!historyFile.exists()) {
                        historyFile.writeLine(historyFields + ";" + newHistoryFields);
                    }
                } else {
                    historyFile.writeLine(historyFields + ";" + newHistoryFields);
                }
                for (SOSFileListEntry entry : objFileListEntries) {
                    historyFile.writeLine(entry.toCsv());
                    lngNoOfHistoryFileRecordsWritten++;
                }
                LOGGER.info(String.format("%s records written to history file '%s', HistoryFileAppendMode = %s", lngNoOfHistoryFileRecordsWritten,
                        historyFileName, objOptions.historyFileAppendMode.getValue()));
            }
        } catch (Exception e) {
            LOGGER.warn(String.format("Error occured during writing of the history file: %s", e.toString()));
        } finally {
            if (historyFile != null) {
                try {
                    historyFile.close();
                } catch (Exception ex) {
                    //
                }
            }
        }
        for (SOSFileListEntry entry : objFileListEntries) {
            if (!entry.getTargetFileName().isEmpty()) {
                msg = entry.toString();
                LOGGER.trace(msg);
                JADE_REPORT_LOGGER.info(msg);
            }
        }
    }

    public void getJumpHistoryFile() {
        String operation = this.objOptions.getDmzOption("operation");
        String historyFilename = this.objOptions.getDmzOption("history");
        if (!operation.isEmpty() && !historyFilename.isEmpty()) {
            File localTempHistory = null;
            Map<String, Map<String, String>> jumpHistoryRecords = null;
            try {
                if ("copyFromInternet".equals(operation)) {
                    ISOSVirtualFile historyFile = this.objDataSourceClient.getFileHandle(historyFilename);
                    if (historyFile.fileExists()) {
                        localTempHistory = transferJumpHistoryFile(historyFile);
                        jumpHistoryRecords = readJumpHistory(localTempHistory, "remote_filename");
                        for (SOSFileListEntry entry : objFileListEntries) {
                            if (!entry.getTargetFileName().isEmpty()) {
                                entry.setJumpHistoryRecord(jumpHistoryRecords.get(adjustFileSeparator(entry.getTargetFileName())));
                            }
                        }
                    }
                } else if ("copyToInternet".equals(operation)) {
                    ISOSVirtualFile historyFile = this.objDataTargetClient.getFileHandle(historyFilename);
                    if (historyFile.fileExists()) {
                        localTempHistory = transferJumpHistoryFile(historyFile);
                        jumpHistoryRecords = readJumpHistory(localTempHistory, "local_filename");
                        for (SOSFileListEntry entry : objFileListEntries) {
                            if (!entry.getTargetFileName().isEmpty()) {
                                entry.setJumpHistoryRecord(jumpHistoryRecords.get(adjustFileSeparator(entry.getTargetFileNameAndPath())));
                            }
                        }
                    }
                }
            } catch (JobSchedulerException e) {
                throw e;
            } catch (Exception e) {
                throw new JobSchedulerException(e);
            }

        }

    }

    private Map<String, Map<String, String>> readJumpHistory(File localTempHistory, String primaryKey) {
        Map<String, Map<String, String>> records = new HashMap<String, Map<String, String>>();
        Map<String, String> recordFields = null;
        String primaryKeyValue = "";
        JSCsvFile hwFile = null;
        try {
            hwFile = new JSCsvFile(localTempHistory.getAbsolutePath());
            hwFile.setCheckColumnCount(false);
            String[] strValues = null;
            hwFile.loadHeaders();
            String[] strHeader = hwFile.getHeaders();
            while ((strValues = hwFile.readCSVLine()) != null) {
                primaryKeyValue = "";
                recordFields = new HashMap<String, String>();
                int j = 0;
                for (String val : strValues) {
                    String strFieldName = strHeader[j++];
                    if (val == null) {
                        val = "";
                    }
                    recordFields.put(strFieldName, val);
                    if (strFieldName.endsWith(primaryKey)) {
                        primaryKeyValue = adjustFileSeparator(val);
                    }
                }
                if (!primaryKeyValue.isEmpty()) {
                    records.put(primaryKeyValue, recordFields);
                }
            }
        } catch (Exception e) {
            throw new JobSchedulerException(e);
        } finally {
            try {
                if (hwFile != null) {
                    hwFile.close();
                }
            } catch (IOException e) {
                //
            }
        }
        return records;
    }

    private File transferJumpHistoryFile(ISOSVirtualFile jumpHistoryFile) {
        File localTempHistory = null;
        if (jumpHistoryFile != null) {
            byte[] buffer = new byte[objOptions.bufferSize.value()];
            int bytesTransferred = 0;
            FileOutputStream fos = null;
            try {
                localTempHistory = File.createTempFile("jade-", null);
                localTempHistory.deleteOnExit();
                fos = new FileOutputStream(localTempHistory);
                while ((bytesTransferred = jumpHistoryFile.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesTransferred);
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
                    jumpHistoryFile.closeInput();
                } catch (Exception e) {
                    //
                }
            }
        }
        return localTempHistory;
    }

    public long size() {
        long intS = 0;
        if (getList() != null) {
            intS = this.getList().size();
        }
        return intS;
    }

    private boolean fileNamesAreEqual(String filenameA, String filenameB, boolean caseSensitiv) {
        String a = filenameA.replaceAll("[\\\\/]+", "/");
        String b = filenameB.replaceAll("[\\\\/]+", "/");
        return caseSensitiv ? a.equals(b) : a.equalsIgnoreCase(b);
    }

    public void handleZeroByteFiles() {
        switch (objOptions.zeroByteTransfer.getEnum()) {
        case yes:
            break;
        case no:
            if (size() > 0) {
                boolean allFilesAreEmpty = true;
                for (SOSFileListEntry entry : getList()) {
                    if (entry.getFileSize() > 0) {
                        allFilesAreEmpty = false;
                        break;
                    }
                }
                if (allFilesAreEmpty) {
                    throw new JobSchedulerException("All files have zero byte size, transfer aborted");
                }
            }
            break;
        case relaxed:
            for (SOSFileListEntry entry : getList()) {
                if (entry.getFileSize() <= 0) {
                    entry.setIgnoredDueToZerobyteConstraint();
                    lngNoOfZeroByteSizeFiles++;
                }
            }
            break;
        case strict:
            for (SOSFileListEntry entry : getList()) {
                if (entry.getFileSize() <= 0) {
                    throw new JobSchedulerException(String.format("zero byte size file detected: %1$s", entry.getSourceFilename()));
                }
            }
        }
    }

    public boolean isEmpty() {
        return this.getList().isEmpty();
    }

}
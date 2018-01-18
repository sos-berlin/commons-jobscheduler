package com.sos.VirtualFileSystem.DataElements;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

import sos.util.SOSString;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.interfaces.IJobSchedulerEventHandler;
import com.sos.JSHelper.interfaces.ISOSFtpOptions;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.IJadeTransferDetailHistoryData;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSConnection2Options;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;
import com.sos.VirtualFileSystem.common.SOSVfsConstants;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSFileListEntry extends SOSVfsMessageCodes implements Runnable, IJadeTransferDetailHistoryData {

    private static final String FIELD_JUMP_USER = "jump_user";
    private static final String FIELD_JUMP_PROTOCOL = "jump_protocol";
    private static final String FIELD_JUMP_PORT = "jump_port";
    private static final String FIELD_JUMP_HOST_IP = "jump_host_ip";
    private static final String FIELD_JUMP_HOST = "jump_host";
    private static final String FIELD_LOG_FILENAME = "log_filename";
    private static final String FIELD_LAST_ERROR_MESSAGE = "last_error_message";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_MD5 = "md5";
    private static final String FIELD_FILE_SIZE = "file_size";
    private static final String FIELD_REMOTE_FILENAME = "remote_filename";
    private static final String FIELD_LOCAL_FILENAME = "local_filename";
    private static final String FIELD_REMOTE_DIR = "remote_dir";
    private static final String FIELD_LOCAL_DIR = "local_dir";
    private static final String FIELD_PORT = "port";
    private static final String FIELD_PROTOCOL = "protocol";
    private static final String FIELD_REMOTE_USER = "remote_user";
    private static final String FIELD_REMOTE_HOST_IP = "remote_host_ip";
    private static final String FIELD_REMOTE_HOST = "remote_host";
    private static final String FIELD_LOCAL_USER = "local_user";
    private static final String FIELD_LOCALHOST_IP = "localhost_ip";
    private static final String FIELD_LOCALHOST = "localhost";
    private static final String FIELD_OPERATION = "operation";
    private static final String FIELD_PPID = "ppid";
    private static final String FIELD_PID = "pid";
    private static final String FIELD_TRANSFER_START = "transfer_start";
    private static final String FIELD_TRANSFER_END = "transfer_end";
    private static final String FIELD_MANDATOR = "mandator";
    private static final String FIELD_GUID = "guid";
    private static final String FIELD_MODIFICATION_DATE = "modification_date";
    private static final Logger LOGGER = Logger.getLogger(SOSFileListEntry.class);
    private static final Logger JADE_REPORT_LOGGER = Logger.getLogger(VFSFactory.getLoggerName());
    private final String guid = UUID.randomUUID().toString();
    private ISOSVirtualFile fleSourceFile = null;
    private String strSourceFileName = null;
    private String strSourceTransferName = null;
    private String strTargetTransferName = null;
    private String strTargetFileName = null;
    private long lngNoOfBytesTransferred = 0;
    private long lngFileSize = -1L;
    private long lastCheckedFileSize = -1L;
    private long lngFileModDate = -1L;
    private long lngTransferProgress = 0;
    private boolean flgTransactionalLocalFile = false;
    private SOSFTPOptions objOptions = null;
    private String strAtomicFileName = EMPTY_STRING;
    private enuTransferStatus eTransferStatus = enuTransferStatus.transferUndefined;
    private ISOSVfsFileTransfer objDataSourceClient = null;
    private ISOSVfsFileTransfer objDataTargetClient = null;
    private ISOSVirtualFile objTargetTransferFile = null;
    private ISOSVirtualFile objSourceTransferFile = null;
    private SOSFileList objParent = null;
    private boolean flgFileExists = false;
    private String checksum = "N/A";
    private String checksum4check = "N/A";
    private Date dteStartTransfer = null;
    private Date dteEndTransfer = null;
    private ISOSVfsFileTransfer objVfsHandler = null;
    private boolean flgSteadyFlag = false;
    private boolean targetFileAlreadyExists = false;
    private ISOSVirtualFile checksumFile = null;
    private FTPFile objFTPFile = null;
    private String strCSVRec = new String();
    private String strRenamedSourceFileName = null;
    private SOSVfsConnectionPool objConnPoolSource = null;
    private SOSVfsConnectionPool objConnPoolTarget = null;
    private String errorMessage = null;
    private Map<String, String> jumpHistoryRecord = null;
    private Date modificationDate = null;
    public long zeroByteCount = 0;
    private IJobSchedulerEventHandler eventHandler = null;
    public static Long sendEventTimeoutInMillis = 15000L;

    public enum enuTransferStatus {
        transferUndefined, waiting4transfer, transferring, transferInProgress, transferred, transfer_skipped, transfer_has_errors, transfer_aborted, compressed, notOverwritten, deleted, renamed, ignoredDueToZerobyteConstraint, setBack, polling
    }

    public enum HistoryRecordType {
        XML, CSV
    }

    private class Buffer {

        private byte[] bytes = new byte[0];
        private int length = 0;

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
            this.length = bytes.length;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }
    }

    public SOSFileListEntry() {
        super(SOSVfsConstants.strBundleBaseName);
    }

    public SOSFileListEntry(final FTPFile pobjFTPFile) {
        this();
        lngFileModDate = pobjFTPFile.getTimestamp().getTimeInMillis();
        strSourceFileName = pobjFTPFile.getName();
        lngFileSize = pobjFTPFile.getSize();
        objFTPFile = pobjFTPFile;
    }

    public SOSFileListEntry(final String pstrLocalFileName) {
        this("", pstrLocalFileName, 0);
    }

    public SOSFileListEntry(final String pstrRemoteFileName, final String pstrLocalFileName, final long plngNoOfBytesTransferred) {
        super(SOSVfsConstants.strBundleBaseName);
        strTargetFileName = pstrRemoteFileName;
        strSourceFileName = pstrLocalFileName;
        lngNoOfBytesTransferred = plngNoOfBytesTransferred;
    }

    private String changeBackslashes(final String pstrV) {
        return pstrV.replaceAll("\\\\", "/");
    }

    public void deleteSourceFile() {
        String file = strSourceFileName;
        objDataSourceClient.getFileHandle(file).delete();
        String msg = SOSVfs_I_0113.params(file);
        LOGGER.info(msg);
        JADE_REPORT_LOGGER.info(msg);
    }

    private boolean doTransfer(final ISOSVirtualFile source, final ISOSVirtualFile target) {
        boolean closed = false;
        if (target == null) {
            raiseException(SOSVfs_E_273.params("Target"));
        }
        if (source == null) {
            raiseException(SOSVfs_E_273.params("Source"));
        }
        MessageDigest md4create = null;
        MessageDigest md4check = null;
        boolean calculateIntegrityHash4Create = true;
        if (calculateIntegrityHash4Create) {
            try {
                md4create = MessageDigest.getInstance(objOptions.integrityHashType.getValue());
            } catch (NoSuchAlgorithmException e1) {
                LOGGER.error(e1.toString());
                objOptions.createIntegrityHashFile.value(false);
                calculateIntegrityHash4Create = false;
            }
        }
        boolean calculateIntegrityHash4Check = objOptions.checkIntegrityHash.isTrue() && objOptions.compressFiles.isTrue();
        if (calculateIntegrityHash4Check) {
            try {
                md4check = MessageDigest.getInstance(objOptions.integrityHashType.getValue());
            } catch (NoSuchAlgorithmException e1) {
                LOGGER.error(e1.toString());
                objOptions.checkIntegrityHash.value(false);
                calculateIntegrityHash4Check = false;
            }
        }
        executePreCommands();
        long totalBytesTransferred = 0;
        this.setStatus(enuTransferStatus.transferring);
        // send event to inform that transfer starts?
        try {
            int cumulativeFileSeperatorLength = 0;
            byte[] buffer = new byte[objOptions.bufferSize.value()];
            int bytesTransferred;
            synchronized (this) {
                if (objOptions.cumulateFiles.isTrue() && objOptions.cumulativeFileSeparator.isNotEmpty()) {
                    String fs = objOptions.cumulativeFileSeparator.getValue();
                    fs = this.replaceVariables(fs) + System.getProperty("line.separator");
                    byte[] bytes = fs.getBytes();
                    cumulativeFileSeperatorLength = bytes.length;
                    Buffer compressedBytes = compress(bytes);
                    target.write(compressedBytes.getBytes());
                    if (calculateIntegrityHash4Check) {
                        md4check.update(bytes);
                    }
                    if (calculateIntegrityHash4Create) {
                        md4create.update(compressedBytes.getBytes());
                    }
                }
                if (source.getFileSize() <= 0) {
                    byte[] bytes = new byte[0];
                    Buffer compressedBytes = compress(bytes);
                    target.write(compressedBytes.getBytes());
                    if (calculateIntegrityHash4Check) {
                        md4check.update(bytes);
                    }
                    if (calculateIntegrityHash4Create) {
                        md4create.update(compressedBytes.getBytes());
                    }
                } else {
//                    int actualBytesTransferred = 0;
                    while ((bytesTransferred = source.read(buffer)) != -1) {
                        try {
//                            actualBytesTransferred += bytesTransferred;
                            Buffer compressedBytes = compress(buffer, bytesTransferred);
                            target.write(compressedBytes.getBytes(), 0, compressedBytes.getLength());
//                            Map<String, String> values = new HashMap<String, String>();
//                            values.put("bytesTransferred", "" + actualBytesTransferred);
//                            values.put("targetSize", "" + target.getFileSize());
//                            Map <String, Map<String, String>> eventParams = new HashMap<String, Map<String, String>>();
//                            eventParams.put("transferring:" + source.getName(), values);
//                            sendEvent(eventParams);
                            if (eventHandler != null) {
                                Map<String, String> values = new HashMap<String, String>();
                                values.put("sourcePath", this.getSourceFilename());
                                values.put("targetPath", this.getTargetFilename());
                                values.put("state", "5");
                                eventHandler.updateDb(null, "YADE_FILE", values);
                            }
                            if (calculateIntegrityHash4Check) {
                                md4check.update(buffer, 0, bytesTransferred);
                            }
                            if (calculateIntegrityHash4Create) {
                                md4create.update(compressedBytes.getBytes(), 0, compressedBytes.getLength());
                            }
                        } catch (JobSchedulerException e) {
                            if (eventHandler != null) {
                                Map<String, String> values = new HashMap<String, String>();
                                values.put("sourcePath", this.getSourceFilename());
                                values.put("state", "7");
                                values.put("errorMessage", e.getMessage());
                                updateDb(null, "YADE_FILE", values);
                            }
                            setEntryErrorMessage(e);
                            LOGGER.error(errorMessage);
                            break;
                        }
                        totalBytesTransferred += bytesTransferred;
                        setTransferProgress(totalBytesTransferred);

                        // event processing
//                        Map<String, String> values = new HashMap<String, String>();
//                        values.put("totalBytesTransferred", "" + totalBytesTransferred);
//                        values.put("targetSize", "" + target.getFileSize());
//                        Map <String, Map<String, String>> eventParams = new HashMap<String, Map<String, String>>();
//                        eventParams.put("transferred:" + source.getName(), values);
//                        sendEvent(eventParams);
                        // end of event processing
                    }
                }
            }
            // TODO: define the structure of the event answer
//            sendEvent(null);
            source.closeInput();
            target.closeOutput();
            closed = true;
            if (objDataTargetClient.isNegativeCommandCompletion()) {
                raiseException(SOSVfs_E_175.params(objTargetTransferFile.getName(), objDataTargetClient.getReplyString()));
            }
            if (calculateIntegrityHash4Create) {
                checksum = toHexString(md4create.digest());
                checksum4check = checksum;
                LOGGER.debug(SOSVfs_I_274.params(checksum, strTargetFileName, objOptions.integrityHashType.getValue()));
            }
            if (calculateIntegrityHash4Check) {
                checksum4check = toHexString(md4check.digest());
                LOGGER.debug(SOSVfs_I_274.params(checksum4check, strSourceFileName, objOptions.integrityHashType.getValue()));
            }
            this.setNoOfBytesTransferred(totalBytesTransferred);
            totalBytesTransferred += cumulativeFileSeperatorLength;
            checkChecksumFile(source, target);
            executeTFNPostCommnands();
            return true;
        } catch (JobSchedulerException e) {
            setEntryErrorMessage(e);
            throw e;
        } catch (Exception e) {
            errorMessage = e.toString();
            throw new JobSchedulerException(e);
        } finally {
            if (!closed) {
                source.closeInput();
                target.closeOutput();
                closed = true;
            }
        }
    }

//    private void sendEvent(Map<String, Map<String, String>> eventParams) {
//        if (eventHandler != null && eventParams != null) {
//            eventHandler.sendEvent(eventParams);
//        }
//    }
    
    private void updateDb(Long id, String type, Map<String, String> values) {
        if (eventHandler != null) {
            // id of the DBItem
            // type of the Item, here always YADE_FILE
            // map of values of the Item, with key = propertyName and value = propertyValue
            eventHandler.updateDb(id, type, values);
        }
    }

    public void createChecksumFile() {
        createChecksumFile(makeFullPathName(objOptions.targetDir.getValue(), strTargetFileName));
    }

    public void createChecksumFile(String targetFileName) {
        targetFileName = resolveDotsInPath(targetFileName);
        if (objOptions.createIntegrityHashFile.isTrue() && isTransferred()) {
            ISOSVirtualFile checksumFile = null;
            try {
                checksumFile = objDataTargetClient.getFileHandle(targetFileName + "." + objOptions.integrityHashType.getValue());
                checksumFile.write(checksum.getBytes());
                LOGGER.info(SOSVfs_I_285.params(checksumFile.getName()));
                setChecksumFile(checksumFile);
            } catch (JobSchedulerException e) {
                throw e;
            } finally {
                if (checksumFile != null) {
                    checksumFile.closeOutput();
                }
            }
        }
    }

    private void checkChecksumFile(ISOSVirtualFile source, ISOSVirtualFile target) {
        if (objOptions.checkIntegrityHash.isTrue()) {
            ISOSVirtualFile sourceChecksumFile = null;
            String sourceChecksumFileName = source.getName() + "." + objOptions.securityHashType.getValue();
            try {
                sourceChecksumFile = getDataSourceClient().getFileHandle(sourceChecksumFileName);
                if (sourceChecksumFile.fileExists()) {
                    String origChecksum = sourceChecksumFile.file2String().trim();
                    if (!origChecksum.equals(checksum4check)) {
                        try {
                            if (target.fileExists()) {
                                target.delete();
                            }
                        } catch (Exception ex) {
                            LOGGER.debug(ex.toString(), ex);
                        }
                        String strT = String.format("Integrity Hash violation. File %1$s, checksum read: '%2$s', checksum calculated: '%3$s'",
                                sourceChecksumFileName, origChecksum, checksum4check);
                        setStatus(enuTransferStatus.transfer_aborted);
                        throw new JobSchedulerException(strT);
                    } else {
                        LOGGER.info(String.format("Integrity Hash is OK: File %1$s, checksum read '%2$s', checksum calculated '%3$s'",
                                sourceChecksumFileName, origChecksum, checksum4check));
                    }
                } else {
                    LOGGER.info(String.format("Checksum file '%1$s' not found", sourceChecksumFileName));
                }
            } catch (JobSchedulerException e) {
                throw e;
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }
    }

    private void setEntryErrorMessage(JobSchedulerException ex) {
        errorMessage = JobSchedulerException.LastErrorMessage;
        if (isEmpty(errorMessage)) {
            errorMessage = ex.toString();
            if (ex.getNestedException() != null) {
                errorMessage += " " + ex.getNestedException().toString();
            }
        }
    }

    private void executeCommands(final String commandOptionName, final ISOSVfsFileTransfer fileTransfer, final SOSOptionString optionCommands) {
        executeCommands(commandOptionName, fileTransfer, optionCommands, null);
    }

    private void executeCommands(final String commandOptionName, final ISOSVfsFileTransfer fileTransfer, final SOSOptionString optionCommands,
            final SOSOptionString optionCommandDelimiter) {
        final String methodName = "SOSFileListEntry::executeCommands";
        String commands = optionCommands.getValue().trim();
        if (commands.length() > 0) {
            commands = replaceVariables(commands);
            LOGGER.debug(String.format("[%s] %s", commandOptionName, SOSVfs_D_0151.params(commands)));
            String delimiter = null;
            if (optionCommandDelimiter != null) {
                delimiter = optionCommandDelimiter.getValue();
            }
            if (delimiter == null) {
                delimiter = ";";
            }
            if (delimiter.isEmpty()) {
                try {
                    fileTransfer.getHandler().executeCommand(commands);
                } catch (JobSchedulerException e) {
                    LOGGER.error(e.toString());
                    throw e;
                } catch (Exception e) {
                    LOGGER.error(e.toString());
                    throw new JobSchedulerException(methodName, e);
                }
            } else {
                String[] values = commands.split(delimiter);
                for (String command : values) {
                    if (command.trim().length() > 0) {
                        try {
                            fileTransfer.getHandler().executeCommand(command);
                        } catch (JobSchedulerException e) {
                            LOGGER.error(e.toString());
                            throw e;
                        } catch (Exception e) {
                            LOGGER.error(e.toString());
                            throw new JobSchedulerException(methodName, e);
                        }
                    }
                }
            }
        }
    }

    public void executeTFNPostCommnands() {
        SOSConnection2OptionsAlternate target = objOptions.getConnectionOptions().getTarget();
        if (target.alternateOptionsUsed.isTrue()) {
            executeCommands("alternative_target_tfn_post_command", objDataTargetClient, target.getAlternatives().tfnPostCommand, target
                    .getAlternatives().commandDelimiter);
        } else {
            executeCommands("tfn_post_command", objDataTargetClient, objOptions.tfnPostCommand);
            executeCommands("target_tfn_post_command", objDataTargetClient, target.tfnPostCommand, target.commandDelimiter);
        }
        SOSConnection2OptionsAlternate source = objOptions.getConnectionOptions().getSource();
        if (source.alternateOptionsUsed.isTrue()) {
            executeCommands("alternative_source_tfn_post_command", objDataSourceClient, source.getAlternatives().tfnPostCommand, source
                    .getAlternatives().commandDelimiter);
        } else {
            executeCommands("source_tfn_post_command", objDataSourceClient, source.tfnPostCommand, source.commandDelimiter);
        }
    }

    public void executePostCommands() {
        SOSConnection2OptionsAlternate target = objOptions.getConnectionOptions().getTarget();
        if (target.alternateOptionsUsed.isTrue()) {
            executeCommands("alternative_target_post_command", objDataTargetClient, target.getAlternatives().postCommand, target
                    .getAlternatives().commandDelimiter);
        } else {
            executeCommands("post_command", objDataTargetClient, objOptions.postCommand);
            executeCommands("target_post_command", objDataTargetClient, target.postCommand, target.commandDelimiter);
        }
        SOSConnection2OptionsAlternate source = objOptions.getConnectionOptions().getSource();
        if (source.alternateOptionsUsed.isTrue()) {
            executeCommands("alternative_source_post_command", objDataSourceClient, source.getAlternatives().postCommand, source
                    .getAlternatives().commandDelimiter);
        } else {
            executeCommands("source_post_command", objDataSourceClient, source.postCommand, source.commandDelimiter);
        }
    }

    private void executePreCommands() {
        SOSConnection2OptionsAlternate target = objOptions.getConnectionOptions().getTarget();
        if (target.alternateOptionsUsed.isTrue()) {
            executeCommands("alternative_target_pre_command", objDataTargetClient, target.getAlternatives().preCommand, target.commandDelimiter);
        } else {
            executeCommands("pre_command", objDataTargetClient, objOptions.preCommand);
            executeCommands("target_pre_command", objDataTargetClient, target.preCommand, target.commandDelimiter);
        }
        SOSConnection2OptionsAlternate source = objOptions.getConnectionOptions().getSource();
        if (source.alternateOptionsUsed.isTrue()) {
            executeCommands("alternative_source_pre_command", objDataSourceClient, source.getAlternatives().preCommand, source
                    .getAlternatives().commandDelimiter);
        } else {
            executeCommands("source_pre_command", objDataSourceClient, source.preCommand, source.commandDelimiter);
        }
    }

    public boolean isFileExists() {
        ISOSVirtualFile objTargetFile = objDataTargetClient.getFileHandle(makeFullPathName(objOptions.targetDir.getValue(), strTargetFileName));
        try {
            flgFileExists = objTargetFile.fileExists();
        } catch (Exception e) {
            flgFileExists = false;
        }
        return flgFileExists;
    }

    public String getAtomicFileName() {
        return strAtomicFileName;
    }

    @Override
    public String getCommand() {
        return EMPTY_STRING;
    }

    @Override
    public Integer getCommandType() {
        return 0;
    }

    @Override
    public Date getCreated() {
        return null;
    }

    @Override
    public String getCreatedBy() {
        return EMPTY_STRING;
    }

    public ISOSVfsFileTransfer getDataSourceClient() {
        return objDataSourceClient;
    }

    public ISOSVfsFileTransfer getDataTargetClient() {
        return objDataTargetClient;
    }

    @Override
    public Date getEndTime() {
        return dteEndTransfer;
    }

    public String getFileName4ResultList() {
        String strT = strTargetFileName;
        if (isEmpty(strT)) {
            strT = strSourceFileName;
        }
        if (objOptions.resultSetFileName.getValue().endsWith(".source.tmp")) {
            strT = strSourceFileName;
        }
        return strT;
    }

    private String getFileNameWithoutPath(final String pstrTargetFileName) {
        String strT = adjustFileSeparator(pstrTargetFileName);
        File fleT = new File(strT);
        strT = fleT.getName();
        return strT;
    }

    @Override
    public Long getFileSize() {
        return lngFileSize;
    }

    public Long getLastCheckedFileSize() {
        return lastCheckedFileSize;
    }

    public void setLastCheckedFileSize(Long fileSize) {
        lastCheckedFileSize = fileSize;
    }

    @Override
    public String getLastErrorMessage() {
        return EMPTY_STRING;
    }

    @Override
    public String getMd5() {
        return checksum;
    }

    @Override
    public Date getModified() {
        return null;
    }

    @Override
    public String getModifiedBy() {
        return EMPTY_STRING;
    }

    private String getPathWithoutFileName(final String pstrTargetFileName) {
        String strT = adjustFileSeparator(pstrTargetFileName);
        File fleT = new File(strT);
        strT = fleT.getParent();
        if (strT == null) {
            strT = "./";
        }
        return adjustFileSeparator(strT);
    }

    @Override
    public String getPid() {
        return objOptions.getPid();
    }

    @Override
    public String getSizeValue() {
        return "";
    }

    @Override
    public String getSourceFilename() {
        return strSourceFileName;
    }

    @Override
    public Date getStartTime() {
        return dteStartTransfer;
    }

    @Override
    public Integer getStatus() {
        return new Integer(eTransferStatus.ordinal());
    }

    @Override
    public String getStatusText() {
        return eTransferStatus.name();
    }

    public ISOSVirtualFile getTargetFile() {
        fleSourceFile = objDataSourceClient.getFileHandle(strSourceFileName);
        strSourceTransferName = fleSourceFile.getName();
        strTargetFileName = fleSourceFile.getName();
        boolean flgIncludeSubdirectories = objOptions.recursive.value();
        if (objOptions.compressFiles.isTrue()) {
            strTargetFileName = strTargetFileName + objOptions.compressedFileExtension.getValue();
        }
        if (objOptions.cumulateFiles.isTrue()) {
            strTargetFileName = objOptions.cumulativeFileName.getValue();
            strTargetTransferName = strTargetFileName;
            objOptions.appendFiles.value(true);
        } else {
            strTargetFileName = getFileNameWithoutPath(strTargetFileName);
            strTargetTransferName = strTargetFileName;
            if (objOptions.getReplacing().isNotEmpty()) {
                try {
                    strTargetFileName = objOptions.getReplacing().doReplace(strTargetFileName, objOptions.getReplacement().getValue());
                } catch (Exception e) {
                    throw new JobSchedulerException(SOSVfs_E_0150.get() + " " + e.toString(), e);
                }
            }
        }
        if (objOptions.isAtomicTransfer() || objOptions.transactionMode.isTrue()) {
            strTargetTransferName = makeAtomicFileName(objOptions);
        }
        if (flgIncludeSubdirectories) {
            String strSourceDir = getPathWithoutFileName(fleSourceFile.getName());
            String strOrigSourceDir = objOptions.sourceDir().getValue();
            if (!fileNamesAreEqual(strSourceDir, strOrigSourceDir, true) && strSourceDir.length() > strOrigSourceDir.length()) {
                String strSubFolder = strSourceDir.substring(strOrigSourceDir.length());
                strSubFolder = adjustFileSeparator(addFileSeparator(strSubFolder));
                strTargetFileName = strTargetFileName.replaceFirst("([^/]*)$", strSubFolder + "$1");
                strTargetTransferName = strSubFolder + strTargetTransferName;
                if (isNotEmpty(this.getAtomicFileName())) {
                    this.setAtomicFileName(strTargetTransferName);
                }
                try {
                    if (objParent.add2SubFolders(strSubFolder)) {
                        objDataTargetClient.mkdir(addFileSeparator(objOptions.targetDir().getValue()) + strSubFolder);
                    }
                } catch (IOException e) {
                    throw new JobSchedulerException(e);
                }
            }
        }
        return null;
    }

    public void setRenamedSourceFilename() {
        SOSConnection2OptionsAlternate sourceOptions = objOptions.getSource();
        if (sourceOptions.replacing.isDirty() && !objOptions.removeFiles.value()) {
            String replaceWith = sourceOptions.replacement.getValue();
            try {
                String sourceFileName = new File(strSourceFileName).getName();
                String sourceParent = strSourceFileName.substring(0, strSourceFileName.length() - sourceFileName.length());
                String newSourceFileName = sourceOptions.replacing.doReplace(sourceFileName, replaceWith).replace('\\', '/');
                if (!newSourceFileName.equals(sourceFileName)) {
                    String sourceDir = addFileSeparator(sourceOptions.directory.getValue()).replace('\\', '/');
                    if (objOptions.recursive.value()) {
                        String subDirs = sourceParent.substring(sourceDir.length());
                        newSourceFileName = subDirs + newSourceFileName;
                    }
                    if (!newSourceFileName.startsWith("/") && !newSourceFileName.matches("^[a-zA-Z]:[\\\\/].*$")) {
                        newSourceFileName = sourceDir + newSourceFileName;
                    }
                    newSourceFileName = resolveDotsInPath(newSourceFileName);
                    if (!strSourceFileName.equals(newSourceFileName)) {
                        strRenamedSourceFileName = newSourceFileName;
                    }
                }
            } catch (JobSchedulerException e) {
                throw e;
            } catch (Exception e) {
                throw new JobSchedulerException(SOSVfs_E_0150.get(), e);
            }
        }
    }

    @Override
    public String getTargetFilename() {
        return strTargetFileName;
    }

    public String getTargetFileNameAndPath() {
        return objOptions.targetDir.getValue() + strTargetFileName;
    }

    public boolean getTransactionalLocalFile() {
        return flgTransactionalLocalFile;
    }

    @Override
    public Integer getTransferDetailsId() {
        return null;
    }

    @Override
    public Integer getTransferId() {
        return null;
    }

    public long getTransferProgress() {
        return lngTransferProgress;
    }

    public enuTransferStatus getTransferStatus() {
        return eTransferStatus;
    }

    public boolean isTransferred() {
        return eTransferStatus == enuTransferStatus.transferred;
    }

    public boolean isSteady() {
        return flgSteadyFlag;
    }

    public void log4Debug() {
        LOGGER.debug(SOSVfs_D_218.params(this.getSourceFileName()));
        LOGGER.debug(SOSVfs_D_219.params(this.getSourceTransferName()));
        LOGGER.debug(SOSVfs_D_220.params(this.getTargetTransferName()));
        LOGGER.debug(SOSVfs_D_221.params(this.getTargetFileName()));
    }

    public String makeAtomicFileName(final ISOSFtpOptions objO) {
        String strAtomicSuffix = objO.getAtomicSuffix().getValue();
        String strAtomicPrefix = objO.getAtomicPrefix().getValue();
        strTargetTransferName = strTargetTransferName + strAtomicSuffix.trim();
        strTargetTransferName = strAtomicPrefix + strTargetTransferName;
        strAtomicFileName = strTargetTransferName.trim();
        return strAtomicFileName;
    }

    private String makeFileNameReplacing(final String pstrFileName) {
        String strR = adjustFileSeparator(pstrFileName);
        String strReplaceWith = objOptions.getReplacement().getValue();
        try {
            strR = objOptions.getReplacing().doReplace(strR, strReplaceWith);
        } catch (Exception e) {
            LOGGER.error(e.toString(), new JobSchedulerException(SOSVfs_E_0150.get(), e));
            throw new JobSchedulerException(SOSVfs_E_0150.get(), e);
        }
        return strR;
    }

    public long getNoOfBytesTransferred() {
        return lngNoOfBytesTransferred;
    }

    public void noOfBytesTransferred(final long plngNoOfBytesTransferred) {
        lngNoOfBytesTransferred = plngNoOfBytesTransferred;
        LOGGER.info(SOSVfs_D_0112.params(plngNoOfBytesTransferred));
    }

    protected String normalized(String str) {
        str = adjustFileSeparator(str);
        str = addFileSeparator(str);
        return str;
    }

    protected String resolveDotsInPath(String path) {
        try {
            return Paths.get(path).normalize().toString().replace('\\', '/');
        } catch (Exception e) {
            return path.replace('\\', '/');
        }
    }

    public void setOptions(final ISOSFtpOptions objOptions2) {
        objOptions = (SOSFTPOptions) objOptions2;
    }

    private void raiseException(final String pstrM) {
        this.setTransferStatus(enuTransferStatus.transfer_aborted);
        LOGGER.error(pstrM);
        throw new JobSchedulerException(pstrM);
    }

    @Deprecated
    public String getRemoteFileName() {
        return strTargetFileName;
    }

    public void renameSourceFile() {
        renameSourceFile(objDataSourceClient.getFileHandle(strSourceFileName));
    }

    private void renameSourceFile(final ISOSVirtualFile sourceFile) {
        if (strRenamedSourceFileName != null) {
            try {
                ISOSVirtualFile renamedSourceFile = objDataSourceClient.getFileHandle(strRenamedSourceFileName);
                if (renamedSourceFile.fileExists()) {
                    renamedSourceFile.delete();
                }
                if (strRenamedSourceFileName.contains("/") && objOptions.makeDirs.isTrue()) {
                    String parent = strRenamedSourceFileName.replaceFirst("[^/]*$", "");
                    objDataSourceClient.mkdir(parent);
                }
                sourceFile.rename(strRenamedSourceFileName);
            } catch (Exception e) {
                throw new JobSchedulerException(SOSVfs_E_0150.get(), e);
            }
        }
    }

    public void rollbackRenameSourceFile() {
        if (strRenamedSourceFileName != null) {
            try {
                ISOSVirtualFile sourceFile = objDataSourceClient.getFileHandle(strSourceFileName);
                ISOSVirtualFile renamedSourceFile = objDataSourceClient.getFileHandle(strRenamedSourceFileName);
                if (!sourceFile.fileExists() && renamedSourceFile.fileExists()) {
                    renamedSourceFile.rename(strSourceFileName);
                }
            } catch (Exception e) {
                throw new JobSchedulerException(SOSVfs_E_0150.get(), e);
            }
        }
    }

    public void renameTargetFile() {
        if (!skipTransfer()) {
            renameTargetFile(objDataTargetClient.getFileHandle(makeFullPathName(objOptions.targetDir.getValue(), strTargetFileName)));
        }
    }

    private void renameTargetFile(ISOSVirtualFile targetFile) {
        String newFileName = targetFile.getName();
        newFileName = resolveDotsInPath(newFileName);
        if (!fileNamesAreEqual(objTargetTransferFile.getName(), newFileName, false)) {
            try {
                if (objOptions.overwriteFiles.isTrue() && targetFile.fileExists()) {
                    setTargetFileAlreadyExists(true);
                    targetFile.delete();
                }
                if (newFileName.contains("/") && objOptions.makeDirs.isTrue()) { // sosftp-158
                    String parent = newFileName.replaceFirst("[^/]*$", "");
                    objDataTargetClient.mkdir(parent);
                }
                objTargetTransferFile.rename(newFileName);
            } catch (Exception e) {
                throw new JobSchedulerException(SOSVfs_E_0150.get(), e);
            }
        }
    }

    private String replaceVariables(final String value) {
        EntryPaths targetFile = new EntryPaths(objOptions.targetDir.getValue(), resolveDotsInPath(makeFullPathName(objOptions.targetDir.getValue(),
                strTargetFileName)));
        EntryPaths targetTransferFile = new EntryPaths(objOptions.targetDir.getValue(), resolveDotsInPath(makeFullPathName(objOptions.targetDir
                .getValue(), strTargetTransferName)));

        EntryPaths sourceFile = new EntryPaths(objOptions.sourceDir.getValue(), resolveDotsInPath(strSourceFileName));
        EntryPaths sourceFileRenamed = new EntryPaths(objOptions.sourceDir.getValue(), strRenamedSourceFileName);
        
        Properties vars = objOptions.getTextProperties();
        vars.put("TargetDirFullName", targetFile.getBaseDirFullName());
        vars.put("SourceDirFullName", sourceFile.getBaseDirFullName());

        vars.put("TargetFileFullName", targetFile.getFullName());
        vars.put("TargetFileRelativeName", targetFile.getRelativeName());
        vars.put("TargetFileBaseName", targetFile.getBaseName());
        vars.put("TargetFileParentFullName", targetFile.getParentDirFullName());
        vars.put("TargetFileParentBaseName", targetFile.getParentDirBaseName());

        vars.put("TargetTransferFileFullName", targetTransferFile.getFullName());
        vars.put("TargetTransferFileRelativeName", targetTransferFile.getRelativeName());
        vars.put("TargetTransferFileBaseName", targetTransferFile.getBaseName());
        vars.put("TargetTransferFileParentFullName", targetTransferFile.getParentDirFullName());
        vars.put("TargetTransferFileParentBaseName", targetTransferFile.getParentDirBaseName());

        vars.put("SourceFileFullName", sourceFile.getFullName());
        vars.put("SourceFileRelativeName", sourceFile.getRelativeName());
        vars.put("SourceFileBaseName", sourceFile.getBaseName());
        vars.put("SourceFileParentFullName", sourceFile.getParentDirFullName());
        vars.put("SourceFileParentBaseName", sourceFile.getParentDirBaseName());

        vars.put("SourceFileRenamedFullName", sourceFileRenamed.getFullName());
        vars.put("SourceFileRenamedRelativeName", sourceFileRenamed.getRelativeName());
        vars.put("SourceFileRenamedBaseName", sourceFileRenamed.getBaseName());
        vars.put("SourceFileRenamedParentFullName", sourceFileRenamed.getParentDirFullName());
        vars.put("SourceFileRenamedParentBaseName", sourceFileRenamed.getParentDirBaseName());

        return objOptions.replaceVars(value);
    }

    @Override
    public void run() {
        boolean flgNewConnectionUsed = false;
        try {
            flgNewConnectionUsed = false;
            if (objDataSourceClient == null) {
                setDataSourceClient((ISOSVfsFileTransfer) objConnPoolSource.getUnused());
            }
            ISOSVirtualFile objSourceFile = objDataSourceClient.getFileHandle(strSourceFileName);
            if (objSourceFile.notExists()) {
                throw new JobSchedulerException(SOSVfs_E_226.params(strSourceFileName));
            }
            if (objDataTargetClient == null && objOptions.isNeedTargetClient()) {
                setDataTargetClient((ISOSVfsFileTransfer) objConnPoolTarget.getUnused());
            }
            if (objDataTargetClient != null && objOptions.isNeedTargetClient()) {
                File subParent = null;
                String subPath = "";
                String strTargetFolderName = objOptions.targetDir.getValue();
                String localDir = objOptions.sourceDir.getValue();
                boolean flgIncludeSubdirectories = objOptions.recursive.value();
                if (flgIncludeSubdirectories) {
                    if (objSourceFile.getParentVfs() != null && objSourceFile.getParentVfsFile().isDirectory()) {
                        subPath = strSourceFileName.substring(localDir.length());
                        subParent = new File(subPath).getParentFile();
                        if (subParent != null) {
                            subPath = adjustFileSeparator(subPath);
                            subPath = subPath.substring(0, subPath.length() - new File(strSourceFileName.toString()).getName().length() - 1);
                            objDataTargetClient.mkdir(strTargetFolderName + "/" + subPath);
                        } else {
                            subPath = "";
                        }
                    }
                }
                this.getTargetFile();
            }
            this.setRenamedSourceFilename();
            if (objOptions.transactional.value()) {
                this.setTransactionalLocalFile();
            }
            switch (objOptions.operation.value()) {
            case getlist:
                return;
            case delete:
                executePreCommands();
                objSourceFile.delete();
                LOGGER.debug(SOSVfs_I_0113.params(strSourceFileName));
                this.setStatus(enuTransferStatus.deleted);
                return;
            case rename:
                File fleT = new File(strSourceFileName);
                String strParent = changeBackslashes(normalized(fleT.getParent()));
                String strNewFileName = makeFileNameReplacing(fleT.getName());
                if (strNewFileName.contains("/") && objOptions.makeDirs.isTrue()) {
                    String strP = normalized(new File(strNewFileName).getParent());
                    objDataSourceClient.mkdir(strP);
                }
                strNewFileName = changeBackslashes(addFileSeparator(strParent) + strNewFileName);
                LOGGER.debug(SOSVfs_I_150.params(strSourceFileName, strNewFileName));
                strTargetFileName = strNewFileName;
                objSourceFile.rename(strNewFileName);
                this.setStatus(enuTransferStatus.renamed);
                return;
            default:
                break;
            }
            ISOSVirtualFile objTargetFile = objDataTargetClient.getFileHandle(makeFullPathName(objOptions.targetDir.getValue(), strTargetFileName));
            if (objOptions.cumulateFiles.isTrue()) {
                if (objOptions.cumulativeFileDelete.isTrue() && !objOptions.flgCumulativeTargetDeleted) {
                    objTargetFile.delete();
                    objOptions.flgCumulativeTargetDeleted = true;
                    LOGGER.debug(String.format("cumulative file '%1$s' deleted.", strTargetFileName));
                }
            }
            objTargetFile.setModeAppend(objOptions.appendFiles.value());
            objTargetFile.setModeRestart(objOptions.resumeTransfer.value());
            if (!fileNamesAreEqual(strTargetFileName, strTargetTransferName, false)) {
                objTargetTransferFile = objDataTargetClient.getFileHandle(makeFullPathName(objOptions.targetDir.getValue(), strTargetTransferName));
            } else {
                objTargetTransferFile = objTargetFile;
            }
            if (objOptions.cumulateFiles.isTrue()) {
                objTargetTransferFile.setModeAppend(objOptions.appendFiles.value());
                objTargetTransferFile.setModeRestart(objOptions.resumeTransfer.value());
            }
            strSourceTransferName = getFileNameWithoutPath(strSourceTransferName);
            objSourceTransferFile = objDataSourceClient.getFileHandle(makeFullPathName(getPathWithoutFileName(strSourceFileName),
                    strSourceTransferName));
            if (eTransferStatus == enuTransferStatus.ignoredDueToZerobyteConstraint) {
                String strM = SOSVfs_D_0110.params(strSourceFileName);
                LOGGER.debug(strM);
                JADE_REPORT_LOGGER.info(strM);
            }

            flgFileExists = objTargetFile.fileExists();
            if (flgFileExists) {
                this.setTargetFileAlreadyExists(true);
                if (objOptions.isDoNotOverwrite()) {
                    LOGGER.debug(SOSVfs_E_228.params(strTargetFileName));
                    this.setNotOverwritten();
                }
            }

            if (!skipTransfer()) {
                this.doTransfer(objSourceTransferFile, objTargetTransferFile);
                long pdteDateTime = objSourceFile.getModificationDateTime();
                LOGGER.debug("sourceFile.getModificationDateTime() = " + pdteDateTime);
                if (pdteDateTime != -1) {
                    modificationDate = new Date(pdteDateTime);
                }
                if (objOptions.keepModificationDate.isTrue()) {
                    if (pdteDateTime != -1) {
                        objTargetFile.setModificationDateTime(pdteDateTime);
                    }
                }
                if ((objOptions.isAtomicTransfer() || objOptions.isReplaceReplacingInEffect()) && objOptions.transactional.isFalse()) {
                    renameTargetFile(objTargetFile);
                }
            }
            if (objOptions.transactional.isFalse()) {
                createChecksumFile(objTargetFile.getName());
                if (strRenamedSourceFileName != null) {
                    renameSourceFile(objSourceFile);
                }
            }
        } catch (JobSchedulerException e) {
            LOGGER.error(SOSVfs_E_229.params(e));
            throw e;
        } catch (Exception e) {
            String strT = SOSVfs_E_229.params(e);
            LOGGER.error(strT);
            throw new JobSchedulerException(strT, e);
        } finally {
            try {
                if (objDataSourceClient != null) {
                    objDataSourceClient.getHandler().release();
                }
                if (objDataTargetClient != null) {
                    objDataTargetClient.getHandler().release();
                }
                if (flgNewConnectionUsed) {
                    if (objDataSourceClient != null) {
                        objDataSourceClient.logout();
                        objDataSourceClient.disconnect();
                    }
                    if (objDataTargetClient != null) {
                        objDataTargetClient.logout();
                        objDataTargetClient.disconnect();
                    }
                }
            } catch (IOException e) {
                //
            }
        }
    }

    public String getChecksum() {
        return checksum;
    }

    public void setAtomicFileName(final String pstrValue) {
        strAtomicFileName = pstrValue;
    }

    public void setConnectionPool4Source(final SOSVfsConnectionPool pobjConnP) {
        objConnPoolSource = pobjConnP;
    }

    public void setConnectionPool4Target(final SOSVfsConnectionPool pobjConnP) {
        objConnPoolTarget = pobjConnP;
    }

    public void setDataSourceClient(final ISOSVfsFileTransfer pobjDataSourceClient) {
        objDataSourceClient = pobjDataSourceClient;
        lngFileModDate = -1;
        if (objDataSourceClient != null) {
            ISOSVirtualFile objFileHandle = objDataSourceClient.getFileHandle(strSourceFileName);
            if (objFileHandle != null) {
                lngFileModDate = objFileHandle.getModificationDateTime();
            }
        }
    }

    public void setDataTargetClient(final ISOSVfsFileTransfer pobjDataTargetClient1) {
        objDataTargetClient = pobjDataTargetClient1;
    }

    public void setNoOfBytesTransferred(final long plngNoOfBytesTransferred) {
        lngNoOfBytesTransferred = plngNoOfBytesTransferred;
        SOSConnection2Options objConnectOptions = objOptions.getConnectionOptions();
        if (!(objOptions.checkSize.isFalse() || objOptions.compressFiles.isTrue() || objOptions.transferMode.isAscii() || objConnectOptions
                .getSource().transferMode.isAscii() || objConnectOptions.getTarget().transferMode.isAscii())) {
            if (lngFileSize <= 0) {
                lngFileSize = objDataSourceClient.getFileHandle(strSourceFileName).getFileSize();
            }
            if (lngFileSize != plngNoOfBytesTransferred) {
                LOGGER.error(SOSVfs_E_216.params(plngNoOfBytesTransferred, lngFileSize, strSourceFileName));
                this.setStatus(enuTransferStatus.transfer_aborted);
                throw new JobSchedulerException(SOSVfs_E_271.get());
            }
        }
        this.setStatus(enuTransferStatus.transferred);
    }

    private boolean skipTransfer() {
        return eTransferStatus == enuTransferStatus.notOverwritten || eTransferStatus == enuTransferStatus.transfer_skipped
                || eTransferStatus == enuTransferStatus.ignoredDueToZerobyteConstraint;
    }

    public void setNotOverwritten() {
        eTransferStatus = enuTransferStatus.notOverwritten;
        dteEndTransfer = now();
        LOGGER.warn(SOSVfs_D_0111.params(strSourceFileName));
    }

    public boolean isNotOverwritten() {
        return eTransferStatus == enuTransferStatus.notOverwritten;
    }

    public void setParent(final SOSFileList objFileList) {
        objParent = objFileList;
        objDataSourceClient = objParent.objDataSourceClient;
        objDataTargetClient = objParent.objDataTargetClient;
        if (objDataSourceClient != null) {
            ISOSVirtualFile sourceFile = objDataSourceClient.getFileHandle(strSourceFileName);
            setSourceFileProperties(sourceFile);
        }
    }

    public void setSourceFileProperties(ISOSVirtualFile file) {
        lngFileSize = file.getFileSize();
        lngFileModDate = file.getModificationDateTime();
    }

    public void setRemoteFileName(final String pstrRemoteFileName) {
        strTargetFileName = pstrRemoteFileName;
    }

    public void setSourceFileName(final String pstrLocalFileName) {
        strSourceFileName = pstrLocalFileName;
    }

    public void setStatus(final enuTransferStatus peTransferStatus) {
        String strM = "";
        eTransferStatus = peTransferStatus;
        switch (peTransferStatus) {
        case transferInProgress:
            strM = SOSVfs_I_0114.get();
            break;
        case waiting4transfer:
            strM = SOSVfs_I_0115.get();
            break;
        case transfer_skipped:
            dteEndTransfer = now();
            strM = SOSVfs_I_0116.get();
            break;
        case transferring:
            strM = SOSVfs_I_0117.get();
            dteStartTransfer = now();
            break;
        case transferred:
            strM = SOSVfs_I_0118.get();
            dteEndTransfer = now();
            break;
        case transfer_aborted:
            strM = SOSVfs_I_0119.get();
            dteEndTransfer = now();
            break;
        default:
            break;
        }
        if (isNotEmpty(strM)) {
            switch (peTransferStatus) {
            case transferInProgress:
                break;
            default:
                strM = SOSVfs_I_0120.params(strM, strSourceFileName);
                break;
            }
        }
    }

    public void setStatusEndTransfer() {
        this.setStatus(enuTransferStatus.transferred);
    }

    public void setStatusEndTransfer(final long plngNoOfBytesTransferred) {
        this.setNoOfBytesTransferred(plngNoOfBytesTransferred);
        this.setStatusEndTransfer();
    }

    public void setStatusStartTransfer() {
        this.setStatus(enuTransferStatus.transferring);
    }

    public void setSteady(final boolean pflgSteadyFlag) {
        flgSteadyFlag = pflgSteadyFlag;
    }

    public void setTransactionalLocalFile() {
        flgTransactionalLocalFile = true;
    }

    public void setTransferProgress(final long plngTransferProgress) {
        this.setStatus(enuTransferStatus.transferInProgress);
    }

    public void setTransferSkipped() {
        eTransferStatus = enuTransferStatus.transfer_skipped;
        dteEndTransfer = now();
        String strM = SOSVfs_D_0110.params(strSourceFileName);
        LOGGER.debug(strM);
        JADE_REPORT_LOGGER.info(strM);
    }

    public void setIgnoredDueToZerobyteConstraint() {
        eTransferStatus = enuTransferStatus.ignoredDueToZerobyteConstraint;
        dteEndTransfer = now();
    }

    public boolean isSourceFileExists() {
        boolean flgT = false;
        try {
            flgT = objDataSourceClient.getFileHandle(strSourceFileName).fileExists();
        } catch (Exception e) {
            //
        }
        return flgT;
    }

    public String getSourceFileName() {
        return strSourceFileName;
    }

    public String getSourceTransferName() {
        return strSourceTransferName;
    }

    public String getTargetFileName() {
        return strTargetFileName;
    }

    public String getTargetTransferName() {
        return strTargetTransferName;
    }

    public String renamedSourceFileName() {
        return strRenamedSourceFileName;
    }

    private String normalizeErrorMessageForXml(String msg) {
        msg = msg.replaceAll("\r?\n", " ");
        msg = StringEscapeUtils.escapeXml(msg);
        int msgLength = msg.length();
        if (msgLength > 255) {
            msg = msg.substring(msgLength - 255, msgLength);
        }
        return msg;
    }

    private String normalizeErrorMessageForCSV(String msg) {
        msg = msg.replaceAll("\r?\n", " ");
        msg = StringEscapeUtils.escapeCsv(msg);
        int msgLength = msg.length();
        if (msgLength > 255) {
            msg = msg.substring(msgLength - 255, msgLength);
        }
        return msg;
    }

    public Map<String, String> getFileAttributes(HistoryRecordType recordType) {
        Map<String, String> fileAttributes = new HashMap<String, String>();
        String mandator = objOptions.mandator.getValue(); // 0-
        String pid = "0";
        try {
            pid = ManagementFactory.getRuntimeMXBean().getName();
            String[] arr = pid.split("@");
            pid = arr[0];
        } catch (Exception e) {
            pid = "0";
        }
        String ppid = System.getProperty(FIELD_PPID, "0");
        String operation = objOptions.operation.getValue();
        SOSConnection2OptionsAlternate source = objOptions.getConnectionOptions().getSource();
        if (source.alternateOptionsUsed.isTrue()) {
            source = source.getAlternatives();
        }
        SOSConnection2OptionsAlternate target = objOptions.getConnectionOptions().getTarget();
        if (target.alternateOptionsUsed.isTrue()) {
            target = target.getAlternatives();
        }
        String localhost = source.host.getLocalHostIfHostIsEmpty();
        String localhost_ip = source.host.getLocalHostAdressIfHostIsEmpty();
        String local_user = source.user.getSystemUserIfUserIsEmpty();
        String remote_host = target.host.getLocalHostIfHostIsEmpty();
        String remote_host_ip = target.host.getLocalHostAdressIfHostIsEmpty();
        String remote_user = target.user.getSystemUserIfUserIsEmpty();
        String protocol = target.protocol.getValue();
        String port = target.port.getValue();
        String local_dir = source.directory.getValue();
        if (isEmpty(local_dir)) {
            local_dir = "";
        } else {
            local_dir = normalized(local_dir);
        }
        String remote_dir = target.directory.getValue();
        if (isEmpty(remote_dir)) {
            remote_dir = "";
        } else {
            remote_dir = normalized(remote_dir);
        }
        String local_filename = this.getSourceFilename();
        local_filename = adjustFileSeparator(local_filename);
        String remote_filename = this.getTargetFilename();
        if (isEmpty(remote_filename)) {
            remote_filename = "";
        } else {
            remote_filename = adjustFileSeparator(remote_filename);
        }
        String fileSize = String.valueOf(this.getFileSize());
        String last_error_message = "";
        if (!isEmpty(errorMessage)) {
            if (recordType.equals(HistoryRecordType.XML)) {
                last_error_message = normalizeErrorMessageForXml(errorMessage);
            } else if (recordType.equals(HistoryRecordType.CSV)) {
                last_error_message = normalizeErrorMessageForCSV(errorMessage);
            }
        }
        String log_filename = objOptions.logFilename.getValue();
        String jump_host = objOptions.jumpHost.getValue();
        String jump_user = objOptions.jumpUser.getValue();
        String jump_host_ip = "";
        String jump_port = "";
        String jump_protocol = "";
        if (!isEmpty(jump_host) && !isEmpty(jump_user)) {
            jump_host_ip = objOptions.jumpHost.getHostAdress();
            jump_port = objOptions.jumpPort.getValue();
            jump_protocol = objOptions.jumpProtocol.getValue();
        }
        Date endTime = this.getEndTime();
        Date startTime = this.getStartTime();
        if (startTime == null) {
            startTime = now();
        }
        if (endTime == null) {
            endTime = startTime;
        }
        fileAttributes.put(FIELD_GUID, this.guid);
        fileAttributes.put(FIELD_MANDATOR, mandator);
        fileAttributes.put(FIELD_TRANSFER_END, getTransferTimeAsString(endTime));
        fileAttributes.put(FIELD_PID, pid);
        fileAttributes.put(FIELD_PPID, ppid);
        fileAttributes.put(FIELD_OPERATION, operation);
        fileAttributes.put(FIELD_LOCALHOST, localhost);
        fileAttributes.put(FIELD_LOCALHOST_IP, localhost_ip);
        fileAttributes.put(FIELD_LOCAL_USER, local_user);
        fileAttributes.put(FIELD_REMOTE_HOST, remote_host);
        fileAttributes.put(FIELD_REMOTE_HOST_IP, remote_host_ip);
        fileAttributes.put(FIELD_REMOTE_USER, remote_user);
        fileAttributes.put(FIELD_PROTOCOL, protocol);
        fileAttributes.put(FIELD_PORT, port);
        fileAttributes.put(FIELD_LOCAL_DIR, local_dir);
        fileAttributes.put(FIELD_REMOTE_DIR, remote_dir);
        fileAttributes.put(FIELD_LOCAL_FILENAME, local_filename);
        fileAttributes.put(FIELD_REMOTE_FILENAME, remote_filename);
        fileAttributes.put(FIELD_FILE_SIZE, fileSize);
        fileAttributes.put(FIELD_MD5, this.getMd5());
        String status = this.getStatusText();
        if ("transferred".equalsIgnoreCase(status)) {
            status = "success";
        }
        fileAttributes.put(FIELD_STATUS, status);
        fileAttributes.put(FIELD_LAST_ERROR_MESSAGE, last_error_message);
        fileAttributes.put(FIELD_LOG_FILENAME, log_filename);
        fileAttributes.put(FIELD_JUMP_HOST, jump_host);
        fileAttributes.put(FIELD_JUMP_HOST_IP, jump_host_ip);
        fileAttributes.put(FIELD_JUMP_PORT, jump_port);
        fileAttributes.put(FIELD_JUMP_PROTOCOL, jump_protocol);
        fileAttributes.put(FIELD_JUMP_USER, jump_user);
        fileAttributes.put(FIELD_TRANSFER_START, getTransferTimeAsString(startTime));
        if (jumpHistoryRecord != null) {
            if ("copyToInternet".equals(objOptions.getDmzOptions().get("operation"))) {
                fileAttributes.put(FIELD_JUMP_HOST, fileAttributes.get(FIELD_REMOTE_HOST));
                fileAttributes.put(FIELD_JUMP_HOST_IP, fileAttributes.get(FIELD_REMOTE_HOST_IP));
                fileAttributes.put(FIELD_JUMP_PORT, fileAttributes.get(FIELD_PORT));
                fileAttributes.put(FIELD_JUMP_PROTOCOL, fileAttributes.get(FIELD_PROTOCOL));
                fileAttributes.put(FIELD_JUMP_USER, fileAttributes.get(FIELD_REMOTE_USER));
                fileAttributes.put(FIELD_REMOTE_HOST, jumpHistoryRecord.get(FIELD_REMOTE_HOST));
                fileAttributes.put(FIELD_REMOTE_HOST_IP, jumpHistoryRecord.getOrDefault(FIELD_REMOTE_HOST_IP, ""));
                fileAttributes.put(FIELD_REMOTE_USER, jumpHistoryRecord.getOrDefault(FIELD_REMOTE_USER, ""));
                fileAttributes.put(FIELD_PROTOCOL, jumpHistoryRecord.getOrDefault(FIELD_PROTOCOL, ""));
                fileAttributes.put(FIELD_PORT, jumpHistoryRecord.getOrDefault(FIELD_PORT, ""));
                fileAttributes.put(FIELD_REMOTE_DIR, jumpHistoryRecord.getOrDefault(FIELD_REMOTE_DIR, ""));
                fileAttributes.put(FIELD_REMOTE_FILENAME, jumpHistoryRecord.getOrDefault(FIELD_REMOTE_FILENAME, ""));
                fileAttributes.put(FIELD_TRANSFER_END, jumpHistoryRecord.getOrDefault(FIELD_TRANSFER_END, ""));
                fileAttributes.put(FIELD_STATUS, jumpHistoryRecord.getOrDefault(FIELD_STATUS, ""));
                if (recordType.equals(HistoryRecordType.XML)) {
                    last_error_message = normalizeErrorMessageForXml(StringEscapeUtils.unescapeXml(jumpHistoryRecord.getOrDefault(
                            FIELD_LAST_ERROR_MESSAGE, "")));
                } else {
                    last_error_message = jumpHistoryRecord.getOrDefault(FIELD_LAST_ERROR_MESSAGE, "");
                }
                fileAttributes.put(FIELD_LAST_ERROR_MESSAGE, last_error_message);
            } else if ("copyFromInternet".equals(objOptions.getDmzOptions().get("operation"))) {
                fileAttributes.put(FIELD_JUMP_HOST, fileAttributes.get(FIELD_LOCALHOST));
                fileAttributes.put(FIELD_JUMP_HOST_IP, fileAttributes.get(FIELD_LOCALHOST_IP));
                fileAttributes.put(FIELD_JUMP_PORT, source.port.getValue());
                fileAttributes.put(FIELD_JUMP_PROTOCOL, source.protocol.getValue());
                fileAttributes.put(FIELD_JUMP_USER, source.user.getValue());
                fileAttributes.put(FIELD_LOCALHOST, jumpHistoryRecord.get(FIELD_LOCALHOST));
                fileAttributes.put(FIELD_LOCALHOST_IP, jumpHistoryRecord.getOrDefault(FIELD_LOCALHOST_IP, ""));
                fileAttributes.put(FIELD_LOCAL_DIR, jumpHistoryRecord.getOrDefault(FIELD_LOCAL_DIR, ""));
                fileAttributes.put(FIELD_LOCAL_FILENAME, jumpHistoryRecord.getOrDefault(FIELD_LOCAL_FILENAME, ""));
                fileAttributes.put(FIELD_TRANSFER_START, jumpHistoryRecord.getOrDefault(FIELD_TRANSFER_START, ""));
            }
        }
        return fileAttributes;
    }

    public String toCsv() {
        Map<String, String> properties = getFileAttributes(HistoryRecordType.CSV);
        StringBuffer strB = new StringBuffer();
        strB.append(properties.get(FIELD_GUID) + ";");
        strB.append(properties.get(FIELD_MANDATOR) + ";");
        strB.append(properties.get(FIELD_TRANSFER_END) + ";");
        strB.append(properties.get(FIELD_PID) + ";");
        strB.append(properties.get(FIELD_PPID) + ";");
        strB.append(properties.get(FIELD_OPERATION) + ";");
        strB.append(properties.get(FIELD_LOCALHOST) + ";");
        strB.append(properties.get(FIELD_LOCALHOST_IP) + ";");
        strB.append(properties.get(FIELD_LOCAL_USER) + ";");
        strB.append(properties.get(FIELD_REMOTE_HOST) + ";");
        strB.append(properties.get(FIELD_REMOTE_HOST_IP) + ";");
        strB.append(properties.get(FIELD_REMOTE_USER) + ";");
        strB.append(properties.get(FIELD_PROTOCOL) + ";");
        strB.append(properties.get(FIELD_PORT) + ";");
        strB.append(properties.get(FIELD_LOCAL_DIR) + ";");
        strB.append(properties.get(FIELD_REMOTE_DIR) + ";");
        strB.append(properties.get(FIELD_LOCAL_FILENAME) + ";");
        strB.append(properties.get(FIELD_REMOTE_FILENAME) + ";");
        strB.append(properties.get(FIELD_FILE_SIZE) + ";");
        strB.append(properties.get(FIELD_MD5) + ";");
        strB.append(properties.get(FIELD_STATUS) + ";");
        strB.append(properties.get(FIELD_LAST_ERROR_MESSAGE) + ";");
        strB.append(properties.get(FIELD_LOG_FILENAME) + ";");
        strB.append(properties.get(FIELD_JUMP_HOST) + ";");
        strB.append(properties.get(FIELD_JUMP_HOST_IP) + ";");
        strB.append(properties.get(FIELD_JUMP_PORT) + ";");
        strB.append(properties.get(FIELD_JUMP_PROTOCOL) + ";");
        strB.append(properties.get(FIELD_JUMP_USER) + ";");
        strB.append(properties.get(FIELD_TRANSFER_START) + ";");
        strB.append(getTransferTimeAsString(now()));
        return strB.toString();
    }

    private String toHexString(final byte[] b) {
        char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        int length = b.length * 2;
        StringBuffer sb = new StringBuffer(length);
        for (byte element : b) {
            sb.append(hexChar[(element & 0xf0) >>> 4]);
            sb.append(hexChar[element & 0x0f]);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        String strT;
        try {
            strT = SOSVfs_D_214.params(this.getTargetFileNameAndPath(), this.getSourceFileName(), this.getNoOfBytesTransferred(), objOptions.operation
                    .getValue());
        } catch (RuntimeException e) {
            LOGGER.error(e.toString());
            strT = "???";
        }
        return strT;
    }

    public void setTransferStatus(final enuTransferStatus peTransferStatus) {
        this.setStatus(peTransferStatus);
    }

    public void setVfsHandler(final ISOSVfsFileTransfer pobjVfs) {
        objVfsHandler = pobjVfs;
    }

    private boolean fileNamesAreEqual(String filenameA, String filenameB, boolean caseSensitiv) {
        String a = filenameA.replaceAll("[\\\\/]+", "/");
        String b = filenameB.replaceAll("[\\\\/]+", "/");
        return caseSensitiv ? a.equals(b) : a.equalsIgnoreCase(b);
    }

    public boolean isTargetFileAlreadyExists() {
        return targetFileAlreadyExists;
    }

    public void setTargetFileAlreadyExists(boolean targetFileAlreadyExists) {
        this.targetFileAlreadyExists = targetFileAlreadyExists;
    }

    public boolean hasChecksumFile() {
        return checksumFile != null;
    }

    public void setChecksumFile(ISOSVirtualFile checksumFile) {
        this.checksumFile = checksumFile;
    }

    public ISOSVirtualFile getChecksumFile() {
        return checksumFile;
    }

    private String getTransferTimeAsString(Date time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setLenient(false);
        return formatter.format(time.getTime());
    }

    public void setJumpHistoryRecord(Map<String, String> jumpHistoryRecord) {
        this.jumpHistoryRecord = jumpHistoryRecord;
    }

    private Buffer compress(byte[] dataToCompress) {
        return compress(dataToCompress, dataToCompress.length);
    }

    private Buffer compress(byte[] dataToCompress, int length) {
        Buffer buf = new Buffer();
        if (objOptions.compressFiles.isTrue()) {
            try {
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream(length);
                try {
                    GZIPOutputStream zipStream = new GZIPOutputStream(byteStream);
                    try {
                        zipStream.write(dataToCompress, 0, length);
                    } finally {
                        zipStream.close();
                    }
                } finally {
                    byteStream.close();
                }
                buf.setBytes(byteStream.toByteArray());
                return buf;
            } catch (Exception e) {
                throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_134.params("GZip"), e);
            }
        } else {
            buf.setBytes(dataToCompress);
            buf.setLength(length);
            return buf;
        }
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public void setEventHandler(IJobSchedulerEventHandler eventHandler) {
        this.eventHandler  = eventHandler;
    }

    private class EntryPaths {

        private String fullName = "";
        private String relativeName = "";
        private String baseName = "";
        private String baseDirFullName = "";
        private String parentDirFullName = "";
        private String parentDirBaseName = "";

        private EntryPaths(final String baseDirPath, final String filePath) {
            if (filePath != null) {
                try {
                    Path baseDir = SOSString.isEmpty(baseDirPath) ? null : Paths.get(baseDirPath);
                    Path file = Paths.get(filePath);
                    Path parentDir = file.getParent();

                    if (parentDir != null) {
                        parentDirFullName = parentDir.toString().replace('\\', '/');
                        parentDirBaseName = parentDir.getFileName().toString();
                        if (baseDir == null) {
                            baseDir = parentDir;
                        }
                    }

                    fullName = file.toString().replace('\\', '/');
                    baseName = file.getFileName().toString();

                    if (baseDir == null) {
                        relativeName = baseName;
                    } else {
                        baseDirFullName = baseDir.normalize().toString().replace('\\', '/');
                        relativeName = baseDir.relativize(file).normalize().toString().replace('\\', '/');
                    }

                } catch (Exception e) {
                    LOGGER.warn(String.format("error on resolve path for baseDirPath=%s, filePath=%s", baseDirPath, filePath), e);
                }
            }
        }

        public String getFullName() {
            return fullName;
        }

        public String getRelativeName() {
            return relativeName;
        }

        public String getBaseName() {
            return baseName;
        }

        public String getBaseDirFullName() {
            return baseDirFullName;
        }

        public String getParentDirFullName() {
            return parentDirFullName;
        }

        public String getParentDirBaseName() {
            return parentDirBaseName;
        }

    }

}
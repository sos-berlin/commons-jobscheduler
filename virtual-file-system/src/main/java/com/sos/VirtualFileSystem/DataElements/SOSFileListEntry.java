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
import com.sos.VirtualFileSystem.common.SOSVfsEnv;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;
import com.sos.i18n.annotation.I18NResourceBundle;

import sos.util.SOSString;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSFileListEntry extends SOSVfsMessageCodes implements Runnable, IJadeTransferDetailHistoryData {

    public enum TransferStatus {
        transferUndefined, waiting4transfer, transferring, transferInProgress, transferred, transfer_skipped, transfer_has_errors, transfer_aborted, compressed, notOverwritten, deleted, renamed, ignoredDueToZerobyteConstraint, setBack, polling
    }

    public enum HistoryRecordType {
        XML, CSV
    }

    private static final Logger LOGGER = Logger.getLogger(SOSFileListEntry.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final boolean isTraceEnabled = LOGGER.isTraceEnabled();
    private static final Logger JADE_REPORT_LOGGER = Logger.getLogger(VFSFactory.getLoggerName());

    private static String ENV_VAR_FILE_TRANSFER_STATUS = "YADE_FILE_TRANSFER_STATUS";
    private static String ENV_VAR_FILE_IS_TRANSFERRED = "YADE_FILE_IS_TRANSFERRED";

    private IJobSchedulerEventHandler eventHandler = null;
    private TransferStatus transferStatus = TransferStatus.transferUndefined;
    private SOSFTPOptions options = null;
    private SOSFileList fileList = null;
    private SOSVfsConnectionPool sourceConnectionPool = null;
    private SOSVfsConnectionPool targetConnectionPool = null;
    private ISOSVfsFileTransfer sourceFileTransfer = null;
    private ISOSVfsFileTransfer targetFileTransfer = null;
    private ISOSVirtualFile sourceTransferFile = null;
    private ISOSVirtualFile targetTransferFile = null;
    private ISOSVirtualFile targetChecksumFile = null;
    private String sourceTransferFileName = null;
    private String targetTransferFileName = null;
    private String sourceFileName = null;
    private String targetFileName = null;

    private String sourceFileNameRenamed = null;
    private long sourceFileSize = -1L;
    private long sourceFileModificationDateTime = -1L;
    private long sourceFileLastCheckedFileSize = -1L;
    private boolean sourceFileSteady = false;
    private String sourceFileChecksum = "N/A";
    private String targetFileChecksum = "N/A";
    private boolean targetFileExists = false;
    private String targetAtomicFileName = EMPTY_STRING;

    private Date transferStart = null;
    private Date transferEnd = null;
    private String errorMessage = null;
    private Map<String, String> jumpHistoryRecord = null;
    private boolean transactional = false;// ? not used???
    private long bytesTransferred = 0;

    private ISOSVfsFileTransfer fileTransfer = null;
    private FTPFile ftpFile = null;

    // history file
    private final String guid = UUID.randomUUID().toString();
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

    private class Buffer {

        private byte[] bytes = new byte[0];
        private int length = 0;

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] val) {
            bytes = val;
            length = val.length;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int val) {
            length = val;
        }
    }

    public SOSFileListEntry() {
        super(SOSVfsConstants.strBundleBaseName);
    }

    public SOSFileListEntry(final FTPFile file) {
        this();
        sourceFileName = file.getName();
        sourceFileSize = file.getSize();
        sourceFileModificationDateTime = file.getTimestamp().getTimeInMillis();
        ftpFile = file;
    }

    public SOSFileListEntry(final String localFileName) {
        this("", localFileName, 0);
    }

    public SOSFileListEntry(final String targetFile, final String sourceFile, final long transferred) {
        super(SOSVfsConstants.strBundleBaseName);
        targetFileName = targetFile;
        sourceFileName = sourceFile;
        bytesTransferred = transferred;
    }

    private String changeBackslashes(final String val) {
        return val.replaceAll("\\\\", "/");
    }

    public void deleteSourceFile() {
        sourceFileTransfer.getFileHandle(sourceFileName).delete();
        String msg = SOSVfs_I_0113.params(sourceFileName);
        LOGGER.info(msg);
        JADE_REPORT_LOGGER.info(msg);
    }

    private boolean doTransfer(final ISOSVirtualFile sourceFile, final ISOSVirtualFile targetFile) {
        boolean closed = false;
        if (targetFile == null) {
            raiseException(SOSVfs_E_273.params("Target"));
        }
        if (sourceFile == null) {
            raiseException(SOSVfs_E_273.params("Source"));
        }

        MessageDigest targetChecksum = null;
        boolean targetCreateIntegrityHashFile = options.createIntegrityHashFile.isTrue();
        if (targetCreateIntegrityHashFile) {
            try {
                targetChecksum = MessageDigest.getInstance(options.integrityHashType.getValue());
            } catch (NoSuchAlgorithmException e1) {
                LOGGER.error(e1.toString());
                options.createIntegrityHashFile.value(false);
                targetCreateIntegrityHashFile = false;
            }
        }

        MessageDigest sourceChecksum = null;
        boolean sourceCheckIntegrityHash = options.checkIntegrityHash.isTrue();
        if (sourceCheckIntegrityHash) {
            try {
                sourceChecksum = MessageDigest.getInstance(options.integrityHashType.getValue());
            } catch (NoSuchAlgorithmException e1) {
                LOGGER.error(e1.toString());
                options.checkIntegrityHash.value(false);
                sourceCheckIntegrityHash = false;
            }
        }
        executePreCommands(false);
        long totalBytesTransferred = 0;
        this.setStatus(TransferStatus.transferring);
        // send event to inform that transfer starts?
        try {
            int cumulativeFileSeperatorLength = 0;
            byte[] buffer = new byte[options.bufferSize.value()];
            int bytesTransferred;
            synchronized (this) {
                if (options.cumulateFiles.isTrue() && options.cumulativeFileSeparator.isNotEmpty()) {
                    String fs = options.cumulativeFileSeparator.getValue();
                    fs = this.replaceVariables(fs) + System.getProperty("line.separator");
                    byte[] bytes = fs.getBytes();
                    cumulativeFileSeperatorLength = bytes.length;
                    Buffer compressedBytes = compress(bytes);
                    targetFile.write(compressedBytes.getBytes());
                    if (sourceCheckIntegrityHash) {
                        sourceChecksum.update(bytes);
                    }
                    if (targetCreateIntegrityHashFile) {
                        targetChecksum.update(compressedBytes.getBytes());
                    }
                }
                if (sourceFile.getFileSize() <= 0) {
                    byte[] bytes = new byte[0];
                    Buffer compressedBytes = compress(bytes);
                    targetFile.write(compressedBytes.getBytes());
                    if (sourceCheckIntegrityHash) {
                        sourceChecksum.update(bytes);
                    }
                    if (targetCreateIntegrityHashFile) {
                        targetChecksum.update(compressedBytes.getBytes());
                    }
                    if (eventHandler != null) {
                        Map<String, String> values = new HashMap<String, String>();
                        values.put("sourcePath", this.getSourceFilename());
                        values.put("targetPath", this.getTargetFilename());
                        values.put("state", "5");
                        eventHandler.updateDb(null, "YADE_FILE", values);
                    }
                } else {
                    // int actualBytesTransferred = 0;
                    if (eventHandler != null) {
                        Map<String, String> values = new HashMap<String, String>();
                        values.put("sourcePath", this.getSourceFilename());
                        values.put("targetPath", this.getTargetFilename());
                        values.put("state", "5");
                        eventHandler.updateDb(null, "YADE_FILE", values);
                    }
                    while ((bytesTransferred = sourceFile.read(buffer)) != -1) {
                        try {
                            // actualBytesTransferred += bytesTransferred;
                            Buffer compressedBytes = compress(buffer, bytesTransferred);
                            targetFile.write(compressedBytes.getBytes(), 0, compressedBytes.getLength());
                            // Map<String, String> values = new HashMap<String, String>();
                            // values.put("bytesTransferred", "" + actualBytesTransferred);
                            // values.put("targetSize", "" + target.getFileSize());
                            // Map <String, Map<String, String>> eventParams = new HashMap<String, Map<String, String>>();
                            // eventParams.put("transferring:" + source.getName(), values);
                            // sendEvent(eventParams);

                            // commented due to not having a progress bar and performance

                            // if (eventHandler != null) {
                            // Map<String, String> values = new HashMap<String, String>();
                            // values.put("sourcePath", this.getSourceFilename());
                            // values.put("targetPath", this.getTargetFilename());
                            // values.put("state", "5");
                            // eventHandler.updateDb(null, "YADE_FILE", values);
                            // }
                            if (sourceCheckIntegrityHash) {
                                sourceChecksum.update(buffer, 0, bytesTransferred);
                            }
                            if (targetCreateIntegrityHashFile) {
                                targetChecksum.update(compressedBytes.getBytes(), 0, compressedBytes.getLength());
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
                        // Map<String, String> values = new HashMap<String, String>();
                        // values.put("totalBytesTransferred", "" + totalBytesTransferred);
                        // values.put("targetSize", "" + target.getFileSize());
                        // Map <String, Map<String, String>> eventParams = new HashMap<String, Map<String, String>>();
                        // eventParams.put("transferred:" + source.getName(), values);
                        // sendEvent(eventParams);
                        // end of event processing
                    }
                }
            }
            // TODO: define the structure of the event answer
            // sendEvent(null);
            sourceFile.closeInput();
            targetFile.closeOutput();
            closed = true;

            LOGGER.info(String.format("[transferred]Source=%s, Target=%s, Bytes=%s", sourceFile.getName(), targetFile.getName(),
                    totalBytesTransferred));

            if (targetFileTransfer.isNegativeCommandCompletion()) {
                raiseException(SOSVfs_E_175.params(targetTransferFile.getName(), targetFileTransfer.getReplyString()));
            }
            if (targetCreateIntegrityHashFile) {
                targetFileChecksum = toHexString(targetChecksum.digest());
                sourceFileChecksum = targetFileChecksum;
                LOGGER.debug(SOSVfs_I_274.params(targetFileChecksum, targetFileName, options.integrityHashType.getValue()));
            }
            if (sourceCheckIntegrityHash) {
                sourceFileChecksum = toHexString(sourceChecksum.digest());
                LOGGER.debug(SOSVfs_I_274.params(sourceFileChecksum, sourceFileName, options.integrityHashType.getValue()));
            }
            setNoOfBytesTransferred(totalBytesTransferred);
            totalBytesTransferred += cumulativeFileSeperatorLength;
            checkSourceChecksumFile(sourceFile, targetFile);
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
                sourceFile.closeInput();
                targetFile.closeOutput();
                closed = true;
            }
        }
    }

    // private void sendEvent(Map<String, Map<String, String>> eventParams) {
    // if (eventHandler != null && eventParams != null) {
    // eventHandler.sendEvent(eventParams);
    // }
    // }

    private void updateDb(Long id, String type, Map<String, String> values) {
        if (eventHandler != null) {
            // id of the DBItem
            // type of the Item, here always YADE_FILE
            // map of values of the Item, with key = propertyName and value = propertyValue
            eventHandler.updateDb(id, type, values);
        }
    }

    public void createTargetChecksumFile() {
        createTargetChecksumFile(makeFullPathName(options.targetDir.getValue(), targetFileName));
    }

    public void createTargetChecksumFile(String targetFileName) {
        if (options.createIntegrityHashFile.isTrue() && isTransferred()) {
            ISOSVirtualFile checksumFile = null;
            try {
                targetFileName = resolveDotsInPath(targetFileName);
                checksumFile = targetFileTransfer.getFileHandle(targetFileName + "." + options.integrityHashType.getValue());
                checksumFile.write(targetFileChecksum.getBytes());
                LOGGER.info(SOSVfs_I_285.params(checksumFile.getName()));
                targetChecksumFile = checksumFile;
            } catch (JobSchedulerException e) {
                throw e;
            } finally {
                if (checksumFile != null) {
                    checksumFile.closeOutput();
                }
            }
        }
    }

    private void checkSourceChecksumFile(ISOSVirtualFile sourceFile, ISOSVirtualFile targetFile) {
        if (options.checkIntegrityHash.isTrue()) {
            ISOSVirtualFile sourceFileChecksumFile = null;
            String sourceFileChecksumFileName = sourceFile.getName() + "." + options.securityHashType.getValue();
            try {
                sourceFileChecksumFile = geSourceFileTransfer().getFileHandle(sourceFileChecksumFileName);
                if (sourceFileChecksumFile.fileExists()) {
                    String checksum = sourceFileChecksumFile.file2String().trim();
                    if (!checksum.equals(sourceFileChecksum)) {
                        try {
                            if (targetFile.fileExists()) {
                                targetFile.delete();
                            }
                        } catch (Exception ex) {
                            LOGGER.debug(ex.toString(), ex);
                        }
                        setStatus(TransferStatus.transfer_aborted);
                        throw new JobSchedulerException(String.format(
                                "Integrity Hash violation. File %1$s, checksum read: '%2$s', checksum calculated: '%3$s'", sourceFileChecksumFileName,
                                checksum, sourceFileChecksum));
                    } else {
                        LOGGER.info(String.format("Integrity Hash is OK: File %1$s, checksum read '%2$s', checksum calculated '%3$s'",
                                sourceFileChecksumFileName, checksum, sourceFileChecksum));
                    }
                } else {
                    LOGGER.info(String.format("Checksum file '%1$s' not found", sourceFileChecksumFileName));
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
        executeCommands(commandOptionName, fileTransfer, optionCommands, null, null);
    }

    private void executeCommands(final String commandOptionName, final ISOSVfsFileTransfer fileTransfer, final SOSOptionString optionCommands,
            final SOSOptionString optionCommandDelimiter) {
        executeCommands(commandOptionName, fileTransfer, optionCommands, optionCommandDelimiter, null);
    }

    private void executeCommands(final String commandOptionName, final ISOSVfsFileTransfer fileTransfer, final SOSOptionString optionCommands,
            final SOSOptionString optionCommandDelimiter, SOSVfsEnv env) {
        String commands = optionCommands.getValue().trim();
        String fileName = "";
        if (commandOptionName.startsWith("source_")) {
            if (sourceTransferFile != null) {
                fileName = sourceTransferFile.getName();
            }
        } else {
            if (targetTransferFile != null) {
                fileName = targetTransferFile.getName();
            }
        }
        if (commands.length() > 0) {
            commands = replaceVariables(commands);
            String delimiter = null;
            if (optionCommandDelimiter != null) {
                delimiter = optionCommandDelimiter.getValue();
            }
            if (delimiter == null) {
                delimiter = ";";
            }
            if (delimiter.isEmpty()) {
                try {
                    LOGGER.info(String.format("[%s][%s]%s", commandOptionName, fileName, commands));
                    fileTransfer.getHandler().executeCommand(commands, env);
                } catch (JobSchedulerException e) {
                    throw new JobSchedulerException(String.format("[%s][%s][%s]", commandOptionName, fileName, commands), e);
                } catch (Exception e) {
                    throw new JobSchedulerException(String.format("[%s][%s][%s]", commandOptionName, fileName, commands), e);
                }
            } else {
                String[] values = commands.split(delimiter);
                if (values.length > 1) {
                    LOGGER.debug(String.format("[%s]commands=%s", commandOptionName, commands));
                }
                for (String command : values) {
                    command = command.trim();
                    if (command.length() > 0) {
                        try {
                            LOGGER.info(String.format("[%s][%s]%s", commandOptionName, fileName, command));
                            fileTransfer.getHandler().executeCommand(command, env);
                        } catch (JobSchedulerException e) {
                            throw new JobSchedulerException(String.format("[%s][%s][%s]", commandOptionName, fileName, command), e);
                        } catch (Exception e) {
                            throw new JobSchedulerException(String.format("[%s][%s][%s]", commandOptionName, fileName, command), e);
                        }
                    }
                }
            }
        }
    }

    public void executeTFNPostCommnands() {
        SOSConnection2OptionsAlternate target = options.getConnectionOptions().getTarget();
        if (target.alternateOptionsUsed.isTrue()) {
            executeCommands("alternative_target_tfn_post_command", targetFileTransfer, target.getAlternatives().tfnPostCommand, target
                    .getAlternatives().commandDelimiter);
        } else {
            executeCommands("tfn_post_command", targetFileTransfer, options.tfnPostCommand);
            executeCommands("target_tfn_post_command", targetFileTransfer, target.tfnPostCommand, target.commandDelimiter);
        }
        SOSConnection2OptionsAlternate source = options.getConnectionOptions().getSource();
        if (source.alternateOptionsUsed.isTrue()) {
            executeCommands("alternative_source_tfn_post_command", sourceFileTransfer, source.getAlternatives().tfnPostCommand, source
                    .getAlternatives().commandDelimiter);
        } else {
            executeCommands("source_tfn_post_command", sourceFileTransfer, source.tfnPostCommand, source.commandDelimiter);
        }
    }

    public void executePostCommands() {
        boolean isTransferred = transferStatus.equals(TransferStatus.transferred);
        String status = transferStatus.name();
        Map<String, String> env = new HashMap<String, String>();
        env.put(ENV_VAR_FILE_TRANSFER_STATUS, status);
        env.put(ENV_VAR_FILE_IS_TRANSFERRED, isTransferred ? "1" : "0");
        SOSVfsEnv envs = new SOSVfsEnv();
        envs.setLocalEnvs(env);

        SOSConnection2OptionsAlternate target = options.getConnectionOptions().getTarget();
        if (target.alternateOptionsUsed.isTrue()) {
            if (!isTransferred && target.getAlternatives().post_command_disable_for_skipped_transfer.value()) {
                LOGGER.info(String.format("[alternative_target_post_command][skip]disable_for_skipped_transfer=true, status=%s", status));
            } else {
                executeCommands("alternative_target_post_command", targetFileTransfer, target.getAlternatives().postCommand, target
                        .getAlternatives().commandDelimiter, envs);
            }
        } else {
            if (!isTransferred && options.post_command_disable_for_skipped_transfer.value()) {
                LOGGER.info(String.format("[post_command][skip]disable_for_skipped_transfer=true, status=%s", status));
            } else {
                executeCommands("post_command", targetFileTransfer, options.postCommand, target.commandDelimiter, envs);
            }
            if (!isTransferred && target.post_command_disable_for_skipped_transfer.value()) {
                LOGGER.info(String.format("[target_post_command][skip]disable_for_skipped_transfer=true, status=%s", status));
            } else {
                executeCommands("target_post_command", targetFileTransfer, target.postCommand, target.commandDelimiter, envs);
            }
        }
        SOSConnection2OptionsAlternate source = options.getConnectionOptions().getSource();
        if (source.alternateOptionsUsed.isTrue()) {
            if (!isTransferred && source.getAlternatives().post_command_disable_for_skipped_transfer.value()) {
                LOGGER.info(String.format("[alternative_source_post_command][skip]disable_for_skipped_transfer=true, status=%s", status));
            } else {
                executeCommands("alternative_source_post_command", sourceFileTransfer, source.getAlternatives().postCommand, source
                        .getAlternatives().commandDelimiter, envs);
            }
        } else {
            if (!isTransferred && source.post_command_disable_for_skipped_transfer.value()) {
                LOGGER.info(String.format("[source_post_command][skip]disable_for_skipped_transfer=true, status=%s", status));
            } else {
                executeCommands("source_post_command", sourceFileTransfer, source.postCommand, source.commandDelimiter, envs);
            }
        }
    }

    private void executePreCommands(boolean isSkipped) {
        SOSConnection2OptionsAlternate target = options.getConnectionOptions().getTarget();
        if (target.alternateOptionsUsed.isTrue()) {
            if (isSkipped) {
                if (target.getAlternatives().pre_command_enable_for_skipped_transfer.value()) {
                    executeCommands("alternative_target_pre_command enable_for_skipped_transfer=true", targetFileTransfer, target
                            .getAlternatives().preCommand, target.commandDelimiter);
                }
            } else {
                executeCommands("alternative_target_pre_command", targetFileTransfer, target.getAlternatives().preCommand, target.commandDelimiter);
            }
        } else {
            if (isSkipped) {
                if (options.pre_command_enable_for_skipped_transfer.value()) {
                    executeCommands("pre_command enable_for_skipped_transfer=true", targetFileTransfer, options.preCommand);
                }
                if (target.pre_command_enable_for_skipped_transfer.value()) {
                    executeCommands("target_pre_command enable_for_skipped_transfer=true", targetFileTransfer, target.preCommand,
                            target.commandDelimiter);
                }
            } else {
                executeCommands("pre_command", targetFileTransfer, options.preCommand);
                executeCommands("target_pre_command", targetFileTransfer, target.preCommand, target.commandDelimiter);
            }
        }
        SOSConnection2OptionsAlternate source = options.getConnectionOptions().getSource();
        if (source.alternateOptionsUsed.isTrue()) {
            if (isSkipped) {
                if (source.getAlternatives().pre_command_enable_for_skipped_transfer.value()) {
                    executeCommands("alternative_source_pre_command enable_for_skipped_transfer=true", sourceFileTransfer, source
                            .getAlternatives().preCommand, source.getAlternatives().commandDelimiter);
                }
            } else {
                executeCommands("alternative_source_pre_command", sourceFileTransfer, source.getAlternatives().preCommand, source
                        .getAlternatives().commandDelimiter);
            }
        } else {
            if (isSkipped) {
                if (source.pre_command_enable_for_skipped_transfer.value()) {
                    executeCommands("source_pre_command enable_for_skipped_transfer=true", sourceFileTransfer, source.preCommand,
                            source.commandDelimiter);
                }
            } else {
                executeCommands("source_pre_command", sourceFileTransfer, source.preCommand, source.commandDelimiter);
            }
        }
    }

    public boolean isTargetFileExists() {
        return targetFileExists;
    }

    public String getTargetAtomicFileName() {
        return targetAtomicFileName;
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

    public ISOSVfsFileTransfer geSourceFileTransfer() {
        return sourceFileTransfer;
    }

    public ISOSVfsFileTransfer geTargetFileTransfer() {
        return targetFileTransfer;
    }

    @Override
    public Date getEndTime() {
        return transferEnd;
    }

    public String getFileName4ResultList() {
        String result = targetFileName;
        if (isEmpty(result)) {
            result = sourceFileName;
        }
        if (options.resultSetFileName.getValue().endsWith(".source.tmp")) {
            result = sourceFileName;
        }
        return result;
    }

    private String getFileNameWithoutPath(final String name) {
        return new File(adjustFileSeparator(name)).getName();
    }

    @Override
    public Long getFileSize() {
        return sourceFileSize;
    }

    public Long getSourceFileLastCheckedFileSize() {
        return sourceFileLastCheckedFileSize;
    }

    public void setSourceFileLastCheckedFileSize(Long val) {
        sourceFileLastCheckedFileSize = val;
    }

    @Override
    public String getLastErrorMessage() {
        return EMPTY_STRING;
    }

    @Override
    public String getMd5() {
        return targetFileChecksum;
    }

    @Override
    public Date getModified() {
        return null;
    }

    @Override
    public String getModifiedBy() {
        return EMPTY_STRING;
    }

    private String getPathWithoutFileName(final String fileName) {
        File file = new File(adjustFileSeparator(fileName));
        String parent = file.getParent();
        if (parent == null) {
            parent = "./";
        }
        return adjustFileSeparator(parent);
    }

    @Override
    public String getPid() {
        return options.getPid();
    }

    @Override
    public String getSizeValue() {
        return "";
    }

    @Override
    public String getSourceFilename() {
        return sourceFileName;
    }

    @Override
    public Date getStartTime() {
        return transferStart;
    }

    @Override
    public Integer getStatus() {
        return new Integer(transferStatus.ordinal());
    }

    @Override
    public String getStatusText() {
        return transferStatus.name();
    }

    public ISOSVirtualFile getTargetFile() {
        ISOSVirtualFile sourceFile = sourceFileTransfer.getFileHandle(sourceFileName);
        sourceTransferFileName = sourceFile.getName();
        targetFileName = sourceFile.getName();
        boolean recursive = options.recursive.value();
        if (options.compressFiles.isTrue()) {
            targetFileName = targetFileName + options.compressedFileExtension.getValue();
        }
        if (options.cumulateFiles.isTrue()) {
            targetFileName = options.cumulativeFileName.getValue();
            targetTransferFileName = targetFileName;
            options.appendFiles.value(true);
        } else {
            targetFileName = getFileNameWithoutPath(targetFileName);
            targetTransferFileName = targetFileName;
            if (options.getReplacing().isNotEmpty()) {
                try {
                    targetFileName = options.getReplacing().doReplace(targetFileName, options.getReplacement().getValue());
                } catch (Exception e) {
                    throw new JobSchedulerException(SOSVfs_E_0150.get() + " " + e.toString(), e);
                }
            }
        }
        if (options.isAtomicTransfer() || options.transactionMode.isTrue()) {
            targetTransferFileName = getTargetAtomicFileName(options);
        }
        if (recursive) {
            String sourceDir = getPathWithoutFileName(sourceFile.getName());
            String sourceDirOrig = options.sourceDir().getValue();
            if (!fileNamesAreEqual(sourceDir, sourceDirOrig, true) && sourceDir.length() > sourceDirOrig.length()) {
                String subFolder = sourceDir.substring(sourceDirOrig.length());
                subFolder = adjustFileSeparator(addFileSeparator(subFolder));
                targetFileName = targetFileName.replaceFirst("([^/]*)$", subFolder + "$1");
                targetTransferFileName = subFolder + targetTransferFileName;
                if (isNotEmpty(getTargetAtomicFileName())) {
                    setTargetAtomicFileName(targetTransferFileName);
                }
                try {
                    if (fileList.add2SubFolders(subFolder)) {
                        targetFileTransfer.mkdir(addFileSeparator(options.targetDir().getValue()) + subFolder);
                    }
                } catch (IOException e) {
                    throw new JobSchedulerException(e);
                }
            }
        }
        return null;
    }

    public void setRenamedSourceFilename() {
        SOSConnection2OptionsAlternate sourceOptions = options.getSource();
        if (sourceOptions.replacing.isDirty() && !options.removeFiles.value()) {
            String replaceWith = sourceOptions.replacement.getValue();
            try {
                String sourceName = new File(sourceFileName).getName();
                String sourceParent = sourceFileName.substring(0, sourceFileName.length() - sourceName.length());
                String sourceNameNew = sourceOptions.replacing.doReplace(sourceName, replaceWith).replace('\\', '/');
                boolean equals = sourceNameNew.equals(sourceName);
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[replaceWath=%s][replaceWith=%s][source=%s][new=%s]equals=%s", sourceOptions.replacing.getValue(),
                            replaceWith, sourceName, sourceNameNew, equals));
                }
                if (!equals) {
                    String sourceDir = addFileSeparator(sourceOptions.directory.getValue()).replace('\\', '/');
                    if (options.recursive.value()) {
                        String subDirs = sourceParent.substring(sourceDir.length());
                        sourceNameNew = subDirs + sourceNameNew;
                    }
                    if (!sourceNameNew.startsWith("/") && !sourceNameNew.matches("^[a-zA-Z]:[\\\\/].*$")) {
                        sourceNameNew = sourceDir + sourceNameNew;
                    }
                    sourceNameNew = resolveDotsInPath(sourceNameNew);
                    if (!sourceFileName.equals(sourceNameNew)) {
                        sourceFileNameRenamed = sourceNameNew;

                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("RenamedSourceFileName=%s", sourceFileNameRenamed));
                        }
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
        return targetFileName;
    }

    public String getTargetFileNameAndPath() {
        return options.targetDir.getValue() + targetFileName;
    }

    public void setTransactional() {
        transactional = true;
    }

    public boolean getTransactional() {
        return transactional;
    }

    @Override
    public Integer getTransferDetailsId() {
        return null;
    }

    @Override
    public Integer getTransferId() {
        return null;
    }

    public TransferStatus getTransferStatus() {
        return transferStatus;
    }

    public boolean isTransferred() {
        return transferStatus.equals(TransferStatus.transferred);
    }

    public boolean isSourceFileSteady() {
        return sourceFileSteady;
    }

    public void log4Debug() {
        LOGGER.debug(SOSVfs_D_218.params(getSourceFileName()));
        LOGGER.debug(SOSVfs_D_219.params(getSourceTransferFileName()));
        LOGGER.debug(SOSVfs_D_220.params(getTargetTransferFileName()));
        LOGGER.debug(SOSVfs_D_221.params(getTargetFileName()));
    }

    public String getTargetAtomicFileName(final ISOSFtpOptions ftpOptions) {
        targetTransferFileName = targetTransferFileName + ftpOptions.getAtomicSuffix().getValue().trim();
        targetTransferFileName = ftpOptions.getAtomicPrefix().getValue() + targetTransferFileName;
        targetAtomicFileName = targetTransferFileName.trim();
        return targetAtomicFileName;
    }

    public void setTargetAtomicFileName(final String val) {
        targetAtomicFileName = val;
    }

    private String makeFileNameReplacing(final String fileName) {
        String name = adjustFileSeparator(fileName);
        String replacement = options.getReplacement().getValue();
        try {
            name = options.getReplacing().doReplace(name, replacement);
        } catch (Exception e) {
            LOGGER.error(e.toString(), new JobSchedulerException(SOSVfs_E_0150.get(), e));
            throw new JobSchedulerException(SOSVfs_E_0150.get(), e);
        }
        return name;
    }

    public long getBytesTransferred() {
        return bytesTransferred;
    }

    public void setBytesTransferred(final long val) {
        bytesTransferred = val;
        LOGGER.info(SOSVfs_D_0112.params(val));
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

    public void setOptions(final ISOSFtpOptions val) {
        options = (SOSFTPOptions) val;
    }

    private void raiseException(final String msg) {
        setTransferStatus(TransferStatus.transfer_aborted);
        LOGGER.error(msg);
        throw new JobSchedulerException(msg);
    }

    public void renameSourceFile() {
        renameSourceFile(sourceFileTransfer.getFileHandle(sourceFileName));
    }

    private void renameSourceFile(final ISOSVirtualFile sourceFile) {
        if (sourceFileNameRenamed != null) {
            try {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[sourceFile=%s][sourceFileNameRenamed=%s]", sourceFile.getName(), sourceFileNameRenamed));
                }

                ISOSVirtualFile renamedSourceFile = sourceFileTransfer.getFileHandle(sourceFileNameRenamed);
                if (renamedSourceFile.fileExists()) {
                    renamedSourceFile.delete();
                }
                if (sourceFileNameRenamed.contains("/") && options.makeDirs.isTrue()) {
                    String parent = sourceFileNameRenamed.replaceFirst("[^/]*$", "");
                    sourceFileTransfer.mkdir(parent);
                }
                sourceFile.rename(sourceFileNameRenamed);
            } catch (Exception e) {
                throw new JobSchedulerException(SOSVfs_E_0150.get(), e);
            }
        }
    }

    public void rollbackRenameSourceFile() {
        if (sourceFileNameRenamed != null) {
            try {
                ISOSVirtualFile sourceFile = sourceFileTransfer.getFileHandle(sourceFileName);
                ISOSVirtualFile sourceFileRenamed = sourceFileTransfer.getFileHandle(sourceFileNameRenamed);
                if (!sourceFile.fileExists() && sourceFileRenamed.fileExists()) {
                    sourceFileRenamed.rename(sourceFileName);
                }
            } catch (Exception e) {
                throw new JobSchedulerException(SOSVfs_E_0150.get(), e);
            }
        }
    }

    public void renameTargetFile() {
        if (!skipTransfer()) {
            renameTargetFile(targetFileTransfer.getFileHandle(makeFullPathName(options.targetDir.getValue(), targetFileName)));
        }
    }

    private void renameTargetFile(ISOSVirtualFile targetFile) {
        String targetFileNewName = targetFile.getName();
        targetFileNewName = resolveDotsInPath(targetFileNewName);
        boolean equals = fileNamesAreEqual(targetTransferFile.getName(), targetFileNewName, false);
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[renameTargetFile][%s][%s]equals=%s", targetTransferFile.getName(), targetFileNewName, equals));
        }
        if (!equals) {
            try {
                if (options.overwriteFiles.isTrue() && targetFile.fileExists()) {
                    targetFileExists = true;
                    targetFile.delete();
                }
                if (targetFileNewName.contains("/") && options.makeDirs.isTrue()) { // sosftp-158
                    String parent = targetFileNewName.replaceFirst("[^/]*$", "");
                    targetFileTransfer.mkdir(parent);
                }
                targetTransferFile.rename(targetFileNewName);
            } catch (Exception e) {
                throw new JobSchedulerException(SOSVfs_E_0150.get(), e);
            }
        }
    }

    private String replaceVariables(final String value) {
        if (isTraceEnabled) {
            LOGGER.trace(String.format("[replaceVariables]%s", value));
        }
        EntryPaths targetFile = new EntryPaths(options.targetDir.getValue(), resolveDotsInPath(makeFullPathName(options.targetDir.getValue(),
                targetFileName)));
        EntryPaths targetTransferFile = new EntryPaths(options.targetDir.getValue(), resolveDotsInPath(makeFullPathName(options.targetDir.getValue(),
                targetTransferFileName)));

        EntryPaths sourceFile = new EntryPaths(options.sourceDir.getValue(), resolveDotsInPath(sourceFileName));
        EntryPaths sourceFileRenamed = new EntryPaths(options.sourceDir.getValue(), sourceFileNameRenamed);

        Properties vars = options.getTextProperties();
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

        return options.replaceVars(value);
    }

    @Override
    public void run() {
        boolean isNewConnectionUsed = false;
        try {
            isNewConnectionUsed = false;
            if (sourceFileTransfer == null) {
                setSourceFileTransfer((ISOSVfsFileTransfer) sourceConnectionPool.getUnused());
            }
            ISOSVirtualFile sourceFile = sourceFileTransfer.getFileHandle(sourceFileName);
            if (sourceFile.notExists()) {
                throw new JobSchedulerException(SOSVfs_E_226.params(sourceFileName));
            }
            if (targetFileTransfer == null && options.isNeedTargetClient()) {
                setTargetFileTransfer((ISOSVfsFileTransfer) targetConnectionPool.getUnused());
            }
            if (targetFileTransfer != null && options.isNeedTargetClient()) {
                String sourceDir = options.sourceDir.getValue();
                String targetDir = options.targetDir.getValue();
                File parentFile = null;
                String subPath = "";
                if (options.recursive.value()) {
                    if (sourceFile.getParentVfs() != null && sourceFile.getParentVfsFile().isDirectory()) {
                        subPath = sourceFileName.substring(sourceDir.length());
                        parentFile = new File(subPath).getParentFile();
                        if (parentFile != null) {
                            subPath = adjustFileSeparator(subPath);
                            subPath = subPath.substring(0, subPath.length() - new File(sourceFileName.toString()).getName().length() - 1);
                            targetFileTransfer.mkdir(targetDir + "/" + subPath);
                        } else {
                            subPath = "";
                        }
                    }
                }
                getTargetFile();
            }

            setRenamedSourceFilename();

            if (options.transactional.value()) {
                setTransactional();
            }
            switch (options.operation.value()) {
            case getlist:
                return;
            case delete:
                executePreCommands(false);
                sourceFile.delete();
                LOGGER.info(SOSVfs_I_0113.params(sourceFileName));
                this.setStatus(TransferStatus.deleted);
                return;
            case rename:
                File file = new File(sourceFileName);
                String parent = changeBackslashes(normalized(file.getParent()));
                String fileNameRenamed = makeFileNameReplacing(file.getName());
                if (fileNameRenamed.contains("/") && options.makeDirs.isTrue()) {
                    sourceFileTransfer.mkdir(normalized(new File(fileNameRenamed).getParent()));
                }
                fileNameRenamed = changeBackslashes(addFileSeparator(parent) + fileNameRenamed);
                LOGGER.info(SOSVfs_I_150.params(sourceFileName, fileNameRenamed));
                targetFileName = fileNameRenamed;
                sourceFile.rename(fileNameRenamed);
                setStatus(TransferStatus.renamed);
                return;
            default:
                break;
            }
            ISOSVirtualFile targetFile = targetFileTransfer.getFileHandle(makeFullPathName(options.targetDir.getValue(), targetFileName));
            if (options.cumulateFiles.isTrue()) {
                if (options.cumulativeFileDelete.isTrue() && !options.flgCumulativeTargetDeleted) {
                    targetFile.delete();
                    options.flgCumulativeTargetDeleted = true;
                    LOGGER.debug(String.format("cumulative file '%1$s' deleted.", targetFileName));
                }
            }
            targetFile.setModeAppend(options.appendFiles.value());
            targetFile.setModeRestart(options.resumeTransfer.value());
            if (!fileNamesAreEqual(targetFileName, targetTransferFileName, false)) {
                targetTransferFile = targetFileTransfer.getFileHandle(makeFullPathName(options.targetDir.getValue(), targetTransferFileName));
            } else {
                targetTransferFile = targetFile;
            }
            if (options.cumulateFiles.isTrue()) {
                targetTransferFile.setModeAppend(options.appendFiles.value());
                targetTransferFile.setModeRestart(options.resumeTransfer.value());
            }
            sourceTransferFileName = getFileNameWithoutPath(sourceTransferFileName);
            sourceTransferFile = sourceFileTransfer.getFileHandle(makeFullPathName(getPathWithoutFileName(sourceFileName), sourceTransferFileName));
            if (transferStatus.equals(TransferStatus.ignoredDueToZerobyteConstraint)) {
                String strM = SOSVfs_D_0110.params(sourceFileName);
                LOGGER.debug(strM);
                JADE_REPORT_LOGGER.info(strM);
            }

            if (targetFile.fileExists()) {
                targetFileExists = true;
                if (options.isDoNotOverwrite()) {
                    LOGGER.debug(SOSVfs_E_228.params(targetFileName));
                    setNotOverwritten();
                }
            }

            if (skipTransfer()) {
                executePreCommands(true);
            } else {

                doTransfer(sourceTransferFile, targetTransferFile);

                if (isDebugEnabled) {
                    LOGGER.debug("[sourceFileModificationDateTime]" + sourceFileModificationDateTime);
                }

                if (options.keepModificationDate.isTrue()) {
                    if (sourceFileModificationDateTime != -1) {
                        targetFile.setModificationDateTime(sourceFileModificationDateTime);
                    }
                }
                if ((options.isAtomicTransfer() || options.isReplaceReplacingInEffect()) && options.transactional.isFalse()) {
                    renameTargetFile(targetFile);
                }
            }
            if (options.transactional.isFalse()) {
                createTargetChecksumFile(targetFile.getName());
                if (sourceFileNameRenamed != null) {
                    renameSourceFile(sourceFile);
                }
            }
        } catch (JobSchedulerException e) {
            LOGGER.error(SOSVfs_E_229.params(e));
            throw e;
        } catch (Exception e) {
            String msg = SOSVfs_E_229.params(e);
            LOGGER.error(msg);
            throw new JobSchedulerException(msg, e);
        } finally {
            try {
                if (sourceFileTransfer != null) {
                    sourceFileTransfer.getHandler().release();
                }
                if (targetFileTransfer != null) {
                    targetFileTransfer.getHandler().release();
                }
                if (isNewConnectionUsed) {
                    if (sourceFileTransfer != null) {
                        sourceFileTransfer.logout();
                        sourceFileTransfer.disconnect();
                    }
                    if (targetFileTransfer != null) {
                        targetFileTransfer.logout();
                        targetFileTransfer.disconnect();
                    }
                }
            } catch (IOException e) {
                //
            }
        }
    }

    public void setSourceConnectionPool(final SOSVfsConnectionPool val) {
        sourceConnectionPool = val;
    }

    public void setTargetConnectionPool(final SOSVfsConnectionPool val) {
        targetConnectionPool = val;
    }

    public void setSourceFileTransfer(final ISOSVfsFileTransfer fileTransfer) {
        sourceFileTransfer = fileTransfer;
        sourceFileModificationDateTime = -1;
        if (sourceFileTransfer != null) {
            ISOSVirtualFile file = sourceFileTransfer.getFileHandle(sourceFileName);
            if (file != null) {
                sourceFileModificationDateTime = file.getModificationDateTime();
            }
        }
    }

    public void setTargetFileTransfer(final ISOSVfsFileTransfer val) {
        targetFileTransfer = val;
    }

    public void setNoOfBytesTransferred(final long bytes) {
        bytesTransferred = bytes;
        SOSConnection2Options connectionOptions = options.getConnectionOptions();
        if (!(options.checkSize.isFalse() || options.compressFiles.isTrue() || options.transferMode.isAscii() || connectionOptions
                .getSource().transferMode.isAscii() || connectionOptions.getTarget().transferMode.isAscii())) {
            if (sourceFileSize <= 0) {
                sourceFileSize = sourceFileTransfer.getFileHandle(sourceFileName).getFileSize();
            }
            if (sourceFileSize != bytesTransferred) {
                LOGGER.error(SOSVfs_E_216.params(bytesTransferred, sourceFileSize, sourceFileName));
                setStatus(TransferStatus.transfer_aborted);
                throw new JobSchedulerException(SOSVfs_E_271.get());
            }
        }
        setStatus(TransferStatus.transferred);
    }

    private boolean skipTransfer() {
        return transferStatus.equals(TransferStatus.notOverwritten) || transferStatus.equals(TransferStatus.transfer_skipped) || transferStatus
                .equals(TransferStatus.ignoredDueToZerobyteConstraint);
    }

    public void setNotOverwritten() {
        transferStatus = TransferStatus.notOverwritten;
        transferEnd = now();
        // LOGGER.warn(SOSVfs_D_0111.params(strSourceFileName));
        LOGGER.warn(String.format("[skipped][due to overwrite constraint]Source=%s", sourceFileName));
    }

    public boolean isNotOverwritten() {
        return transferStatus.equals(TransferStatus.notOverwritten);
    }

    public void setFileList(final SOSFileList list) {
        fileList = list;
        sourceFileTransfer = fileList.sourceFileTransfer;
        targetFileTransfer = fileList.targetFileTransfer;
        if (sourceFileTransfer != null) {
            setSourceFileProperties(sourceFileTransfer.getFileHandle(sourceFileName));
        }
    }

    public void setSourceFileProperties(ISOSVirtualFile file) {
        sourceFileSize = file.getFileSize();
        sourceFileModificationDateTime = file.getModificationDateTime();
    }

    public void setStatus(final TransferStatus val) {
        String msg = "";
        transferStatus = val;
        switch (val) {
        case transferInProgress:
            msg = SOSVfs_I_0114.get();
            break;
        case waiting4transfer:
            msg = SOSVfs_I_0115.get();
            break;
        case transfer_skipped:
            transferEnd = now();
            msg = SOSVfs_I_0116.get();
            break;
        case transferring:
            msg = SOSVfs_I_0117.get();
            transferStart = now();
            break;
        case transferred:
            msg = SOSVfs_I_0118.get();
            transferEnd = now();
            break;
        case transfer_aborted:
            msg = SOSVfs_I_0119.get();
            transferEnd = now();
            break;
        default:
            break;
        }
        if (isNotEmpty(msg)) {
            switch (val) {
            case transferInProgress:
                break;
            default:
                msg = SOSVfs_I_0120.params(msg, sourceFileName);
                break;
            }
        }
    }

    public void setStatusEndTransfer() {
        setStatus(TransferStatus.transferred);
    }

    public void setStatusEndTransfer(final long val) {
        setNoOfBytesTransferred(val);
        setStatusEndTransfer();
    }

    public void setStatusStartTransfer() {
        setStatus(TransferStatus.transferring);
    }

    public void setSourceFileSteady(final boolean val) {
        sourceFileSteady = val;
    }

    public void setTransferProgress(final long val) {
        setStatus(TransferStatus.transferInProgress);
    }

    public void setTransferSkipped() {
        transferStatus = TransferStatus.transfer_skipped;
        transferEnd = now();
        String msg = SOSVfs_D_0110.params(sourceFileName);
        LOGGER.debug(msg);
        JADE_REPORT_LOGGER.info(msg);
    }

    public void setIgnoredDueToZerobyteConstraint() {
        transferStatus = TransferStatus.ignoredDueToZerobyteConstraint;
        transferEnd = now();
    }

    public boolean isSourceFileExists() {
        try {
            return sourceFileTransfer.getFileHandle(sourceFileName).fileExists();
        } catch (Exception e) {
            //
        }
        return false;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public String getSourceTransferFileName() {
        return sourceTransferFileName;
    }

    public String getTargetFileName() {
        if (targetFileName == null || targetFileName.isEmpty()) {
            return getSourceFilename();
        }
        return targetFileName;
    }

    public String getTargetTransferFileName() {
        return targetTransferFileName;
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
        String mandator = options.mandator.getValue(); // 0-
        String pid = "0";
        try {
            pid = ManagementFactory.getRuntimeMXBean().getName();
            String[] arr = pid.split("@");
            pid = arr[0];
        } catch (Exception e) {
            pid = "0";
        }
        String ppid = System.getProperty(FIELD_PPID, "0");
        String operation = options.operation.getValue();
        SOSConnection2OptionsAlternate source = options.getConnectionOptions().getSource();
        if (source.alternateOptionsUsed.isTrue()) {
            source = source.getAlternatives();
        }
        SOSConnection2OptionsAlternate target = options.getConnectionOptions().getTarget();
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
        String local_filename = getSourceFilename();
        local_filename = adjustFileSeparator(local_filename);
        String remote_filename = getTargetFilename();
        if (isEmpty(remote_filename)) {
            remote_filename = "";
        } else {
            remote_filename = adjustFileSeparator(remote_filename);
        }
        String fileSize = String.valueOf(getFileSize());
        String last_error_message = "";
        if (!isEmpty(errorMessage)) {
            if (recordType.equals(HistoryRecordType.XML)) {
                last_error_message = normalizeErrorMessageForXml(errorMessage);
            } else if (recordType.equals(HistoryRecordType.CSV)) {
                last_error_message = normalizeErrorMessageForCSV(errorMessage);
            }
        }
        String log_filename = options.logFilename.getValue();
        String jump_host = options.jumpHost.getValue();
        String jump_user = options.jumpUser.getValue();
        String jump_host_ip = "";
        String jump_port = "";
        String jump_protocol = "";
        if (!isEmpty(jump_host) && !isEmpty(jump_user)) {
            jump_host_ip = options.jumpHost.getHostAdress();
            jump_port = options.jumpPort.getValue();
            jump_protocol = options.jumpProtocol.getValue();
        }
        Date endTime = getEndTime();
        Date startTime = getStartTime();
        if (startTime == null) {
            startTime = now();
        }
        if (endTime == null) {
            endTime = startTime;
        }
        fileAttributes.put(FIELD_GUID, guid);
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
        fileAttributes.put(FIELD_MD5, getMd5());
        String status = getStatusText();
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
            if ("copyToInternet".equals(options.getDmzOptions().get("operation"))) {
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
            } else if ("copyFromInternet".equals(options.getDmzOptions().get("operation"))) {
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
        String msg;
        try {
            msg = SOSVfs_D_214.params(getTargetFileNameAndPath(), getSourceFileName(), getBytesTransferred(), options.operation.getValue());
        } catch (RuntimeException e) {
            LOGGER.error(e.toString());
            msg = "???";
        }
        return msg;
    }

    public void setTransferStatus(final TransferStatus val) {
        setStatus(val);
    }

    public void setVfsHandler(final ISOSVfsFileTransfer val) {
        fileTransfer = val;
    }

    private boolean fileNamesAreEqual(String filenameA, String filenameB, boolean caseSensitiv) {
        String a = filenameA.replaceAll("[\\\\/]+", "/");
        String b = filenameB.replaceAll("[\\\\/]+", "/");
        return caseSensitiv ? a.equals(b) : a.equalsIgnoreCase(b);
    }

    public boolean hasTargetChecksumFile() {
        return targetChecksumFile != null;
    }

    public ISOSVirtualFile getTargetChecksumFile() {
        return targetChecksumFile;
    }

    private String getTransferTimeAsString(Date time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setLenient(false);
        return formatter.format(time.getTime());
    }

    public void setJumpHistoryRecord(Map<String, String> val) {
        jumpHistoryRecord = val;
    }

    private Buffer compress(byte[] dataToCompress) {
        return compress(dataToCompress, dataToCompress.length);
    }

    private Buffer compress(byte[] dataToCompress, int length) {
        Buffer buf = new Buffer();
        if (options.compressFiles.isTrue()) {
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

    public long getSourceFileModificationdateTime() {
        return sourceFileModificationDateTime;
    }

    public void setEventHandler(IJobSchedulerEventHandler val) {
        eventHandler = val;
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
                        Path pdfn = parentDir.getFileName();
                        if (pdfn != null) {
                            parentDirBaseName = pdfn.toString();
                        }
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
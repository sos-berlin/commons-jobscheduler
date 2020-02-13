package com.sos.VirtualFileSystem.DataElements;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.interfaces.ISOSFtpOptions;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.IJadeTransferDetailHistoryData;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSConnection2Options;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.common.SOSFileEntry;
import com.sos.VirtualFileSystem.common.SOSVfsConstants;
import com.sos.VirtualFileSystem.common.SOSVfsEnv;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;
import com.sos.i18n.annotation.I18NResourceBundle;

import sos.util.SOSDate;
import sos.util.SOSString;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSFileListEntry extends SOSVfsMessageCodes implements Runnable, IJadeTransferDetailHistoryData {

    public enum TransferStatus {
        transferUndefined, waiting4transfer, transferring, transferInProgress, transferred, transfer_skipped, transfer_has_errors, transfer_aborted, compressed, notOverwritten, deleted, renamed, ignoredDueToZerobyteConstraint, setBack, polling, moved
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSFileListEntry.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final boolean isTraceEnabled = LOGGER.isTraceEnabled();
    private static final Logger JADE_REPORT_LOGGER = LoggerFactory.getLogger(VFSFactory.getLoggerName());

    private static String ENV_VAR_FILE_TRANSFER_STATUS = "YADE_FILE_TRANSFER_STATUS";
    private static String ENV_VAR_FILE_IS_TRANSFERRED = "YADE_FILE_IS_TRANSFERRED";

    private TransferStatus transferStatus = TransferStatus.transferUndefined;
    private SOSFileList parent = null;
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

    private boolean transactional = false;// ? not used???
    private long bytesTransferred = 0;
    private SOSFileEntry entry;
    private Instant startTime;
    private Instant endTime;

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

    public SOSFileListEntry(final SOSFileEntry val) {
        this("", val.getFullPath(), 0);
        entry = val;
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
        parent.getSourceClient().getFileHandle(sourceFileName).delete(true);
        String msg = SOSVfs_I_0113.params(sourceFileName);
        LOGGER.info(msg);
        JADE_REPORT_LOGGER.info(msg);
    }

    private boolean doTransfer(ISOSVirtualFile source, String fileHandlerSourceFileName, ISOSVirtualFile target, String fileHandlerTargetFileName) {
        boolean closed = false;
        if (target == null) {
            setTransferStatus(TransferStatus.transfer_aborted);

            String msg = SOSVfs_E_273.params("Target");
            LOGGER.error(msg);
            throw new JobSchedulerException(msg);
        }
        if (source == null) {
            setTransferStatus(TransferStatus.transfer_aborted);

            String msg = SOSVfs_E_273.params("Source");
            LOGGER.error(msg);
            throw new JobSchedulerException(msg);
        }

        MessageDigest targetChecksum = null;
        boolean targetCreateIntegrityHashFile = parent.getOptions().createIntegrityHashFile.isTrue();
        if (targetCreateIntegrityHashFile) {
            try {
                targetChecksum = MessageDigest.getInstance(parent.getOptions().integrityHashType.getValue());
            } catch (NoSuchAlgorithmException e1) {
                LOGGER.error(e1.toString());
                parent.getOptions().createIntegrityHashFile.value(false);
                targetCreateIntegrityHashFile = false;
            }
        }

        MessageDigest sourceChecksum = null;
        boolean sourceCheckIntegrityHash = parent.getOptions().checkIntegrityHash.isTrue();
        if (sourceCheckIntegrityHash) {
            try {
                sourceChecksum = MessageDigest.getInstance(parent.getOptions().integrityHashType.getValue());
            } catch (NoSuchAlgorithmException e1) {
                LOGGER.error(e1.toString());
                parent.getOptions().checkIntegrityHash.value(false);
                sourceCheckIntegrityHash = false;
            }
        }
        executePreCommands(false);
        long totalBytesTransferred = 0;
        this.setStatus(TransferStatus.transferring);
        // send event to inform that transfer starts?

        long fileSize = entry == null ? source.getFileSize() : entry.getFilesize();
        try {
            boolean run = true;
            int count = 0;
            int cumulativeFileSeperatorLength = 0;

            while (run) {
                count++;
                totalBytesTransferred = 0;

                try {
                    startTime = Instant.now();
                    cumulativeFileSeperatorLength = 0;
                    byte[] buffer = new byte[parent.getOptions().bufferSize.value()];
                    int bytesTransferred;
                    synchronized (this) {
                        if (parent.getOptions().cumulateFiles.isTrue() && parent.getOptions().cumulativeFileSeparator.isNotEmpty()) {
                            String fs = parent.getOptions().cumulativeFileSeparator.getValue();
                            fs = this.replaceVariables(fs) + System.getProperty("line.separator");
                            byte[] bytes = fs.getBytes();
                            cumulativeFileSeperatorLength = bytes.length;
                            Buffer compressedBytes = compress(bytes);
                            target.write(compressedBytes.getBytes());
                            if (sourceCheckIntegrityHash) {
                                sourceChecksum.update(bytes);
                            }
                            if (targetCreateIntegrityHashFile) {
                                targetChecksum.update(compressedBytes.getBytes());
                            }
                        }
                        if (fileSize <= 0) {
                            byte[] bytes = new byte[0];
                            Buffer compressedBytes = compress(bytes);
                            target.write(compressedBytes.getBytes());
                            if (sourceCheckIntegrityHash) {
                                sourceChecksum.update(bytes);
                            }
                            if (targetCreateIntegrityHashFile) {
                                targetChecksum.update(compressedBytes.getBytes());
                            }
                            if (parent.getEventHandler() != null) {
                                Map<String, String> values = new HashMap<String, String>();
                                values.put("sourcePath", this.getSourceFilename());
                                values.put("targetPath", this.getTargetFilename());
                                values.put("state", "5");
                                parent.getEventHandler().updateDb(null, "YADE_FILE", values);
                            }
                        } else {
                            // int actualBytesTransferred = 0;
                            if (parent.getEventHandler() != null) {
                                Map<String, String> values = new HashMap<String, String>();
                                values.put("sourcePath", this.getSourceFilename());
                                values.put("targetPath", this.getTargetFilename());
                                values.put("state", "5");
                                parent.getEventHandler().updateDb(null, "YADE_FILE", values);
                            }
                            while ((bytesTransferred = source.read(buffer)) != -1) {
                                try {
                                    // actualBytesTransferred += bytesTransferred;
                                    Buffer compressedBytes = compress(buffer, bytesTransferred);
                                    target.write(compressedBytes.getBytes(), 0, compressedBytes.getLength());
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
                                    if (parent.getEventHandler() != null) {
                                        Map<String, String> values = new HashMap<String, String>();
                                        values.put("sourcePath", this.getSourceFilename());
                                        values.put("state", "7");
                                        values.put("errorMessage", e.getMessage());
                                        updateDb(null, "YADE_FILE", values);
                                    }
                                    setEntryErrorMessage(parent.getSourceClient(), parent.getTargetClient(), e);
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
                    source.closeInput();
                    target.closeOutput();
                    closed = true;
                    run = false;
                    endTime = Instant.now();
                } catch (Exception e) {
                    // e.printStackTrace();
                    if (count == parent.getOptions().connection_error_max_retry.value()) {
                        LOGGER.error(String.format("[%s]%s", fileHandlerSourceFileName, e.toString()), e);
                        run = false;
                        throw e;
                    } else {
                        JobSchedulerException.LastErrorMessage = "";
                    }
                    parent.setLastErrorMessage(e.toString());
                    LOGGER.warn(String.format("[%s]%s", fileHandlerSourceFileName, e.toString()), e);
                    if (!closed) {
                        source.closeInput();
                        target.closeOutput();
                        closed = true;
                    }

                    ISOSVirtualFile vfs = tryReconnect("source", parent.getSourceClient(), parent.getOptions().getSource(), fileHandlerSourceFileName,
                            count);
                    if (vfs != null) {
                        source = vfs;
                    }
                    ISOSVirtualFile vft = tryReconnect("target", parent.getTargetClient(), parent.getOptions().getTarget(), fileHandlerTargetFileName,
                            count);
                    if (vft != null) {
                        target = vft;
                    }
                    if (vfs == null && vft == null) {
                        throw e;
                    }
                    // TODO check file after rerun- should be deleted or will overridden?
                }
            }

            LOGGER.info(String.format("[transferred]Source=%s, Target=%s, Bytes=%s %s", source.getName(), target.getName(), totalBytesTransferred,
                    getDateTimeInfos(startTime, endTime)));
            // LOGGER.info(String.format("[transferred]Source=%s, Target=%s, Bytes=%s %s", fileHandlerSourceFileName, fileHandlerTargetFileName,
            // totalBytesTransferred, getDateTimeInfos(startTime, endTime)));

            if (parent.getTargetClient().isNegativeCommandCompletion()) {
                setTransferStatus(TransferStatus.transfer_aborted);
                String msg = SOSVfs_E_175.params(targetTransferFile.getName(), parent.getTargetClient().getReplyString());
                LOGGER.error(msg);
                throw new JobSchedulerException(msg);
            }
            if (targetCreateIntegrityHashFile) {
                targetFileChecksum = toHexString(targetChecksum.digest());
                sourceFileChecksum = targetFileChecksum;
                LOGGER.debug(SOSVfs_I_274.params(targetFileChecksum, targetFileName, parent.getOptions().integrityHashType.getValue()));
            }
            if (sourceCheckIntegrityHash) {
                sourceFileChecksum = toHexString(sourceChecksum.digest());
                LOGGER.debug(SOSVfs_I_274.params(sourceFileChecksum, sourceFileName, parent.getOptions().integrityHashType.getValue()));
            }
            setNoOfBytesTransferred(totalBytesTransferred, fileSize);
            totalBytesTransferred += cumulativeFileSeperatorLength;
            checkSourceChecksumFile(source, target);
            executeTFNPostCommnands();
            return true;
        } catch (JobSchedulerException e) {
            e.printStackTrace();
            setEntryErrorMessage(parent.getSourceClient(), parent.getTargetClient(), e);
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            parent.setLastErrorMessage(e.toString());
            throw new JobSchedulerException(e);
        } finally {
            if (endTime == null) {
                endTime = Instant.now();
            }
            if (!closed) {
                source.closeInput();
                target.closeOutput();
                closed = true;
            }
        }
    }

    private ISOSVirtualFile tryReconnect(String range, ISOSVfsFileTransfer transfer, SOSConnection2OptionsAlternate opt, String fileName, int count) {
        if (transfer.isConnected()) {
            return null;
        }

        boolean run = true;
        while (run) {
            try {
                LOGGER.info("----------------------------------------------------");
                LOGGER.info(String.format("[%s][%s] connection lost. wait %ss and try reconnect %s of %s ...", range, fileName, parent
                        .getOptions().connection_error_retry_wait_interval.value(), count, parent.getOptions().connection_error_max_retry.value()));
                LOGGER.info("----------------------------------------------------");
                if (parent.getOptions().connection_error_retry_wait_interval.value() > 0) {
                    try {
                        Thread.sleep(parent.getOptions().connection_error_retry_wait_interval.value() * 1_000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                transfer.reconnect(opt);
                return transfer.getFileHandle(fileName);
            } catch (Throwable e) {
                LOGGER.warn(e.toString(), e);
                if (count == parent.getOptions().connection_error_max_retry.value()) {
                    run = false;
                }
                count++;
            }
        }
        return null;
    }

    // private void sendEvent(Map<String, Map<String, String>> eventParams) {
    // if (eventHandler != null && eventParams != null) {
    // eventHandler.sendEvent(eventParams);
    // }
    // }

    private void updateDb(Long id, String type, Map<String, String> values) {
        if (parent.getEventHandler() != null) {
            // id of the DBItem
            // type of the Item, here always YADE_FILE
            // map of values of the Item, with key = propertyName and value = propertyValue
            parent.getEventHandler().updateDb(id, type, values);
        }
    }

    public Instant getStartTime() {
        return startTime;
    }

    @Override
    public Instant getEndTime() {
        return endTime;
    }

    public void createTargetChecksumFile() {
        createTargetChecksumFile(makeFullPathName(parent.getOptions().targetDir.getValue(), targetFileName));
    }

    public void createTargetChecksumFile(String targetFileName) {
        if (parent.getOptions().createIntegrityHashFile.isTrue() && isTransferred()) {
            ISOSVirtualFile checksumFile = null;
            try {
                targetFileName = resolveDotsInPath(targetFileName);
                checksumFile = parent.getTargetClient().getFileHandle(targetFileName + "." + parent.getOptions().integrityHashType.getValue());
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
        if (parent.getOptions().checkIntegrityHash.isTrue()) {
            ISOSVirtualFile sourceFileChecksumFile = null;
            String sourceFileChecksumFileName = sourceFile.getName() + "." + parent.getOptions().securityHashType.getValue();
            try {
                sourceFileChecksumFile = parent.getSourceClient().getFileHandle(sourceFileChecksumFileName);
                if (sourceFileChecksumFile.fileExists()) {
                    String checksum = sourceFileChecksumFile.file2String().trim();
                    if (!checksum.equals(sourceFileChecksum)) {
                        try {
                            if (targetFile.fileExists()) {
                                targetFile.delete(false);
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

    private void setEntryErrorMessage(ISOSVfsFileTransfer sourceTransfer, ISOSVfsFileTransfer targetTransfer, JobSchedulerException ex) {
        StringBuilder msg = new StringBuilder();
        if (sourceTransfer != null && !sourceTransfer.isConnected()) {
            msg.append("[source not connected]");
        }
        if (targetTransfer != null && !targetTransfer.isConnected()) {
            msg.append("[target not connected]");
        }
        if (isEmpty(JobSchedulerException.LastErrorMessage)) {
            msg.append(ex.toString());
            if (ex.getNestedException() != null) {
                msg.append(" ").append(ex.getNestedException().toString());
            }
        } else {
            msg.append(JobSchedulerException.LastErrorMessage);
        }
        parent.setLastErrorMessage(msg.toString());
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
        SOSConnection2OptionsAlternate target = parent.getOptions().getConnectionOptions().getTarget();
        if (target.alternateOptionsUsed.isTrue()) {
            executeCommands("alternative_target_tfn_post_command", parent.getTargetClient(), target.getAlternatives().tfnPostCommand, target
                    .getAlternatives().commandDelimiter);
        } else {
            executeCommands("tfn_post_command", parent.getTargetClient(), parent.getOptions().tfnPostCommand);
            executeCommands("target_tfn_post_command", parent.getTargetClient(), target.tfnPostCommand, target.commandDelimiter);
        }
        SOSConnection2OptionsAlternate source = parent.getOptions().getConnectionOptions().getSource();
        if (source.alternateOptionsUsed.isTrue()) {
            executeCommands("alternative_source_tfn_post_command", parent.getSourceClient(), source.getAlternatives().tfnPostCommand, source
                    .getAlternatives().commandDelimiter);
        } else {
            executeCommands("source_tfn_post_command", parent.getSourceClient(), source.tfnPostCommand, source.commandDelimiter);
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

        SOSConnection2OptionsAlternate target = parent.getOptions().getConnectionOptions().getTarget();
        if (target.alternateOptionsUsed.isTrue()) {
            if (!isTransferred && target.getAlternatives().post_command_disable_for_skipped_transfer.value()) {
                LOGGER.info(String.format("[alternative_target_post_command][skip]disable_for_skipped_transfer=true, status=%s", status));
            } else {
                executeCommands("alternative_target_post_command", parent.getTargetClient(), target.getAlternatives().postCommand, target
                        .getAlternatives().commandDelimiter, envs);
            }
        } else {
            if (!isTransferred && parent.getOptions().post_command_disable_for_skipped_transfer.value()) {
                LOGGER.info(String.format("[post_command][skip]disable_for_skipped_transfer=true, status=%s", status));
            } else {
                executeCommands("post_command", parent.getTargetClient(), parent.getOptions().postCommand, target.commandDelimiter, envs);
            }
            if (!isTransferred && target.post_command_disable_for_skipped_transfer.value()) {
                LOGGER.info(String.format("[target_post_command][skip]disable_for_skipped_transfer=true, status=%s", status));
            } else {
                executeCommands("target_post_command", parent.getTargetClient(), target.postCommand, target.commandDelimiter, envs);
            }
        }
        SOSConnection2OptionsAlternate source = parent.getOptions().getConnectionOptions().getSource();
        if (source.alternateOptionsUsed.isTrue()) {
            if (!isTransferred && source.getAlternatives().post_command_disable_for_skipped_transfer.value()) {
                LOGGER.info(String.format("[alternative_source_post_command][skip]disable_for_skipped_transfer=true, status=%s", status));
            } else {
                executeCommands("alternative_source_post_command", parent.getSourceClient(), source.getAlternatives().postCommand, source
                        .getAlternatives().commandDelimiter, envs);
            }
        } else {
            if (!isTransferred && source.post_command_disable_for_skipped_transfer.value()) {
                LOGGER.info(String.format("[source_post_command][skip]disable_for_skipped_transfer=true, status=%s", status));
            } else {
                executeCommands("source_post_command", parent.getSourceClient(), source.postCommand, source.commandDelimiter, envs);
            }
        }
    }

    private void executePreCommands(boolean isSkipped) {
        SOSConnection2OptionsAlternate target = parent.getOptions().getConnectionOptions().getTarget();
        if (target.alternateOptionsUsed.isTrue()) {
            if (isSkipped) {
                if (target.getAlternatives().pre_command_enable_for_skipped_transfer.value()) {
                    executeCommands("alternative_target_pre_command enable_for_skipped_transfer=true", parent.getTargetClient(), target
                            .getAlternatives().preCommand, target.commandDelimiter);
                }
            } else {
                executeCommands("alternative_target_pre_command", parent.getTargetClient(), target.getAlternatives().preCommand,
                        target.commandDelimiter);
            }
        } else {
            if (isSkipped) {
                if (parent.getOptions().pre_command_enable_for_skipped_transfer.value()) {
                    executeCommands("pre_command enable_for_skipped_transfer=true", parent.getTargetClient(), parent.getOptions().preCommand);
                }
                if (target.pre_command_enable_for_skipped_transfer.value()) {
                    executeCommands("target_pre_command enable_for_skipped_transfer=true", parent.getTargetClient(), target.preCommand,
                            target.commandDelimiter);
                }
            } else {
                executeCommands("pre_command", parent.getTargetClient(), parent.getOptions().preCommand);
                executeCommands("target_pre_command", parent.getTargetClient(), target.preCommand, target.commandDelimiter);
            }
        }
        SOSConnection2OptionsAlternate source = parent.getOptions().getConnectionOptions().getSource();
        if (source.alternateOptionsUsed.isTrue()) {
            if (isSkipped) {
                if (source.getAlternatives().pre_command_enable_for_skipped_transfer.value()) {
                    executeCommands("alternative_source_pre_command enable_for_skipped_transfer=true", parent.getSourceClient(), source
                            .getAlternatives().preCommand, source.getAlternatives().commandDelimiter);
                }
            } else {
                executeCommands("alternative_source_pre_command", parent.getSourceClient(), source.getAlternatives().preCommand, source
                        .getAlternatives().commandDelimiter);
            }
        } else {
            if (isSkipped) {
                if (source.pre_command_enable_for_skipped_transfer.value()) {
                    executeCommands("source_pre_command enable_for_skipped_transfer=true", parent.getSourceClient(), source.preCommand,
                            source.commandDelimiter);
                }
            } else {
                executeCommands("source_pre_command", parent.getSourceClient(), source.preCommand, source.commandDelimiter);
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

    public String getFileName4ResultList() {
        String result = targetFileName;
        if (isEmpty(result)) {
            result = sourceFileName;
        }
        if (parent.getOptions().resultSetFileName.getValue().endsWith(".source.tmp")) {
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
        return parent.getOptions().getPid();
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
    public Integer getStatus() {
        return new Integer(transferStatus.ordinal());
    }

    @Override
    public String getStatusText() {
        return transferStatus.name();
    }

    public ISOSVirtualFile getTargetFile() {
        ISOSVirtualFile sourceFile = parent.getSourceClient().getFileHandle(sourceFileName);
        sourceTransferFileName = sourceFile.getName();
        targetFileName = sourceFile.getName();
        boolean recursive = parent.getOptions().recursive.value();
        if (parent.getOptions().compressFiles.isTrue()) {
            targetFileName = targetFileName + parent.getOptions().compressedFileExtension.getValue();
        }
        if (parent.getOptions().cumulateFiles.isTrue()) {
            targetFileName = parent.getOptions().cumulativeFileName.getValue();
            targetTransferFileName = targetFileName;
            parent.getOptions().appendFiles.value(true);
        } else {
            targetFileName = getFileNameWithoutPath(targetFileName);
            targetTransferFileName = targetFileName;
            if (parent.getOptions().getReplacing().isNotEmpty()) {
                try {
                    targetFileName = parent.getOptions().getReplacing().doReplace(targetFileName, parent.getOptions().getReplacement().getValue());
                } catch (Exception e) {
                    throw new JobSchedulerException(SOSVfs_E_0150.get() + " " + e.toString(), e);
                }
            }
        }
        if (parent.getOptions().isAtomicTransfer() || parent.getOptions().transactionMode.isTrue()) {
            targetTransferFileName = getTargetAtomicFileName(parent.getOptions());
        }
        if (recursive) {
            String sourceDir = getPathWithoutFileName(sourceFile.getName());
            String sourceDirOrig = parent.getOptions().sourceDir().getValue();
            if (!fileNamesAreEqual(sourceDir, sourceDirOrig, true) && sourceDir.length() > sourceDirOrig.length()) {
                String subFolder = sourceDir.substring(sourceDirOrig.length());
                subFolder = adjustFileSeparator(addFileSeparator(subFolder));
                targetFileName = targetFileName.replaceFirst("([^/]*)$", subFolder + "$1");
                targetTransferFileName = subFolder + targetTransferFileName;
                if (isNotEmpty(getTargetAtomicFileName())) {
                    setTargetAtomicFileName(targetTransferFileName);
                }
                try {
                    if (parent.add2SubFolders(subFolder)) {
                        parent.getTargetClient().mkdir(addFileSeparator(parent.getOptions().targetDir().getValue()) + subFolder);
                    }
                } catch (IOException e) {
                    throw new JobSchedulerException(e);
                }
            }
        }
        return null;
    }

    public void setRenamedSourceFilename() {
        SOSConnection2OptionsAlternate sourceOptions = parent.getOptions().getSource();
        if (sourceOptions.replacing.isDirty() && !parent.getOptions().removeFiles.value()) {
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
                    if (parent.getOptions().recursive.value()) {
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
        return parent.getOptions().targetDir.getValue() + targetFileName;
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
        String replacement = parent.getOptions().getReplacement().getValue();
        try {
            name = parent.getOptions().getReplacing().doReplace(name, replacement);
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

    public void renameSourceFile() {
        renameSourceFile(parent.getSourceClient().getFileHandle(sourceFileName));
    }

    private void renameSourceFile(final ISOSVirtualFile sourceFile) {
        if (sourceFileNameRenamed != null) {
            try {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[sourceFile=%s][sourceFileNameRenamed=%s]", sourceFile.getName(), sourceFileNameRenamed));
                }

                ISOSVirtualFile renamedSourceFile = parent.getSourceClient().getFileHandle(sourceFileNameRenamed);
                if (renamedSourceFile.fileExists()) {
                    renamedSourceFile.delete(false);
                }
                if (sourceFileNameRenamed.contains("/") && parent.getOptions().makeDirs.isTrue()) {
                    String parentName = sourceFileNameRenamed.replaceFirst("[^/]*$", "");
                    parent.getSourceClient().mkdir(parentName);
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
                ISOSVirtualFile sourceFile = parent.getSourceClient().getFileHandle(sourceFileName);
                ISOSVirtualFile sourceFileRenamed = parent.getSourceClient().getFileHandle(sourceFileNameRenamed);
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
            renameTargetFile(parent.getTargetClient().getFileHandle(makeFullPathName(parent.getOptions().targetDir.getValue(), targetFileName)));
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
                if (parent.getOptions().overwriteFiles.isTrue() && targetFile.fileExists()) {
                    targetFileExists = true;
                    targetFile.delete(false);
                }

                // TODO check - if needed
                if (targetFileNewName.contains("/") && parent.getOptions().makeDirs.isTrue()) { // sosftp-158
                    String parentName = targetFileNewName.replaceFirst("[^/]*$", "");
                    parent.getTargetClient().mkdir(parentName);
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
        EntryPaths targetFile = new EntryPaths(parent.getOptions().targetDir.getValue(), resolveDotsInPath(makeFullPathName(parent
                .getOptions().targetDir.getValue(), targetFileName)));
        EntryPaths targetTransferFile = new EntryPaths(parent.getOptions().targetDir.getValue(), resolveDotsInPath(makeFullPathName(parent
                .getOptions().targetDir.getValue(), targetTransferFileName)));

        EntryPaths sourceFile = new EntryPaths(parent.getOptions().sourceDir.getValue(), resolveDotsInPath(sourceFileName));
        EntryPaths sourceFileRenamed = new EntryPaths(parent.getOptions().sourceDir.getValue(), sourceFileNameRenamed);

        Properties vars = parent.getOptions().getTextProperties();
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

        return parent.getOptions().replaceVars(value);
    }

    @Override
    public void run() {
        try {
            ISOSVirtualFile sourceFile = parent.getSourceClient().getFileHandle(sourceFileName);
            if (parent.getOptions().isNeedTargetClient()) {
                if (parent.getOptions().recursive.value()) {
                    String sourceDir = parent.getOptions().sourceDir.getValue();
                    String targetDir = parent.getOptions().targetDir.getValue();
                    File parentFile = null;
                    String subPath = "";

                    if (sourceFile.getParentVfs() != null && sourceFile.getParentVfsFile().isDirectory()) {
                        subPath = sourceFileName.substring(sourceDir.length());
                        parentFile = new File(subPath).getParentFile();
                        if (parentFile != null) {
                            subPath = adjustFileSeparator(subPath);
                            subPath = subPath.substring(0, subPath.length() - new File(sourceFileName.toString()).getName().length() - 1);
                            parent.getTargetClient().mkdir(targetDir + "/" + subPath);
                        } else {
                            subPath = "";
                        }
                    }
                }
                getTargetFile();
            }

            setRenamedSourceFilename();

            if (parent.getOptions().transactional.value()) {
                setTransactional();
            }
            boolean removeSourceFile = false;
            switch (parent.getOptions().operation.value()) {
            case getlist:
                return;
            case delete:
                executePreCommands(false);
                sourceFile.delete(true);
                LOGGER.info(SOSVfs_I_0113.params(sourceFileName));
                this.setStatus(TransferStatus.deleted);
                return;
            case rename:
                File file = new File(sourceFileName);
                String parentName = changeBackslashes(normalized(file.getParent()));
                String fileNameRenamed = makeFileNameReplacing(file.getName());
                if (fileNameRenamed.contains("/") && parent.getOptions().makeDirs.isTrue()) {
                    parent.getSourceClient().mkdir(normalized(new File(fileNameRenamed).getParent()));
                }
                fileNameRenamed = changeBackslashes(addFileSeparator(parentName) + fileNameRenamed);
                LOGGER.info(SOSVfs_I_150.params(sourceFileName, fileNameRenamed));
                targetFileName = fileNameRenamed;
                sourceFile.rename(fileNameRenamed);
                setStatus(TransferStatus.renamed);
                return;
            case move:
                removeSourceFile = true;
                break;
            default:
                break;
            }

            String fileHandlerTargetFileName = makeFullPathName(parent.getOptions().targetDir.getValue(), targetFileName);
            ISOSVirtualFile targetFile = parent.getTargetClient().getFileHandle(fileHandlerTargetFileName);
            if (parent.getOptions().cumulateFiles.isTrue()) {
                if (parent.getOptions().cumulativeFileDelete.isTrue() && !parent.getOptions().flgCumulativeTargetDeleted) {
                    targetFile.delete(true);
                    parent.getOptions().flgCumulativeTargetDeleted = true;
                    LOGGER.debug(String.format("cumulative file '%1$s' deleted.", targetFileName));
                }
            }
            targetFile.setModeAppend(parent.getOptions().appendFiles.value());
            targetFile.setModeRestart(parent.getOptions().resumeTransfer.value());
            if (!fileNamesAreEqual(targetFileName, targetTransferFileName, false)) {
                targetTransferFile = parent.getTargetClient().getFileHandle(makeFullPathName(parent.getOptions().targetDir.getValue(),
                        targetTransferFileName));
            } else {
                targetTransferFile = targetFile;
            }
            if (parent.getOptions().cumulateFiles.isTrue()) {
                targetTransferFile.setModeAppend(parent.getOptions().appendFiles.value());
                targetTransferFile.setModeRestart(parent.getOptions().resumeTransfer.value());
            }
            sourceTransferFileName = getFileNameWithoutPath(sourceTransferFileName);
            String fileHandlerSourceFileName = makeFullPathName(getPathWithoutFileName(sourceFileName), sourceTransferFileName);
            sourceTransferFile = parent.getSourceClient().getFileHandle(fileHandlerSourceFileName);
            if (transferStatus.equals(TransferStatus.ignoredDueToZerobyteConstraint)) {
                String strM = SOSVfs_D_0110.params(sourceFileName);
                LOGGER.debug(strM);
                JADE_REPORT_LOGGER.info(strM);
            }

            if (targetFile.fileExists()) {
                targetFileExists = true;
                if (parent.getOptions().isDoNotOverwrite()) {
                    LOGGER.debug(SOSVfs_E_228.params(targetFileName));
                    setNotOverwritten();
                }
            }

            if (skipTransfer()) {
                executePreCommands(true);
            } else {

                doTransfer(sourceTransferFile, fileHandlerSourceFileName, targetTransferFile, fileHandlerTargetFileName);

                if (isDebugEnabled) {
                    if (parent.getOptions().checkSteadyStateOfFiles.isTrue()) {
                        LOGGER.debug("[sourceFileModificationDateTime]" + sourceFileModificationDateTime);
                    }
                }

                if (parent.getOptions().keepModificationDate.isTrue()) {
                    if (sourceFileModificationDateTime != -1) {
                        targetFile.setModificationDateTime(sourceFileModificationDateTime);
                    }
                }
                if ((parent.getOptions().isAtomicTransfer() || parent.getOptions().isReplaceReplacingInEffect()) && parent.getOptions().transactional
                        .isFalse()) {
                    renameTargetFile(targetFile);
                }
            }
            if (parent.getOptions().transactional.isFalse()) {
                createTargetChecksumFile(targetFile.getName());
                if (sourceFileNameRenamed != null) {
                    renameSourceFile(sourceFile);
                }

                if (removeSourceFile) {
                    sourceFile.delete(false);
                    transferStatus = TransferStatus.moved;
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
                if (parent.getSourceClient() != null) {
                    parent.getSourceClient().getHandler().release();
                }
                if (parent.getTargetClient() != null) {
                    parent.getTargetClient().getHandler().release();
                }

            } catch (Exception e) {
                //
            }
        }
    }

    public void setNoOfBytesTransferred(final long bytes, long fileSize) {
        bytesTransferred = bytes;
        SOSConnection2Options connectionOptions = parent.getOptions().getConnectionOptions();
        if (!(parent.getOptions().checkSize.isFalse() || parent.getOptions().compressFiles.isTrue() || parent.getOptions().transferMode.isAscii()
                || connectionOptions.getSource().transferMode.isAscii() || connectionOptions.getTarget().transferMode.isAscii())) {
            if (sourceFileSize <= 0) {
                sourceFileSize = fileSize;// sourceFileTransfer.getFileHandle(sourceFileName).getFileSize();
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
        endTime = Instant.now();
        LOGGER.warn(String.format("[skipped][due to overwrite constraint]Source=%s", sourceFileName));
    }

    public boolean isNotOverwritten() {
        return transferStatus.equals(TransferStatus.notOverwritten);
    }

    public void setParent(final SOSFileList list) {
        parent = list;
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
            msg = SOSVfs_I_0116.get();
            break;
        case transferring:
            msg = SOSVfs_I_0117.get();
            break;
        case transferred:
            msg = SOSVfs_I_0118.get();
            break;
        case transfer_aborted:
            msg = SOSVfs_I_0119.get();
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

    public void setStatusEndTransfer(final long val, long fileSize) {
        setNoOfBytesTransferred(val, fileSize);
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
        endTime = Instant.now();
        String msg = SOSVfs_D_0110.params(sourceFileName);
        LOGGER.debug(msg);
        JADE_REPORT_LOGGER.info(msg);
    }

    public void setIgnoredDueToZerobyteConstraint() {
        transferStatus = TransferStatus.ignoredDueToZerobyteConstraint;
        endTime = Instant.now();
    }

    public boolean isSourceFileExists() {
        try {
            return parent.getSourceClient().getFileHandle(sourceFileName).fileExists();
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
            msg = SOSVfs_D_214.params(getTargetFileNameAndPath(), getSourceFileName(), getBytesTransferred(), parent.getOptions().operation
                    .getValue(), getDateTimeInfos(startTime, endTime));
        } catch (RuntimeException e) {
            LOGGER.error(e.toString());
            msg = "???";
        }
        return msg;
    }

    public static String getDateTimeInfos(Instant start, Instant end) {
        StringBuilder sb = new StringBuilder("(");
        if (start != null) {
            sb.append(SOSDate.getDateTime(start));
        }
        sb.append("-");
        if (end != null) {
            sb.append(SOSDate.getDateTime(end));
        }
        sb.append(")");
        if (start != null && end != null) {
            sb.append(SOSDate.getDuration(start, end));
        }
        return sb.toString();
    }

    public SOSFileEntry getEntry() {
        return entry;
    }

    public void setTransferStatus(final TransferStatus val) {
        setStatus(val);
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

    private Buffer compress(byte[] dataToCompress) {
        return compress(dataToCompress, dataToCompress.length);
    }

    private Buffer compress(byte[] dataToCompress, int length) {
        Buffer buf = new Buffer();
        if (parent.getOptions().compressFiles.isTrue()) {
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

    public long getSourceFileModificationDateTime() {
        return sourceFileModificationDateTime;
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
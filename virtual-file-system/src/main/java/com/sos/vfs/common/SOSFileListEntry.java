package com.sos.vfs.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.vfs.common.interfaces.IJadeTransferDetailHistoryData;
import com.sos.vfs.common.interfaces.ISOSProvider;
import com.sos.vfs.common.interfaces.ISOSProviderFile;
import com.sos.vfs.common.options.SOSBaseOptions;
import com.sos.vfs.common.options.SOSProviderOptions;
import com.sos.vfs.common.options.SOSTransfer;
import com.sos.vfs.local.SOSLocal;
import com.sos.vfs.sftp.SOSSFTP;

import sos.util.SOSDate;
import sos.util.SOSString;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSFileListEntry extends SOSVFSMessageCodes implements Runnable, IJadeTransferDetailHistoryData {

    public enum TransferStatus {
        transferUndefined, waiting4transfer, transferring, transferInProgress, transferred, transfer_skipped, transfer_has_errors, transfer_aborted, compressed, notOverwritten, deleted, renamed, ignoredDueToZerobyteConstraint, setBack, polling, moved
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSFileListEntry.class);
    private static final Logger JADE_REPORT_LOGGER = LoggerFactory.getLogger(SOSVFSFactory.REPORT_LOGGER_NAME);

    private static String ENV_VAR_FILE_TRANSFER_STATUS = "YADE_FILE_TRANSFER_STATUS";
    private static String ENV_VAR_FILE_IS_TRANSFERRED = "YADE_FILE_IS_TRANSFERRED";
    private static String DEFAULT_CHECKSUM = "N/A";

    private TransferStatus transferStatus = TransferStatus.transferUndefined;
    private SOSFileList parent = null;
    private SOSFileEntry entry;
    private ISOSProviderFile sourceTransferFile = null;
    private ISOSProviderFile targetTransferFile = null;
    private ISOSProviderFile targetChecksumFile = null;
    private Instant startTime;
    private Instant endTime;

    private String sourceTransferFileName = null;
    private String sourceFileName = null;
    private String sourceFileNameRenamed = null;
    private String sourceFileChecksum = DEFAULT_CHECKSUM;
    private long sourceFileSize = -1L;
    private long sourceFileModificationDateTime = -1L;
    private long sourceFileLastCheckedFileSize = -1L;
    private boolean sourceFileSteady = false;

    private String targetTransferFileName = null;
    private String targetFileName = null;
    private String targetFileChecksum = DEFAULT_CHECKSUM;
    private String targetAtomicFileName = EMPTY_STRING;
    private boolean targetFileExistsBeforeTransfer = false;

    private boolean transactional = false;// ? not used???
    private long bytesTransferred = 0;
    private long transferNumber = 0;

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

    public SOSFileListEntry(final SOSFileEntry fileEntry) {
        super(SOSVFSFactory.BUNDLE_NAME);

        entry = fileEntry;
        sourceFileName = entry.getFullPath();
        sourceFileSize = entry.getFilesize();
        targetFileName = "";
        bytesTransferred = 0;
    }

    private String changeBackslashes(final String val) {
        return val.replaceAll("\\\\", "/");
    }

    public void deleteSourceFile() {
        parent.getSourceProvider().getFile(sourceFileName).delete(false);
        logSourceFileDeleted();
    }

    private void logSourceFileDeleted() {
        String msg = String.format("[%s]%s", transferNumber, SOSVfs_I_0113.params(sourceFileName));
        LOGGER.info(msg);
        JADE_REPORT_LOGGER.info(msg);
    }

    private boolean doTransfer(ISOSProviderFile source, String fileHandlerSourceFileName, ISOSProviderFile target, String fileHandlerTargetFileName)
            throws Exception {
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
        boolean targetCreateIntegrityHashFile = parent.getBaseOptions().createIntegrityHashFile.isTrue();
        if (targetCreateIntegrityHashFile) {
            try {
                targetChecksum = MessageDigest.getInstance(parent.getBaseOptions().integrityHashType.getValue());
            } catch (NoSuchAlgorithmException e1) {
                LOGGER.error(e1.toString(), e1);
                parent.getBaseOptions().createIntegrityHashFile.value(false);
                targetCreateIntegrityHashFile = false;
            }
        }

        MessageDigest sourceChecksum = null;
        boolean sourceCheckIntegrityHash = parent.getBaseOptions().checkIntegrityHash.isTrue();
        if (sourceCheckIntegrityHash) {
            try {
                sourceChecksum = MessageDigest.getInstance(parent.getBaseOptions().integrityHashType.getValue());
            } catch (NoSuchAlgorithmException e1) {
                LOGGER.error(e1.toString(), e1);
                parent.getBaseOptions().checkIntegrityHash.value(false);
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
            int retryCountSource = 0;
            int retryCountTarget = 0;
            int cumulativeFileSeperatorLength = 0;

            while (run) {
                try {
                    startTime = Instant.now();
                    totalBytesTransferred = 0;
                    cumulativeFileSeperatorLength = 0;
                    byte[] buffer = new byte[parent.getBaseOptions().bufferSize.value()];
                    int bytesTransferred;
                    synchronized (this) {
                        if (parent.getBaseOptions().cumulateFiles.isTrue() && parent.getBaseOptions().cumulativeFileSeparator.isNotEmpty()) {
                            String fs = parent.getBaseOptions().cumulativeFileSeparator.getValue();
                            fs = this.replaceVariables(fs, null) + System.getProperty("line.separator");
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
                            if (parent.getEventHandler() != null) {
                                Map<String, String> values = new HashMap<String, String>();
                                values.put("sourcePath", this.getSourceFilename());
                                values.put("targetPath", this.getTargetFilename());
                                values.put("state", "5");
                                parent.getEventHandler().updateDb(null, "YADE_FILE", values);
                            }
                            while ((bytesTransferred = source.read(buffer)) != -1) {
                                try {
                                    Buffer compressedBytes = compress(buffer, bytesTransferred);
                                    target.write(compressedBytes.getBytes(), 0, compressedBytes.getLength());
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
                                    throw e;
                                }
                                totalBytesTransferred += bytesTransferred;
                                setTransferProgress(totalBytesTransferred);
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
                } catch (Throwable e) {
                    if (parent.getRetryCountMax() < 1) {
                        throw e;
                    }
                    if (retryCountSource > 0 && retryCountSource == parent.getRetryCountMax()) {
                        throw e;
                    }
                    if (retryCountTarget > 0 && retryCountTarget == parent.getRetryCountMax()) {
                        throw e;
                    }

                    JobSchedulerException.LastErrorMessage = "";
                    parent.setLastErrorMessage(e.toString());
                    LOGGER.warn(String.format("[%s][source=%s][target=%s]%s", transferNumber, source.getName(), target.getName(), e.toString()), e);
                    if (!closed) {
                        source.closeInput();
                        target.closeOutput();
                        closed = true;
                    }

                    ReconnectResult rvfs = tryReconnect("Source", parent.getSourceProvider(), source, retryCountSource);
                    retryCountSource = rvfs.counter;
                    ISOSProviderFile vfs = rvfs.providerFile;
                    if (vfs != null) {
                        source = vfs;
                    }
                    ReconnectResult rvft = tryReconnect("Target", parent.getTargetProvider(), target, retryCountTarget);
                    retryCountTarget = rvft.counter;
                    ISOSProviderFile vft = rvft.providerFile;
                    if (vft != null) {
                        target = vft;
                    }
                    if (vfs == null && vft == null) {
                        throw e;
                    }
                }
            }
            LOGGER.info(String.format("[%s][transferred]Source=%s, Target=%s, Bytes=%s %s", transferNumber, SOSCommonProvider.normalizePath(source
                    .getName()), SOSCommonProvider.normalizePath(target.getName()), totalBytesTransferred, getDateTimeInfos(startTime, endTime)));

            if (parent.getTargetProvider().isNegativeCommandCompletion()) {
                setTransferStatus(TransferStatus.transfer_aborted);
                String msg = String.format("[%s]%s", transferNumber, SOSVfs_E_175.params(targetTransferFile.getName(), parent.getTargetProvider()
                        .getReplyString()));
                LOGGER.error(msg);
                throw new JobSchedulerException(msg);
            }
            if (targetCreateIntegrityHashFile) {
                targetFileChecksum = toHexString(targetChecksum.digest());
                sourceFileChecksum = targetFileChecksum;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[%s]%s", transferNumber, SOSVfs_I_274.params(targetFileChecksum, targetFileName, parent
                            .getBaseOptions().integrityHashType.getValue())));
                }
            }
            if (sourceCheckIntegrityHash) {
                sourceFileChecksum = toHexString(sourceChecksum.digest());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[%s]%s", transferNumber, SOSVfs_I_274.params(sourceFileChecksum, sourceFileName, parent
                            .getBaseOptions().integrityHashType.getValue())));
                }
            }
            setNoOfBytesTransferred(totalBytesTransferred, fileSize);
            totalBytesTransferred += cumulativeFileSeperatorLength;
            checkSourceChecksumFile(source, target);
            executeTFNPostCommnands();
            return true;
        } catch (JobSchedulerException e) {
            // e.printStackTrace();
            setEntryErrorMessage(parent.getSourceProvider(), parent.getTargetProvider(), e);
            throw e;
        } catch (Throwable e) {
            StringBuilder sb = new StringBuilder("[").append(transferNumber).append("]");
            if (parent.getSourceProvider() != null) {
                sb.append("[source=").append(sourceFileName).append("]");
            }
            if (parent.getTargetProvider() != null) {
                sb.append("[target=").append(targetTransferFileName).append("]");
            }
            sb.append(e.toString());
            String msg = sb.toString();
            parent.setLastErrorMessage(msg);
            throw new JobSchedulerException(msg, e);
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

    private ReconnectResult tryReconnect(String range, ISOSProvider provider, ISOSProviderFile file, int retryCount) {
        ReconnectResult r = new ReconnectResult();
        r.counter = retryCount;

        if (provider.isConnected()) {
            if (!(provider instanceof SOSLocal)) {
                return r;
            }
        }

        boolean run = true;
        while (run) {
            try {
                JobSchedulerException.LastErrorMessage = "";

                r.counter++;
                LOGGER.info("----------------------------------------------------");
                LOGGER.info(String.format("[%s][%s][%s]wait %s and try reconnect %s of %s ...", transferNumber, range, file.getName(), parent
                        .getBaseOptions().connection_error_retry_interval.getValue(), r.counter, parent
                                .getBaseOptions().connection_error_retry_count_max.value()));
                LOGGER.info("----------------------------------------------------");
                if (parent.getRetryInterval() > 0) {
                    try {
                        Thread.sleep(parent.getRetryInterval() * 1_000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                provider.reconnect();
                r.providerFile = provider.getFile(file.getName());
                return r;
            } catch (Throwable e) {
                if (r.counter == parent.getRetryCountMax()) {
                    run = false;
                    throw e;
                } else {
                    parent.setLastErrorMessage(e.toString());
                }
            }
        }
        return r;
    }

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
        createTargetChecksumFile(makeFullPathName(parent.getBaseOptions().targetDir.getValue(), targetFileName));
    }

    public void createTargetChecksumFile(String targetFileName) {
        if (parent.getBaseOptions().createIntegrityHashFile.isTrue() && isTransferred()) {
            ISOSProviderFile checksumFile = null;
            try {
                targetFileName = resolveDotsInPath(targetFileName);
                checksumFile = parent.getTargetProvider().getFile(targetFileName + "." + parent.getBaseOptions().integrityHashType.getValue());
                checksumFile.write(targetFileChecksum.getBytes());
                LOGGER.info(String.format("[%s]%s", transferNumber, SOSVfs_I_285.params(checksumFile.getName())));
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

    private void checkSourceChecksumFile(ISOSProviderFile sourceFile, ISOSProviderFile targetFile) {
        if (parent.getBaseOptions().checkIntegrityHash.isTrue()) {
            ISOSProviderFile sourceFileChecksumFile = null;
            String sourceFileChecksumFileName = sourceFile.getName() + "." + parent.getBaseOptions().securityHashType.getValue();
            try {
                sourceFileChecksumFile = parent.getSourceProvider().getFile(sourceFileChecksumFileName);
                if (sourceFileChecksumFile.fileExists()) {
                    String checksum = sourceFileChecksumFile.file2String().trim();
                    if (!checksum.equals(sourceFileChecksum)) {
                        try {
                            if (targetFile.fileExists()) {
                                targetFile.delete(false);
                            }
                        } catch (Throwable ex) {
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

    private void setEntryErrorMessage(ISOSProvider sourceTransfer, ISOSProvider targetTransfer, JobSchedulerException ex) {
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

    private void executeCommands(final String commandOptionName, final ISOSProvider fileTransfer, final SOSOptionString optionCommands,
            final SOSOptionString optionCommandDelimiter) {
        executeCommands(commandOptionName, fileTransfer, optionCommands, optionCommandDelimiter, null);
    }

    private void executeCommands(final String commandOptionName, final ISOSProvider provider, final SOSOptionString optionCommands,
            final SOSOptionString optionCommandDelimiter, SOSEnv env) {
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
            commands = replaceVariables(commands, provider);
            String delimiter = null;
            if (optionCommandDelimiter != null) {
                delimiter = optionCommandDelimiter.getValue();
            }
            if (delimiter == null) {
                delimiter = ";";
            }
            if (delimiter.isEmpty()) {
                try {
                    LOGGER.info(String.format("[%s][%s][%s]%s", transferNumber, commandOptionName, fileName, commands));
                    provider.executeCommand(commands, env);
                } catch (JobSchedulerException e) {
                    throw new JobSchedulerException(String.format("[%s][%s][%s][%s]", transferNumber, commandOptionName, fileName, commands), e);
                } catch (Exception e) {
                    throw new JobSchedulerException(String.format("[%s][%s][%s][%s]", transferNumber, commandOptionName, fileName, commands), e);
                }
            } else {
                String[] values = commands.split(delimiter);
                if (values.length > 1) {
                    LOGGER.debug(String.format("[%s][%s]commands=%s", transferNumber, commandOptionName, commands));
                }
                for (String command : values) {
                    command = command.trim();
                    if (command.length() > 0) {
                        try {
                            LOGGER.info(String.format("[%s][%s][%s]%s", transferNumber, commandOptionName, fileName, command));
                            provider.executeCommand(command, env);
                        } catch (JobSchedulerException e) {
                            throw new JobSchedulerException(String.format("[%s][%s][%s][%s]", transferNumber, commandOptionName, fileName, command),
                                    e);
                        } catch (Exception e) {
                            throw new JobSchedulerException(String.format("[%s][%s][%s][%s]", transferNumber, commandOptionName, fileName, command),
                                    e);
                        }
                    }
                }
            }
        }
    }

    public void executeTFNPostCommnands() throws Exception {
        SOSProviderOptions target = parent.getBaseOptions().getTransfer().getTarget();
        if (target.alternateOptionsUsed.isTrue()) {
            executeCommands("alternative_target_tfn_post_command", parent.getTargetProvider(), target.getAlternative().tfnPostCommand, target
                    .getAlternative().commandDelimiter);
        } else {
            executeCommands("target_tfn_post_command", parent.getTargetProvider(), target.tfnPostCommand, target.commandDelimiter);
        }
        SOSProviderOptions source = parent.getBaseOptions().getTransfer().getSource();
        if (source.alternateOptionsUsed.isTrue()) {
            executeCommands("alternative_source_tfn_post_command", parent.getSourceProvider(), source.getAlternative().tfnPostCommand, source
                    .getAlternative().commandDelimiter);
        } else {
            executeCommands("source_tfn_post_command", parent.getSourceProvider(), source.tfnPostCommand, source.commandDelimiter);
        }
    }

    public void executePostCommands() throws Exception {
        boolean isTransferred = transferStatus.equals(TransferStatus.transferred);
        String status = transferStatus.name();
        Map<String, String> env = new HashMap<String, String>();
        env.put(ENV_VAR_FILE_TRANSFER_STATUS, status);
        env.put(ENV_VAR_FILE_IS_TRANSFERRED, isTransferred ? "1" : "0");
        SOSEnv envs = new SOSEnv();
        envs.setLocalEnvs(env);

        SOSProviderOptions target = parent.getBaseOptions().getTransfer().getTarget();
        if (target.alternateOptionsUsed.isTrue()) {
            if (!isTransferred && target.getAlternative().post_command_disable_for_skipped_transfer.value()) {
                LOGGER.info(String.format("[%s][alternative_target_post_command][skip]disable_for_skipped_transfer=true, status=%s", transferNumber,
                        status));
            } else {
                executeCommands("alternative_target_post_command", parent.getTargetProvider(), target.getAlternative().postCommand, target
                        .getAlternative().commandDelimiter, envs);
            }
        } else {
            if (!isTransferred && (parent.getBaseOptions().post_command_disable_for_skipped_transfer.value()
                    || target.post_command_disable_for_skipped_transfer.value())) {
                LOGGER.info(String.format("[%s][target_post_command][skip]disable_for_skipped_transfer=true, status=%s", transferNumber, status));
            } else {
                executeCommands("target_post_command", parent.getTargetProvider(), target.postCommand, target.commandDelimiter, envs);
            }
        }
        SOSProviderOptions source = parent.getBaseOptions().getTransfer().getSource();
        if (source.alternateOptionsUsed.isTrue()) {
            if (!isTransferred && source.getAlternative().post_command_disable_for_skipped_transfer.value()) {
                LOGGER.info(String.format("[%s][alternative_source_post_command][skip]disable_for_skipped_transfer=true, status=%s", transferNumber,
                        status));
            } else {
                executeCommands("alternative_source_post_command", parent.getSourceProvider(), source.getAlternative().postCommand, source
                        .getAlternative().commandDelimiter, envs);
            }
        } else {
            if (!isTransferred && source.post_command_disable_for_skipped_transfer.value()) {
                LOGGER.info(String.format("[%s][source_post_command][skip]disable_for_skipped_transfer=true, status=%s", transferNumber, status));
            } else {
                executeCommands("source_post_command", parent.getSourceProvider(), source.postCommand, source.commandDelimiter, envs);
            }
        }
    }

    private void executePreCommands(boolean isSkipped) throws Exception {
        SOSProviderOptions target = parent.getBaseOptions().getTransfer().getTarget();
        if (target.alternateOptionsUsed.isTrue()) {
            if (isSkipped) {
                if (target.getAlternative().pre_command_enable_for_skipped_transfer.value()) {
                    executeCommands("alternative_target_pre_command enable_for_skipped_transfer=true", parent.getTargetProvider(), target
                            .getAlternative().preCommand, target.commandDelimiter);
                }
            } else {
                executeCommands("alternative_target_pre_command", parent.getTargetProvider(), target.getAlternative().preCommand,
                        target.commandDelimiter);
            }
        } else {
            if (isSkipped) {
                if (parent.getBaseOptions().pre_command_enable_for_skipped_transfer.value() || target.pre_command_enable_for_skipped_transfer
                        .value()) {
                    executeCommands("target_pre_command enable_for_skipped_transfer=true", parent.getTargetProvider(), target.preCommand,
                            target.commandDelimiter);
                }
            } else {
                executeCommands("target_pre_command", parent.getTargetProvider(), target.preCommand, target.commandDelimiter);
            }
        }
        SOSProviderOptions source = parent.getBaseOptions().getTransfer().getSource();
        if (source.alternateOptionsUsed.isTrue()) {
            if (isSkipped) {
                if (source.getAlternative().pre_command_enable_for_skipped_transfer.value()) {
                    executeCommands("alternative_source_pre_command enable_for_skipped_transfer=true", parent.getSourceProvider(), source
                            .getAlternative().preCommand, source.getAlternative().commandDelimiter);
                }
            } else {
                executeCommands("alternative_source_pre_command", parent.getSourceProvider(), source.getAlternative().preCommand, source
                        .getAlternative().commandDelimiter);
            }
        } else {
            if (isSkipped) {
                if (source.pre_command_enable_for_skipped_transfer.value()) {
                    executeCommands("source_pre_command enable_for_skipped_transfer=true", parent.getSourceProvider(), source.preCommand,
                            source.commandDelimiter);
                }
            } else {
                executeCommands("source_pre_command", parent.getSourceProvider(), source.preCommand, source.commandDelimiter);
            }
        }
    }

    public boolean isTargetFileExistsBeforeTransfer() {
        return targetFileExistsBeforeTransfer;
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
        if (parent.getBaseOptions().resultSetFileName.getValue().endsWith(".source.tmp")) {
            result = sourceFileName;
        }
        return result;
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
        if (targetFileChecksum == null || targetFileChecksum.equals(DEFAULT_CHECKSUM)) {
            return sourceFileChecksum;
        }
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

    private String getFileNameWithoutPath(final String name) {
        return new File(adjustFileSeparator(name)).getName();
    }

    private String getPathWithoutFileName(final ISOSProvider provider, final String fileName) {
        File file = new File(adjustFileSeparator(fileName));
        String parent = file.getParent();
        if (parent == null) {
            parent = "./";
        } else {
            if (provider.isSFTP()) {
                if (SOSSFTP.hasWindowsOpenSSHDriverLetterSpecifier(fileName)) {// e.g. /C:/tmp/file.txt
                    parent = "/" + parent; // new File above removes the leading /
                }
            }
        }
        return adjustFileSeparator(parent);
    }

    @Override
    public String getPid() {
        String pid = ManagementFactory.getRuntimeMXBean().getName();
        String arr[] = pid.split("@");
        return arr[0];
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

    public ISOSProviderFile getTargetFile() throws Exception {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s][sourceFileName=%s]-------------------------------------", transferNumber, sourceFileName));
        }
        ISOSProviderFile sourceFile = parent.getSourceProvider().getFile(sourceFileName);
        sourceTransferFileName = sourceFile.getName();
        targetFileName = entry.getNormalizedFilename() == null ? sourceFile.getName() : entry.getNormalizedFilename();
        if (parent.getBaseOptions().compressFiles.isTrue()) {
            targetFileName = targetFileName + parent.getBaseOptions().compressedFileExtension.getValue();
        }
        if (parent.getBaseOptions().cumulateFiles.isTrue()) {
            targetFileName = parent.getBaseOptions().cumulativeFileName.getValue();
            targetTransferFileName = targetFileName;
            parent.getBaseOptions().appendFiles.value(true);
        } else {
            targetFileName = getFileNameWithoutPath(targetFileName);
            targetTransferFileName = targetFileName;
            if (parent.getBaseOptions().getReplacing().isNotEmpty()) {
                try {
                    targetFileName = parent.getBaseOptions().getReplacing().doReplace(targetFileName, parent.getBaseOptions().getReplacement()
                            .getValue());
                } catch (JobSchedulerException e) {
                    throw e;
                } catch (Throwable e) {
                    throw new JobSchedulerException(SOSVfs_E_0150.get() + " " + e.toString(), e);
                }
            }
        }
        if (parent.getBaseOptions().isAtomicTransfer() || parent.getBaseOptions().transactionMode.isTrue()) {
            targetTransferFileName = getTargetAtomicFileName(parent.getBaseOptions());
        }
        // YADE-600
        if (parent.getBaseOptions().recursive.value()) {
            String sourceDir = getPathWithoutFileName(parent.getSourceProvider(), sourceFile.getName());
            String sourceDirOrig = parent.getSourceProvider().getFile(parent.getBaseOptions().sourceDir.getValue()).getName();

            if (!fileNamesAreEqual(sourceDir, sourceDirOrig, true) && sourceDir.length() > sourceDirOrig.length()) {
                String subFolder = sourceDir.substring(sourceDirOrig.length());
                subFolder = adjustFileSeparator(addFileSeparator(subFolder));
                if (parent.isFullPathSelection()) {
                    // YADE-600
                    int w = subFolder.indexOf(":");
                    if (w > -1) {
                        subFolder = subFolder.substring(w + 1);
                        subFolder = StringUtils.stripStart(subFolder, "/");
                    }
                }
                targetFileName = targetFileName.replaceFirst("([^/]*)$", subFolder + "$1");
                targetTransferFileName = subFolder + targetTransferFileName;

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[%s][target][%s][sourceDir=%s][sourceDirOrig=%s]not equals", transferNumber, targetFileName,
                            sourceDir, sourceDirOrig));
                }

                if (isNotEmpty(getTargetAtomicFileName())) {
                    setTargetAtomicFileName(targetTransferFileName);
                }
                try {
                    String dir = addFileSeparator(parent.getBaseOptions().targetDir.getValue()) + subFolder;
                    if (parent.add2SubFolders(subFolder)) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug(String.format("[%s][target][%s][%s]check mkdir", transferNumber, targetFileName, dir));
                        }
                        parent.getTargetProvider().mkdir(dir);
                    } else {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug(String.format("[%s][target][%s][%s]dir exists", transferNumber, targetFileName, dir));
                        }
                    }
                } catch (IOException e) {
                    throw new JobSchedulerException(e);
                }
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[%s][target][%s][sourceDir=%s][sourceDirOrig=%s]equals", transferNumber, targetFileName, sourceDir,
                            sourceDirOrig));
                }
            }
        }

        String fileHandlerTargetFileName = makeFullPathName(parent.getBaseOptions().targetDir.getValue(), targetFileName);
        ISOSProviderFile targetFile = parent.getTargetProvider().getFile(fileHandlerTargetFileName);
        if (parent.getBaseOptions().cumulateFiles.isTrue()) {
            if (parent.getBaseOptions().cumulativeFileDelete.isTrue() && !parent.getBaseOptions().getCumulativeTargetDeleted()) {
                targetFile.delete(true);
                parent.getBaseOptions().setCumulativeTargetDeleted(true);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[%s][target][%s]cumulative file deleted", transferNumber, SOSCommonProvider.normalizePath(targetFile
                            .getName())));
                }
            }
        }
        targetFile.setModeAppend(parent.getBaseOptions().appendFiles.value());
        targetFile.setModeRestart(parent.getBaseOptions().resumeTransfer.value());
        if (!fileNamesAreEqual(targetFileName, targetTransferFileName, false)) {
            targetTransferFile = parent.getTargetProvider().getFile(makeFullPathName(parent.getBaseOptions().targetDir.getValue(),
                    targetTransferFileName));
        } else {
            targetTransferFile = targetFile;
        }
        if (parent.getBaseOptions().cumulateFiles.isTrue()) {
            targetTransferFile.setModeAppend(parent.getBaseOptions().appendFiles.value());
            targetTransferFile.setModeRestart(parent.getBaseOptions().resumeTransfer.value());
        }

        targetFileExistsBeforeTransfer = false;
        if (targetFile.fileExists()) {
            targetFileExistsBeforeTransfer = true;
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][target][%s]targetFileExistsBeforeTransfer=true", transferNumber, SOSCommonProvider.normalizePath(
                        targetFile.getName())));
            }
            if (parent.getBaseOptions().isDoNotOverwrite()) {
                setNotOverwritten(targetFile);
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][target][%s]targetFileExistsBeforeTransfer=false", transferNumber, SOSCommonProvider.normalizePath(
                        targetFile.getName())));
            }
        }
        return targetFile;
    }

    public void setRenamedSourceFilename() throws Exception {
        SOSProviderOptions sourceOptions = parent.getBaseOptions().getSource();
        if (sourceOptions.replacing.isDirty() && !parent.getBaseOptions().removeFiles.value()) {
            String replaceWith = sourceOptions.replacement.getValue();
            try {
                boolean isDebugEnabled = LOGGER.isDebugEnabled();
                String sourceName = new File(sourceFileName).getName();
                String sourceParent = sourceFileName.substring(0, sourceFileName.length() - sourceName.length());
                String sourceNameNew = sourceOptions.replacing.doReplace(sourceName, replaceWith).replace('\\', '/');
                boolean equals = sourceNameNew.equals(sourceName);
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][source][replaceWath=%s][replaceWith=%s][%s][new=%s]equals=%s", transferNumber,
                            sourceOptions.replacing.getValue(), replaceWith, sourceName, sourceNameNew, equals));
                }
                if (!equals) {
                    String sourceDir = addFileSeparator(sourceOptions.directory.getValue()).replace('\\', '/');
                    if (parent.getBaseOptions().recursive.value()) {
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
                            LOGGER.debug(String.format("[%s][source]renamedSourceFileName=%s", transferNumber, sourceFileNameRenamed));
                        }
                    }
                }

            } catch (JobSchedulerException e) {
                throw e;
            } catch (Throwable e) {
                throw new JobSchedulerException(SOSVfs_E_0150.get(), e);
            }
        }
    }

    @Override
    public String getTargetFilename() {
        return targetFileName;
    }

    public String getTargetFileNameAndPath() {
        return SOSCommonProvider.normalizePath(parent.getBaseOptions().targetDir.getValue(), targetFileName);
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

    private boolean isTransferred() {
        return transferStatus.equals(TransferStatus.transferred);
    }

    public boolean isSourceFileSteady() {
        return sourceFileSteady;
    }

    private String getTargetAtomicFileName(final SOSBaseOptions options) {
        targetTransferFileName = targetTransferFileName + options.atomicSuffix.getValue().trim();
        targetTransferFileName = options.atomicPrefix.getValue() + targetTransferFileName;
        targetAtomicFileName = targetTransferFileName.trim();
        return targetAtomicFileName;
    }

    public void setTargetAtomicFileName(final String val) {
        targetAtomicFileName = val;
    }

    private String makeFileNameReplacing(final String fileName) {
        String name = adjustFileSeparator(fileName);
        String replacement = parent.getBaseOptions().getReplacement().getValue();
        try {
            name = parent.getBaseOptions().getReplacing().doReplace(name, replacement);
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Throwable e) {
            throw new JobSchedulerException(SOSVfs_E_0150.get(), e);
        }
        return name;
    }

    private long getBytesTransferred() {
        return bytesTransferred;
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
        renameSourceFile(parent.getSourceProvider().getFile(sourceFileName));
    }

    private void renameSourceFile(final ISOSProviderFile sourceFile) {
        if (sourceFileNameRenamed != null) {
            try {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[%s][source][%s][renamed=%s]", transferNumber, sourceFile.getName(), sourceFileNameRenamed));
                }

                ISOSProviderFile renamedSourceFile = parent.getSourceProvider().getFile(sourceFileNameRenamed);
                if (renamedSourceFile.fileExists()) {
                    renamedSourceFile.delete(false);
                }
                if (sourceFileNameRenamed.contains("/") && parent.getBaseOptions().makeDirs.isTrue()) {
                    String parentName = sourceFileNameRenamed.replaceFirst("[^/]*$", "");
                    parent.getSourceProvider().mkdir(parentName);
                }
                sourceFile.rename(sourceFileNameRenamed);
            } catch (JobSchedulerException e) {
                throw e;
            } catch (Throwable e) {
                throw new JobSchedulerException(SOSVfs_E_0150.get(), e);
            }
        }
    }

    public void rollbackRenameSourceFile() {
        if (sourceFileNameRenamed != null) {
            try {
                ISOSProviderFile sourceFile = parent.getSourceProvider().getFile(sourceFileName);
                ISOSProviderFile sourceFileRenamed = parent.getSourceProvider().getFile(sourceFileNameRenamed);
                if (!sourceFile.fileExists() && sourceFileRenamed.fileExists()) {
                    sourceFileRenamed.rename(sourceFileName);
                }
            } catch (JobSchedulerException e) {
                throw e;
            } catch (Throwable e) {
                throw new JobSchedulerException(SOSVfs_E_0150.get(), e);
            }
        }
    }

    public void renameTargetFile() {
        if (!skipTransfer()) {
            renameTargetFile(parent.getTargetProvider().getFile(makeFullPathName(parent.getBaseOptions().targetDir.getValue(), targetFileName)));
        }
    }

    private void renameTargetFile(ISOSProviderFile targetFile) {
        String targetFileNewName = targetFile.getName();
        targetFileNewName = resolveDotsInPath(targetFileNewName);
        boolean equals = fileNamesAreEqual(targetTransferFile.getName(), targetFileNewName, false);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(String.format("[%s][target][%s][%s]equals=%s", transferNumber, SOSCommonProvider.normalizePath(targetTransferFile.getName()),
                    SOSCommonProvider.normalizePath(targetFileNewName), equals));
        }
        if (!equals) {
            try {
                String targetFileOldName = SOSCommonProvider.normalizePath(targetTransferFile.getName());
                if (parent.getBaseOptions().overwriteFiles.isTrue() && targetFileExistsBeforeTransfer) {
                    try {
                        targetFile.delete(false);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug(String.format("[%s][target][%s][overwriteFiles=true]deleted", transferNumber, SOSCommonProvider
                                    .normalizePath(targetFile.getName())));
                        }
                    } catch (Throwable e) {
                        if (!parent.getTargetProvider().isConnected()) {
                            throw e;
                        }
                        if (targetFile.fileExists()) {
                            throw e;
                        }
                    }
                }
                targetTransferFile.rename(targetFileNewName);
                LOGGER.info(String.format("[%s]%s", transferNumber, SOSVFSMessageCodes.SOSVfs_I_150.params(targetFileOldName, SOSCommonProvider
                        .normalizePath(targetFileNewName))));
            } catch (JobSchedulerException e) {
                throw e;
            } catch (Throwable e) {
                throw new JobSchedulerException(SOSVfs_E_0150.get(), e);
            }
        }
    }

    private String replaceVariables(final String value, final ISOSProvider provider) {
        boolean isTraceEnabled = LOGGER.isTraceEnabled();
        if (isTraceEnabled) {
            LOGGER.trace(String.format("[%s][replaceVariables]%s", transferNumber, value));
        }

        if (provider != null) {
            EntryPaths targetFile = new EntryPaths(parent.getTargetProvider(), parent.getBaseOptions().targetDir.getValue(), resolveDotsInPath(
                    makeFullPathName(parent.getBaseOptions().targetDir.getValue(), targetFileName)));
            EntryPaths targetTransferFile = new EntryPaths(parent.getTargetProvider(), parent.getBaseOptions().targetDir.getValue(),
                    resolveDotsInPath(makeFullPathName(parent.getBaseOptions().targetDir.getValue(), targetTransferFileName)));

            EntryPaths sourceFile = new EntryPaths(parent.getSourceProvider(), parent.getBaseOptions().sourceDir.getValue(), resolveDotsInPath(
                    sourceFileName));
            EntryPaths sourceFileRenamed = new EntryPaths(parent.getSourceProvider(), parent.getBaseOptions().sourceDir.getValue(),
                    sourceFileNameRenamed);

            Properties vars = new Properties(); // parent.getBaseOptions().getTextProperties();
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
            if (isTraceEnabled) {
                LOGGER.trace(vars.toString());
            }
            parent.getBaseOptions().getTextProperties().putAll(vars);
        }
        return parent.getBaseOptions().replaceVars(value);
    }

    @Override
    public void run() {
        try {
            ISOSProviderFile sourceFile = parent.getSourceProvider().getFile(sourceFileName);
            ISOSProviderFile targetFile = null;
            if (parent.getBaseOptions().isNeedTargetClient()) {
                targetFile = getTargetFile();
            }

            setRenamedSourceFilename();

            if (parent.getBaseOptions().transactional.value()) {
                setTransactional();
            }
            boolean removeSourceFile = false;
            switch (parent.getBaseOptions().operation.value()) {
            case getlist:
                return;
            case delete:
                executePreCommands(false);
                sourceFile.delete(true);
                LOGGER.info(String.format("[%s]%s", transferNumber, SOSVfs_I_0113.params(sourceFileName)));
                this.setStatus(TransferStatus.deleted);
                return;
            case rename:
                File file = new File(sourceFileName);
                String parentName = changeBackslashes(normalized(file.getParent()));
                String fileNameRenamed = makeFileNameReplacing(file.getName());
                if (fileNameRenamed.contains("/") && parent.getBaseOptions().makeDirs.isTrue()) {
                    parent.getSourceProvider().mkdir(normalized(new File(fileNameRenamed).getParent()));
                }
                fileNameRenamed = changeBackslashes(addFileSeparator(parentName) + fileNameRenamed);
                LOGGER.info(String.format("[%s]%s", transferNumber, SOSVfs_I_150.params(sourceFileName, fileNameRenamed)));
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

            sourceTransferFileName = getFileNameWithoutPath(sourceTransferFileName);
            String fileHandlerSourceFileName = null;
            if (parent.getSourceProvider().isHTTP()) {
                fileHandlerSourceFileName = sourceFileName;
            } else {
                fileHandlerSourceFileName = makeFullPathName(getPathWithoutFileName(parent.getSourceProvider(), sourceFileName),
                        sourceTransferFileName);
            }

            sourceTransferFile = parent.getSourceProvider().getFile(fileHandlerSourceFileName);
            if (transferStatus.equals(TransferStatus.ignoredDueToZerobyteConstraint)) {
                String msg = String.format("[%s][skip][TransferZeroByteFiles=relaxed]Source=%s, Bytes=0", transferNumber, SOSCommonProvider
                        .normalizePath(sourceFileName));
                LOGGER.info(msg);
                JADE_REPORT_LOGGER.info(msg);
            }

            if (skipTransfer()) {
                executePreCommands(true);
            } else {
                String fileHandlerTargetFileName = makeFullPathName(parent.getBaseOptions().targetDir.getValue(), targetFileName);
                doTransfer(sourceTransferFile, fileHandlerSourceFileName, targetTransferFile, fileHandlerTargetFileName);

                if (LOGGER.isDebugEnabled()) {
                    if (parent.getBaseOptions().checkSteadyStateOfFiles.isTrue()) {
                        LOGGER.debug(String.format("[%s][checkSteadyStateOfFiles][sourceFileModificationDateTime]%s", transferNumber,
                                sourceFileModificationDateTime));
                    }
                }

                if (parent.getBaseOptions().keepModificationDate.isTrue()) {
                    if (sourceFileModificationDateTime <= 0) {
                        sourceFileModificationDateTime = sourceTransferFile.getModificationDateTime();
                    }
                    if (sourceFileModificationDateTime != -1) {
                        targetFile.setModificationDateTime(sourceFileModificationDateTime);
                    }
                }
                if ((parent.getBaseOptions().isAtomicTransfer() || parent.getBaseOptions().getReplacing().isNotEmpty()) && parent
                        .getBaseOptions().transactional.isFalse()) {
                    renameTargetFile(targetFile);
                }
            }
            if (parent.getBaseOptions().transactional.isFalse()) {
                createTargetChecksumFile(targetFile.getName());
                if (sourceFileNameRenamed != null) {
                    renameSourceFile(sourceFile);
                }

                if (removeSourceFile) {
                    try {
                        sourceFile.delete(false);
                        transferStatus = TransferStatus.moved;
                        logSourceFileDeleted();
                    } catch (Throwable e) {
                        transferStatus = TransferStatus.transfer_aborted;
                        targetFile.delete(false);
                        String msg = String.format("[%s][transfer_aborted]Target=%s deleted", transferNumber, SOSCommonProvider.normalizePath(
                                targetFile.getName()));
                        LOGGER.info(msg);
                        JADE_REPORT_LOGGER.info(msg);
                        throw e;
                    }
                }
            }
        } catch (JobSchedulerException e) {
            setTransferredStatusAborted();
            Throwable t = e.getCause() == null ? e : e.getCause();
            LOGGER.error("[" + transferNumber + "][" + transferStatus + "][Source=" + sourceFileName + "]" + e.toString(), t);
            throw e;
        } catch (Throwable e) {
            setTransferredStatusAborted();
            String msg = "[" + transferNumber + "][" + transferStatus + "][Source=" + sourceFileName + "]" + e.toString();
            LOGGER.error(msg);
            throw new JobSchedulerException(msg, e);
        }
    }

    private void setTransferredStatusAborted() {
        if (transferStatus == null) {
            setStatus(TransferStatus.transfer_aborted);
            return;
        }

        switch (transferStatus) {
        case deleted:
        case ignoredDueToZerobyteConstraint:
        case moved:
        case notOverwritten:
            break;
        case polling:
        case renamed:
        case compressed:
        case setBack:
        case transferInProgress:
        case transferUndefined:
        case transfer_aborted:
        case transfer_has_errors:
        case transfer_skipped:
        case transferred:
        case transferring:
        case waiting4transfer:
            setStatus(TransferStatus.transfer_aborted);
            break;
        default:
            break;
        }
    }

    public void setNoOfBytesTransferred(final long bytes, long fileSize) throws Exception {
        bytesTransferred = bytes;
        SOSTransfer connectionOptions = parent.getBaseOptions().getTransfer();
        if (!(parent.getBaseOptions().checkSize.isFalse() || parent.getBaseOptions().compressFiles.isTrue() || parent.getBaseOptions().transferMode
                .isAscii() || connectionOptions.getSource().transferMode.isAscii() || connectionOptions.getTarget().transferMode.isAscii())) {
            if (sourceFileSize <= 0) {
                sourceFileSize = fileSize;// sourceFileTransfer.getFileHandle(sourceFileName).getFileSize();
            }
            if (sourceFileSize != bytesTransferred) {
                LOGGER.error(String.format("[%s]%s", transferNumber, SOSVfs_E_216.params(bytesTransferred, sourceFileSize, sourceFileName)));
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

    private void setNotOverwritten(ISOSProviderFile targetFile) {
        transferStatus = TransferStatus.notOverwritten;
        endTime = Instant.now();
        String msg = String.format("[%s][skip][DisableOverwriteFiles=true]Target=%s", transferNumber, SOSCommonProvider.normalizePath(targetFile
                .getName()));
        LOGGER.info(msg);
        JADE_REPORT_LOGGER.info(msg);
    }

    public void setParent(final SOSFileList list) {
        parent = list;
    }

    public void setSourceFileSteadyProperties(ISOSProviderFile file) {
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

    public void setSourceFileSteady(final boolean val) {
        sourceFileSteady = val;
    }

    private void setTransferProgress(final long val) {
        setStatus(TransferStatus.transferInProgress);
    }

    public void setIgnoredDueToZerobyteConstraint() {
        transferStatus = TransferStatus.ignoredDueToZerobyteConstraint;
        endTime = Instant.now();
    }

    public boolean isSourceFileExists() {
        try {
            return parent.getSourceProvider().getFile(sourceFileName).fileExists();
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
            msg = String.format("[%s]%s", transferNumber, SOSVfs_D_214.params(getTargetFileNameAndPath(), getSourceFileName(), getBytesTransferred(),
                    parent.getBaseOptions().operation.getValue(), getDateTimeInfos(startTime, endTime)));
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

    public ISOSProviderFile getTargetChecksumFile() {
        return targetChecksumFile;
    }

    private Buffer compress(byte[] dataToCompress) {
        return compress(dataToCompress, dataToCompress.length);
    }

    private Buffer compress(byte[] dataToCompress, int length) {
        Buffer buf = new Buffer();
        if (parent.getBaseOptions().compressFiles.isTrue()) {
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
                throw new JobSchedulerException(SOSVFSMessageCodes.SOSVfs_E_134.params("GZip"), e);
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

    public void setTransferNumber(long val) {
        transferNumber = val;
    }

    public long getTransferNumber() {
        return transferNumber;
    }

    private class ReconnectResult {

        private ISOSProviderFile providerFile;
        private int counter;
    }

    private class EntryPaths {

        private String fullName = "";
        private String relativeName = "";
        private String baseName = "";
        private String baseDirFullName = "";
        private String parentDirFullName = "";
        private String parentDirBaseName = "";

        // TODO only 1 method nio.Path independent
        private EntryPaths(final ISOSProvider provider, final String baseDirPath, final String filePath) {
            if (provider != null && filePath != null) {
                try {
                    if (provider.isSFTP() && SOSSFTP.hasWindowsOpenSSHDriverLetterSpecifier(filePath)) {
                        setForOpenSSHWindows(baseDirPath, filePath);
                    } else if (provider.isHTTP()) {
                        setHttp(SOSString.isEmpty(baseDirPath) ? provider.getProviderOptions().host.getValue() : baseDirPath, filePath);
                    } else {
                        setFromPath(baseDirPath, filePath);
                    }

                } catch (Exception e) {
                    LOGGER.warn(String.format("[%s]error on resolve path for baseDirPath=%s, filePath=%s", transferNumber, baseDirPath, filePath), e);
                }
            }
        }

        private void setFromPath(final String baseDirPath, final String filePath) throws Exception {
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
        }

        private void setForOpenSSHWindows(final String baseDirPath, final String filePath) throws Exception {
            File baseDir = SOSString.isEmpty(baseDirPath) ? null : new File(baseDirPath);
            File file = new File(filePath);
            File parentDir = file.getParentFile();

            if (parentDir != null) {
                parentDirFullName = "/" + parentDir.toString().replace('\\', '/');
                String pdfn = parentDir.getName();
                if (pdfn != null) {
                    parentDirBaseName = pdfn;
                }
                if (baseDir == null) {
                    baseDir = parentDir;
                }
            }

            fullName = "/" + file.toString().replace('\\', '/');
            baseName = file.getName().toString();

            if (baseDir == null) {
                relativeName = baseName;
            } else {
                baseDirFullName = "/" + baseDir.getCanonicalPath().replace('\\', '/');
                relativeName = baseName;
                // relativeName = baseDir.relativize(file).normalize().toString().replace('\\', '/');
            }
        }

        private void setHttp(final String baseDirPath, final String filePath) throws Exception {
            String baseDir = SOSString.isEmpty(baseDirPath) ? null : SOSCommonProvider.normalizeDirectoryPath(baseDirPath);
            fullName = SOSCommonProvider.normalizePath(filePath);// SOSCommonProvider.normalizePath(baseDir == null ? "" : baseDir, filePath);
            baseName = SOSCommonProvider.getBaseNameFromPath(fullName);
            String parentDir = SOSCommonProvider.getFullParentFromPath(fullName);
            if (parentDir != null) {
                parentDirFullName = parentDir;
                parentDirBaseName = SOSCommonProvider.getBaseNameFromPath(parentDirFullName);
                if (baseDir == null) {
                    baseDir = parentDirFullName;
                }
            }

            if (baseDir == null) {
                relativeName = baseName;
            } else {
                baseDirFullName = baseDir;
                if (fullName.startsWith(baseDirFullName)) {
                    String r = fullName.substring(baseDirFullName.length());
                    relativeName = r.startsWith("/") ? r.substring(1) : r;
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
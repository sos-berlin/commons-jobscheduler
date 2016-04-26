package com.sos.VirtualFileSystem.DataElements;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
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
    private final static Logger logger = Logger.getLogger(SOSFileListEntry.class);
    private final static Logger objJadeReportLogger = Logger.getLogger(VFSFactory.getLoggerName());
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
    @SuppressWarnings("unused")
    private ISOSVfsFileTransfer objVfsHandler = null;
    private final String guid = UUID.randomUUID().toString();
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
    public long zeroByteCount = 0;

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

    public void DeleteSourceFile() {
        String file = strSourceFileName;
        objDataSourceClient.getFileHandle(file).delete();
        String msg = SOSVfs_I_0113.params(file);
        logger.info(msg);
        objJadeReportLogger.info(msg);
    }

    private boolean doTransfer(final ISOSVirtualFile source, final ISOSVirtualFile target) {
        boolean closed = false;
        if (target == null) {
            RaiseException(SOSVfs_E_273.params("Target"));
        }
        if (source == null) {
            RaiseException(SOSVfs_E_273.params("Source"));
        }
        MessageDigest md4create = null;
        MessageDigest md4check = null;
        boolean calculateIntegrityHash4Create = true;
        if (calculateIntegrityHash4Create) {
            try {
                md4create = MessageDigest.getInstance(objOptions.IntegrityHashType.Value());
            } catch (NoSuchAlgorithmException e1) {
                logger.error(e1.toString());
                objOptions.CreateIntegrityHashFile.value(false);
                calculateIntegrityHash4Create = false;
            }
        }
        boolean calculateIntegrityHash4Check = objOptions.CheckIntegrityHash.isTrue() && objOptions.compress_files.isTrue();
        if (calculateIntegrityHash4Check) {
            try {
                md4check = MessageDigest.getInstance(objOptions.IntegrityHashType.Value());
            } catch (NoSuchAlgorithmException e1) {
                logger.error(e1.toString());
                objOptions.CheckIntegrityHash.value(false);
                calculateIntegrityHash4Check = false;
            }
        }
        executePreCommands();
        long totalBytesTransferred = 0;
        this.setStatus(enuTransferStatus.transferring);
        try {
            int cumulativeFileSeperatorLength = 0;
            byte[] buffer = new byte[objOptions.BufferSize.value()];
            int bytesTransferred;
            synchronized (this) {
                if (objOptions.CumulateFiles.isTrue() && objOptions.CumulativeFileSeparator.IsNotEmpty()) {
                    String fs = objOptions.CumulativeFileSeparator.Value();
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
                    while ((bytesTransferred = source.read(buffer)) != -1) {
                        try {
                            Buffer compressedBytes = compress(buffer, bytesTransferred);
                            target.write(compressedBytes.getBytes(), 0, compressedBytes.getLength());
                            if (calculateIntegrityHash4Check) {
                                md4check.update(buffer, 0, bytesTransferred);
                            }
                            if (calculateIntegrityHash4Create) {
                                md4create.update(compressedBytes.getBytes(), 0, compressedBytes.getLength());
                            }
                        } catch (JobSchedulerException e) {
                            setEntryErrorMessage(e);
                            logger.error(errorMessage);
                            break;
                        }
                        totalBytesTransferred += bytesTransferred;
                        setTransferProgress(totalBytesTransferred);
                    }
                }
            }
            source.closeInput();
            target.closeOutput();
            closed = true;
            if (objDataTargetClient.isNegativeCommandCompletion()) {
                RaiseException(SOSVfs_E_175.params(objTargetTransferFile.getName(), objDataTargetClient.getReplyString()));
            }
            if (calculateIntegrityHash4Create) {
                checksum = toHexString(md4create.digest());
                checksum4check = checksum;
                logger.debug(SOSVfs_I_274.params(checksum, strTargetFileName, objOptions.IntegrityHashType.Value()));
            }
            if (calculateIntegrityHash4Check) {
                checksum4check = toHexString(md4check.digest());
                logger.debug(SOSVfs_I_274.params(checksum4check, strSourceFileName, objOptions.IntegrityHashType.Value()));
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

    public void createChecksumFile() {
        createChecksumFile(MakeFullPathName(objOptions.TargetDir.Value(), strTargetFileName));
    }

    public void createChecksumFile(String targetFileName) {
        targetFileName = resolveDotsInPath(targetFileName);
        if (objOptions.CreateIntegrityHashFile.isTrue() && isTransferred()) {
            ISOSVirtualFile checksumFile = null;
            try {
                checksumFile = objDataTargetClient.getFileHandle(targetFileName + "." + objOptions.IntegrityHashType.Value());
                checksumFile.write(checksum.getBytes());
                logger.info(SOSVfs_I_285.params(checksumFile.getName()));
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
        if (objOptions.CheckIntegrityHash.isTrue()) {
            ISOSVirtualFile sourceChecksumFile = null;
            String sourceChecksumFileName = source.getName() + "." + objOptions.SecurityHashType.Value();
            try {
                sourceChecksumFile = getDataSourceClient().getFileHandle(sourceChecksumFileName);
                if (sourceChecksumFile.FileExists()) {
                    String origChecksum = sourceChecksumFile.File2String().trim();
                    if (!origChecksum.equals(checksum4check)) {
                        try {
                            if (target.FileExists()) {
                                target.delete();
                            }
                        } catch (Exception ex) {
                            logger.debug(ex.toString(), ex);
                        }
                        String strT =
                                String.format("Integrity Hash violation. File %1$s, checksum read: '%2$s', checksum calculated: '%3$s'",
                                        sourceChecksumFileName, origChecksum, checksum4check);
                        setStatus(enuTransferStatus.transfer_aborted);
                        throw new JobSchedulerException(strT);
                    } else {
                        logger.info(String.format("Integrity Hash is OK: File %1$s, checksum read '%2$s', checksum calculated '%3$s'",
                                sourceChecksumFileName, origChecksum, checksum4check));
                    }
                } else {
                    logger.info(String.format("Checksum file '%1$s' not found", sourceChecksumFileName));
                }
            } catch (JobSchedulerException e) {
                throw e;
            } catch (Exception e) {
                logger.error(e.toString(), e);
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

    private void executeCommands(final ISOSVfsFileTransfer pobjDataClient, final SOSOptionString pstrCommandString) {
        final String conMethodName = "SOSFileListEntry::executeCommands";
        if (pstrCommandString.IsNotEmpty()) {
            String strT = pstrCommandString.Value();
            strT = replaceVariables(strT);
            String strM = SOSVfs_D_0151.params(strT);
            logger.debug(strM);
            String[] strA = strT.split(";");
            for (String strCmd : strA) {
                try {
                    pobjDataClient.getHandler().ExecuteCommand(strCmd);
                } catch (JobSchedulerException e) {
                    logger.error(e.toString());
                    throw e;
                } catch (Exception e) {
                    logger.error(e.toString());
                    throw new JobSchedulerException(conMethodName, e);
                }
            }
        }
    }

    public void executeTFNPostCommnands() {
        SOSConnection2OptionsAlternate target = objOptions.getConnectionOptions().Target();
        if (target.AlternateOptionsUsed.isTrue()) {
            executeCommands(objDataTargetClient, target.Alternatives().TFN_Post_Command);
        } else {
            executeCommands(objDataTargetClient, objOptions.TFN_Post_Command);
            executeCommands(objDataTargetClient, target.TFN_Post_Command);
        }
        SOSConnection2OptionsAlternate source = objOptions.getConnectionOptions().Source();
        if (source.AlternateOptionsUsed.isTrue()) {
            executeCommands(objDataSourceClient, source.Alternatives().TFN_Post_Command);
        } else {
            executeCommands(objDataSourceClient, source.TFN_Post_Command);
        }
    }

    public void executePostCommands() {
        SOSConnection2OptionsAlternate target = objOptions.getConnectionOptions().Target();
        if (target.AlternateOptionsUsed.isTrue()) {
            executeCommands(objDataTargetClient, target.Alternatives().Post_Command);
        } else {
            executeCommands(objDataTargetClient, objOptions.Post_Command);
            executeCommands(objDataTargetClient, target.Post_Command);
        }
        SOSConnection2OptionsAlternate source = objOptions.getConnectionOptions().Source();
        if (source.AlternateOptionsUsed.isTrue()) {
            executeCommands(objDataSourceClient, source.Alternatives().Post_Command);
        } else {
            executeCommands(objDataSourceClient, source.Post_Command);
        }
    }

    private void executePreCommands() {
        SOSConnection2OptionsAlternate target = objOptions.getConnectionOptions().Target();
        if (target.AlternateOptionsUsed.isTrue()) {
            executeCommands(objDataTargetClient, target.Alternatives().Pre_Command);
        } else {
            executeCommands(objDataTargetClient, objOptions.Pre_Command);
            executeCommands(objDataTargetClient, target.Pre_Command);
        }
        SOSConnection2OptionsAlternate source = objOptions.getConnectionOptions().Source();
        if (source.AlternateOptionsUsed.isTrue()) {
            executeCommands(objDataSourceClient, source.Alternatives().Pre_Command);
        } else {
            executeCommands(objDataSourceClient, source.Pre_Command);
        }
    }

    public boolean FileExists() {
        ISOSVirtualFile objTargetFile = objDataTargetClient.getFileHandle(MakeFullPathName(objOptions.TargetDir.Value(), strTargetFileName));
        try {
            flgFileExists = objTargetFile.FileExists();
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
        if (objOptions.ResultSetFileName.Value().endsWith(".source.tmp")) {
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
        if (objOptions.compress_files.isTrue()) {
            strTargetFileName = strTargetFileName + objOptions.compressed_file_extension.Value();
        }
        if (objOptions.CumulateFiles.isTrue()) {
            strTargetFileName = objOptions.CumulativeFileName.Value();
            strTargetTransferName = strTargetFileName;
            objOptions.append_files.value(true);
        } else {
            strTargetFileName = getFileNameWithoutPath(strTargetFileName);
            strTargetTransferName = strTargetFileName;
            if (objOptions.getreplacing().IsNotEmpty()) {
                try {
                    strTargetFileName = objOptions.getreplacing().doReplace(strTargetFileName, objOptions.getreplacement().Value());
                } catch (Exception e) {
                    throw new JobSchedulerException(SOSVfs_E_0150.get() + " " + e.toString(), e);
                }
            }
        }
        if (objOptions.isAtomicTransfer() || objOptions.TransactionMode.isTrue()) {
            strTargetTransferName = MakeAtomicFileName(objOptions);
        }
        if (flgIncludeSubdirectories) {
            String strSourceDir = getPathWithoutFileName(fleSourceFile.getName());
            String strOrigSourceDir = objOptions.SourceDir().Value();
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
                        objDataTargetClient.mkdir(addFileSeparator(objOptions.TargetDir().Value()) + strSubFolder);
                    }
                } catch (IOException e) {
                    throw new JobSchedulerException(e);
                }
            }
        }
        return null;
    }

    public void setRenamedSourceFilename() {
        SOSConnection2OptionsAlternate sourceOptions = objOptions.Source();
        if (sourceOptions.replacing.isDirty() && !objOptions.remove_files.value()) {
            String replaceWith = sourceOptions.replacement.Value();
            try {
                String sourceFileName = new File(strSourceFileName).getName();
                String sourceParent = strSourceFileName.substring(0, strSourceFileName.length() - sourceFileName.length());
                String newSourceFileName = sourceOptions.replacing.doReplace(sourceFileName, replaceWith).replace('\\', '/');
                if (!newSourceFileName.equals(sourceFileName)) {
                    String sourceDir = addFileSeparator(sourceOptions.Directory.Value());
                    if (objOptions.recursive.value()) {
                        String subDirs = sourceParent.substring(sourceDir.length());
                        newSourceFileName = newSourceFileName.replaceFirst("([^/]*)$", subDirs + "$1");
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
        return objOptions.TargetDir.Value() + strTargetFileName;
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

    public void Log4Debug() {
        logger.debug(SOSVfs_D_218.params(this.SourceFileName()));
        logger.debug(SOSVfs_D_219.params(this.SourceTransferName()));
        logger.debug(SOSVfs_D_220.params(this.TargetTransferName()));
        logger.debug(SOSVfs_D_221.params(this.TargetFileName()));
    }

    public String MakeAtomicFileName(final ISOSFtpOptions objO) {
        String strAtomicSuffix = objO.getatomic_suffix().Value();
        String strAtomicPrefix = objO.getatomic_prefix().Value();
        strTargetTransferName = strTargetTransferName + strAtomicSuffix.trim();
        strTargetTransferName = strAtomicPrefix + strTargetTransferName;
        strAtomicFileName = strTargetTransferName.trim();
        return strAtomicFileName;
    }

    private String MakeFileNameReplacing(final String pstrFileName) {
        String strR = adjustFileSeparator(pstrFileName);
        String strReplaceWith = objOptions.getreplacement().Value();
        try {
            strR = objOptions.getreplacing().doReplace(strR, strReplaceWith);
        } catch (Exception e) {
            logger.error(e.toString(), new JobSchedulerException(SOSVfs_E_0150.get(), e));
            throw new JobSchedulerException(SOSVfs_E_0150.get(), e);
        }
        return strR;
    }

    public long NoOfBytesTransferred() {
        return lngNoOfBytesTransferred;
    }

    public void NoOfBytesTransferred(final long plngNoOfBytesTransferred) {
        lngNoOfBytesTransferred = plngNoOfBytesTransferred;
        logger.info(SOSVfs_D_0112.params(plngNoOfBytesTransferred));
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

    public void Options(final ISOSFtpOptions objOptions2) {
        objOptions = (SOSFTPOptions) objOptions2;
    }

    private void RaiseException(final String pstrM) {
        this.TransferStatus(enuTransferStatus.transfer_aborted);
        logger.error(pstrM);
        throw new JobSchedulerException(pstrM);
    }

    @Deprecated
    public String RemoteFileName() {
        return strTargetFileName;
    }

    public void renameSourceFile() {
        RenameSourceFile(objDataSourceClient.getFileHandle(strSourceFileName));
    }

    private void RenameSourceFile(final ISOSVirtualFile sourceFile) {
        if (strRenamedSourceFileName != null) {
            try {
                ISOSVirtualFile renamedSourceFile = objDataSourceClient.getFileHandle(strRenamedSourceFileName);
                if (renamedSourceFile.FileExists()) {
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
                if (!sourceFile.FileExists() && renamedSourceFile.FileExists()) {
                    renamedSourceFile.rename(strSourceFileName);
                }
            } catch (Exception e) {
                throw new JobSchedulerException(SOSVfs_E_0150.get(), e);
            }
        }
    }

    public void renameTargetFile() {
        if (!skipTransfer()) {
            RenameTargetFile(objDataTargetClient.getFileHandle(MakeFullPathName(objOptions.TargetDir.Value(), strTargetFileName)));
        }
    }

    private void RenameTargetFile(ISOSVirtualFile targetFile) {
        String newFileName = targetFile.getName();
        newFileName = resolveDotsInPath(newFileName);
        if (!fileNamesAreEqual(objTargetTransferFile.getName(), newFileName, false)) {
            try {
                if (objOptions.overwrite_files.isTrue() && targetFile.FileExists()) {
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

    private String replaceVariables(final String pstrReplaceIn) {
        String strT = pstrReplaceIn;
        String renamedSourceFileName = (strRenamedSourceFileName != null) ? strRenamedSourceFileName : "";
        strT = strT.replace("$TargetFileName", resolveDotsInPath(MakeFullPathName(objOptions.TargetDir.Value(), strTargetFileName)));
        strT = strT.replace("$TargetTransferFileName", resolveDotsInPath(MakeFullPathName(objOptions.TargetDir.Value(), strTargetTransferName)));
        strT = strT.replace("$SourceFileName", resolveDotsInPath(strSourceFileName));
        strT = strT.replace("$SourceTransferFileName", resolveDotsInPath(strSourceTransferName));
        strT = strT.replace("$RenamedSourceFileName", renamedSourceFileName);
        Properties objProp = objOptions.getTextProperties();
        objProp.put("TargetFileName", strTargetFileName);
        objProp.put("TargetTransferFileName", strTargetTransferName);
        objProp.put("SourceFileName", strSourceFileName);
        objProp.put("SourceTransferFileName", strSourceTransferName);
        objProp.put("$TargetDirName", objOptions.TargetDir.Value());
        objProp.put("TargetDirName", objOptions.TargetDir.Value());
        objProp.put("$SourceDirName", objOptions.SourceDir.Value());
        objProp.put("SourceDirName", objOptions.SourceDir.Value());
        objProp.put("$RenamedSourceFileName", renamedSourceFileName);
        objProp.put("RenamedSourceFileName", renamedSourceFileName);
        strT = objOptions.replaceVars(strT);
        return strT;
    }

    @Override
    public void run() {
        boolean flgNewConnectionUsed = false;
        try {
            flgNewConnectionUsed = false;
            if (objDataSourceClient == null) {
                setDataSourceClient((ISOSVfsFileTransfer) objConnPoolSource.getUnused());
            }
            if (objDataTargetClient == null & objOptions.NeedTargetClient()) {
                setDataTargetClient((ISOSVfsFileTransfer) objConnPoolTarget.getUnused());
            }
            ISOSVirtualFile objSourceFile = objDataSourceClient.getFileHandle(strSourceFileName);
            if (objSourceFile.notExists()) {
                throw new JobSchedulerException(SOSVfs_E_226.params(strSourceFileName));
            }
            File subParent = null;
            String subPath = "";
            String strTargetFolderName = objOptions.TargetDir.Value();
            String localDir = objOptions.SourceDir.Value();
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
            this.setRenamedSourceFilename();
            if (objOptions.transactional.value()) {
                this.setTransactionalLocalFile();
            }
            switch (objOptions.operation.value()) {
            case getlist:
                return;
            case delete:
                objSourceFile.delete();
                logger.debug(SOSVfs_I_0113.params(strSourceFileName));
                this.setStatus(enuTransferStatus.deleted);
                return;
            case rename:
                File fleT = new File(strSourceFileName);
                String strParent = changeBackslashes(normalized(fleT.getParent()));
                String strNewFileName = MakeFileNameReplacing(fleT.getName());
                if (strNewFileName.contains("/") && objOptions.makeDirs.isTrue()) {
                    String strP = normalized(new File(strNewFileName).getParent());
                    objDataSourceClient.mkdir(strP);
                }
                strNewFileName = changeBackslashes(addFileSeparator(strParent) + strNewFileName);
                logger.debug(SOSVfs_I_150.params(strSourceFileName, strNewFileName));
                strTargetFileName = strNewFileName;
                objSourceFile.rename(strNewFileName);
                this.setStatus(enuTransferStatus.renamed);
                return;
            default:
                break;
            }
            ISOSVirtualFile objTargetFile = objDataTargetClient.getFileHandle(MakeFullPathName(objOptions.TargetDir.Value(), strTargetFileName));
            if (objOptions.CumulateFiles.isTrue()) {
                if (objOptions.CumulativeFileDelete.isTrue() && !objOptions.flgCumulativeTargetDeleted) {
                    objTargetFile.delete();
                    objOptions.flgCumulativeTargetDeleted = true;
                    logger.debug(String.format("cumulative file '%1$s' deleted.", strTargetFileName));
                }
            }
            objTargetFile.setModeAppend(objOptions.append_files.value());
            objTargetFile.setModeRestart(objOptions.ResumeTransfer.value());
            if (!fileNamesAreEqual(strTargetFileName, strTargetTransferName, false)) {
                objTargetTransferFile = objDataTargetClient.getFileHandle(MakeFullPathName(objOptions.TargetDir.Value(), strTargetTransferName));
            } else {
                objTargetTransferFile = objTargetFile;
            }
            if (objOptions.CumulateFiles.isTrue()) {
                objTargetTransferFile.setModeAppend(objOptions.append_files.value());
                objTargetTransferFile.setModeRestart(objOptions.ResumeTransfer.value());
            }
            strSourceTransferName = getFileNameWithoutPath(strSourceTransferName);
            objSourceTransferFile =
                    objDataSourceClient.getFileHandle(MakeFullPathName(getPathWithoutFileName(strSourceFileName), strSourceTransferName));
            if (eTransferStatus == enuTransferStatus.ignoredDueToZerobyteConstraint) {
                String strM = SOSVfs_D_0110.params(strSourceFileName);
                logger.debug(strM);
                objJadeReportLogger.info(strM);
            }
            if (objOptions.DoNotOverwrite()) {
                flgFileExists = objTargetFile.FileExists();
                if (flgFileExists) {
                    this.setTargetFileAlreadyExists(true);
                    logger.debug(SOSVfs_E_228.params(strTargetFileName));
                    this.setNotOverwritten();
                }
            }

            if (!skipTransfer()) {
                this.doTransfer(objSourceTransferFile, objTargetTransferFile);
                if (objOptions.KeepModificationDate.isTrue()) {
                    long pdteDateTime = objSourceFile.getModificationDateTime();
                    if (pdteDateTime != -1) {
                        objTargetFile.setModificationDateTime(pdteDateTime);
                    }
                }
                if ((objOptions.isAtomicTransfer() || objOptions.isReplaceReplacingInEffect()) && objOptions.transactional.isFalse()) {
                    RenameTargetFile(objTargetFile);
                }
            }
            if (objOptions.transactional.isFalse()) {
                createChecksumFile(objTargetFile.getName());
                if (strRenamedSourceFileName != null) {
                    RenameSourceFile(objSourceFile);
                }
            }
        } catch (JobSchedulerException e) {
            logger.error(SOSVfs_E_229.params(e));
            throw e;
        } catch (Exception e) {
            String strT = SOSVfs_E_229.params(e);
            logger.error(strT);
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
        if (!(objOptions.check_size.isFalse() || objOptions.compress_files.isTrue() || objOptions.transfer_mode.isAscii()
                || objConnectOptions.Source().transfer_mode.isAscii() || objConnectOptions.Target().transfer_mode.isAscii())) {
            if (lngFileSize <= 0) {
                lngFileSize = objDataSourceClient.getFileHandle(strSourceFileName).getFileSize();
            }
            if (lngFileSize != plngNoOfBytesTransferred) {
                logger.error(SOSVfs_E_216.params(plngNoOfBytesTransferred, lngFileSize, strSourceFileName));
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
        dteEndTransfer = Now();
        logger.warn(SOSVfs_D_0111.params(strSourceFileName));
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
            dteEndTransfer = Now();
            strM = SOSVfs_I_0116.get();
            break;
        case transferring:
            strM = SOSVfs_I_0117.get();
            dteStartTransfer = Now();
            break;
        case transferred:
            strM = SOSVfs_I_0118.get();
            dteEndTransfer = Now();
            break;
        case transfer_aborted:
            strM = SOSVfs_I_0119.get();
            dteEndTransfer = Now();
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
        dteEndTransfer = Now();
        String strM = SOSVfs_D_0110.params(strSourceFileName);
        logger.debug(strM);
        objJadeReportLogger.info(strM);
    }

    public void setIgnoredDueToZerobyteConstraint() {
        eTransferStatus = enuTransferStatus.ignoredDueToZerobyteConstraint;
        dteEndTransfer = Now();
    }

    public boolean SourceFileExists() {
        boolean flgT = false;
        try {
            flgT = objDataSourceClient.getFileHandle(strSourceFileName).FileExists();
        } catch (Exception e) {
        }
        return flgT;
    }

    public String SourceFileName() {
        return strSourceFileName;
    }

    public String SourceTransferName() {
        return strSourceTransferName;
    }

    public String TargetFileName() {
        return strTargetFileName;
    }

    public String TargetTransferName() {
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
        String mandator = objOptions.mandator.Value(); // 0-
        String pid = "0";
        try {
            pid = ManagementFactory.getRuntimeMXBean().getName();
            String[] arr = pid.split("@");
            pid = arr[0];
        } catch (Exception e) {
            pid = "0";
        }
        String ppid = System.getProperty(FIELD_PPID, "0");
        String operation = objOptions.operation.Value();
        SOSConnection2OptionsAlternate source = objOptions.getConnectionOptions().Source();
        if (source.AlternateOptionsUsed.isTrue()) {
            source = source.Alternatives();
        }
        SOSConnection2OptionsAlternate target = objOptions.getConnectionOptions().Target();
        if (target.AlternateOptionsUsed.isTrue()) {
            target = target.Alternatives();
        }
        String localhost = source.host.getLocalHostIfHostIsEmpty();
        String localhost_ip = source.host.getLocalHostAdressIfHostIsEmpty();
        String local_user = source.user.getSystemUserIfUserIsEmpty();
        String remote_host = target.host.getLocalHostIfHostIsEmpty();
        String remote_host_ip = target.host.getLocalHostAdressIfHostIsEmpty();
        String remote_user = target.user.getSystemUserIfUserIsEmpty();
        String protocol = target.protocol.Value();
        String port = target.port.Value();
        String local_dir = source.Directory.Value();
        if (isEmpty(local_dir)) {
            local_dir = "";
        } else {
            local_dir = normalized(local_dir);
        }
        String remote_dir = target.Directory.Value();
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
        String log_filename = objOptions.log_filename.Value();
        String jump_host = objOptions.jump_host.Value();
        String jump_user = objOptions.jump_user.Value();
        String jump_host_ip = "";
        String jump_port = "";
        String jump_protocol = "";
        if (!isEmpty(jump_host) && !isEmpty(jump_user)) {
            jump_host_ip = objOptions.jump_host.getHostAdress();
            jump_port = objOptions.jump_port.Value();
            jump_protocol = objOptions.jump_protocol.Value();
        }
        Date endTime = this.getEndTime();
        Date startTime = this.getStartTime();
        if (startTime == null) {
            startTime = Now();
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
                    last_error_message =
                            normalizeErrorMessageForXml(StringEscapeUtils.unescapeXml(jumpHistoryRecord.getOrDefault(FIELD_LAST_ERROR_MESSAGE, "")));
                } else {
                    last_error_message = jumpHistoryRecord.getOrDefault(FIELD_LAST_ERROR_MESSAGE, "");
                }
                fileAttributes.put(FIELD_LAST_ERROR_MESSAGE, last_error_message);
            } else if ("copyFromInternet".equals(objOptions.getDmzOptions().get("operation"))) {
                fileAttributes.put(FIELD_JUMP_HOST, fileAttributes.get(FIELD_LOCALHOST));
                fileAttributes.put(FIELD_JUMP_HOST_IP, fileAttributes.get(FIELD_LOCALHOST_IP));
                fileAttributes.put(FIELD_JUMP_PORT, source.port.Value());
                fileAttributes.put(FIELD_JUMP_PROTOCOL, source.protocol.Value());
                fileAttributes.put(FIELD_JUMP_USER, source.user.Value());
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
        strB.append(getTransferTimeAsString(Now()));
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
            strT = SOSVfs_D_214.params(this.getTargetFileNameAndPath(), this.SourceFileName(), this.NoOfBytesTransferred(),
                            objOptions.operation.Value());
        } catch (RuntimeException e) {
            logger.error(e.toString());
            strT = "???";
        }
        return strT;
    }

    public void TransferStatus(final enuTransferStatus peTransferStatus) {
        this.setStatus(peTransferStatus);
    }

    public void VfsHandler(final ISOSVfsFileTransfer pobjVfs) {
        objVfsHandler = pobjVfs;
    }

    private boolean fileNamesAreEqual(String filenameA, String filenameB, boolean caseSensitiv) {
        String a = filenameA.replaceAll("[\\\\/]+", "/");
        String b = filenameB.replaceAll("[\\\\/]+", "/");
        return (caseSensitiv) ? a.equals(b) : a.equalsIgnoreCase(b);
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
        if (objOptions.compress_files.isTrue()) {
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

}
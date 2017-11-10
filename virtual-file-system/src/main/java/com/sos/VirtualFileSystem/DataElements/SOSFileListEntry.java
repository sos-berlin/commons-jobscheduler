package com.sos.VirtualFileSystem.DataElements;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.util.Base64;
import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionTime;
import com.sos.JSHelper.interfaces.ISOSFtpOptions;
import com.sos.JSHelper.io.Files.JSTextFile;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.IJadeTransferDetailHistoryData;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSConnection2Options;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;
import com.sos.VirtualFileSystem.common.SOSVfsConstants;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;
import com.sos.VirtualFileSystem.zip.SOSVfsZip;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author Klaus Buettner */
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
    private static final String FIELD_TRANSFER_TIMESTAMP = "transfer_timestamp";
    private static final String FIELD_MANDATOR = "mandator";
    private static final String FIELD_GUID = "guid";
    private static final Logger logger = Logger.getLogger(SOSFileListEntry.class);
    private static final Logger objJadeReportLogger = Logger.getLogger(VFSFactory.getLoggerName());
    private static String conClassName = "SOSFileListEntry";
    private ISOSVirtualFile fleSourceTransferFile = null;
    private ISOSVirtualFile fleSourceFile = null;
    private ISOSVirtualFile fleTargetFile = null;
    private String strSourceFileName = null;
    private String strSourceTransferName = null;
    private String strTargetTransferName = null;
    private String strTargetFileName = null;
    private long lngNoOfBytesTransferred = 0;
    private long lngFileSize = -1L;
    private long lastCheckedFileSize = -1L;
    private long lngFileModDate = -1L;
    private long lngTransferProgress = 0;
    private boolean flgTransactionalRemoteFile = false;
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
    private String strMD5Hash = "n.a.";
    private Date dteStartTransfer = null;
    private Date dteEndTransfer = null;
    private ISOSVfsFileTransfer objVfsHandler = null;
    private final String guid = UUID.randomUUID().toString();
    private boolean flgSteadyFlag = false;
    private final boolean flgTransferHistoryAlreadySent = false;
    private boolean targetFileAlreadyExists = false;
    private FTPFile objFTPFile = null;
    private String strCSVRec = new String();
    private String strRenamedSourceFileName = null;
    private SOSVfsConnectionPool objConnPoolSource = null;
    private SOSVfsConnectionPool objConnPoolTarget = null;
    private String errorMessage = null;
    public long zeroByteCount = 0;
    public boolean flgIsHashFile = false;

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

    private void addCSv(final String... pstrVal) {
        for (String string : pstrVal) {
            if (!strCSVRec.isEmpty()) {
                strCSVRec += ";";
            }
            strCSVRec += string;
        }
    }

    private String changeBackslashes(final String pstrV) {
        return pstrV.replaceAll("\\\\", "/");
    }

    public boolean CheckFileSizeIsChanging() throws Exception {
        final String conMethodName = conClassName + "::polling";
        String lastmd5file = "";
        String message = "";
        try {
            String fileName = strSourceFileName;
            return true;
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_225.params(conMethodName, e));
        }
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
        boolean createSecurityHash = objOptions.CreateSecurityHash.value() && flgIsHashFile == false;
        MessageDigest md = null;
        if (createSecurityHash) {
            try {
                md = MessageDigest.getInstance(objOptions.SecurityHashType.Value());
            } catch (NoSuchAlgorithmException e1) {
                logger.error(e1.getLocalizedMessage(), e1);
                createSecurityHash = false;
            }
        }
        executePreCommands();
        long totalBytesTransferred = 0;
        Base64 base64 = null;
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
                    if (createSecurityHash) {
                        md.update(compressedBytes.getBytes());
                    }
                }
                if (source.getFileSize() <= 0) {
                    Buffer compressedBytes = compress(new byte[0]);
                    target.write(compressedBytes.getBytes());
                    if (createSecurityHash) {
                        md.update(compressedBytes.getBytes());
                    }
                } else {
                    while ((bytesTransferred = source.read(buffer)) != -1) {
                        try {
                            Buffer compressedBytes = compress(buffer, bytesTransferred);
                            target.write(compressedBytes.getBytes(), 0, compressedBytes.getLength());
                            if (createSecurityHash) {
                                md.update(compressedBytes.getBytes(), 0, compressedBytes.getLength());
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
            if (createSecurityHash) {
                strMD5Hash = toHexString(md.digest());
                logger.debug(SOSVfs_I_274.params(strMD5Hash, strSourceTransferName, objOptions.SecurityHashType.Value()));
                if (objOptions.CreateSecurityHashFile.isTrue()) {
                    JSTextFile file = new JSTextFile(strSourceFileName + "." + objOptions.SecurityHashType.Value());
                    file.WriteLine(strMD5Hash);
                    file.close();
                    file.deleteOnExit();
                }
            }
            this.setNoOfBytesTransferred(totalBytesTransferred);
            totalBytesTransferred += cumulativeFileSeperatorLength;
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
        final String conMethodName = conClassName + "::executeCommands";
        if (pstrCommandString.IsNotEmpty()) {
            String strT = pstrCommandString.Value();
            strT = replaceVariables(strT);
            String strM = SOSVfs_D_0151.params(strT);
            logger.debug(strM);
            String[] strA = strT.split(";");
            for (String strCmd : strA) {
                try {
                    pobjDataClient.getHandler().ExecuteCommand(strCmd);
                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage());
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
        return strMD5Hash;
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
        fleSourceTransferFile = null;
        fleSourceFile = objDataSourceClient.getFileHandle(strSourceFileName);
        fleTargetFile = null;
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
            setTransactionalRemoteFile();
            strTargetTransferName = MakeAtomicFileName(objOptions);
        }
        if (flgIncludeSubdirectories) {
            String strSourceDir = getPathWithoutFileName(fleSourceFile.getName());
            String strOrigSourceDir = objOptions.SourceDir().Value();
            if (!fileNamesAreEqual(strSourceDir, strOrigSourceDir, true)) {
                if (strSourceDir.length() > strOrigSourceDir.length()) {
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
                    String sourceDir = addFileSeparator(sourceOptions.Directory.Value()).replace('\\', '/');
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
        String strT = objOptions.TargetDir.Value() + strTargetFileName;
        return strT;
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
            logger.error(e.getLocalizedMessage(), new JobSchedulerException(SOSVfs_E_0150.get(), e));
            throw new JobSchedulerException(SOSVfs_E_0150.get(), e);
        }
        return strR;
    }

    public long NoOfBytesTransferred() {
        return lngNoOfBytesTransferred;
    }

    public void NoOfBytesTransferred(final long plngNoOfBytesTransferred) {
        lngNoOfBytesTransferred = plngNoOfBytesTransferred;
        String strM = SOSVfs_D_0112.params(plngNoOfBytesTransferred);
        logger.info(strM);
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

    private void RaiseException(final Exception e, final String pstrM) {
        logger.error(pstrM + " (" + e.getLocalizedMessage() + ")");
        throw new JobSchedulerException(pstrM, e);
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
                if (newFileName.contains("/") && objOptions.makeDirs.isTrue()) {
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
        String replaced = value;

        Path targetDir = Paths.get(objOptions.TargetDir.Value());
        Path sourceDir = Paths.get(objOptions.SourceDir.Value());

        EntryPaths targetFile = new EntryPaths(targetDir, resolveDotsInPath(MakeFullPathName(objOptions.TargetDir.Value(), strTargetFileName)));
        EntryPaths targetTransferFile = new EntryPaths(targetDir, resolveDotsInPath(MakeFullPathName(objOptions.TargetDir.Value(), strTargetTransferName)));

        EntryPaths sourceFile = new EntryPaths(sourceDir, resolveDotsInPath(strSourceFileName));
        EntryPaths sourceFileRenamed = new EntryPaths(sourceDir, strRenamedSourceFileName);

        // deprecated vars
        replaced = replaced.replace("$TargetFileName", targetFile.getFullName());
        replaced = replaced.replace("$TargetTransferFileName", targetTransferFile.getFullName());
        replaced = replaced.replace("$SourceFileName", sourceFile.getFullName());
        replaced = replaced.replace("$SourceTransferFileName", resolveDotsInPath(strSourceTransferName));
        replaced = replaced.replace("$RenamedSourceFileName", sourceFileRenamed.getFullName());

        Properties vars = objOptions.getTextProperties();
        // deprecated vars
        vars.put("TargetFileName", targetFile.getRelativeName());
        vars.put("TargetTransferFileName", targetTransferFile.getFullName());
        vars.put("SourceFileName", sourceFile.getFullName());
        vars.put("SourceTransferFileName", strSourceTransferName);
        vars.put("RenamedSourceFileName", sourceFileRenamed.getFullName());
        vars.put("TargetDirName", objOptions.TargetDir.Value());
        vars.put("SourceDirName", objOptions.SourceDir.Value());

        // new vars
        vars.put("TargetDirFullName", targetDir.normalize().toString().replace('\\', '/'));
        vars.put("SourceDirFullName", sourceDir.normalize().toString().replace('\\', '/'));

        vars.put("TargetFileFullName", targetFile.getFullName());
        vars.put("TargetFileRelativeName", targetFile.getRelativeName());
        vars.put("TargetFileBaseName", targetFile.getBaseName());
        vars.put("TargetFileParentFullName", targetFile.getParentFullName());
        vars.put("TargetFileParentBaseName", targetFile.getParentBaseName());

        vars.put("TargetTransferFileFullName", targetTransferFile.getFullName());
        vars.put("TargetTransferFileRelativeName", targetTransferFile.getRelativeName());
        vars.put("TargetTransferFileBaseName", targetTransferFile.getBaseName());
        vars.put("TargetTransferFileParentFullName", targetTransferFile.getParentFullName());
        vars.put("TargetTransferFileParentBaseName", targetTransferFile.getParentBaseName());

        vars.put("SourceFileFullName", sourceFile.getFullName());
        vars.put("SourceFileRelativeName", sourceFile.getRelativeName());
        vars.put("SourceFileBaseName", sourceFile.getBaseName());
        vars.put("SourceFileParentFullName", sourceFile.getParentFullName());
        vars.put("SourceFileParentBaseName", sourceFile.getParentBaseName());

        vars.put("SourceFileRenamedFullName", sourceFileRenamed.getFullName());
        vars.put("SourceFileRenamedRelativeName", sourceFileRenamed.getRelativeName());
        vars.put("SourceFileRenamedBaseName", sourceFileRenamed.getBaseName());
        vars.put("SourceFileRenamedParentFullName", sourceFileRenamed.getParentFullName());
        vars.put("SourceFileRenamedParentBaseName", sourceFileRenamed.getParentBaseName());

        return objOptions.replaceVars(replaced);
    }

    @Override
    public void run() {
        boolean flgNewConnectionUsed = false;
        try {
            logger.info(SOSVfs_I_0108.params(strSourceFileName));
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
            objSourceTransferFile = objDataSourceClient.getFileHandle(MakeFullPathName(getPathWithoutFileName(strSourceFileName), strSourceTransferName));
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
            if (objOptions.transactional.isFalse() && strRenamedSourceFileName != null) {
                RenameSourceFile(objSourceFile);
            }
        } catch (JobSchedulerException e) {
            String strT = SOSVfs_E_229.params(e);
            logger.error(strT);
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

    public String SecurityHash() {
        return strMD5Hash;
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
            } else {
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

    public void setTransactionalRemoteFile() {
        flgTransactionalRemoteFile = true;
    }

    public void setTransferProgress(final long plngTransferProgress) {
        lngTransferProgress = plngTransferProgress;
        this.setStatus(enuTransferStatus.transferInProgress);
        String lstrTargetFileName = strTargetFileName;
        if (objDataTargetClient.getHandler() instanceof SOSVfsZip) {
            lstrTargetFileName = SOSVfs_D_217.params(objOptions.remote_dir.Value(), strTargetFileName);
        }
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
        msg = msg.replaceAll("\n\r", " ");
        msg = msg.replaceAll("\n", " ");
        msg = StringEscapeUtils.escapeXml(msg);
        if (msg.length() > 150) {
            msg = msg.substring(0, 150);
        }
        return msg;
    }

    private String normalizeErrorMessageForCSV(String msg) {
        msg = msg.replaceAll("\n\r", " ");
        msg = msg.replaceAll("\n", " ");
        return StringEscapeUtils.escapeCsv(msg);
    }

    public Properties getFileAttributesAsProperties(HistoryRecordType recordType) {
        Properties properties = new Properties();
        String mandator = objOptions.mandator.Value(); // 0-
        String transfer_timestamp = EMPTY_STRING;
        try {
            transfer_timestamp = sos.util.SOSDate.getCurrentTimeAsString();
        } catch (Exception e) {
        }
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
            local_dir = fleSourceFile.getParentVfs();
            if (isEmpty(local_dir)) {
                local_dir = "n.a";
            }
        }
        local_dir = normalized(local_dir);
        String remote_dir = target.Directory.Value();
        if (isEmpty(remote_dir)) {
            remote_dir = fleTargetFile.getParentVfs();
            if (isEmpty(remote_dir)) {
                remote_dir = "n.a";
            }
        }
        remote_dir = normalized(remote_dir);
        String local_filename = this.getSourceFilename();
        local_filename = adjustFileSeparator(local_filename);
        String remote_filename = this.getTargetFilename();
        if (isEmpty(remote_filename)) {
            remote_filename = "n.a.";
        } else {
            remote_filename = adjustFileSeparator(remote_filename);
        }
        String fileSize = String.valueOf(this.getFileSize());
        String md5 = this.getMd5();
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
        properties.put(FIELD_GUID, this.guid);
        properties.put(FIELD_MANDATOR, mandator);
        properties.put(FIELD_TRANSFER_TIMESTAMP, transfer_timestamp);
        properties.put(FIELD_PID, pid);
        properties.put(FIELD_PPID, ppid);
        properties.put(FIELD_OPERATION, operation);
        properties.put(FIELD_LOCALHOST, localhost);
        properties.put(FIELD_LOCALHOST_IP, localhost_ip);
        properties.put(FIELD_LOCAL_USER, local_user);
        properties.put(FIELD_REMOTE_HOST, remote_host);
        properties.put(FIELD_REMOTE_HOST_IP, remote_host_ip);
        properties.put(FIELD_REMOTE_USER, remote_user);
        properties.put(FIELD_PROTOCOL, protocol);
        properties.put(FIELD_PORT, port);
        properties.put(FIELD_LOCAL_DIR, local_dir);
        properties.put(FIELD_REMOTE_DIR, remote_dir);
        properties.put(FIELD_LOCAL_FILENAME, local_filename);
        properties.put(FIELD_REMOTE_FILENAME, remote_filename);
        properties.put(FIELD_FILE_SIZE, fileSize);
        properties.put(FIELD_MD5, md5);
        String status = this.getStatusText();
        if ("transferred".equalsIgnoreCase(status)) {
            status = "success";
        }
        properties.put(FIELD_STATUS, status);
        properties.put(FIELD_LAST_ERROR_MESSAGE, last_error_message);
        properties.put(FIELD_LOG_FILENAME, log_filename);
        properties.put(FIELD_JUMP_HOST, jump_host);
        properties.put(FIELD_JUMP_HOST_IP, jump_host_ip);
        properties.put(FIELD_JUMP_PORT, jump_port);
        properties.put(FIELD_JUMP_PROTOCOL, jump_protocol);
        properties.put(FIELD_JUMP_USER, jump_user);
        return properties;
    }

    public String toCsv() {
        HashMap<String, String> properties = new HashMap(getFileAttributesAsProperties(HistoryRecordType.CSV));
        addCSv(properties.get(FIELD_GUID));
        addCSv(properties.get(FIELD_MANDATOR));
        addCSv(properties.get(FIELD_TRANSFER_TIMESTAMP));
        addCSv(properties.get(FIELD_PID));
        addCSv(properties.get(FIELD_PPID));
        addCSv(properties.get(FIELD_OPERATION));
        addCSv(properties.get(FIELD_LOCALHOST));
        addCSv(properties.get(FIELD_LOCALHOST_IP));
        addCSv(properties.get(FIELD_LOCAL_USER));
        addCSv(properties.get(FIELD_REMOTE_HOST));
        addCSv(properties.get(FIELD_REMOTE_HOST_IP));
        addCSv(properties.get(FIELD_REMOTE_USER));
        addCSv(properties.get(FIELD_PROTOCOL));
        addCSv(properties.get(FIELD_PORT));
        addCSv(properties.get(FIELD_LOCAL_DIR));
        addCSv(properties.get(FIELD_REMOTE_DIR));
        addCSv(properties.get(FIELD_LOCAL_FILENAME));
        addCSv(properties.get(FIELD_REMOTE_FILENAME));
        addCSv(properties.get(FIELD_FILE_SIZE));
        addCSv(properties.get(FIELD_MD5));
        addCSv(properties.get(FIELD_STATUS));
        addCSv(properties.get(FIELD_LAST_ERROR_MESSAGE));
        addCSv(properties.get(FIELD_LOG_FILENAME));
        addCSv(properties.get(FIELD_JUMP_HOST));
        addCSv(properties.get(FIELD_JUMP_HOST_IP));
        addCSv(properties.get(FIELD_JUMP_PORT));
        addCSv(properties.get(FIELD_JUMP_PROTOCOL));
        addCSv(properties.get(FIELD_JUMP_USER));
        SOSOptionTime modified = new SOSOptionTime(null, null, null, "", "", false);
        modified.value(lngFileModDate);
        addCSv(modified.getTimeAsString(lngFileModDate));
        return strCSVRec;
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
            strT = SOSVfs_D_214.params(this.getTargetFileNameAndPath(), this.SourceFileName(), this.NoOfBytesTransferred(), objOptions.operation.Value());
        } catch (RuntimeException e) {
            logger.error(e.getLocalizedMessage());
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
        return caseSensitiv ? a.equals(b) : a.equalsIgnoreCase(b);
    }

    public boolean isTargetFileAlreadyExists() {
        return targetFileAlreadyExists;
    }

    public void setTargetFileAlreadyExists(boolean targetFileAlreadyExists) {
        this.targetFileAlreadyExists = targetFileAlreadyExists;
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

    private class EntryPaths {

        private String fullName = "";
        private String relativeName = "";
        private String baseName = "";
        private String parentFullName = "";
        private String parentBaseName = "";

        private EntryPaths(final Path baseDir, final String filePath) {
            if (filePath != null) {
                try {
                    Path path = Paths.get(filePath);

                    fullName = path.toString().replace('\\', '/');
                    relativeName = baseDir.relativize(path).normalize().toString().replace('\\', '/');
                    baseName = path.getFileName().toString();

                    Path parent = path.getParent();
                    if (parent != null) {
                        parentFullName = parent.toString().replace('\\', '/');
                        parentBaseName = parent.getFileName().toString();
                    }
                } catch (Exception e) {
                    logger.warn(String.format("error on resolve path for baseDir=%s, filePath=%s", baseDir.toString(), filePath), e);
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

        public String getParentFullName() {
            return parentFullName;
        }

        public String getParentBaseName() {
            return parentBaseName;
        }

    }

}
package com.sos.VirtualFileSystem.FTP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.DataElements.SOSFileList;
import com.sos.VirtualFileSystem.DataElements.SOSFileListEntry;
import com.sos.VirtualFileSystem.DataElements.SOSFolderName;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSSession;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFileSystem;
import com.sos.VirtualFileSystem.common.SOSVfsEnv;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsFtp2 extends SOSVfsFtpBaseClass2 implements ISOSVfsFileTransfer, ISOSVFSHandler, ISOSVirtualFileSystem, ISOSConnection {

    private static final String CLASS_NAME = "SOSVfsFtp";
    private static final Logger LOGGER = Logger.getLogger(SOSVfsFtp2.class);
    private FTPClient objFTPClient = null;
    private boolean simulateShell = false;

    @Deprecated
    public SOSVfsFtp2() {
        super();
    }

    /** @param directory The new working directory.
     * @return The reply code received from the server.
     * @throws IOException If an I/O error occurs while either sending a command
     *             to the server or receiving a reply from the server. */
    @Override
    public int cd(final String directory) throws IOException {
        return Client().cwd(directory);
    }

    @Override
    public boolean changeWorkingDirectory(final String pathname) {
        final String conMethodName = CLASS_NAME + "::changeWorkingDirectory";
        boolean flgR = true;
        try {
            String strT = pathname.replaceAll("\\\\", "/");
            Client().cwd(strT);
            LOGGER.debug(SOSVfs_D_135.params(strT, getReplyString(), "[directory exists]"));
            flgR = objFTPReply.isSuccessCode();
        } catch (IOException e) {
            raiseException(e, getHostID(SOSVfs_E_0105.params(conMethodName)));
        }
        return flgR;
    }

    @Override
    protected final FTPClient Client() {
        if (objFTPClient == null) {
            objFTPClient = new FTPClient();
            objProtocolCommandListener = new SOSFtpClientLogger(getHostID(""));
            if (objConnection2Options != null && objConnection2Options.protocolCommandListener.isTrue()) {
                objFTPClient.addProtocolCommandListener(objProtocolCommandListener);
                LOGGER.debug("ProtocolcommandListener added and activated");
            }
            String strAddFTPProtocol = System.getenv("AddFTPProtocol");
            if (strAddFTPProtocol != null && "true".equalsIgnoreCase(strAddFTPProtocol)) {
                objFTPClient.addProtocolCommandListener(objProtocolCommandListener);
            }
        }
        return objFTPClient;
    }

    @Override
    public void closeInput() {
    }

    private void closeInput(InputStream objO) {
        try {
            if (objO != null) {
                objO.close();
                objO = null;
            }
        } catch (IOException e) {
        }
    }

    private void closeObject(OutputStream objO) {
        try {
            if (objO != null) {
                objO.flush();
                objO.close();
                objO = null;
            }
        } catch (Exception e) {
        }
    }

    @Override
    public String createScriptFile(final String pstrContent) throws Exception {
        notImplemented();
        return null;
    }

    /** return a listing of the files of the current directory in long format on
     * the remote machine
     * 
     * @return a listing of the contents of the current directory on the remote
     *         machine
     * @exception Exception
     * @see #nList()
     * @see #nList(String )
     * @see #dir(String ) */
    @Override
    public SOSFileList dir() {
        final String conMethodName = CLASS_NAME + "::dir";
        try {
            return dir(".");
        } catch (Exception e) {
            raiseException(e, getHostID(SOSVfs_E_0105.params(conMethodName)));
        }
        return null;
    }

    @Override
    public SOSFileList dir(final SOSFolderName pobjFolderName) {
        this.dir(pobjFolderName.getValue());
        return null;
    }

    /** return a listing of the files in a directory in long format on the remote
     * machine
     * 
     * @param pathname on remote machine
     * @return a listing of the contents of a directory on the remote machine
     * @exception Exception
     * @see #nList()
     * @see #nList(String )
     * @see #dir() */
    @Override
    public SOSFileList dir(final String pathname) {
        Vector<String> strList = getFilenames(pathname);
        String[] strT = strList.toArray(new String[strList.size()]);
        SOSFileList objFileList = new SOSFileList(strT);
        return objFileList;
    }

    /** return a listing of a directory in long format on the remote machine
     *
     * @param pathname on remote machine
     * @return a listing of the contents of a directory on the remote machine
     * @exception Exception
     * @see #nList()
     * @see #nList(String )
     * @see #dir() */
    @Override
    public SOSFileList dir(final String pathname, final int flag) {
        final String conMethodName = CLASS_NAME + "::dir";
        SOSFileList fileList = new SOSFileList();
        FTPFile[] listFiles = null;
        try {
            listFiles = Client().listFiles(pathname);
        } catch (IOException e) {
            raiseException(e, getHostID(SOSVfs_E_0105.params(conMethodName)));
        }
        for (FTPFile listFile : listFiles) {
            if (flag > 0 && listFile.isDirectory()) {
                fileList.addAll(this.dir(pathname + "/" + listFile.toString(), flag >= 1024 ? flag : flag + 1024));
            } else {
                if (flag >= 1024) {
                    fileList.add(pathname + "/" + listFile.toString());
                } else {
                    fileList.add(listFile.toString());
                }
            }
        }
        return fileList;
    }

    @Override
    public void disconnect() {
        final String conMethodName = CLASS_NAME + "::disconnect";
        try {
            if (Client().isConnected()) {
                Client().disconnect();
            }
        } catch (IOException e) {
            raiseException(e, getHostID(SOSVfs_E_0105.params(conMethodName)));
        }
    }

    @Override
    public void executeCommand(final String strCmd) throws Exception {
        executeCommand(strCmd, null);
    }

    @Override
    public void executeCommand(final String strCmd, SOSVfsEnv env) throws Exception {
        final String conMethodName = CLASS_NAME + "::ExecuteCommand";
        objFTPClient.sendCommand(strCmd);
        LOGGER.debug(getHostID(SOSVfs_E_0106.params(conMethodName, strCmd, getReplyString())));
        objFTPClient.sendCommand("NOOP");
        getReplyString();
    }

    @Override
    public void flush() {
    }

    /** Retrieves a named file from the ftp server.
     *
     * @param localFile The name of the local file.
     * @param remoteFile The name of the remote file.
     * @exception Exception
     * @see #getFile(String, String ) */
    @Override
    public void get(final String remoteFile, final String localFile) {
        final String conMethodName = CLASS_NAME + "::get";

        FileOutputStream out = null;
        boolean rc = false;
        try {
            out = new FileOutputStream(localFile);
            rc = Client().retrieveFile(remoteFile, out);
            if (!rc) {
                raiseException(getHostID(SOSVfs_E_0105.params(conMethodName)));
            }
        } catch (IOException e) {
            raiseException(e, getHostID(SOSVfs_E_0105.params(conMethodName)));
        } finally {
            closeObject(out);
        }
    }

    public FTPClient getClient() {
        return Client();
    }

    @Override
    public ISOSConnection getConnection() {
        return this;
    }

    @Override
    public Integer getExitCode() {
        notImplemented();
        return null;
    }

    @Override
    public String getExitSignal() {
        notImplemented();
        return null;
    }

    /** Retrieves a named file from the ftp server.
     *
     * @param localFile The name of the local file.
     * @param remoteFile The name of the remote file.
     * @return The total number of bytes retrieved.
     * @see #get(String, String )
     * @exception Exception */
    @Override
    public long getFile(final String remoteFile, final String localFile) {
        final boolean flgAppendLocalFile = false;
        return this.getFile(remoteFile, localFile, flgAppendLocalFile);
    }

    /** Retrieves a named file from the ftp server.
     *
     * @param localFile The name of the local file.
     * @param remoteFile The name of the remote file.
     * @param append Appends the remote file to the local file.
     * @return The total number of bytes retrieved.
     * @see #get(String, String )
     * @exception Exception */
    @Override
    public long getFile(final String remoteFile, final String localFile, final boolean append) {
        final String conMethodName = CLASS_NAME + "::getFile";
        InputStream in = null;
        OutputStream out = null;
        long totalBytes = 0;
        try {
            in = Client().retrieveFileStream(remoteFile);
            if (in == null) {
                throw new JobSchedulerException(SOSVfs_E_143.params(getReplyString()));
            }
            if (!isPositiveCommandCompletion()) {
                throw new JobSchedulerException(SOSVfs_E_144.params("getFile()", remoteFile, getReplyString()));
            }
            byte[] buffer = new byte[4096];
            out = new FileOutputStream(new File(localFile), append);
            int bytes_read = 0;
            synchronized (this) {
                while ((bytes_read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytes_read);
                    out.flush();
                    totalBytes += bytes_read;
                }
            }
            closeInput(in);
            closeObject(out);
            this.completePendingCommand();
            if (totalBytes > 0) {
                return totalBytes;
            } else {
                return -1L;
            }
        } catch (IOException e) {
            raiseException(e, getHostID(SOSVfs_E_0105.params(conMethodName)));
        } finally {
            closeInput(in);
            closeObject(out);
        }
        return totalBytes;
    }

    @Override
    public ISOSVirtualFile getFileHandle(final String pstrFilename) {
        String strT = pstrFilename.replaceAll("\\\\", "/");
        ISOSVirtualFile objFtpFile = new SOSVfsFtpFile(strT);
        objFtpFile.setHandler(this);
        return objFtpFile;
    }

    @Override
    @Deprecated
    public String[] getFilelist(final String folder, final String regexp, final int flag, final boolean flgRecurseSubFolder, String integrityHashType) {
        vecDirectoryListing = nList(folder, flgRecurseSubFolder);
        Vector<String> strB = new Vector<String>();
        Pattern pattern = Pattern.compile(regexp, flag);
        for (String strFile : vecDirectoryListing) {
            String strFileName = new File(strFile).getName();
            // file list should not contain the checksum files
            if (integrityHashType != null && strFileName.endsWith(integrityHashType)) {
                continue;
            }
            Matcher matcher = pattern.matcher(strFileName);
            if (matcher.find()) {
                strB.add(strFile);
            }
        }
        return strB.toArray(new String[strB.size()]);
    }

    /** return a listing of the contents of a directory in short format on the
     * remote machine (without subdirectory)
     *
     * @return a listing of the contents of a directory on the remote machine
     *
     * @exception Exception
     * @see #nList(String )
     * @see #dir()
     * @see #dir(String ) */
    private Vector<String> getFilenames() throws Exception {
        return getFilenames("", false, null);
    }

    private Vector<String> getFilenames(final boolean flgRecurseSubFolders) throws Exception {
        return getFilenames("", flgRecurseSubFolders, null);
    }

    private Vector<String> getFilenames(final String pathname) {
        return getFilenames(pathname, false, null);
    }

    @Override
    public Vector<ISOSVirtualFile> getFiles() {
        return null;
    }

    @Override
    public Vector<ISOSVirtualFile> getFiles(final String string) {
        return null;
    }

    @Override
    public ISOSVFSHandler getHandler() {
        return this;
    }

    @Override
    public SOSFileListEntry getNewVirtualFile(final String pstrFileName) {
        SOSFileListEntry objF = new SOSFileListEntry(pstrFileName);
        objF.setVfsHandler(this);
        return objF;
    }

    @Override
    public ISOSSession getSession() {
        return null;
    }

    @Override
    public StringBuffer getStdErr() throws Exception {
        // TO DO Auto-generated method stub
        return null;
    }

    @Override
    public StringBuffer getStdOut() throws Exception {
        // TO DO Auto-generated method stub
        return null;
    }

    @Override
    public String[] listNames(final String pathname) throws IOException {
        String strT = pathname.replaceAll("\\\\", "/");
        String strA[] = Client().listNames(strT);
        if (strA != null) {
            for (int i = 0; i < strA.length; i++) {
                strA[i] = strA[i].replaceAll("\\\\", "/");
            }
        } else {
            strA = new String[] {};
        }
        LOGGER.debug(SOSVfs_D_137.params(Client().getReplyString(), Client().getReplyCode()));
        return strA;
    }

    /** return a listing of the contents of a directory in short format on the
     * remote machine
     *
     * @return a listing of the contents of a directory on the remote machine
     *
     * @exception Exception
     * @see #nList(String )
     * @see #dir()
     * @see #dir(String ) */
    @Override
    public Vector<String> nList() throws Exception {
        return getFilenames();
    }

    /** return a listing of the contents of a directory in short format on the
     * remote machine
     *
     * @return a listing of the contents of a directory on the remote machine
     *
     * @exception Exception
     * @see #nList(String )
     * @see #dir()
     * @see #dir(String ) */
    @Override
    public Vector<String> nList(final boolean recursive) throws Exception {
        return getFilenames(recursive);
    }

    /** return a listing of the contents of a directory in short format on the
     * remote machine
     * 
     * @param pathname on remote machine
     * @return a listing of the contents of a directory on the remote machine
     *
     * @exception Exception
     * @see #dir() */
    @Override
    public Vector<String> nList(final String pathname) {
        return getFilenames(pathname);
    }

    @Override
    public Vector<String> nList(final String pathname, final boolean flgRecurseSubFolder) {
        final String conMethodName = CLASS_NAME + "::nList";
        try {
            return getFilenames(pathname, flgRecurseSubFolder, null);
        } catch (Exception e) {
            raiseException(e, getHostID(SOSVfs_E_0105.params(conMethodName)));
        }
        return null;
    }

    @Override
    public void openInputFile(final String pstrFileName) {
    }

    @Override
    public void openOutputFile(final String pstrFileName) {
    }

    /** Stores a file on the server using the given name.
     * 
     * @param localFile The name of the local file.
     * @param remoteFile The name of the remote file.
     * @return True if successfully completed, false if not.
     * @exception Exception
     * @see #putFile(String, String ) */
    @Override
    public void put(final String localFile, final String remoteFile) {
        final String conMethodName = CLASS_NAME + "::put";
        FileInputStream in = null;
        boolean rc = false;
        try {
            in = new FileInputStream(localFile);
            rc = Client().storeFile(remoteFile, in);
            if (!rc) {
                raiseException(SOSVfs_E_154.params("put"));
            }
        } catch (Exception e) {
            raiseException(e, getHostID(SOSVfs_E_0105.params(conMethodName)));
        } finally {
            closeInput(in);
        }
    }

    @Override
    public void putFile(final ISOSVirtualFile objVirtualFile) {
        final String conMethodName = CLASS_NAME + "::putFile";
        String strName = objVirtualFile.getName();
        ISOSVirtualFile objVF = this.getFileHandle(strName);
        OutputStream objOS = objVF.getFileOutputStream();
        InputStream objFI = objVirtualFile.getFileInputStream();
        int lngBufferSize = 1024;
        byte[] buffer = new byte[lngBufferSize];
        int intBytesTransferred;
        long totalBytes = 0;
        try {
            synchronized (this) {
                while ((intBytesTransferred = objFI.read(buffer)) != -1) {
                    objOS.write(buffer, 0, intBytesTransferred);
                    totalBytes += intBytesTransferred;
                }
                objFI.close();
                objOS.flush();
                objOS.close();
            }
        } catch (Exception e) {
            raiseException(e, getHostID(SOSVfs_E_0105.params(conMethodName)));
        }
    }

    /** written to store a file on the server using the given name.
     *
     * @param localfile The name of the local file.
     * @param an OutputStream through which data can be
     * @return The total number of bytes written.
     * @exception Exception */
    @Override
    public long putFile(final String localFile, final OutputStream out) {
        final String conMethodName = CLASS_NAME + "::putFile";
        if (out == null) {
            raiseException("OutputStream null value.");
        }
        FileInputStream in = null;
        long lngTotalBytesWritten = 0;
        try {
            byte[] buffer = new byte[4096];
            in = new FileInputStream(new File(localFile));
            int bytesWritten;
            synchronized (this) {
                while ((bytesWritten = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesWritten);
                    lngTotalBytesWritten += bytesWritten;
                }
            }
            closeInput(in);
            closeObject(out);
            this.completePendingCommand();
            return lngTotalBytesWritten;
        } catch (Exception e) {
            raiseException(e, getHostID(SOSVfs_E_0105.params(conMethodName)));
        } finally {
            closeInput(in);
            closeObject(out);
        }
        return lngTotalBytesWritten;
    }

    /** Stores a file on the server using the given name.
     *
     * @param localFile The name of the local file.
     * @param remoteFile The name of the remote file.
     * @return The total number of bytes written.
     *
     * @exception Exception
     * @see #put(String, String ) */
    @Override
    public long putFile(final String localFile, final String remoteFile) throws Exception {
        OutputStream outputStream = Client().storeFileStream(remoteFile);
        if (isNegativeCommandCompletion()) {
            raiseException(SOSVfs_E_144.params("storeFileStream()", remoteFile, getReplyString()));
        }
        long i = putFile(localFile, outputStream);
        LOGGER.debug(SOSVfs_D_146.params(localFile, remoteFile));
        return i;
    }

    @Override
    public int read(final byte[] bteBuffer) {
        return 0;
    }

    @Override
    public int read(final byte[] bteBuffer, final int intOffset, final int intLength) {
        return 0;
    }

    @Override
    public boolean remoteIsWindowsShell() {
        // TO DO Auto-generated method stub
        return false;
    }

    @Override
    public void rename(final String from, final String to) {
        try {
            this.Client().rename(from, to);
        } catch (IOException e) {
            raiseException(e, SOSVfs_E_134.params("rename()"));
        }
        LOGGER.info(SOSVfs_I_150.params(from, to));
    }

    @Override
    public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {
        // TO DO Auto-generated method stub
    }

    @Override
    public void write(final byte[] bteBuffer) {
    }

    @Override
    public void write(final byte[] bteBuffer, final int intOffset, final int intLength) {

    }

    @Override
    public boolean isSimulateShell() {
        return this.simulateShell;
    }

    @Override
    public void setSimulateShell(boolean simulateShell) {
        this.simulateShell = simulateShell;
    }

}
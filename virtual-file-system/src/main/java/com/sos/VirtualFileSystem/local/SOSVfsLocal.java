package com.sos.VirtualFileSystem.local;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import org.apache.log4j.Logger;

import sos.util.SOSFile;
import sos.util.SOSFilelistFilter;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionTransferMode;
import com.sos.JSHelper.interfaces.ISOSConnectionOptions;
import com.sos.JSHelper.interfaces.ISOSDataProviderOptions;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.VirtualFileSystem.DataElements.SOSFileList;
import com.sos.VirtualFileSystem.DataElements.SOSFolderName;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSSession;
import com.sos.VirtualFileSystem.Interfaces.ISOSShellOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFileSystem;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFolder;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.common.SOSFileEntries;
import com.sos.VirtualFileSystem.common.SOSVfsBaseClass;
import com.sos.VirtualFileSystem.shell.cmdShell;
import com.sos.i18n.annotation.I18NResourceBundle;


@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsLocal extends SOSVfsBaseClass implements ISOSVfsFileTransfer, ISOSVFSHandler, ISOSVirtualFileSystem, ISOSConnection {

	private final Logger		logger				= Logger.getLogger(SOSVfsLocal.class);
	private final InputStream	objInputStream		= null;
	private final OutputStream	objOutputStream		= null;

	private String				strReplyString		= "";
	@SuppressWarnings("unused")
	private File				objWorkingDirectory	= null;

	private cmdShell	objCmdShell	= null;
	
	private boolean simulateShell = false;

	public SOSVfsLocal() {
	}

	/**
	 * \brief appendFile
	 *
	 * \details
	 * Appends a File, which pathname is given as a String-Parameter, to another
	 * file, with name is given as string-parameter.
	 *
	 * \return the size of the file after append-operation
	 *
	 * @param localFile
	 * @param remoteFile
	 * @return
	 */
	// TODO appendFile with ISOSVirtualFile	
	@Override
	public long appendFile(final String strSourceFileName, final String strTargetFileName) {
		JSFile objTargetFile = new JSFile(strTargetFileName);
		long lngFileSize = 0;
		try {
			lngFileSize = objTargetFile.AppendFile(strSourceFileName);
		}
		catch (Exception e) {
			String strM = SOSVfs_E_134.params("appendFile()");
			logger.error(strM, e);
			throw new JobSchedulerException(strM, e);
		}
		return lngFileSize;
	}

	@Override
	public void ascii() {
	}

	@Override
	public ISOSConnection Authenticate(final ISOSAuthenticationOptions pobjAO) throws Exception {
		strReplyString = "230 Login successful.";
		return this;
	}

	@Override
	public void binary() {
	}

	
	@Override
	public boolean changeWorkingDirectory(final String pstrPathName) {
		boolean flgResult = true;
		// TODO use this directory on any file-operation
		File fleFile = new File(pstrPathName);
		if (fleFile.exists()) {
			if (fleFile.isDirectory()) {
				objWorkingDirectory = new File(pstrPathName);
			}
			else {
				flgResult = false;
			}
		}
		else {
			flgResult = false;
		}
		return flgResult;
	}

	@Override
	public void close() {
	}

	@Override
	public void CloseConnection() throws Exception {
		strReplyString = "ok";
	}

	@Override
	public void closeInput() {
	}

	@Override
	public void closeOutput() {
	}

	@Override
	public void CloseSession() throws Exception {
		strReplyString = "221 Goodbye.";
	}

	@Override
	public void CompletePendingCommand() {
	}

	@Override
	public ISOSConnection Connect() throws Exception {
		strReplyString = "ok";
		return this;
	}

	@Override
	public ISOSConnection Connect(final ISOSConnectionOptions pobjConnectionOptions) throws Exception {
		this.Connect();
		return this;
	}

	@Override
	public ISOSConnection Connect(final SOSConnection2OptionsAlternate pobjConnectionOptions) throws Exception {
		return null;
	}

	@Override
	public ISOSConnection Connect(final ISOSDataProviderOptions pobjConnectionOptions) throws Exception {
		return null;
	}

	@Override
	public ISOSConnection Connect(final String pstrHostName, final int pintPortNumber) throws Exception {
		return null;
	}

	@Override
	public void ControlEncoding(final String pstrControlEncoding) {
	}

	@Override
	public String createScriptFile(final String pstrContent) throws Exception {
		return EMPTY_STRING;
	}

	@Override
	public void delete(final String pathname) throws IOException {
		File objF = new File(getRealFileName(pathname));
		objF.delete();
	}

	@Override
	public SOSFileList dir(final SOSFolderName pobjFolderName) {
		return null;
	}

	@Override
	public SOSFileList dir(final String pathname, final int flag) {
		return null;
	}

	@Override
	public void disconnect() throws IOException {
	}

	@Override
	public void doPostLoginOperations() {

	}

	@Override
	public String DoPWD() {
		return null;
	}

	@Override
	public void ExecuteCommand(final String strCmd) throws Exception {
		if (objCmdShell == null) {
			objCmdShell = new cmdShell();
		}
		String strT = strCmd;
		if (objCmdShell.isWindows() == true) {
			//strT = strT.replaceAll("/", "\\\\");
			//Kommandos auf Windows koennen Optionen haben wie /F /Y, die nicht in \F \Y umbenannt werden duerfen
			strT = replaceCommand4Windows(strT);
		}
		int exitCode = objCmdShell.executeCommand(strT);
		if(exitCode != 0){
			throw new JobSchedulerException(SOSVfs_E_191.params(exitCode+""));
		}
	}
	
	public String replaceCommand4Windows(final String strCmd) {
		String strT = strCmd;
		//http://www.sos-berlin.com/jira/browse/SOSFTP-204
		// Die folgenden beiden "replace" Aufrufe aendern nur Slashes, denen kein Leerzeichen vorangeht und kein Slash folgt.
		//1.Schritt: alle slashs, denen ein slash folgt, bevor ein Leerzeichen folgt ( " /F //host/c/foo/bar c:/foo/bar" -> " /F \\host\c\foo/bar c:\foo/bar") 
		strT = strT.replaceAll("/(?=[^ ]*/)", "\\\\");
		//2.Schritt: alle slashs, die nicht auf ein Leerzeichen folgen ( " /F //host/c/foo/bar c:/foo/bar" -> " /F \\host\c\foo\bar c:\foo\bar")
		strT = strT.replaceAll("(?<! )/", "\\\\");
		
		return strT;
	}

	@Override
	public void flush() {
	}

	@Override
	public OutputStream getAppendFileStream(final String strFileName) {
		return null;
	}

	@Override
	public ISOSConnection getConnection() {
		return null;
	}

	@Override
	public Integer getExitCode() {
		return 0;
	}

	@Override
	public String getExitSignal() {
		return EMPTY_STRING;
	}

	@Override
	public long getFile(final String remoteFile, final String localFile) throws Exception {
		return 0;
	}

	@Override
	public long getFile(final String pstrSourceFileName, final String pstrTargetFileName, final boolean append) throws Exception {

		long lngFileSize = 0;

		if (append == false) {
			JSFile objF = new JSFile(pstrSourceFileName);
			lngFileSize = objF.length();
			objF.copy(pstrTargetFileName);
		}
		else {
			lngFileSize = this.appendFile(pstrSourceFileName, pstrTargetFileName);
		}
		return lngFileSize;
	}

	@Override
	public ISOSVirtualFile getFileHandle(final String pstrFileName) {
		SOSVfsLocalFile objF = new SOSVfsLocalFile(pstrFileName);
		objF.setHandler(this);
		return objF;
	}

	@Override
	public String[] getFilelist(final String folder, final String regexp, final int flag, final boolean pflgRecurseSubFolder) {
		String[] strS = null;
		try {
			Vector<File> objA = SOSFile.getFolderlist(folder, regexp, flag, pflgRecurseSubFolder);
			Vector<String> objV = new Vector<String>(objA.size());
			for (File objF : objA) {
				if (objF.isDirectory() == false) {
					objV.add(objF.getPath());
				}
			}
			strS = objV.toArray(new String[objV.size()]);
		}
		catch (Exception e) {
			logger.error(e.getLocalizedMessage());
		}
		return strS;
	}

	private Vector<File> getFilelistVector(final String folder, final String regexp, final int RegExpFlag) throws Exception {

		Vector<File> filelist = new Vector<File>();

		if (folder == null || folder.length() == 0) {
			throw new FileNotFoundException("empty directory not allowed!!");
		}

		File f = new File(folder);
		if (!f.exists()) {
			throw new FileNotFoundException("directory does not exist: " + folder);
		}

		filelist = new Vector<File>();
		File[] files = f.listFiles(new SOSFilelistFilter(regexp, RegExpFlag));
		for (File file : files) {
			if (file.isDirectory()) {
			}
			else
				if (file.isFile()) {
					filelist.add(file);
				}
				else {
					// unknown
				}
		}

		return filelist;
	}

	/**
	 * 
	 * @param folder
	 * @param regexp
	 * @param flag
	 * @param withSubFolder
	 * @return
	 * @throws Exception
	 */
	private Vector<File> getFilelistVector(final String folder, final String regexp, final int flag, final boolean withSubFolder) throws Exception {
		Vector<File> filelist = new Vector<File>();
		File file = null;
		File[] subDir = null;

		file = new File(folder);
		subDir = file.listFiles();
		filelist.addAll(getFilelistVector(folder, regexp, flag));
		if (withSubFolder) {
			for (File element : subDir) {
				if (element.isDirectory()) {
					filelist.addAll(getFilelistVector(element.getPath(), regexp, flag, true));
				}
			}
		}
		return filelist;
	}

	@Override
	public OutputStream getFileOutputStream() {
		return objOutputStream;
	}

	@Override
	public Vector<ISOSVirtualFile> getFiles() {
		notImplemented();
		return null;
	}

	@Override
	public Vector<ISOSVirtualFile> getFiles(final String string) {
		notImplemented();
		return null;
	}

	@Override
	public long getFileSize(final String strFileName) {
		return 0;
	}

	@Override
	public String[] getFolderlist(final String folder, final String regexp, final int flag, final boolean pflgRecurseSubFolder) {
		String[] strS = null;
		try {
			Vector<File> objA = SOSFile.getFolderlist(folder, regexp, flag, pflgRecurseSubFolder);
			Vector<String> objV = new Vector<String>(objA.size());
			for (File objF : objA) {
				if (objF.isDirectory() == true) {
					objV.add(objF.getAbsolutePath());
				}
			}
			strS = objV.toArray(new String[objV.size()]);
		}
		catch (Exception e) {
			logger.error(e.getLocalizedMessage());
		}
		return strS;
	}

	@Override
	public ISOSVFSHandler getHandler() {
		return this;
	}

	@Override
	public InputStream getInputStream() {
		return objInputStream;
	}

	@Override
	public InputStream getInputStream(final String strFileName) {
		return null;
	}

	@Override
	public String getModificationTime(final String strFileName) {
		return null;
	}

	@Override
	public OutputStream getOutputStream() {
		return null;
	}

	@Override
	public OutputStream getOutputStream(final String strFileName) {
		return null;
	}

	private String getRealFileName(final String pstrPathname) {
		return pstrPathname;
	}

	@Override
	public String getReplyString() {
		return strReplyString;
	}

	@Override
	public ISOSSession getSession() {
		return null;
	}

	@Override
	public StringBuffer getStdErr() throws Exception {
		return new StringBuffer("");
	}

	@Override
	public StringBuffer getStdOut() throws Exception {
		return new StringBuffer("");
	}

	@Override
	public boolean isConnected() {
		return true;
	}

	@Override
	public boolean isDirectory(final String strFileName) {
		return new File(strFileName).isDirectory();
	}

	@Override
	public boolean isNegativeCommandCompletion() {
		return false;
	}

	/**
	 * \brief listNames
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param pathname
	 * @return
	 * @throws IOException
	 */
	@Override
	public String[] listNames(final String pathname) throws IOException {

		File objF = new File(pathname);
		File[] objA = objF.listFiles();
		String[] strT = new String[objA.length];
		int i = 0;
		for (File file : objA) {
			strT[i++] = file.getAbsolutePath();
		}
		return strT;
	}

	/**
	 * \brief login
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param strUserName
	 * @param strPassword
	 */
	@Override
	public void login(final String strUserName, final String strPassword) {
	}

	/**
	 * \brief logout
	 *
	 * \details
	 *
	 * \return
	 *
	 * @throws IOException
	 */
	@Override
	public void logout() throws IOException {
	}

	/**
	 * \brief mkdir
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param pobjFolderName
	 * @return
	 * @throws IOException
	 */
	@Override
	public ISOSVirtualFolder mkdir(final SOSFolderName pobjFolderName) throws IOException {
		new File(pobjFolderName.Value()).mkdir();
		return null;
	}

	/**
	 * \brief mkdir
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param pathname
	 * @throws IOException
	 */
	@Override
	public void mkdir(final String pathname) throws IOException {
		File objF = new File(pathname);
		if (objF.exists() == false) {
			objF.mkdirs();
		}
		else {
			if (objF.isDirectory() == false) {
				throw new JobSchedulerException(SOSVfs_E_277.params(pathname));
			}
		}
	}

	@Override
	public Vector<String> nList() throws Exception {
		notImplemented();
		return null;
	}

	@Override
	public Vector<String> nList(final boolean recursive) throws Exception {
		notImplemented();
		return null;
	}

	@Override
	public Vector<String> nList(final String pathname) {
		notImplemented();
		return null;
	}

	@Override
	public Vector<String> nList(final String pathname, final boolean flgRecurseSubFolder) {
		notImplemented();
		return null;
	}

	@Override
	public void openInputFile(final String pstrFileName) {
		notImplemented();
	}

	@Override
	public void openOutputFile(final String pstrFileName) {
		notImplemented();
	}

	@Override
	public ISOSSession OpenSession(final ISOSShellOptions pobjShellOptions) throws Exception {
		return null;
	}

	@Override
	public int passive() {
		return 0;
	}

	/**
	 * \brief put
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param localFile
	 * @param remoteFile
	 */
	@Override
	public void put(final String localFile, final String remoteFile) {
	}

	@Override
	public void putFile(final ISOSVirtualFile objVirtualFile) {

		String strName = objVirtualFile.getName();
		strName = new File(strName).getAbsolutePath();
		if (strName.startsWith("c:") == true) {
			strName = strName.substring(3);
		}
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
		}
		catch (Exception e) {
			throw new JobSchedulerException(SOSVfs_E_134.params("putFile()"), e);
		}
		finally {
		}

	}

	@Override
	public long putFile(final String localFile, final OutputStream out) {
		return 0;
	}

	@Override
	public long putFile(final String localFile, final String remoteFile) throws Exception {
		return 0;
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
		return false;
	}

	@Override
	public void rename(final String strFileName, final String pstrNewFileName) {
	}

	@Override
	public boolean rmdir(final SOSFolderName pobjFolderName) throws IOException {
		new File(pobjFolderName.Value()).delete();
		return true;
	}

	@Override
	public void rmdir(final String pstrFolderName) throws IOException {
		new File(pstrFolderName).delete();
	}


	@Override
	public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {
	}

	@Override
	public void setLogin(final boolean pflgIsLogin) {
	}

	@Override
	public ISOSVirtualFile TransferMode(final SOSOptionTransferMode pobjFileTransferMode) {
		return null;
	}

	@Override
	public void write(final byte[] bteBuffer) {
		notImplemented();
	}

	@Override
	public void write(final byte[] bteBuffer, final int intOffset, final int intLength) {
		notImplemented();
	}

    @Override
    public SOSFileEntries getSOSFileEntries() {
        return sosFileEntries;
    }

	@Override
	public void reconnect(SOSConnection2OptionsAlternate options) {
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

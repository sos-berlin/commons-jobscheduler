package com.sos.VirtualFileSystem.common;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.i18n.annotation.I18NResourceBundle;

/**
 * @author KB
 *
 */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsTransferFileBaseClass extends SOSVfsCommonFile {
	private final static Logger		logger		= Logger.getLogger(SOSVfsTransferFileBaseClass.class);
	protected String	fileName	= EMPTY_STRING;

	public SOSVfsTransferFileBaseClass() {
		super("SOSVirtualFileSystem");
	}

	public SOSVfsTransferFileBaseClass(final String pFileName) {
		this();
		String name = pFileName;
		if (objVFSHandler != null) {
			String currentDir = objVFSHandler.DoPWD();
			logDEBUG(SOSVfs_I_126.params(currentDir));
			if (name.startsWith("./") == true) {
				name = name.replace("./", currentDir + "/");
			}
		}
		fileName = adjustFileSeparator(name);
	}

	/**
	 * \brief FileExists
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean FileExists() {
		boolean flgResult = false;
		logDEBUG(SOSVfs_D_156.params(fileName));
		// TODO hier wird im aktuellen Verzeichnis gesucht. geht schief, wenn die datei im Subfolder ist
		// TODO Der Dateiname darf hier nur aus dem Namen der Datei bestehen. Ist die Datei in einem Subfolder, dann muß der Subfolder
		// ebenfalls Namensbestandteil sein.
		// TODO im Moment kommt der Dateiname mal mit und mal ohne Pfadname hier an.
		// TODO Methoden bauen: GibDateiNameOhnePFad und GibDateiNameMitPfad
		if (1 == 1) {
			File fleF = new File(AdjustRelativePathName(fileName));
			String strP = fleF.getParent();
			if (strP == null) {
				strP = ".";
			}
			strP = ".";
			String strN = fleF.getName();
			if (objVFSHandler.getFileSize(fileName) >= 0) {
				flgResult = true;
			}
			/**
			 * inperformant. the approach with size is much more better and faster.
			 */
			// Vector<String> vecTargetFileNamesList = objVFSHandler.nList(strP);
			// flgResult = vecTargetFileNamesList.contains(strFileName);
			// if (flgResult == false) {
			// flgResult = vecTargetFileNamesList.contains(strN);
			// }
		}
		else {
			Vector<String> vecTargetFileNamesList = objVFSHandler.nList(".");
			String strCurrDir = objVFSHandler.DoPWD();
			logDEBUG(SOSVfs_I_126.params(strCurrDir));
			String strT = fileName;
			if (strT.startsWith(strCurrDir) == false) {
				strT = strCurrDir + "/" + fileName;
			}
			flgResult = vecTargetFileNamesList.contains(strT);
			if (flgResult == false) { // Evtl. Windows?
				flgResult = vecTargetFileNamesList.contains(strCurrDir + "\\" + fileName);
			}
		}
		logDEBUG(SOSVfs_D_157.params(flgResult, fileName));
		return flgResult;
	}

	/**
	 * \brief delete
	 *
	 * \details
	 *
	 * \return
	 *
	 */
	@Override
	public boolean delete() {
		try {
			objVFSHandler.delete(fileName);
		}
		catch (Exception e) {
			SOSVfs_E_158.get();
			RaiseException(e, SOSVfs_E_158.params("delete()", fileName));
		}
		return true;
	}

	/**
	 * \brief getFileAppendStream
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public OutputStream getFileAppendStream() {
		OutputStream objO = null;
		try {
			fileName = AdjustRelativePathName(fileName);
			objO = objVFSHandler.getAppendFileStream(fileName);
		}
		catch (Exception e) {
			RaiseException(e, SOSVfs_E_158.params("getFileAppendStream()", fileName));
		}
		return objO;
	}

	/**
	 * \brief getFileInputStream
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public InputStream getFileInputStream() {
		try {
			if (objInputStream == null) {
				fileName = AdjustRelativePathName(fileName);

				objInputStream = objVFSHandler.getInputStream(fileName);
				if (objInputStream == null) {
					objVFSHandler.openInputFile(fileName);
				}
			}
		}
		catch (Exception e) {
			RaiseException(e, SOSVfs_E_158.params("getFileInputStream()", fileName));
		}
		return objInputStream;
	}

	/**
	 * \brief getFileOutputStream
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public OutputStream getFileOutputStream() {
		try {
			if (objOutputStream == null) {
				fileName = AdjustRelativePathName(fileName);
//				int intTransferMode = ChannelSftp.OVERWRITE;
//				if (flgModeAppend) {
//					intTransferMode = ChannelSftp.APPEND;
//				}
//				else if (flgModeRestart ){
//					intTransferMode = ChannelSftp.RESUME;
//				}
//
//				SOSVfsSFtpJCraft objJ = (SOSVfsSFtpJCraft) objVFSHandler;
//				objOutputStream = objJ.getClient().put(fileName, intTransferMode);
//
				if (objOutputStream == null) {
					objVFSHandler.openOutputFile(fileName);
				}
			}
		}
		catch (Exception e) {
			RaiseException(e, SOSVfs_E_158.params("getFileOutputStream()", fileName));
		}
		return objOutputStream;
	}

	protected String AdjustRelativePathName(final String pstrPathName) {
		String strT = pstrPathName;

		if (pstrPathName.startsWith("./") || pstrPathName.startsWith(".\\")) {
			String strPath = objVFSHandler.DoPWD() + "/";
			strT = new File(pstrPathName).getName();
			strT = strT.replaceAll("\\\\", "/");
			strT = strPath + strT;
			logDEBUG(SOSVfs_D_159.params(pstrPathName, strT));
		}

		strT = strT.replaceAll("\\\\", "/");
		return strT;
	}

	/**
	 * \brief getFilePermissions
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 * @throws Exception
	 */
	@Override
	public Integer getFilePermissions() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * \brief getFileSize
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 * @throws Exception
	 */
	@Override
	public long getFileSize() {
		@SuppressWarnings("unused")
		long lngFileSize = -1;
		try {
			lngFileSize = objVFSHandler.getFileSize(fileName);
		}
		catch (Exception e) {
			RaiseException(e, SOSVfs_E_134.params("getFileSize()"));
		}
		return lngFileSize;
	}

	/**
	 * \brief getName
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public String getName() {
		return fileName;
	}

	/**
	 * \brief getParent
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public String getParentVfs() {
		return null;
	}

	/**
	 * \brief getParentFile
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public ISOSVirtualFile getParentVfsFile() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * \brief isDirectory
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean isDirectory() {
		return objVFSHandler.isDirectory(fileName);
	}

	/**
	 * \brief isEmptyFile
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public boolean isEmptyFile() {
		return this.getFileSize() <= 0;
	}

	/**
	 * \brief notExists
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public boolean notExists() {
		boolean flgResult = false;
		try {
			flgResult = this.FileExists() == false;
		}
		catch (Exception e) {
			e.printStackTrace();
			RaiseException(e, SOSVfs_E_134.params("notExists()"));
		}
		return flgResult;
	}

	/**
	 * \brief putFile
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param fleFile
	 * @throws Exception
	 */
	@Override
	public void putFile(final File fleFile) {
		notImplemented();
	}

	/**
	 * \brief putFile
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param strFileName
	 * @throws Exception
	 */
	@Override
	public void putFile(@SuppressWarnings("hiding") final String strFileName) {
		notImplemented();
	}

	/**
	 * \brief rename
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param pstrNewFileName
	 */
	@Override
	public void rename(final String pstrNewFileName) {
		objVFSHandler.rename(fileName, pstrNewFileName);
	}

	/**
	 * \brief setFilePermissions
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param pintNewPermission
	 * @throws Exception
	 */
	@Override
	public void setFilePermissions(final Integer pintNewPermission) {
		notImplemented();
	}

	/**
	 * \brief setHandler
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param pobjVFSHandler
	 */
	@Override
	public void setHandler(final ISOSVfsFileTransfer pobjVFSHandler) {
		// this.objVFSHandler = (SOSVfsFtp) pobjVFSHandler;
		objVFSHandler = pobjVFSHandler;
	}

	@Override
	public String getModificationTime() {
		String strT = "";
		try {
			strT = objVFSHandler.getModificationTime(fileName);
		}
		catch (Exception e) {
			RaiseException(e, SOSVfs_E_134.params("getModificationTime()"));
		}
		return strT;
	}

	@Override
	public String MakeZIPFile(final String pstrZipFileNameExtension) {
		logINFO(SOSVfs_I_160.params("MakeZIPFile()"));
		return fileName;
	}

	@Override
	public void close() {

		this.closeInput();
		this.closeOutput();
	}

	@Override
	public void closeInput() {
		try {
			if (objInputStream != null) {
				objInputStream.close();
				objInputStream = null;
			}
		}
		catch (Exception ex) {
		}
	}

	@Override
	public void closeOutput() {
		try {
			if (objOutputStream != null) {
				objOutputStream.flush();
				objOutputStream.close();
				objOutputStream = null;
			}
		}
		catch (Exception ex) {
		}
	}

	@Override
	public void flush() {
		try {
			this.getFileOutputStream().flush();
		}
		catch (IOException e) {
			RaiseException(e, SOSVfs_E_134.params("flush()"));
		}
	}

	@Override
	public int read(final byte[] bteBuffer) {
		// wird überschrieben
		return 0;
	}

	@Override
	public int read(final byte[] bteBuffer, final int intOffset, final int intLength) {
		// wird überschrieben
		return 0;
	}

	@Override
	public void write(final byte[] bteBuffer, final int intOffset, final int intLength) {
		// wird überschrieben
	}

	@Override
	public void write(final byte[] bteBuffer) {
		notImplemented();
	}

	@Override
	public void putFile(final ISOSVirtualFile pobjVirtualFile) throws Exception {
		notImplemented();
	}

	/**
	 *
	 * \brief RaiseException
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @param e
	 * @param msg
	 */
	protected void RaiseException(final Exception e, final String msg) {
		logger.error(msg);
		throw new JobSchedulerException(msg, e);
	}

	/**
	 *
	 * \brief RaiseException
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @param msg
	 */
	protected void RaiseException(final String msg) {
		logger.error(msg);
		throw new JobSchedulerException(msg);
	}

	/**
	 *
	 * \brief getLogPrefix
	 *
	 * \details
	 *
	 * \return String
	 *
	 * @return
	 */
	private String getLogPrefix() {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
		String[] classNameArr = ste.getClassName().split("\\.");

		return "(" + classNameArr[classNameArr.length - 1] + "::" + ste.getMethodName() + ") ";
	}

	/**
	 *
	 * \brief logINFO
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @param msg
	 */
	protected void logINFO(final Object msg) {
		logger.info(this.getLogPrefix() + msg);
	}

	/**
	 *
	 * \brief logDEBUG
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @param msg
	 */
	protected void logDEBUG(final Object msg) {
		logger.debug(this.getLogPrefix() + msg);
	}

	/**
	 *
	 * \brief logWARN
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @param msg
	 */
	protected void logWARN(final Object msg) {
		logger.warn(this.getLogPrefix() + msg);
	}

	/**
	 *
	 * \brief logERROR
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @param msg
	 */
	protected void logERROR(final Object msg) {
		logger.error(this.getLogPrefix() + msg);
	}

	@Override
	public long setModificationDateTime(final long pdteDateTime) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getModificationDateTime() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public boolean isReadable () {
		boolean flgF = true;
		
		return flgF;
	}

}

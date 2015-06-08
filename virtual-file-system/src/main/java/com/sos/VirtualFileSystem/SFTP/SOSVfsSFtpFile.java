package com.sos.VirtualFileSystem.SFTP;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.common.SOSVfsCommonFile;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.trilead.ssh2.SFTPv3FileAttributes;
import com.trilead.ssh2.SFTPv3FileHandle;

/**
 * @author KB
 *
 */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsSFtpFile extends SOSVfsCommonFile {
	private final String		conClassName		= "SOSVfsSFtpFile";
	private final Logger				logger				= Logger.getLogger(SOSVfsSFtpFile.class);
	// private SOSVfsFtp objVfsHandler = null;
	// private ISOSVfsFileTransfer objVfsHandler = null;
	// private InputStream objInputStream = null;
	// private OutputStream objOutputStream = null;
	private SFTPv3FileHandle	objFileHandle		= null;
	private String				strFileName			= EMPTY_STRING;
	private long				lngFileReadOffset	= 0;
	private long				lngFileWriteOffset	= 0;										// write operation in progress

	public SOSVfsSFtpFile(final String pstrFileName) {
		final String conMethodName = conClassName + "::SOSVfsSFtpFile";
		String strF = pstrFileName;
		if (objVFSHandler != null) {
			String strCurrDir = objVFSHandler.DoPWD();
			// logger.debug(SOSVfs_D_171.params( conMethodName, strCurrDir));
			logger.debug(SOSVfs_D_171.params(conMethodName, strCurrDir));
			if (strF.startsWith("./") == true) {
				strF = strF.replace("./", strCurrDir + "/");
			}
		}
		strFileName = adjustFileSeparator(strF);
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
		final String conMethodName = conClassName + "::FileExists";
		boolean flgResult = false;
		logger.debug(SOSVfs_D_172.params(conMethodName, strFileName));
		// TODO hier wird im aktuellen Verzeichnis gesucht. geht schief, wenn die datei im Subfolder ist
		// TODO Der Dateiname darf hier nur aus dem Namen der Datei bestehen. Ist die Datei in einem Subfolder, dann muß der Subfolder
		// ebenfalls Namensbestandteil sein.
		// TODO im Moment kommt der Dateiname mal mit und mal ohne Pfadname hier an.
		// TODO Methoden bauen: GibDateiNameOhnePFad und GibDateiNameMitPfad
//		if (1 == 1) {
//			File fleF = new File(AdjustRelativePathName(strFileName));
//			String strP = fleF.getParent();
//			if (strP == null) {
//				strP = ".";
//			}
//			strP = ".";
//			String strN = fleF.getName();
			if (objVFSHandler.getFileSize(strFileName) >= 0) {
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
//		}
//		else {
//			Vector<String> vecTargetFileNamesList = objVFSHandler.nList(".");
//			String strCurrDir = objVFSHandler.DoPWD();
//			logger.debug(SOSVfs_D_171.get(conMethodName, strCurrDir));
//			String strT = strFileName;
//			if (strT.startsWith(strCurrDir) == false) {
//				strT = strCurrDir + "/" + strFileName;
//			}
//			flgResult = vecTargetFileNamesList.contains(strT);
//			if (flgResult == false) { // Evtl. Windows?
//				flgResult = vecTargetFileNamesList.contains(strCurrDir + "\\" + strFileName);
//			}
//		}
		logger.debug(SOSVfs_D_157.params(conMethodName, flgResult, strFileName));
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
		final String conMethodName = conClassName + "::delete";
		try {
			objVFSHandler.delete(strFileName);
		}
		catch (IOException e) {
			throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName + " -> " + strFileName), e);
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
		final String conMethodName = conClassName + "::getFileAppendStream";
		OutputStream objO = null;
		try {
			strFileName = AdjustRelativePathName(strFileName);
			objO = objVFSHandler.getAppendFileStream(strFileName);
		}
		catch (Exception e) {
			throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
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
		final String conMethodName = conClassName + "::getFileInputStream";
		try {
			if (objInputStream == null) {
				strFileName = AdjustRelativePathName(strFileName);
				objInputStream = objVFSHandler.getInputStream(strFileName);
				if (objInputStream == null) {
					objVFSHandler.openInputFile(strFileName);
				}
			}
		}
		catch (Exception e) {
			throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
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
		final String conMethodName = conClassName + "::getFileOutputStream";
		try {
			if (objOutputStream == null) {
				strFileName = AdjustRelativePathName(strFileName);
				if (flgModeAppend) {
					objOutputStream = objVFSHandler.getAppendFileStream(strFileName);
				}
				else {
					objOutputStream = objVFSHandler.getOutputStream(strFileName);
				}
				if (objOutputStream == null) {
					objVFSHandler.openOutputFile(strFileName);
				}
			}
		}
		catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
		}
		return objOutputStream;
	}

	private String AdjustRelativePathName(final String pstrPathName) {
		String strT = pstrPathName;

		if (pstrPathName.startsWith("./") || pstrPathName.startsWith(".\\")) {
			String strPath = objVFSHandler.DoPWD() + "/";
			strT = new File(pstrPathName).getName();
			strT = strPath + strT;
			logger.debug(SOSVfs_D_159.params(pstrPathName, strT));
		}

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
		final String conMethodName = conClassName + "::getFileSize";
		long lngFileSize = -1;
		try {
			lngFileSize = objVFSHandler.getFileSize(strFileName);
		}
		catch (Exception e) {
			throw new JobSchedulerException(SOSVfs_E_134.params("getFileSize()"), e);
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
		return strFileName;
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
		boolean flgResult = objVFSHandler.isDirectory(strFileName);
		return flgResult;
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
		boolean flgResult = this.getFileSize() <= 0;
		return flgResult;
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
		final String conMethodName = conClassName + "::notExists";
		boolean flgResult = false;
		try {
			flgResult = this.FileExists() == false;
		}
		catch (Exception e) {
			throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
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
		objVFSHandler.rename(adjustFileSeparator(strFileName), adjustFileSeparator(pstrNewFileName));
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
		final String conMethodName = conClassName + "::getModificationTime";
		String strT = "";
		try {
			strT = objVFSHandler.getModificationTime(strFileName);
		}
		catch (Exception e) {
			throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
		}
		return strT;
	}

	@Override
	public String MakeZIPFile(final String pstrZipFileNameExtension) {
		logger.info(SOSVfs_I_160.params("MakeZIPFile()"));
		return strFileName;
	}

	@Override
	public void close() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::close";
		try {
			if (objFileHandle != null) {
				objFileHandle.getClient().closeFile(objFileHandle);
			}
		}
		catch (Exception e) {
			throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
		}
		finally {
			objFileHandle = null;
			lngFileReadOffset = 0;
			lngFileWriteOffset = 0;
		}
	}

	@Override
	public void closeInput() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::closeInput";
		close();
	}

	@Override
	public void closeOutput() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::closeOutput";
		close();
	}

	@Override
	public void flush() {
		final String conMethodName = conClassName + "::flush";
		try {
			this.getFileOutputStream().flush();
		}
		catch (IOException e) {
			throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
		}
	}

	@Override
	public int read(final byte[] bteBuffer) {
		final String conMethodName = conClassName + "::read";
		int intLength = bteBuffer.length;
		// TODO Filesize is max 2 GB, due to intoffset
		int lngBytesRed = read(bteBuffer, (int) lngFileReadOffset, intLength);
		// try {
		// InputStream objI = this.getFileInputStream();
		// if (objI != null) {
		// lngBytesRed = objI.read(bteBuffer);
		// }
		// else {
		// lngBytesRed = objVFSHandler.read(bteBuffer);
		// }
		// }
		// catch (IOException e) {
		// e.printStackTrace();
		// throw new JobSchedulerException(String.format("%1$s failed", conMethodName), e);
		// }
		lngFileReadOffset += lngBytesRed;
		return lngBytesRed;
	}

	@Override
	public int read(final byte[] bteBuffer, final int intOffset, final int intLength) {
		final String conMethodName = conClassName + "::read";
		int lngBytesRed = 0;
		try {
			if (objFileHandle == null) {
				SOSVfsSFtp objT = (SOSVfsSFtp) objVFSHandler;
				objFileHandle = objT.getInputFileHandle(strFileName);
			}
			lngBytesRed = objFileHandle.getClient().read(objFileHandle, intOffset, bteBuffer, 0, intLength);
		}
		catch (IOException e) {
			throw new JobSchedulerException(SOSVfs_E_173.params(conMethodName, strFileName), e);
		}
		return lngBytesRed;
	}

	@Override
	public void write(final byte[] bteBuffer, final int intOffset, final int intLength) {
		final String conMethodName = conClassName + "::write";
		try {
			if (objFileHandle == null) {
				SOSVfsSFtp objT = (SOSVfsSFtp) objVFSHandler;
				objFileHandle = objT.getOutputFileHandle(strFileName);
				lngFileWriteOffset = 0;
			}
			objFileHandle.getClient().write(objFileHandle, lngFileWriteOffset, bteBuffer, 0, intLength);
			lngFileWriteOffset += intLength;
		}
		catch (Exception e) {
			throw new JobSchedulerException(SOSVfs_E_174.params(conMethodName, strFileName, lngFileWriteOffset, intLength), e);
		}
	}

	@Override
	public void write(final byte[] bteBuffer) {
		final String conMethodName = conClassName + "::write";
		notImplemented();
		try {
			this.getFileOutputStream().write(bteBuffer);
		}
		catch (IOException e) {
			throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
		}
	}

	@Override
	public void putFile(final ISOSVirtualFile pobjVirtualFile) throws Exception {
		// TODO Auto-generated method stub
		notImplemented();
	}

	@Override
	public long getModificationDateTime() {
		long lngR = 0;
		if (objFileHandle != null) {
			try {
				SFTPv3FileAttributes objFA = objFileHandle.getClient().fstat(objFileHandle);
				lngR = objFA.mtime;
			}
			catch (IOException e) {
				logger.error(e.getLocalizedMessage());
			}
		}
		// TODO Auto-generated method stub
		return lngR;
	}

	@Override
	public long setModificationDateTime(final long pdteDateTime) {
		long lngR = 0;
		if (objFileHandle != null) {
			try {
				SFTPv3FileAttributes objFA = objFileHandle.getClient().fstat(objFileHandle);
				objFA.mtime = new Integer((int) pdteDateTime);
				lngR = pdteDateTime;
			}
			catch (IOException e) {
				logger.error(e.getLocalizedMessage());
			}
		}
		return lngR;
	}
}

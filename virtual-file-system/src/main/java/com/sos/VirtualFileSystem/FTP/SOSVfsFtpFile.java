package com.sos.VirtualFileSystem.FTP;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Vector;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.common.SOSVfsCommonFile;
import com.sos.VirtualFileSystem.common.SOSVfsConstants;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsFtpFile extends SOSVfsCommonFile {
	private final String	conClassName	= "SOSVfsFtpFile";
	private final Logger	logger			= Logger.getLogger(SOSVfsFtpFile.class);
	private String			strFileName		= EMPTY_STRING;
	private FTPFile			objFTPFile		= null;

	public SOSVfsFtpFile(final String pstrFileName) {
		super(SOSVfsConstants.strBundleBaseName);

		final String conMethodName = conClassName + "::SOSVfsFtpFile";
		String strF = pstrFileName;
		if (objVFSHandler != null) {
			if (strF.startsWith("./") == true) {
				String strCurrDir = objVFSHandler.DoPWD();
				logger.debug(SOSVfs_D_171.params(conMethodName, strCurrDir));
				strF = strF.replace("./", strCurrDir + "/");
			}
		}
		strFileName = strF;
		
	}
	
	public SOSVfsFtpFile(final FTPFile pobjFileFile) {
		super(SOSVfsConstants.strBundleBaseName);

		final String conMethodName = conClassName + "::SOSVfsFtpFile";
		String strF = pobjFileFile.getName();
		if (objVFSHandler != null) {
			String strCurrDir = objVFSHandler.DoPWD();
			logger.debug(SOSVfs_D_171.params(conMethodName, strCurrDir));
			if (strF.startsWith("./") == true) {
				strF = strF.replace("./", strCurrDir + "/");
			}
		}
		strFileName = strF;
		objFTPFile	= pobjFileFile;
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
		if (1 == 1) {
			File fleF = new File(AdjustRelativePathName(strFileName));
			String strP = fleF.getParent();
			if (strP == null) {
				strP = ".";
			}
			strP = ".";
			String strN = fleF.getName();
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
		}
		else {
			Vector<String> vecTargetFileNamesList = objVFSHandler.nList(".");
			String strCurrDir = objVFSHandler.DoPWD();
			logger.debug(String.format("%1$s: currDir = %2$s", conMethodName, strCurrDir));
			String strT = strFileName;
			if (strT.startsWith(strCurrDir) == false) {
				strT = strCurrDir + "/" + strFileName;
			}
			flgResult = vecTargetFileNamesList.contains(strT);
			if (flgResult == false) { // Evtl. Windows?
				flgResult = vecTargetFileNamesList.contains(strCurrDir + "\\" + strFileName);
			}
		}
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
			throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
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

	@Override
	public boolean isReadable () {
		boolean flgF = true;
		
		return flgF;
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
		objVFSHandler.rename(strFileName, pstrNewFileName);
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
		logger.info(SOSVfs_I_160.params("MakeZIPFile"));
		return strFileName;
	}

	@Override
	public void close() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::close";
		if (objOutputStream != null) {
			this.closeOutput();
			objOutputStream = null;
		}
		else {
			if (objInputStream != null) {
				this.closeInput();
				objInputStream = null;
			}
		}
	}

	@Override
	public void closeInput() {
		final String conMethodName = conClassName + "::closeInput";
		try {
			if (objInputStream != null) {
				try {
					objInputStream.close();
					objInputStream = null;
					// nur rufen, wenn vorher ein FTP-Commando abgesetzt wurde. ansonsten hängt der Prozeß
					/**
					 * nicht ganz klar, wann es notwendig, erlaubt und schädlich ist.
					 * Hängt kein cmd, dann wird dieser Aufruf die Ftp-Session aufhängen.
					 *
					 * Auf alle Fälle muß es nach einem RETR und einem STOR kommen, andernfalls
					 * werden die Antworten auf Kommandos versetzt geliefert (es wird auf das vorletzte
					 * Kommando geantwortet). Und das führt z.B. dazu, daß ein SIZE mit der NOOP-meldung
					 * beantwortet wird. Folge: Datei nicht vorhanden, weil keine Größeninfo erkennbar.
					 */
					objVFSHandler.CompletePendingCommand() ;
				}
				catch (Exception e) {
					e.printStackTrace(System.err);  //  SocketTimeOut???
					throw e;
				}
			}
		}
		catch (Exception e) {
			throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
		}
		finally {
			objInputStream = null;
		}
	}

	@Override
	public void closeOutput() {
		final String conMethodName = conClassName + "::closeOutput";
		try {
			OutputStream objO = this.getFileOutputStream();
			if (objO != null) {
				objO.flush();
				objO.close();
				objO = null;
				// siehe kommentar bei closeInput (nur rufen, wenn vorher ein FTP-Commando abgesetzt wurde. ansonsten hängt der Prozeß
				objVFSHandler.CompletePendingCommand();
				if (objVFSHandler.isNegativeCommandCompletion()) {
					throw new JobSchedulerException(SOSVfs_E_175.params(strFileName, objVFSHandler.getReplyString()));
				}
			}
		}
		catch (IOException e) {
			throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
		}
		finally {
			objOutputStream = null;
		}
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
		int lngBytesRed = 0;
		try {
			InputStream objI = this.getFileInputStream();
			if (objI != null) {
				lngBytesRed = objI.read(bteBuffer);
			}
			else {
				lngBytesRed = objVFSHandler.read(bteBuffer);
			}
		}
		catch (IOException e) {
			throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
		}
		return lngBytesRed;
	}

	@Override
	public int read(final byte[] bteBuffer, final int intOffset, final int intLength) {
		final String conMethodName = conClassName + "::read";
		int lngBytesRed = 0;
		try {
			InputStream objI = this.getFileInputStream();
			if (objI != null) {
				lngBytesRed = objI.read(bteBuffer, intOffset, intLength);
			}
			else {
				lngBytesRed = objVFSHandler.read(bteBuffer, intOffset, intLength);
			}
		}
		catch (IOException e) {
			throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
		}
		return lngBytesRed;
	}

	@Override
	public void write(final byte[] bteBuffer, final int intOffset, final int intLength) {
		final String conMethodName = conClassName + "::write";
		try {
			if (this.getFileOutputStream() == null) {
				throw new JobSchedulerException(SOSVfs_E_176.params(conMethodName, strFileName));
			}
			this.getFileOutputStream().write(bteBuffer, intOffset, intLength);
		}
		catch (IOException e) {
			throw new JobSchedulerException(String.format("%1$s failed for file %2$s", conMethodName, strFileName), e);
		}
	}

	@Override
	public void write(final byte[] bteBuffer) {
		final String conMethodName = conClassName + "::write";
		try {
			this.getFileOutputStream().write(bteBuffer);
		}
		catch (IOException e) {
			throw new JobSchedulerException(SOSVfs_E_134.get(conMethodName), e);
		}
	}

	@Override
	public void putFile(final ISOSVirtualFile pobjVirtualFile) throws Exception {
		notImplemented();
	}

	@Override
	public long getModificationDateTime() {
		String strT = objVFSHandler.getModificationTime(strFileName);
		long lngT = -1;
		if (strT != null) {
			if (strT.startsWith("213 ")) {
				strT = strT.substring(3);
			}
			try {
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
				lngT = df.parse(strT.trim()).getTime();
//				lngT = new Date(strT,).getTime();  // strT is in the Format 20120807122702
//				lngT = new Integer(strT);
			}
			catch (Exception e) {
				lngT = -1;
			}
		}
		else {
			lngT = -1;
		}
		return lngT;
	}

	@Override
	public long setModificationDateTime(final long pdteDateTime) {
		return 0;
	}
}

package com.sos.VirtualFileSystem.SFTP;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.jcraft.jsch.ChannelSftp;
import com.sos.VirtualFileSystem.common.SOSVfsTransferFileBaseClass;
import com.sos.i18n.annotation.I18NResourceBundle;

/**
 * @author KB
 *
 */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsSFtpFileJCraft extends SOSVfsTransferFileBaseClass {
	/**
	 *
	 * \brief SOSVfsSFtpFileJCraft
	 *
	 * \details
	 *
	 * @param pstrFileName
	 */
	public SOSVfsSFtpFileJCraft(final String pstrFileName) {
		super(pstrFileName);
	}

	/**
	 *
	 * \brief read
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param bteBuffer
	 * @return
	 */
	@Override
	public int read(final byte[] bteBuffer) {
		try {
			InputStream is = this.getFileInputStream();
			if (is == null) {
				throw new Exception(SOSVfs_E_177.get());
			}
			return is.read(bteBuffer);
		}
		catch (Exception e) {
			RaiseException(e, SOSVfs_E_173.params("read", fileName));
			return 0;
		}
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

				int intTransferMode = ChannelSftp.OVERWRITE;
				if (flgModeAppend) {
					intTransferMode = ChannelSftp.APPEND;
				}
				else if (flgModeRestart ){
					intTransferMode = ChannelSftp.RESUME;
				}

				SOSVfsSFtpJCraft objJ = (SOSVfsSFtpJCraft) objVFSHandler;
				objInputStream = objJ.getClient().get(fileName, intTransferMode);
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
	 *
	 * \brief read
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param bteBuffer
	 * @param intOffset
	 * @param intLength
	 * @return
	 */
	@Override
	public int read(final byte[] bteBuffer, final int intOffset, final int intLength) {
		try {
			InputStream is = this.getFileInputStream();
			if (is == null) {
				throw new Exception(SOSVfs_E_177.get());
			}
			return is.read(bteBuffer, intOffset, intLength);
		}
		catch (Exception e) {
			RaiseException(e, SOSVfs_E_173.params("read", fileName));
			return 0;
		}
	}

	/**
	 *
	 * \brief write
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param bteBuffer
	 * @param intOffset
	 * @param intLength
	 */
	@Override
	public void write(final byte[] bteBuffer, final int intOffset, final int intLength) {
		try {

			OutputStream os = this.getFileOutputStream();
			if (os == null) {
				throw new Exception(SOSVfs_E_147.get());
			}
			os.write(bteBuffer, intOffset, intLength);
		}
		catch (Exception e) {
			RaiseException(e, SOSVfs_E_173.params("write", fileName));
		}
	}

	@Override
	public void write(final byte[] bteBuffer) {
		try {
			this.getFileOutputStream().write(bteBuffer);
		}
		catch (IOException e) {
			RaiseException(e, SOSVfs_E_134.params("write()"));
		}
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
				fileName = super.AdjustRelativePathName(fileName);
				int intTransferMode = ChannelSftp.OVERWRITE;
				if (flgModeAppend) {
					intTransferMode = ChannelSftp.APPEND;
				}
				else if (flgModeRestart ){
					intTransferMode = ChannelSftp.RESUME;
				}

				SOSVfsSFtpJCraft objJ = (SOSVfsSFtpJCraft) objVFSHandler;
				/**
				 * kb 2014-07-21
				 * warum wurde die folgende Zeile auskommentiert und durch die dahinter
				 * stehende, jetzt auskkommentiert, ersetzt? Damit ist kein AppendMode
				 * möglich und das ist ein schwerer Fehler.
				 * siehe hierzu: http://www.sos-berlin.com/otrs/index.pl?Action=AgentZoom&TicketID=1520
				 */
				objOutputStream = objJ.getClient().put(fileName, intTransferMode);
//				objOutputStream = objJ.getClient().put(fileName);

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

	@Override
	public long getModificationDateTime() {

		String dateTime = null;
		long mt = 0;

//		try {
//			SftpATTRS objAttr = objVFSHandler.  (fileName);
//			if (objAttr != null) {
//				mt = objAttr.getMTime();
//				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//				dateTime = df.format(new Date(mt));
//			}
//		}
//		catch (SftpException e) {
//			// e.printStackTrace();
//		}
		return mt;
	}

	@Override
	public long setModificationDateTime(final long pdteDateTime) {

		return 0;
	}

	@Override
	public boolean isReadable () {
		boolean flgF = true;
		
		return flgF;
	}

}

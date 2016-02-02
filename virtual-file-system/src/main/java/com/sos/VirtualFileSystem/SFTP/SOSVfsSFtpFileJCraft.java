package com.sos.VirtualFileSystem.SFTP;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.jcraft.jsch.ChannelSftp;
import com.sos.VirtualFileSystem.common.SOSVfsTransferFileBaseClass;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsSFtpFileJCraft extends SOSVfsTransferFileBaseClass {

	public SOSVfsSFtpFileJCraft(final String fileName) {
		super(fileName);
	}

	@Override
	public int read(final byte[] buffer) {
		try {
			InputStream is = this.getFileInputStream();
			if (is == null) {
				throw new Exception(SOSVfs_E_177.get());
			}
			return is.read(buffer);
		}
		catch (Exception e) {
			RaiseException(e, SOSVfs_E_173.params("read", fileName));
			return 0;
		}
	}

	@Override
	public InputStream getFileInputStream() {
		try {
			if (objInputStream == null) {
				fileName = AdjustRelativePathName(fileName);

				int transferMode = ChannelSftp.OVERWRITE;
				if (flgModeAppend) {
					transferMode = ChannelSftp.APPEND;
				}
				else if (flgModeRestart ){
					transferMode = ChannelSftp.RESUME;
				}

				SOSVfsSFtpJCraft handler = (SOSVfsSFtpJCraft) objVFSHandler;
				objInputStream = handler.getClient().get(fileName, transferMode);
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

	@Override
	public int read(final byte[] buffer, final int offset, final int length) {
		try {
			InputStream is = this.getFileInputStream();
			if (is == null) {
				throw new Exception(SOSVfs_E_177.get());
			}
			return is.read(buffer, offset, length);
		}
		catch (Exception e) {
			RaiseException(e, SOSVfs_E_173.params("read", fileName));
			return 0;
		}
	}

	@Override
	public void write(final byte[] buffer, final int offset, final int length) {
		try {

			OutputStream os = this.getFileOutputStream();
			if (os == null) {

				throw new Exception(SOSVfs_E_147.get());
			}
			os.write(buffer, offset, length);
		}
		catch (Exception e) {
			RaiseException(e, SOSVfs_E_173.params("write", fileName));
		}
	}
	
	
	@Override
	public void write(final byte[] buffer) {
		try {
			this.getFileOutputStream().write(buffer);
		}
		catch (IOException e) {
			RaiseException(e, SOSVfs_E_134.params("write()"));
		}
	}

	@Override
	public OutputStream getFileOutputStream() {
		try {
			if (objOutputStream == null) {
				fileName = super.AdjustRelativePathName(fileName);
				int transferMode = ChannelSftp.OVERWRITE;
				if (flgModeAppend) {
					transferMode = ChannelSftp.APPEND;
				}
				else if (flgModeRestart ){
					transferMode = ChannelSftp.RESUME;
				}
				
				SOSVfsSFtpJCraft handler = (SOSVfsSFtpJCraft) objVFSHandler;
				objOutputStream = handler.getClient().put(fileName, transferMode);
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

}

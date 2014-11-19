package com.sos.VirtualFileSystem.HTTP;
import java.io.InputStream;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.common.SOSVfsTransferFileBaseClass;
import com.sos.i18n.annotation.I18NResourceBundle;


/**
 * @author KB
 *
 */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsHTTPFile extends SOSVfsTransferFileBaseClass {
	/**
	 * 
	 * @param pstrFileName
	 */
	public SOSVfsHTTPFile(final String path) {
		super(path);
	}

	
	@Override
	public boolean FileExists() {
	
		Long fs = objVFSHandler.getFileSize(fileName);
		
		return fs >= 0;
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
	public int read(byte[] buffer) {
		try {
			InputStream is = this.getFileInputStream();
			
			if (is == null) {
				throw new JobSchedulerException(SOSVfs_E_177.get());
			}
			return is.read(buffer);
		}
		catch (Exception e) {
			RaiseException(e, SOSVfs_E_173.params("read", this.fileName));
			return 0;
		}
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
	public int read(byte[] buffer, int offset, int len) {
		try {
			InputStream is = this.getFileInputStream();
			if (is == null) {
				throw new Exception(SOSVfs_E_177.get());
			}
			return is.read(buffer, offset, len);
		}
		catch (Exception e) {
			RaiseException(e, SOSVfs_E_173.params("read", this.fileName));
			return 0;
		}
	}
	
}

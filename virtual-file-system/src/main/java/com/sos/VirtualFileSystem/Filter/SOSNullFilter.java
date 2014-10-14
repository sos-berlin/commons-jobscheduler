/**
 *
 */
package com.sos.VirtualFileSystem.Filter;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.VirtualFileSystem.Filter.Options.SOSFilterOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSFileContentFilter;

/**
 * @author KB
 *
 */
public class SOSNullFilter extends JSJobUtilitiesClass<SOSFilterOptions> implements ISOSFileContentFilter {

	private final String		conClassName	= this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String	conSVNVersion	= "$Id$";
	private final Logger		logger			= Logger.getLogger(this.getClass());


	protected byte[]			bteBuffer		= null;

	public SOSNullFilter() {
		super();
	}

	public SOSNullFilter(final SOSFilterOptions pobjOptions) {
		super(pobjOptions);
		objOptions = pobjOptions;
	}

	/* (non-Javadoc)
	* @see com.sos.VirtualFileSystem.Interfaces.ISOSFileContentFilter#Options()
	*/
	@Override
	public SOSFilterOptions Options() {
		if (objOptions == null) {
			objOptions = new SOSFilterOptions();
		}
		return objOptions;
	}

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Interfaces.ISOSFileContentFilter#write(byte[], int, int)
	 */
	@Override
	public void write(final byte[] pbteBuffer, final int intOffset, final int intLength) {
		bteBuffer = pbteBuffer;
		doProcess();
	}

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Interfaces.ISOSFileContentFilter#write(byte[])
	 */
	@Override
	public void write(final byte[] pbteBuffer) {
		bteBuffer = pbteBuffer;

		doProcess();
	}

	protected String byte2String(final byte[] pbteBuffer) {
		String strT = "";
		try {
//			strT = new String(bteBuffer, "UTF-8");
			strT = new String(bteBuffer);
			logger.trace(strT);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return strT;
	}

	protected void doProcess() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::doProcess";
		if (bteBuffer != null) {
			String strT = byte2String(bteBuffer);

			bteBuffer = strT.getBytes();
			logger.trace(byte2String(bteBuffer));
		}

	} // private void doProcess

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Interfaces.ISOSFileContentFilter#read(byte[])
	 */
	@Override
	public int read(byte[] pbteBuffer) {
		pbteBuffer = bteBuffer;
		bteBuffer = null;
		return pbteBuffer.length;
	}

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Interfaces.ISOSFileContentFilter#read(byte[], int, int)
	 */
	@Override
	public int read(byte[] pbteBuffer, final int intOffset, final int intLength) {
		pbteBuffer = bteBuffer;
		bteBuffer = null;
		return pbteBuffer.length;
	}

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Interfaces.ISOSFileContentFilter#readBuffer()
	 */
	@Override
	public byte[] readBuffer() {
		return bteBuffer;
	}

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Interfaces.ISOSFileContentFilter#readBuffer(byte[], int, int)
	 */
	@Override
	public byte[] readBuffer(final int intOffset, final int intLength) {
		return bteBuffer;
	}

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Interfaces.ISOSFileContentFilter#close()
	 */
	@Override
	public void close() {

	}

	@Override
	public byte[] read() {
		return bteBuffer;
	}

	@Override
	public byte[] read(final int intOffset, final int intLength) {
		return bteBuffer;
	}

	@Override
	public void write(final String pstrBuffer) {
		if (pstrBuffer != null) {
			bteBuffer = pstrBuffer.getBytes();
			doProcess();
		}
		else {
			bteBuffer = null;
		}
	}

	@Override
	public String readString() {
		String strT = null;
		if (bteBuffer != null) {
			strT = byte2String(bteBuffer);
		}
		return strT;
	}

	protected void dumpByteBuffer () {
		String strT = "";
		for (byte element : bteBuffer) {
			strT += element + " ";
		}
		logger.debug(strT);
		logger.debug(byte2String(bteBuffer));
	}

	@Override
	public void open() {
		
	}

	@Override
	public byte[] getBuffer() {
		return null;
	}

}

/**
 *
 */
package com.sos.VirtualFileSystem.Filter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.sos.VirtualFileSystem.Filter.Options.SOSFilterOptions;

/**
 * @author KB
 *
 */
public class SOSUnix2DosFilter extends SOSNullFilter {

	private final String		conClassName	= this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String	conSVNVersion	= "$Id$";
	@SuppressWarnings("unused")
	private final Logger		logger			= Logger.getLogger(this.getClass());

	public SOSUnix2DosFilter() {
		super();
	}

	public SOSUnix2DosFilter(final SOSFilterOptions pobjOptions) {
		super(pobjOptions);
	}

	@Override
	protected void doProcess() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::doProcess";

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);

		byte pb = -1;
		dumpByteBuffer();

		try {
			for (byte b: bteBuffer) {
				if (b == 10 && pb != 13) {
					dos.write((byte) 13);
				}
				dos.write(b);
				pb = b;
			}
			dos.flush();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		bteBuffer = baos.toByteArray();
		dumpByteBuffer();

	} // private void doProcess

}

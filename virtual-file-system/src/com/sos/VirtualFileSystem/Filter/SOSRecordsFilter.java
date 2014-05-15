/**
 *
 */
package com.sos.VirtualFileSystem.Filter;

import org.apache.log4j.Logger;

import com.sos.VirtualFileSystem.Filter.Options.SOSFilterOptions;

/**
 * @author KB
 *
 */
public class SOSRecordsFilter extends SOSNullFilter {

	private final String		conClassName		= this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String	conSVNVersion		= "$Id$";
	private final Logger		logger				= Logger.getLogger(this.getClass());

	private boolean				flgIncludeRecord	= false;

	private long lngRecordsIncluded = 0;
	private long lngRecordsExcluded = 0;
	
	/**
	 *
	 */
	public SOSRecordsFilter() {
		super();
	}

	public SOSRecordsFilter(final SOSFilterOptions pobjOptions) {
		super(pobjOptions);
		logger.debug(conClassName);
	}

	@Override
	protected void doProcess() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::doProcess";
		if (bteBuffer == null) {
			return;
		}
		String strT = byte2String(bteBuffer);

		if (objOptions.excludeLinesBefore.match(strT)) {
			flgIncludeRecord = true;
		}

		if (flgIncludeRecord == true) {
			bteBuffer = strT.getBytes();
			lngRecordsIncluded++;
		}
		else {
			bteBuffer = null;
			lngRecordsExcluded++;
		}

		if (objOptions.exclude_lines_after.match(strT)) {
			flgIncludeRecord = false;
		}

	} // private void doProcess
	
	@Override
	public void close() {
		
		objJSJobUtilities.setJSParam(conClassName + ".records_included", String.valueOf(lngRecordsIncluded));
		objJSJobUtilities.setJSParam(conClassName + ".records_excluded", String.valueOf(lngRecordsExcluded));
		
	}
}

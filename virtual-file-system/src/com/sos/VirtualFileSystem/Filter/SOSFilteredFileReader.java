/**
 *
 */
package com.sos.VirtualFileSystem.Filter;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.VirtualFileSystem.Filter.Options.SOSFilterOptions;

/**
 * @author KB
 *
 */
public class SOSFilteredFileReader extends JSToolBox implements ISOSFilteredFileReader {

	private final String			conClassName		= this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String		conSVNVersion		= "$Id$";
	private final Logger			logger				= Logger.getLogger(this.getClass());

	private Vector<SOSNullFilter>	lstFilters	= null;

	protected SOSFilterOptions		objFilterOptions	= null;
	private JSFile					objFile2Read		= null;
	private ISOSFilteredFileReader	objProcessHandler	= this;

	public SOSFilteredFileReader() {
		super();
		logger.debug(conClassName);
	}

	public SOSFilteredFileReader(final JSFile pobjJSFile) {
		this();
		objFile2Read = pobjJSFile;
	}

	public void Options(final SOSFilterOptions pobjOptions) {
		objFilterOptions = pobjOptions;
	}

	public SOSFilterOptions Options() {
		if (objFilterOptions == null) {
			objFilterOptions = new SOSFilterOptions();
		}
		return objFilterOptions;
	}

	public void setProcesshandler(final ISOSFilteredFileReader pobjProcessHandler) {
		objProcessHandler = pobjProcessHandler;
	}

	public void setFile(final JSFile pobjJSFile) {
		objFile2Read = pobjJSFile;
	}

	public void runMultipleFiles(final String pstrPathName) {
		SOSOptionFolderName objFN = new SOSOptionFolderName(pstrPathName);
		for (File file : objFN.listFiles()) {
			this.setFile(new JSFile(file.getAbsolutePath()));
			this.run();
		}
	}


	public void run() {

		if (lstFilters == null) {
			lstFilters = objFilterOptions.getFilter();
		}

		StringBuffer strBuffer = null;
		while ((strBuffer = objFile2Read.GetLine()) != null) {
			String strB = strBuffer.toString();
			for (SOSNullFilter sosNullFilter : lstFilters) {
				sosNullFilter.write(strB);
				strB = sosNullFilter.readString();
			}
			if (strB != null) {
				objProcessHandler.processRecord(strB);
			}
		}

		try {
			objFile2Read.close();
			objFilterOptions.startPostProcessing(null);
			objFilterOptions.startCloseProcessing(null);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void processRecord(final String pstrRecord) {

		//		System.out.println(pstrRecord);
	}

	@Override
	public void atStartOfData() {

	}

	@Override
	public void atEndOfData() {

	}

}

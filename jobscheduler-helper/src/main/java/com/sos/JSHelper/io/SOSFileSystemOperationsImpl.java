package com.sos.JSHelper.io;

import java.io.File;

public class SOSFileSystemOperationsImpl extends SOSFileSystemOperations {

	@Override
	protected String getActionName() {
		return "";
	}

	@Override
	protected boolean handleOneFile(File sourceFile, File targetFile, boolean overwrite, boolean gracious) {
		return false;
	}

}

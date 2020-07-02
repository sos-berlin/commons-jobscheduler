package com.sos.JSHelper.io;

import java.io.File;
import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class SOSFileSystemOperationsCopy extends SOSFileSystemOperations {

	private static final String ACTION_NAME = "copyFiles: ";

	@Override
	protected String getActionName() {
		return ACTION_NAME;
	}

	@Override
	protected boolean handleOneFile(final File source, final File target, final boolean overwrite,
			final boolean gracious) throws Exception {
		boolean rc = false;
		if (source.equals(target)) {
			throw new JobSchedulerException("cannot copy file to itself: " + source.getCanonicalPath());
		}
		if (overwrite || !target.exists()) {
			long modificationDate = source.lastModified();
			rc = copyFile(source, target);
			target.setLastModified(modificationDate);
			LOGGER.info("copy " + source.getPath() + " to " + target.getPath());
			return rc;
		} else if (!gracious) {
			throw new JobSchedulerException("file already exists: " + target.getCanonicalPath());
		} else {
			LOGGER.info("file already exists: " + target.getCanonicalPath());
			return rc;
		}
	}

}

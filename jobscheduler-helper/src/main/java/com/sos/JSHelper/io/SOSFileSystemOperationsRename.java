package com.sos.JSHelper.io;

import java.io.File;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class SOSFileSystemOperationsRename extends SOSFileSystemOperations{

	private static final String ACTION_NAME = "renameFiles: ";

	@Override
	protected String getActionName() {
		return ACTION_NAME;
	}
 

	protected boolean handleOneFile(final File source, final File target, final boolean overwrite, final boolean gracious)
			throws Exception {
		if (source.equals(target)) {
			throw new JobSchedulerException("cannot rename file to itself: " + source.getCanonicalPath());
		}
		if (!overwrite && target.exists()) {
			if (!gracious) {
				throw new JobSchedulerException("file already exists: " + target.getCanonicalPath());
			} else {
				LOGGER.info("file already exists: " + target.getCanonicalPath());
				return false;
			}
		} else {
			if (target.exists() && !target.delete()) {
				throw new JobSchedulerException("cannot overwrite " + target.getCanonicalPath());
			}
			if (!source.renameTo(target)) {
				boolean rc = copyFile(source, target);
				if (rc) {
					rc = source.delete();
					if (!rc) {
						rc = target.delete();
						throw new JobSchedulerException("cannot rename file from " + source.getCanonicalPath() + " to "
								+ target.getCanonicalPath());
					}
				} else {
					throw new JobSchedulerException("cannot rename file from " + source.getCanonicalPath() + " to "
							+ target.getCanonicalPath());
				}
			} else {
				LOGGER.info("rename " + source.getPath() + " to " + target.getPath());
			}
		}
		return true;
	}

}

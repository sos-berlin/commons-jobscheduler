package sos.scheduler.file;
import static com.sos.scheduler.messages.JSMessages.JSJ_F_0010;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.i18n.annotation.I18NResourceBundle;

/**
 * This job renames a file or several files of a directory.
 * It can be used standalone or as an order driven job.
 *
 * @author Florian Schreiber <fs@sos-berlin.com>
 * @since  2006-11-01
*/
@I18NResourceBundle(baseName = "com.sos.scheduler.messages", defaultLocale = "en")
public class JobSchedulerRenameFile extends JobSchedulerFileOperationBase {
	private final String	conSVNVersion	= "$Id$";
	private static final String conClassName = "JobSchedulerRenameFile";
	@Override
	public boolean spooler_init() {
		return super.spooler_init();
	}

	@Override
	public boolean spooler_process() {
		try {
			initialize(conSVNVersion);
			if (file == null) {
				file = source; // alias
			}
			CheckMandatoryFile();
			//
			intNoOfHitsInResultSet = SOSFileOperations.renameFileCnt(file, target, fileSpec, flags, isCaseInsensitive, replacing, replacement, minFileAge,
					maxFileAge, minFileSize, maxFileSize, skipFirstFiles, skipLastFiles, objSOSLogger);
			//
			flgOperationWasSuccessful = intNoOfHitsInResultSet > 0;
			processResult(flgOperationWasSuccessful, source);
			return setReturnResult(flgOperationWasSuccessful);
		}
		catch (Exception e) {
			try {
				e.printStackTrace(System.err);
				processResult(flgOperationWasSuccessful, source);
				String strM = JSJ_F_0010.params( conClassName, e.getLocalizedMessage());
				logger.fatal(strM);
				throw new JobSchedulerException(strM, e);
			}
			catch (Exception x) {
			}
			return false;
		}
	}

	protected void processResult(final boolean rc1, final String message) {
		// do nothing, entry point for subclasses
	}

}

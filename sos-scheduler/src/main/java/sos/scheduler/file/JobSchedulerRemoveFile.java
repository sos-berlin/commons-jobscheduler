package sos.scheduler.file;
import static com.sos.scheduler.messages.JSMessages.JSJ_F_0010;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.i18n.annotation.I18NResourceBundle;

/**
 * This job removes one file or several files from the file system.
 * It can be used standalone or as an order driven job.
 *
 * @author Florian Schreiber <fs@sos-berlin.com>
 * @since  2006-11-01
*/
@I18NResourceBundle(baseName = "com.sos.scheduler.messages", defaultLocale = "en")
public class JobSchedulerRemoveFile extends JobSchedulerFileOperationBase {
	private final static String	conClassName	= "JobSchedulerRemoveFile";

	@Override
	public boolean spooler_init() {
		return super.spooler_init();
	}

	@Override
	public boolean spooler_process() {
		try {
			initialize("");
			if (file == null) { // alias
				file = source;
			}
			CheckMandatoryFile();
			
			intNoOfHitsInResultSet = SOSFileOperations.removeFileCnt(file, fileSpec, flags, isCaseInsensitive, minFileAge, maxFileAge, minFileSize,
					maxFileSize, skipFirstFiles, skipLastFiles, objSOSLogger);
			
			flgOperationWasSuccessful = intNoOfHitsInResultSet > 0;
			return setReturnResult(flgOperationWasSuccessful);
		}
		catch (Exception e) {
			String strM = JSJ_F_0010.params(conClassName, e.getLocalizedMessage());
			logger.error(strM);
			logger.trace("", e);
			return signalFailure();
		}
	}
}

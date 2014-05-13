package sos.scheduler.file;

import static com.sos.scheduler.messages.JSMessages.JSJ_F_0010;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.i18n.annotation.I18NResourceBundle;

/**
 * This job checks for file existence of a file or several files of a directory.
 * It can be used standalone or as an order driven job.
 *
 * @author Uwe Risse <uwe.risse@sos-berlin.com>
 * @since  2006-12-08
 * @version $Id$
*/
@I18NResourceBundle(baseName = "com.sos.scheduler.messages", defaultLocale = "en")
public class JobSchedulerCanWrite extends JobSchedulerFileOperationBase {
	private final String	conSVNVersion	= "$Id$";
	private static final String conClassName = "JobSchedulerCanWrite";

	@Override
	public boolean spooler_init() {
		return super.spooler_init();
	}

	@Override
	public boolean spooler_process() {
		try {
			initialize(conSVNVersion);
			CheckMandatoryFile();
			//
			flgOperationWasSuccessful = SOSFileOperations.canWrite(file, fileSpec, isCaseInsensitive, objSOSLogger);
			//
			return setReturnResult(flgOperationWasSuccessful);
		}
		catch (Exception e) {
			try {
				e.printStackTrace(System.err);
				String strM = JSJ_F_0010.params( conClassName, e.getLocalizedMessage());
				logger.fatal(strM);
				throw new JobSchedulerException(strM, e);
			}
			catch (Exception x) {
			}
			return signalFailure();
		}
	}
}

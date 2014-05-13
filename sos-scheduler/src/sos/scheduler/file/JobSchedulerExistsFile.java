package sos.scheduler.file;
import static com.sos.scheduler.messages.JSMessages.JSJ_F_0010;

import java.util.regex.Pattern;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.SOSFileSystemOperations;
import com.sos.i18n.annotation.I18NResourceBundle;

/**
 * This job checks for file existence of a file or several files of a directory.
 * It can be used standalone or as an order driven job.
 *
 * @since  2006-12-08
 * @Version $Id$
*/
@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JobSchedulerExistsFile extends JobSchedulerFileOperationBase {
	private final String	conSVNVersion	= "$Id$";
	private final String conClassName = this.getClass().getName();

	@Override
	public boolean spooler_init() {
		return super.spooler_init();
	}

	@Override
	public boolean spooler_process() {
		try {
			initialize(conSVNVersion);
			CheckMandatoryFile();
			SOSFileOperations = new SOSFileSystemOperations();
			flgOperationWasSuccessful = SOSFileOperations.existsFile(file, fileSpec, Pattern.CASE_INSENSITIVE, minFileAge, maxFileAge, minFileSize, maxFileSize, skipFirstFiles,
					skipLastFiles, objSOSLogger);

			if (flgOperationWasSuccessful == true) {
				flgOperationWasSuccessful = checkSteadyStateOfFiles();
			}
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
			return false;
		}
	}
}

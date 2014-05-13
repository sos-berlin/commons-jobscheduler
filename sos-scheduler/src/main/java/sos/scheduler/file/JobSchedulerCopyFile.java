package sos.scheduler.file;
import static com.sos.scheduler.messages.JSMessages.JSJ_F_0010;
import static com.sos.scheduler.messages.JSMessages.JSJ_F_0011;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.i18n.annotation.I18NResourceBundle;

/**
 * This job copies a file or several files of a directory.
 * It can be used standalone or as an order driven job.
 *
 * @author Florian Schreiber <fs@sos-berlin.com>
 * @since  2006-11-01
 * @version $Id$
*/
@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JobSchedulerCopyFile extends JobSchedulerFileOperationBase {
	private final String	conSVNVersion	= "$Id$";
	@SuppressWarnings("hiding")
	private final String	conClassName	= this.getClass().getName();

	private void doFileOperation(final String strSource, final String strTarget) throws Exception {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::doFileOperation";

		intNoOfHitsInResultSet += SOSFileOperations.copyFileCnt(strSource, strTarget, fileSpec, flags, isCaseInsensitive, replacing, replacement, minFileAge,
				maxFileAge, minFileSize, maxFileSize, skipFirstFiles, skipLastFiles, objSOSLogger);
		saveResultList();

	} // private void doFileOperation
	@Override
	public boolean spooler_init() {
		return super.spooler_init();
	}

	@Override
	public boolean spooler_process() {
		try {
			initialize(conSVNVersion);
			CheckMandatorySource();
			// CheckMandatoryTarget(); // is not mandatory

			String[] fileSource = source.split(";");
			String[] fileTarget = null;
			if (isNotNull(target)) {
				fileTarget = target.split(";");
				if (fileSource.length != fileTarget.length) {
					String strM = JSJ_F_0011.params(fileSource.length, fileTarget.length);
					logger.fatal(strM);
					throw new JobSchedulerException(strM);
				}
			}

			String[] fileSpecs = fileSpec.split(";");
			boolean flgPathAndSpecHasSameNumberOfItems = fileSource.length == fileSpecs.length;
			fileSpec = fileSpecs[0];
			for (int i = 0; i < fileSource.length; i++) {
				String strSource = fileSource[i];
				String strTarget = null;
				if (isNotNull(target)) {
					strTarget = fileTarget[i];
				}
				if (isNotEmpty(fileSpec)) {
					if (flgPathAndSpecHasSameNumberOfItems == true) {
						fileSpec = fileSpecs[i];
					}
				}
				doFileOperation(strSource, strTarget);
			}

			flgOperationWasSuccessful = intNoOfHitsInResultSet > 0;
			return setReturnResult(flgOperationWasSuccessful);
		}
		catch (Exception e) {
			try {
				String strM = JSJ_F_0010.params(conClassName, e.getLocalizedMessage());
				logger.fatal(strM + "\n" + StackTrace2String(e));
				throw new JobSchedulerException(strM, e);
			}
			catch (Exception x) {
			}
			return signalFailure();
		}
	}

}

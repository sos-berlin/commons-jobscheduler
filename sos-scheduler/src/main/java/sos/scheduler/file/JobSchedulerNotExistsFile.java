package sos.scheduler.file;

import static com.sos.scheduler.messages.JSMessages.JSJ_F_0010;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.i18n.annotation.I18NResourceBundle;

/** This job checks for file non-existence of a file or several files of a
 * directory. It can be used standalone or as an order driven job.
 *
 * @author Florian Schreiber
 * @since 2006-12-08 */
@I18NResourceBundle(baseName = "com.sos.scheduler.messages", defaultLocale = "en")
public class JobSchedulerNotExistsFile extends JobSchedulerFileOperationBase {

    private final String conSVNVersion = "$Id$";
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
            flgOperationWasSuccessful = !SOSFileOperations.existsFile(file, fileSpec, isCaseInsensitive, minFileAge, maxFileAge, minFileSize, maxFileSize, skipFirstFiles, skipLastFiles, objSOSLogger);
            return setReturnResult(flgOperationWasSuccessful);
        } catch (Exception e) {
            try {
                logger.error(e.getMessage(), e);
                String strM = JSJ_F_0010.params(conClassName, e.getLocalizedMessage());
                logger.fatal(strM);
                throw new JobSchedulerException(strM, e);
            } catch (Exception x) {
            }
            return false;
        }
    }
}

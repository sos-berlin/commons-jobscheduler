package sos.scheduler.file;

import static com.sos.scheduler.messages.JSMessages.JSJ_F_0010;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author Uwe Risse */
@I18NResourceBundle(baseName = "com.sos.scheduler.messages", defaultLocale = "en")
public class JobSchedulerCanWrite extends JobSchedulerFileOperationBase {

    private static final Logger LOGGER = Logger.getLogger(JobSchedulerCanWrite.class);

    @Override
    public boolean spooler_init() {
        return super.spooler_init();
    }

    @Override
    public boolean spooler_process() {
        try {
            initialize();
            CheckMandatoryFile();
            flgOperationWasSuccessful = SOSFileOperations.canWrite(file, fileSpec, isCaseInsensitive, objSOSLogger);
            return setReturnResult(flgOperationWasSuccessful);
        } catch (Exception e) {
            try {
                LOGGER.error(e.getMessage(), e);
                String strM = JSJ_F_0010.params("JobSchedulerCanWrite", e.getLocalizedMessage());
                LOGGER.fatal(strM);
                throw new JobSchedulerException(strM, e);
            } catch (Exception x) {
            }
            return signalFailure();
        }
    }
}

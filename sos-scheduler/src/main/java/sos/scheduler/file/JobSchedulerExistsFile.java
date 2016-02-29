package sos.scheduler.file;

import static com.sos.scheduler.messages.JSMessages.JSJ_F_0010;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.SOSFileSystemOperations;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JobSchedulerExistsFile extends JobSchedulerFileOperationBase {

    private static final Logger LOGGER = Logger.getLogger(JobSchedulerExistsFile.class);
    private static final String CLASSNAME = "JobSchedulerExistsFile";
    private final String conSVNVersion = "$Id$";

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
            flgOperationWasSuccessful = SOSFileOperations.existsFile(file, fileSpec, Pattern.CASE_INSENSITIVE, minFileAge, 
                    maxFileAge, minFileSize, maxFileSize, skipFirstFiles, skipLastFiles, objSOSLogger);
            if (flgOperationWasSuccessful == true) {
                flgOperationWasSuccessful = checkSteadyStateOfFiles();
            }
            return setReturnResult(flgOperationWasSuccessful);
        } catch (Exception e) {
            try {
                LOGGER.error(e.getMessage(), e);
                String strM = JSJ_F_0010.params(CLASSNAME, e.getMessage());
                logger.fatal(strM);
                throw new JobSchedulerException(strM, e);
            } catch (Exception x) {
            }
            return false;
        }
    }
    
}

package sos.scheduler.file;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.localization.Messages;
import org.apache.log4j.Logger;

import java.util.Locale;

@I18NResourceBundle(baseName = "com.sos.scheduler.messages", defaultLocale = "en")
public class JobSchedulerFolderTreeMain extends JSToolBox {

    private static final Logger LOGGER = Logger.getLogger(JobSchedulerFolderTreeMain.class);
    protected static Messages Messages = null;
    protected JobSchedulerFolderTreeOptions objOptions = null;

    public final static void main(String[] pstrArgs) {
        final String conMethodName = "JobSchedulerFolderTreeMain::Main";
        Messages = new Messages("com_sos_scheduler_messages", Locale.getDefault());
        LOGGER.info("JobSchedulerFolderTree - Main");
        try {
            JobSchedulerFolderTree objM = new JobSchedulerFolderTree();
            JobSchedulerFolderTreeOptions objO = objM.Options();
            objO.CommandLineArgs(pstrArgs);
            objM.Execute();
        } catch (Exception e) {
            LOGGER.error(conMethodName + ": " + "Error occured ..." + e.getMessage(), e);
            int intExitCode = 99;
            LOGGER.error(Messages.getMsg("JSJ-E-105: %1$s - terminated with exit-code %2$d", conMethodName, intExitCode), e);
            System.exit(intExitCode);
        }
        LOGGER.info(Messages.getMsg("JSJ-I-106: %1$s - ended without errors", conMethodName));
    }

}
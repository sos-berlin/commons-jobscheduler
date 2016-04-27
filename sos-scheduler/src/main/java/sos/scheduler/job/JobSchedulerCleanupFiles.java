package sos.scheduler.job;

import static com.sos.scheduler.messages.JSMessages.JFO_E_0016;
import static com.sos.scheduler.messages.JSMessages.JFO_I_0014;
import static com.sos.scheduler.messages.JSMessages.JFO_I_0015;
import static com.sos.scheduler.messages.JSMessages.JFO_I_0019;
import static com.sos.scheduler.messages.JSMessages.JFO_I_0020;
import static com.sos.scheduler.messages.JSMessages.JSJ_F_0010;

import java.io.File;
import java.util.Vector;

import sos.scheduler.file.JobSchedulerFileOperationBase;
import sos.util.SOSFile;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author andreas.pueschel@sos-berlin.com */
@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JobSchedulerCleanupFiles extends JobSchedulerFileOperationBase {

    private final static String conClassName = "JobSchedulerCleanupFiles";

    @Override
    public boolean spooler_init() {
        return super.spooler_init();
    }

    @Override
    public boolean spooler_process() {
        initialize();
        try {
            if (isEmpty(filePath)) {
                filePath = conPropertyJAVA_IO_TMPDIR;
            }
            String[] filePaths = filePath.split(";");
            String tmpFileSpec = getParamValue(new String[] { conParameterFILE_SPEC, conParameterFILE_SPECIFICATION }, EMPTY_STRING);
            if (isEmpty(tmpFileSpec)) {
                fileSpec = "^(sos.*)";
            }
            if (lngFileAge <= 0) {
                lngFileAge = calculateFileAge(getParamValue(conParameterFILE_AGE, "24:00"));
            }
            String[] fileSpecs = fileSpec.split(";");
            boolean flgPathAndSpecHasSameNumberOfItems = filePaths.length == fileSpecs.length;
            fileSpec = fileSpecs[0];
            for (int i = 0; i < filePaths.length; i++) {
                int counter = 0;
                filePath = filePaths[i];
                if (conPropertyJAVA_IO_TMPDIR.equalsIgnoreCase(filePath.trim())) {
                    filePath = System.getProperty(conPropertyJAVA_IO_TMPDIR);
                }
                if (flgPathAndSpecHasSameNumberOfItems) {
                    fileSpec = fileSpecs[i];
                }
                logger.debug(JFO_I_0019.params(filePath));
                Vector<File> filelist = SOSFile.getFolderlist(filePath, fileSpec, 0);
                if (filelist.isEmpty()) {
                    logger.info(JFO_I_0020.params(fileSpec));
                }
                if (warningFileLimit > 0 && filelist.size() >= warningFileLimit) {
                    logger.error(JFO_E_0016.params(filelist.size(), filePath, warningFileLimit, conParameterWARNING_FILE_LIMIT));
                }
                for (File tempFile : filelist) {
                    long interval = System.currentTimeMillis() - tempFile.lastModified();
                    if (interval > lngFileAge) {
                        counter += SOSFile.deleteFile(tempFile);
                        logger.info(JFO_I_0014.params(tempFile.getAbsolutePath()));
                    }
                }
                if (counter > 0) {
                    logger.info(JFO_I_0015.params(counter, filePath));
                }
            }
        } catch (Exception e) {
            throw new JobSchedulerException(JSJ_F_0010.params(conClassName, e.getLocalizedMessage()), e);
        }
        return signalSuccess();
    }

}
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
// "JFO_I_0014"; // File deleted: %1$s"
// "JFO_I_0020";
// "JFO_I_0019";
// "JFO_I_0015"; // %1$d %2$s files deleted
// "JFO_E_0016";

/** @author andreas.pueschel@sos-berlin.com
 *
 *
 *         cleanup temporary files created by other jobs Paramters: file_path
 *         Path for the files to remove file_specification Regular expression as
 *         file specification file_age Minimum file-age either milliseconds or
 *         hh:mm[:ss] $Id$ */
@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JobSchedulerCleanupFiles extends JobSchedulerFileOperationBase {

    private final String conSVNVersion = "$Id$";
    private final static String conClassName = "JobSchedulerCleanupFiles";

    @Override
    public boolean spooler_init() {
        return super.spooler_init();
    }

    @Override
    public boolean spooler_process() {

        initialize(conSVNVersion);

        try {
            if (isEmpty(filePath)) {
                filePath = conPropertyJAVA_IO_TMPDIR;  // JS-788
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
                if (filePath.trim().equalsIgnoreCase(conPropertyJAVA_IO_TMPDIR)) {
                    filePath = System.getProperty(conPropertyJAVA_IO_TMPDIR);
                }
                if (flgPathAndSpecHasSameNumberOfItems == true) {
                    fileSpec = fileSpecs[i];
                }

                logger.debug(JFO_I_0019.params(filePath));  // Looking for files
                                                           // in: %1$s
                Vector<File> filelist = SOSFile.getFolderlist(filePath, fileSpec, 0);
                if (filelist.size() == 0) {
                    logger.info(JFO_I_0020.params(fileSpec));  // .. No files
                                                              // matched.
                }

                if (warningFileLimit > 0 && filelist.size() >= warningFileLimit) {  // '%1$s'
                                                                                   // files
                                                                                   // were
                                                                                   // found
                                                                                   // in
                                                                                   // directory
                                                                                   // '%2$s'.
                                                                                   // That
                                                                                   // is
                                                                                   // more
                                                                                   // than
                                                                                   // specified
                                                                                   // with
                                                                                   // param
                                                                                   // '%4$s'
                                                                                   // =
                                                                                   // '%3$d'.
                    logger.error(JFO_E_0016.params(filelist.size(), filePath, warningFileLimit, conParameterWARNING_FILE_LIMIT));
                }

                for (File tempFile : filelist) {
                    long interval = System.currentTimeMillis() - tempFile.lastModified();
                    if (interval > lngFileAge) {
                        counter += SOSFile.deleteFile(tempFile);
                        logger.info(JFO_I_0014.params(tempFile.getAbsolutePath()));
                    }
                } // for
                if (counter > 0) {
                    String strT = filePath;
                    logger.info(JFO_I_0015.params(counter, strT));
                }
            } // for

        } catch (Exception e) {
            throw new JobSchedulerException(JSJ_F_0010.params(conClassName, e.getLocalizedMessage()), e);
        }

        // return false;
        return signalSuccess();
    }
    /*
     * ! \var JSJ_I_0012 \brief "File deleted: %1$s"
     */

}

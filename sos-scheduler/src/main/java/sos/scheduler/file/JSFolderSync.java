package sos.scheduler.file;

import java.io.File;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JSFolderSync extends JSFileOperationBase {

    private static final Logger LOGGER = Logger.getLogger(JSFolderSync.class);
    private static final String JFO_I_0010 = "JFO_I_0010";
    private static final String JFO_I_0011 = "JFO_I_0011";
    private static final String JFO_I_0012 = "JFO_I_0012";
    private static final String JFO_I_0013 = "JFO_I_0013";

    public JSFolderSync() {
        super();
    }

    public boolean Execute() {
        final String conMethodName = "JSFolderSync::Execute";
        LOGGER.debug(String.format(Messages.getMsg("JSJ-I-110"), conMethodName));
        try {
            initialize();
            Options().file.CheckMandatory();
            Options().target.CheckMandatory();
            Options().file_spec.setRegExpFlags(Pattern.CASE_INSENSITIVE);
            flgOperationWasSuccessful = existsFile(Options().file, Options().file_spec, Options().min_file_age, Options().max_file_age,
                    Options().min_file_size, Options().max_file_size, Options().skip_first_files, Options().skip_last_files, -1, -1);
            Vector<File> vecSourceList = new Vector<File>();
            vecSourceList.addAll(lstResultList);
            lstResultList = new Vector<File>();
            flgOperationWasSuccessful = existsFile(Options().target, Options().file_spec, Options().min_file_age, Options().max_file_age,
                    Options().min_file_size, Options().max_file_size, Options().skip_first_files, Options().skip_last_files, -1, -1);
            Vector<File> vecTargetList = new Vector<File>();
            vecTargetList.addAll(lstResultList);
            Vector<File> vecSyncList = new Vector<File>();
            for (File objSourceFile : vecSourceList) {
                String strSourceFileName = objSourceFile.getName();
                long lngLastModifiedSource = objSourceFile.lastModified();
                boolean flgFileIsOnTarget = false;
                boolean flgIsNewerOnSource = false;
                for (File objTargetFile : vecTargetList) {
                    if (strSourceFileName.equalsIgnoreCase(objTargetFile.getName())) {
                        flgFileIsOnTarget = true;
                        if (lngLastModifiedSource > objTargetFile.lastModified()) {
                            flgIsNewerOnSource = true;
                        }
                        LOGGER.info(Messages.getMsg(JFO_I_0010, objSourceFile.getAbsoluteFile(), flgIsNewerOnSource));
                        break;
                    }
                }
                if (!flgFileIsOnTarget) {
                    LOGGER.debug(Messages.getMsg(JFO_I_0011, objSourceFile.getName()));
                    vecSyncList.add(objSourceFile);
                }
                if (flgIsNewerOnSource) {
                    LOGGER.debug(Messages.getMsg(JFO_I_0012, objSourceFile.getName()));
                    vecSyncList.add(objSourceFile);
                }
            }
            LOGGER.info(Messages.getMsg(JFO_I_0013, vecSyncList.size()));
            for (File objFile2Copy : vecSyncList) {
                String strFileName = objFile2Copy.getAbsolutePath();
                JSFile objF = new JSFile(strFileName);
                String strTargetFileName = Options().target.Value() + objFile2Copy.getName();
                JSFile objTarget = new JSFile(strTargetFileName);
                objTarget.setLastModified(objFile2Copy.lastModified());
            }
            if (!vecSyncList.isEmpty()) {
                flgOperationWasSuccessful = true;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            String strM = Messages.getMsg("JSJ-F-107", conMethodName);
            LOGGER.fatal(strM);
            throw new JobSchedulerException(strM);
        }
        LOGGER.debug(Messages.getMsg("JSJ-I-111", conMethodName));
        return flgOperationWasSuccessful;
    }

    public void init() {
        doInitialize();
    }

    private void doInitialize() {
        // doInitialize
    }

}
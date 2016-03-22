package sos.scheduler.file;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JSExistsFile extends JSFileOperationBase {

    private static final Logger LOGGER = Logger.getLogger(JSExistsFile.class);

    public JSExistsFile() {
        super();
    }

    public boolean Execute() throws Exception {
        final String conMethodName = "JSExistsFile::Execute";
        LOGGER.debug(String.format(Messages.getMsg("JSJ-I-110"), conMethodName));
        try {
            initialize();
            Options().file.CheckMandatory();
            Options().file_spec.setRegExpFlags(Pattern.CASE_INSENSITIVE);
            flgOperationWasSuccessful = existsFile(Options().file, Options().file_spec, Options().min_file_age, Options().max_file_age, Options().min_file_size, Options().max_file_size, Options().skip_first_files, Options().skip_last_files, -1, -1);
            flgOperationWasSuccessful = createResultListParam(flgOperationWasSuccessful);
            return flgOperationWasSuccessful;
        } catch (Exception e) {
            LOGGER.error(Messages.getMsg("JSJ-I-107", conMethodName), e);
        } finally {
            if (flgOperationWasSuccessful) {
                LOGGER.debug(Messages.getMsg("JSJ-I-111", conMethodName));
            }
        }
        return flgOperationWasSuccessful;
    }

    public void init() {
        doInitialize();
    }

    private void doInitialize() {
        // doInitialize
    }

}
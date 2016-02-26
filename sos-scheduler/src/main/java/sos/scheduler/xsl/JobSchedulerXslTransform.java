package sos.scheduler.xsl;

import static com.sos.scheduler.messages.JSMessages.JSJ_F_107;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_110;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_111;

import java.io.File;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.JSHelper.io.Files.JSXMLFile;

public class JobSchedulerXslTransform extends JSJobUtilitiesClass<JobSchedulerXslTransformOptions> {

    private static final Logger LOGGER = Logger.getLogger(JobSchedulerXslTransform.class);
    protected HashMap<String, String> hsmParameters = null;

    public JobSchedulerXslTransform() {
        super(new JobSchedulerXslTransformOptions());
    }

    @Override
    public JobSchedulerXslTransformOptions Options() {
        if (objOptions == null) {
            objOptions = new JobSchedulerXslTransformOptions();
        }
        return objOptions;
    }

    public JobSchedulerXslTransform Execute() throws Exception {
        final String methodName = "JobSchedulerXslTransform::Execute";
        LOGGER.debug(JSJ_I_110.get(methodName));
        try {
            Options().CheckMandatory();
            LOGGER.debug(Options().dirtyString());
            JSXMLFile objXMLFile = new JSXMLFile(Options().FileName.Value());
            if (Options().XslFileName.IsEmpty()) {
                LOGGER.info("no xslt-file specified. copy xml file only");
                String strXML = objXMLFile.getContent();
                JSFile outFile = new JSFile(Options().OutputFileName.Value());
                outFile.setCharSet4OutputFile("UTF-8");
                outFile.Write(strXML);
                outFile.close();
            } else {
                objXMLFile.setParameters(hsmParameters);
                objXMLFile.Transform(new File(Options().XslFileName.Value()), new File(Options().OutputFileName.Value()));
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new JobSchedulerException(JSJ_F_107.get(methodName) + ": " + e.getMessage(), e);
        }
        JSJ_I_111.toLog(methodName);
        return this;
    }

    public void init() {
        doInitialize();
    }

    public void setParameters(final HashMap<String, String> pobjHshMap) {
        hsmParameters = pobjHshMap;
    }

    private void doInitialize() {
        // doInitialize
    } 

}
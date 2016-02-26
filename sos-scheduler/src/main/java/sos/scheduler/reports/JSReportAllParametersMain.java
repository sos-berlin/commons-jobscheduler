package sos.scheduler.reports;

import com.sos.JSHelper.Basics.JSToolBox;
import org.apache.log4j.Logger;

public class JSReportAllParametersMain extends JSToolBox {

    private static final Logger LOGGER = Logger.getLogger(JSReportAllParametersMain.class);

    protected JSReportAllParametersOptions objOptions = null;

    public final static void main(String[] pstrArgs) {
        final String methodName = "JSReportAllParametersMain::Main";
        LOGGER.info("JSReportAllParameters - Main");
        try {
            JSReportAllParameters objM = new JSReportAllParameters();
            JSReportAllParametersOptions objO = objM.Options();
            objO.CommandLineArgs(pstrArgs);
            objM.Execute();
        } catch (Exception e) {
            LOGGER.error(methodName + ": " + "Error occured ..." + e.getMessage(), e);
            int intExitCode = 99;
            LOGGER.error(String.format("JSJ-E-105: %1$s - terminated with exit-code %2$d", methodName, intExitCode), e);
            System.exit(intExitCode);
        }
        LOGGER.info(String.format("JSJ-I-106: %1$s - ended without errors", methodName));
    }

}
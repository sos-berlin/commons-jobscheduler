package sos.scheduler.db;

import com.sos.JSHelper.Basics.JSToolBox;
import org.apache.log4j.Logger;

public class SOSSQLPlusJobMain extends JSToolBox {

    private static final Logger LOGGER = Logger.getLogger(SOSSQLPlusJobMain.class);
    protected SOSSQLPlusJobOptions objOptions = null;

    public final static void main(final String[] pstrArgs) {
        final String conMethodName = "SOSSQLPlusJobMain::Main";
        LOGGER.info("SOSSQLPlusJob - Main");
        try {
            SOSSQLPlusJob objM = new SOSSQLPlusJob();
            SOSSQLPlusJobOptions objO = objM.Options();
            objO.CommandLineArgs(pstrArgs);
            objM.Execute();
        } catch (Exception e) {
            LOGGER.error(conMethodName + ": " + "Error occured ..." + e.getMessage(), e);
            int intExitCode = 99;
            LOGGER.error(String.format("JSJ-E-105: %1$s - terminated with exit-code %2$d", conMethodName, intExitCode), e);
            System.exit(intExitCode);
        }
        LOGGER.info(String.format("JSJ-I-106: %1$s - ended without errors", conMethodName));
        System.exit(0);
    }

}
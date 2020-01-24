package sos.net.ssh;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.i18n.annotation.I18NResourceBundle;

/** @author Klaus Buettner */
@I18NResourceBundle(baseName = "com_sos_net_messages", defaultLocale = "en")
public class SOSSSHJob2Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSSHJob2Main.class);

    public final static void main(final String[] pstrArgs) throws Exception {
        final String conMethodName = "SOSSSHJob2Main::Main";
        LOGGER.info("SOSSSHJob2 - Main");
        LOGGER.info("User-Dir : " + System.getProperty("user.dir"));
        try {
            SOSSSHJob2 objM = new SOSSSHJobTrilead();
            SOSSSHJobOptions objO = objM.getOptions();
            objO.commandLineArgs(pstrArgs);
            objM.execute();
        } catch (Exception e) {
            LOGGER.error(conMethodName + ": " + "Error occured ..." + e.getMessage(), e);
            LOGGER.info(conMethodName + ": terminated with exit-code 99");
            System.exit(99);
        }
        LOGGER.info(conMethodName + ": terminated without errors");
    }

}

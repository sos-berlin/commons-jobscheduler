package sos.net.sosftp2;

import java.util.Properties;

import sos.configuration.SOSConfiguration;
import sos.net.SOSFTPCommand;
import sos.util.SOSLogger;

public class SOSFTPCommandSSH extends SOSFTPCommand {

    public SOSFTPCommandSSH(SOSLogger logger, Properties arguments_) throws Exception {
        super(logger, arguments_);

    }

    public SOSFTPCommandSSH(SOSConfiguration sosConfiguration_, SOSLogger logger) throws Exception {
        super(sosConfiguration_, logger);

    }

    /** overwrite Method from super class.
     * 
     * Es darf keinen Banner für das SSH Job (=operation=execute) ausgegeben
     * werden, wenn banner_header bzw. banner_footer nicht angegeben sind.
     * 
     * This program logs output to stdout or to a file that has been specified
     * by the parameter log_filename. A template can be used in order to
     * organize the output that is created. The output is grouped into header,
     * file list and footer. This specifies a template file for header and
     * footer output. Templates can use internal variables and parameters as
     * placeholders in the form %{placeholder}.
     * 
     * @param header
     * @return String
     * @throws Exception */
    public String getBanner(boolean header) throws Exception {
        this.bannerHeader = "";
        this.bannerFooter = "";
        return super.getBanner(header);
    }

}

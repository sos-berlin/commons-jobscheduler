package com.sos.JSHelper.Options;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/** @author KB */
public class SOSOptionPortNumber extends SOSOptionInteger {

    private static final long serialVersionUID = 8291980761608522995L;
    public static final int conPort4FTP = 21;
    public static final int conPort4SSH = 22;
    public static final int conPort4SFTP = 22;
    public static final int conPort4Telnet = 23;
    public static final int conPort4smtp = 25;
    public static final int conPort4http = 80;
    public static final int conPort4https = 443;
    public static final int conPort4pop3 = 110;
    public static final int conPort4imap = 143;
    public static final int conPortWebDav = 443;
    public static final int conPort4FTPS = 990;

    public SOSOptionPortNumber(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue,
            final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
    }

    public SOSOptionPortNumber(final String pstrPortNumber) {
        super(null, null, "", pstrPortNumber, "", false);
    }

    public static int getStandardSFTPPort() {
        return conPort4SFTP;
    }

    public static int getStandardFTPPort() {
        return conPort4FTP;
    }

    @Override
    public void Value(final String pstrPortNo) {
        String strP = pstrPortNo;
        String pstrPortNumber = pstrPortNo;
        if (pstrPortNumber == null) {
            strP = "0";
        }
        try {
            if (isNotEmpty(strP)) {
                strP = stripQuotes(strP);
                int portNum = Integer.parseInt(strP);
                if (portNum >= 0 && portNum <= 65535) {
                    super.Value(strP);
                } else {
                    throw new JobSchedulerException(String.format("invalid port number: %1$s", strP));
                }
            }
        } catch (Exception e) {
            throw new JobSchedulerException(String.format("invalid port number: %1$s\n%2$s", strP, e.getMessage()));
        }
    }

}

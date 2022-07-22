package com.sos.vfs.ftp;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Options.SOSOptionAuthenticationMethod.enuAuthenticationMethods;
import com.sos.vfs.common.SOSFileEntry;
import com.sos.vfs.common.options.SOSProviderOptions;

import sos.util.SOSString;

public class SOSFTPTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSFTPTest.class);

    private SOSProviderOptions getOptions() {
        SOSProviderOptions o = new SOSProviderOptions();
        o.host.setValue("localhost");
        o.port.setValue("21");
        o.authMethod.setValue(enuAuthenticationMethods.password);

        o.user.setValue("user");
        o.password.setValue("password");
        return o;
    }

    @Ignore
    @Test
    public void testInfoMethods() {
        SOSFTP p = new SOSFTP();
        try {
            p.connect(getOptions());

            List<SOSFileEntry> r = p.listNames("/transfer-1", true, true);
            LOGGER.info(p.getReplyString());
            if (r != null) {
                LOGGER.info("found=" + r.size());
                for (SOSFileEntry e : r) {
                    LOGGER.info(SOSString.toString(e));
                    String path = e.getFullPath();
                    LOGGER.info("  isDirecory=" + p.isDirectory(path) + ", directoryExists=" + p.directoryExists(path));
                    LOGGER.info("  fileExists=" + p.fileExists(path) + ",  size=" + p.size(path));
                    LOGGER.info("  getModificationDateTime=" + p.getModificationDateTime(path));

                    if (!e.isDirectory()) {
                        SOSFileEntry f = p.getFileEntry(e.getFullPath());
                        LOGGER.info("    " + SOSString.toString(f));
                    }
                }
            }

        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            p.disconnect();
        }
    }

    @Ignore
    @Test
    public void testExecuteMethods() {
        SOSFTP p = new SOSFTP();
        try {
            p.connect(getOptions());

            p.mkdir("test/a/b/c");
            p.rmdir("test/a");

        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            p.disconnect();
        }
    }

}

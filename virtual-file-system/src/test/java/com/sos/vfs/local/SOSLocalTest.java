package com.sos.vfs.local;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Options.SOSOptionTransferType.TransferTypes;
import com.sos.vfs.common.options.SOSBaseOptions;
import com.sos.vfs.common.options.SOSProviderOptions;

public class SOSLocalTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSLocalTest.class);

    private SOSBaseOptions baseOptions;
    private SOSProviderOptions providerOptions;

    private SOSBaseOptions getBaseOptions() {
        if (baseOptions == null) {
            baseOptions = new SOSBaseOptions();
        }
        return baseOptions;
    }

    private SOSProviderOptions getProviderOptions() {
        if (providerOptions == null) {
            providerOptions = new SOSProviderOptions();
            providerOptions.protocol.setValue(TransferTypes.local);
            providerOptions.host.setValue("localhost");
        }
        return providerOptions;
    }

    @Ignore
    @Test
    public void testInfoMethods() {
        SOSLocal p = new SOSLocal();
        p.setBaseOptions(getBaseOptions());
        try {
            p.connect(getProviderOptions());

            File f = new File("/home/sos/test.txt");
            LOGGER.info("[File().lastModified   ]" + f.lastModified());
            LOGGER.info("[Provider              ]" + p.getFile(f.getAbsolutePath()).getModificationDateTime());

            FileTime ft = Files.getLastModifiedTime(f.toPath());
            LOGGER.info("[FileTime][MILLISECONDS]" + ft.to(TimeUnit.MILLISECONDS));
            LOGGER.info("[FileTime][MICROSECONDS]" + ft.to(TimeUnit.MICROSECONDS));
            LOGGER.info("[FileTime][NANOSECONDS ]" + ft.to(TimeUnit.NANOSECONDS));

            // Files.setLastModifiedTime(f.toPath(), FileTime.fromMillis(0L));
            // p.getFile(f.getAbsolutePath()).setModificationDateTime(0L);

        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            p.disconnect();
        }
    }

}

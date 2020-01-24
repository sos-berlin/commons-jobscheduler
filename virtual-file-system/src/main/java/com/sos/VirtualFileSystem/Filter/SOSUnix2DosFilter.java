package com.sos.VirtualFileSystem.Filter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.VirtualFileSystem.Filter.Options.SOSFilterOptions;

/** @author KB */
public class SOSUnix2DosFilter extends SOSNullFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSUnix2DosFilter.class);

    public SOSUnix2DosFilter() {
        super();
    }

    public SOSUnix2DosFilter(final SOSFilterOptions pobjOptions) {
        super(pobjOptions);
    }

    @Override
    protected void doProcess() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        byte pb = -1;
        dumpByteBuffer();
        try {
            for (byte b : bteBuffer) {
                if (b == 10 && pb != 13) {
                    dos.write((byte) 13);
                }
                dos.write(b);
                pb = b;
            }
            dos.flush();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        bteBuffer = baos.toByteArray();
        dumpByteBuffer();
    }

}

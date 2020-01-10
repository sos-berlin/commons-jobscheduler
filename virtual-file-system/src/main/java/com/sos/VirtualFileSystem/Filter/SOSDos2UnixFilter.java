package com.sos.VirtualFileSystem.Filter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.VirtualFileSystem.Filter.Options.SOSFilterOptions;

/** @author KB */
public class SOSDos2UnixFilter extends SOSNullFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSDos2UnixFilter.class);

    public SOSDos2UnixFilter() {
        super();
    }

    public SOSDos2UnixFilter(final SOSFilterOptions pobjOptions) {
        super(pobjOptions);
    }

    @Override
    protected void doProcess() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        byte b = -1;
        byte nb = -1;
        int j = 0;
        int iLen = bteBuffer.length;
        String strT = "";
        for (byte element : bteBuffer) {
            strT += element + " ";
        }
        LOGGER.debug(strT);
        try {
            for (int i = 0; i < iLen; i++) {
                b = bteBuffer[i];
                if (b == 13) {
                    if (i < iLen) {
                        nb = bteBuffer[++i];
                    } else {
                        nb = -1;
                    }
                    if (nb == -1) {
                        dos.write(b);
                    } else {
                        if (nb == 10) {
                            dos.write(nb);
                        } else {
                            dos.write(b);
                            dos.write(nb);
                        }
                    }
                } else {
                    dos.write(b);
                }
            }
            dos.flush();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        bteBuffer = baos.toByteArray();
        strT = "";
        for (byte element : bteBuffer) {
            strT += element + " ";
        }
        LOGGER.debug(strT);
        LOGGER.debug(byte2String(bteBuffer));
    }

}

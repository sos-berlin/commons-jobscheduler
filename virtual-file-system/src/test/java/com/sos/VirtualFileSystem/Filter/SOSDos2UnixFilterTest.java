package com.sos.VirtualFileSystem.Filter;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSDos2UnixFilterTest extends SOSNullFilterBase<SOSDos2UnixFilter> {

    @SuppressWarnings("unused")
    private final String conClassName = this.getClass().getSimpleName();
    @SuppressWarnings("unused")
    private static final String conSVNVersion = "$Id$";
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSDos2UnixFilterTest.class);

    public SOSDos2UnixFilterTest() {
        super(new SOSDos2UnixFilter());
    }

    @Test
    public void testWriteByteArray() {
        String strT = "abcdef\r\nabcdef";
        String strT2 = "abcdef\nabcdef";
        objF.write(strT.getBytes());

        bteBuffer = objF.read();
        String strX = new String(bteBuffer);
        LOGGER.debug(strX);
        Assert.assertEquals(strT2, strX);
    }

}

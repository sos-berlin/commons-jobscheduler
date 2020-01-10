package com.sos.VirtualFileSystem.Filter;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSUnix2DosFilterTest extends SOSNullFilterBase<SOSUnix2DosFilter> {

    @SuppressWarnings("unused")
    private final String conClassName = this.getClass().getSimpleName();
    @SuppressWarnings("unused")
    private static final String conSVNVersion = "$Id$";
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSUnix2DosFilterTest.class);

    public SOSUnix2DosFilterTest() {
        super(new SOSUnix2DosFilter());
    }

    @Test
    public void testWriteByteArray() {
        String strT = "abcdef\nabcdef";
        String strT2 = "abcdef\r\nabcdef";
        objF.write(strT.getBytes());

        bteBuffer = objF.read();
        String strX = new String(bteBuffer);
        LOGGER.debug(strX);
        Assert.assertEquals(strT2, strX);
    }

}

package com.sos.VirtualFileSystem.Filter;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

public class SOSUnix2DosFilterTest extends SOSNullFilterBase<SOSUnix2DosFilter> {

    @SuppressWarnings("unused")
    private final String conClassName = this.getClass().getSimpleName();
    @SuppressWarnings("unused")
    private static final String conSVNVersion = "$Id$";
    private final Logger logger = Logger.getLogger(this.getClass());

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
        logger.debug(strX);
        Assert.assertEquals(strT2, strX);
    }

}

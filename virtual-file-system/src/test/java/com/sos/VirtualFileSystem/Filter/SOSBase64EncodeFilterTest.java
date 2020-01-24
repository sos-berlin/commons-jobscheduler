package com.sos.VirtualFileSystem.Filter;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSBase64EncodeFilterTest extends SOSNullFilterBase<SOSBase64EncodeFilter> {

    @SuppressWarnings("unused")
    private final String conClassName = this.getClass().getSimpleName();
    @SuppressWarnings("unused")
    private static final String conSVNVersion = "$Id$";
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSBase64EncodeFilterTest.class);

    public SOSBase64EncodeFilterTest() {
        super(new SOSBase64EncodeFilter());
    }

    @Test
    public void testWriteByteArray() {
        String strT = "Hallo, Welt!";
        String strT2 = "SGFsbG8sIFdlbHQh";
        objF.write(strT.getBytes());

        bteBuffer = objF.read();
        String strX = new String(bteBuffer);
        LOGGER.debug(strX);
        // bteBuffer ends with "\r\n", therefore "\r\n" has to be added to
        // expected value
        Assert.assertEquals(strT2 + "\r\n", strX);
    }

}

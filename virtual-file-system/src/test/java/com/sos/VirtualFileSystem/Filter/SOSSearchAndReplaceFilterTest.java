package com.sos.VirtualFileSystem.Filter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.sos.VirtualFileSystem.Filter.Options.SOSFilterOptions;

public class SOSSearchAndReplaceFilterTest {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SOSSearchAndReplaceFilterTest.class);
    private SOSSearchAndReplaceFilter objF = null;
    private SOSFilterOptions objFO = null;
    byte[] bteBuffer = null;

    @Before
    public void setUp() throws Exception {
        objF = new SOSSearchAndReplaceFilter();
        objFO = objF.getOptions();
    }

    @Test
    public void testWriteByteArray() {
        String strT = "Hallo, Welt!";
        objFO.replaceWhat.setValue("(Hallo)");
        objFO.replaceWith.setValue("<b>$1</b>");
        objF.write(strT.getBytes());
        bteBuffer = objF.read();
        String strX = new String(bteBuffer);
        LOGGER.debug(strX);
        Assert.assertEquals("<b>Hallo</b>, Welt!", strX);
    }

    @Test
    public void testWriteByteArray2() {
        String strT = "Hallo, Welt!";
        objFO.replaceWhat.setValue("(Welt)");
        objFO.replaceWith.setValue("<b>$1</b>");
        objF.write(strT);
        String strX = objF.readString();
        LOGGER.debug(strX);
        Assert.assertEquals("Hallo, <b>Welt</b>!", strX);
    }

}

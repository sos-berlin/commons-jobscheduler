package com.sos.VirtualFileSystem.Filter;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sos.VirtualFileSystem.Filter.Options.SOSFilterOptions;

public class SOSSearchAndReplaceFilterTest {

    private static final Logger LOGGER = Logger.getLogger(SOSSearchAndReplaceFilterTest.class);
    private SOSSearchAndReplaceFilter objF = null;
    private SOSFilterOptions objFO = null;
    byte[] bteBuffer = null;

    @Before
    public void setUp() throws Exception {
        objF = new SOSSearchAndReplaceFilter();
        objFO = objF.getOptions();
        BasicConfigurator.configure();
        LOGGER.setLevel(Level.DEBUG);
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

package com.sos.VirtualFileSystem.Filter;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;

public class SOSNullFilterBase<T> {

    @SuppressWarnings("unused")
    private final String conClassName = this.getClass().getSimpleName();
    @SuppressWarnings("unused")
    private static final String conSVNVersion = "$Id$";
    private final Logger logger = Logger.getLogger(this.getClass());

    protected SOSNullFilter objF = null;
    protected byte[] bteBuffer = null;

    public SOSNullFilterBase(final T pobjT) {
        objF = (SOSNullFilter) pobjT;
    }

    @Before
    public void setUp() throws Exception {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.DEBUG);
    }

    // public void testOptions() {
    // }
    //
    // public void testOptionsSOSFTPOptions() {
    // }
    //
    // public void testWriteByteArrayIntInt() {
    // }
    //
    // public void testWriteByteArray() {
    // String strT = "Hallo, Welt!";
    // objF.write(strT.getBytes());
    //
    // bteBuffer = objF.read();
    // String strX = new String(bteBuffer);
    // logger.debug(strX);
    // Assert.assertEquals(strT, strX);
    // }
    //
    // public void testReadByteArray() {
    // }
    //
    // public void testReadByteArrayIntInt() {
    // }
    //
    // public void testReadBufferByteArray() {
    // }
    //
    // public void testReadBufferByteArrayIntInt() {
    // }
    //
    // public void testClose() {
    // }

}

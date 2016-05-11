package com.sos.VirtualFileSystem.Filter;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;

public class SOSNullFilterBase<T> {

    protected SOSNullFilter objF = null;
    protected byte[] bteBuffer = null;
    private static final Logger LOGGER = Logger.getLogger(SOSNullFilterBase.class);

    public SOSNullFilterBase(final T pobjT) {
        objF = (SOSNullFilter) pobjT;
    }

    @Before
    public void setUp() throws Exception {
        LOGGER.setLevel(Level.DEBUG);
    }

}
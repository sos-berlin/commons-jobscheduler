package com.sos.JSHelper.Options;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.io.Files.JSFile;

public class SOSOptionFileStringTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSOptionFileStringTest.class);
    private SOSOptionFileString objCS = null;

    @Before
    public void setUp() throws Exception {
        objCS = new SOSOptionFileString(null, "test", "Description", null, null, false);
    }

    

}
package com.sos.JSHelper.Options;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author KB */
public class SOSOptionHexStringTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSOptionHexStringTest.class);

    @Test
    public void testValue() {
        SOSOptionHexString objHS = new SOSOptionHexString(null, "key", "desc", "value", "", false);
        objHS.setValue("das&#x0d; ist&#x0a; das&#x0d; Haus&#x0a; vom&#x0d; Nikolaus");
        LOGGER.info(objHS.getValue());
        assertEquals("unescapeXMLEntities", "das\r ist\n das\r Haus\n vom\r Nikolaus", objHS.getValue());
    }

}
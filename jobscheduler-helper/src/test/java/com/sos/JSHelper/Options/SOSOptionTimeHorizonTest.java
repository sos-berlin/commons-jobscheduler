package com.sos.JSHelper.Options;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.Test;

public class SOSOptionTimeHorizonTest {

    private static final Logger LOGGER = Logger.getLogger(SOSOptionTimeHorizonTest.class);

    public SOSOptionTimeHorizon timeHorizon = new SOSOptionTimeHorizon(null, "SOSOptionTimeHorizonTest.variablename", "OptionDescription", "1:00:00:00", 
            "1:00:00:00", true);

    @Test
    public final void testValueString() {
        String expected = "-60";
        timeHorizon.setValue(expected);
        assertEquals(expected, timeHorizon.getValue());
        LOGGER.info(timeHorizon.getValue());
        LOGGER.info(timeHorizon.getEndFromNow().toString());
    }

    @Test
    public void testIsDirty() {
        timeHorizon.setValue("-30");
        LOGGER.info(timeHorizon.isDirty());
    }

}
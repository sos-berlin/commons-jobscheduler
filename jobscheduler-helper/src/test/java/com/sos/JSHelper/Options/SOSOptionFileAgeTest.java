package com.sos.JSHelper.Options;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/** @author KB */
public class SOSOptionFileAgeTest {

    private SOSOptionFileAge objOption = null;

    @Before
    public void setUp() throws Exception {
        objOption = new SOSOptionFileAge(null, "file_age", "file_age", "0", "0", false);
    }

    @Test
    public final void testGetAgeAsMS() {
        objOption.setValue("01:30:30");
        assertEquals("...", (3600 + 1800 + 30) * 1000, objOption.getAgeAsMS());
    }

    @Test
    public final void testGetAgeAsMSDays() {
        objOption.setValue("10 days");
        assertEquals("...", (3600 * 24 * 10) * 1000, objOption.getAgeAsMS());
    }

    @Test
    public final void testGetAgeAsMSWeeks() {
        objOption.setValue("10 weeks");
        assertEquals("...", (long) (3600 * 24 * 7 * 10) * 1000, objOption.getAgeAsMS());
    }

    @Test
    public final void testGetAgeAsMSHours() {
        objOption.setValue("10 Hours");
        assertEquals("...", (3600 * 10) * 1000, objOption.getAgeAsMS());
    }

    @Test
    public final void testGetAgeAsMSMinutes() {
        objOption.setValue("10 minutes");
        assertEquals("...", (60 * 10) * 1000, objOption.getAgeAsMS());
    }

    @Test
    public final void testGetAgeAsMSSeconds() {
        objOption.setValue("10 seconds");
        assertEquals("...", (10) * 1000, objOption.getAgeAsMS());
    }

}
package com.sos.JSHelper.Options;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/** @author KB */
public class SOSOptionBooleanTest {

    SOSOptionBoolean objOption = null;

    @Before
    public void setUp() throws Exception {
        objOption = new SOSOptionBoolean(null, ".TestOption", "Title", "true", "true", true);
        objOption.value(true);
        objOption.Value("true");
    }

    @Test
    public void testValueString() {
        assertEquals("Value must be true", "true", objOption.Value());
        objOption.Value("false");
        assertEquals("Value must be false", "false", objOption.Value());
    }

    @Test
    public void testValue() {
        objOption.Value("true");
        assertTrue("Value must be true", objOption.value());
        objOption.Value("1");
        assertTrue("Value must be true", objOption.value());
        objOption.Value("false");
        assertFalse("Value must be false", objOption.value());
    }

    @Test
    public void testString2Bool() {
        assertTrue("Must be true", objOption.string2Bool("on"));
        assertTrue("Must be true", objOption.string2Bool("true"));
        assertTrue("Must be true", objOption.string2Bool("1"));
        assertTrue("Must be true", objOption.string2Bool("yes"));
        assertFalse("Must be false", objOption.string2Bool("no"));
        assertFalse("Must be false", objOption.string2Bool("false"));
        assertFalse("Must be false", objOption.string2Bool("0"));
        assertFalse("Must be false", objOption.string2Bool("off"));
    }

    @Test
    public void testIsTrue() {
        assertTrue("Value must be true", objOption.isTrue());
        assertTrue("Value must be true", objOption.value());
    }

    @Test
    public void testIsFalse() {
        assertFalse("Value must be false", objOption.isFalse());
        objOption.value(false);
        assertTrue("Value must be false", !objOption.value());
    }

}
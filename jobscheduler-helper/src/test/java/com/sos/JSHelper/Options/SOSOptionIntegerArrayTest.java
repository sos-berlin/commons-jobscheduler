package com.sos.JSHelper.Options;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/** @author KB */
public class SOSOptionIntegerArrayTest {

    private SOSOptionIntegerArray objOption = null;

    @Before
    public void setUp() throws Exception {
        objOption = new SOSOptionIntegerArray(null, "key", "Description", "1 2 3", "", false);
    }

    @Test
    public void testToString() {
        assertEquals("must be '1 2 3'", "1 2 3", objOption.getValue());
    }

    @Test
    public void testValueString() {
        String strTestString = "1,2,3,4,5";
        objOption.setValue(strTestString);
        assertEquals("Must be '" + strTestString + "'", strTestString, objOption.getValue());
        assertEquals("Size must be 5", 5, objOption.getValues().size());
        int iElem = objOption.getValues().elementAt(3 - 1);
        assertEquals("Value must be 3", 3, iElem);
        strTestString = "1 2 3 4 5";
        objOption.setValue(strTestString);
        assertEquals("Must be '" + strTestString + "'", strTestString, objOption.getValue());
        assertEquals("Size must be 5", 5, objOption.getValues().size());
        iElem = objOption.getValues().elementAt(3 - 1);
        assertEquals("Value must be 3", 3, iElem);
        strTestString = "1; 2; 3; 4; 5";
        objOption.setValue(strTestString);
        assertEquals("Must be '" + strTestString + "'", strTestString, objOption.getValue());
        assertEquals("Size must be 5", 5, objOption.getValues().size());
        iElem = objOption.getValues().elementAt(3 - 1);
        assertEquals("Value must be 3", 3, iElem);
    }

    @Test(expected = JobSchedulerException.class)
    public void testValues() {
        String strTestString = "a b c true";
        objOption.setValue(strTestString);
        fail("Expected JobSchedulerException because args are not integer");
    }

    @Test(expected = JobSchedulerException.class)
    public void testValues2() {
        String strTestString = "a b c true";
        String strMessage = "Expected JobSchedulerException because args are not integer";
        objOption.setValue(strTestString);
        fail(strMessage);
    }

    @Test
    public void testContains() {
        assertTrue("Value of 2 ist part of the value-list", objOption.contains(2));
        assertFalse("Value of 7 ist *not* part of the value-list", objOption.contains(7));
        String strTestString = "1; 2; 3; 4; 5-10";
        objOption.setValue(strTestString);
        assertTrue("Value of 7 ist *now* part of the value-list", objOption.contains(7));
    }

}
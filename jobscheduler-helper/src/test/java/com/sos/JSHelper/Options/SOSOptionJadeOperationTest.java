/**
 * 
 */
package com.sos.JSHelper.Options;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Options.SOSOptionJadeOperation.enuJadeOperations;

/** @author KB */
public class SOSOptionJadeOperationTest {

    private SOSOptionJadeOperation objOperation = null;

    /** @throws java.lang.Exception */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /** @throws java.lang.Exception */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /** @throws java.lang.Exception */
    @Before
    public void setUp() throws Exception {
        objOperation = new SOSOptionJadeOperation(null, "operation", "operation", enuJadeOperations.undefined.getText(), enuJadeOperations.copy.getText(), true);
    }

    /** @throws java.lang.Exception */
    @After
    public void tearDown() throws Exception {
    }

    /** Test method for
     * {@link com.sos.JSHelper.Options.SOSOptionJadeOperation#setValue(java.lang.String)}
     * . */
    @Test
    public void testValueString() {
        objOperation.setValue("send");
        assertEquals("operation send", "send", objOperation.getValue());
    }

    @Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
    public void testWrongValueString() {
        objOperation.setValue("xxxxx");
        assertEquals("operation send", "xxxxx", objOperation.getValue());
    }

    /** Test method for
     * {@link com.sos.JSHelper.Options.SOSOptionJadeOperation#setValue(com.sos.JSHelper.Options.SOSOptionJadeOperation.enuJadeOperations)}
     * . */
    @Test
    public void testValueEnuJadeOperations() {
        objOperation.setValue(enuJadeOperations.copy);
        assertEquals("operation send", enuJadeOperations.copy, objOperation.value());
    }

    /** Test method for
     * {@link com.sos.JSHelper.Options.SOSOptionJadeOperation#value()}. */
    @Test
    public void testValue() {
        objOperation.setValue(enuJadeOperations.copy);
        assertEquals("test Value", enuJadeOperations.copy.getText(), objOperation.getValue());
    }

    /** Test method for
     * {@link com.sos.JSHelper.Options.SOSOptionJadeOperation#isOperationSend()}
     * . */
    @Test
    public void testIsOperationSend() {
        objOperation.setValue("send");
        assertTrue("operation send", objOperation.isOperationSend());
        objOperation.setValue(enuJadeOperations.copy);
        assertFalse("operation send", objOperation.isOperationSend());
    }

    /** Test method for
     * {@link com.sos.JSHelper.Options.SOSOptionJadeOperation#isOperationReceive()}
     * . */
    @Test
    public void testIsOperationReceive() {
        objOperation.setValue("receive");
        assertTrue("operation send", objOperation.isOperationReceive());
        objOperation.setValue(enuJadeOperations.copy);
        assertFalse("operation send", objOperation.isOperationReceive());
    }

    @Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
    public void testCheckMandatory() {
        String strValue = null;
        objOperation.setValue(strValue);
        objOperation.checkMandatory();
    }

    @Test
    public void testCheckMandatory2() {
        String strValue = "send";
        objOperation.setValue(strValue);
        objOperation.checkMandatory();
    }

    @Test
    public void testGetValues() {
        objOperation.setValue(enuJadeOperations.copy);

        String[] strL = objOperation.getValueList();
        for (String string : strL) {
            System.out.println("----" + string);
        }
        System.out.println(strL.toString());
    }
}

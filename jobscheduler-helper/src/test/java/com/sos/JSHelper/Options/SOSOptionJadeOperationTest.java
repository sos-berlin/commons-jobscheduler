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
        objOperation = new SOSOptionJadeOperation(null, "operation", "operation", enuJadeOperations.undefined.Text(), enuJadeOperations.copy.Text(), true);
    }

    /** @throws java.lang.Exception */
    @After
    public void tearDown() throws Exception {
    }

    /** Test method for
     * {@link com.sos.JSHelper.Options.SOSOptionJadeOperation#Value(java.lang.String)}
     * . */
    @Test
    public void testValueString() {
        objOperation.Value("send");
        assertEquals("operation send", "send", objOperation.Value());
    }

    @Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
    public void testWrongValueString() {
        objOperation.Value("xxxxx");
        assertEquals("operation send", "xxxxx", objOperation.Value());
    }

    /** Test method for
     * {@link com.sos.JSHelper.Options.SOSOptionJadeOperation#Value(com.sos.JSHelper.Options.SOSOptionJadeOperation.enuJadeOperations)}
     * . */
    @Test
    public void testValueEnuJadeOperations() {
        objOperation.Value(enuJadeOperations.copy);
        assertEquals("operation send", enuJadeOperations.copy, objOperation.value());
    }

    /** Test method for
     * {@link com.sos.JSHelper.Options.SOSOptionJadeOperation#value()}. */
    @Test
    public void testValue() {
        objOperation.Value(enuJadeOperations.copy);
        assertEquals("test Value", enuJadeOperations.copy.Text(), objOperation.Value());
    }

    /** Test method for
     * {@link com.sos.JSHelper.Options.SOSOptionJadeOperation#isOperationSend()}
     * . */
    @Test
    public void testIsOperationSend() {
        objOperation.Value("send");
        assertTrue("operation send", objOperation.isOperationSend());
        objOperation.Value(enuJadeOperations.copy);
        assertFalse("operation send", objOperation.isOperationSend());
    }

    /** Test method for
     * {@link com.sos.JSHelper.Options.SOSOptionJadeOperation#isOperationReceive()}
     * . */
    @Test
    public void testIsOperationReceive() {
        objOperation.Value("receive");
        assertTrue("operation send", objOperation.isOperationReceive());
        objOperation.Value(enuJadeOperations.copy);
        assertFalse("operation send", objOperation.isOperationReceive());
    }

    @Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
    public void testCheckMandatory() {
        String strValue = null;
        objOperation.Value(strValue);
        objOperation.CheckMandatory();
    }

    @Test
    public void testCheckMandatory2() {
        String strValue = "send";
        objOperation.Value(strValue);
        objOperation.CheckMandatory();
    }

    @Test
    public void testGetValues() {
        objOperation.Value(enuJadeOperations.copy);

        String[] strL = objOperation.getValueList();
        for (String string : strL) {
            System.out.println("----" + string);
        }
        System.out.println(strL.toString());
    }
}

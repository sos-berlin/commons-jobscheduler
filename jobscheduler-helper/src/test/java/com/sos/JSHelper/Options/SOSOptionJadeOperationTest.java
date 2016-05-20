/**
 * 
 */
package com.sos.JSHelper.Options;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Options.SOSOptionJadeOperation.enuJadeOperations;

/** @author KB */
public class SOSOptionJadeOperationTest {
    
    private static final Logger LOGGER = Logger.getLogger(SOSOptionJadeOperationTest.class); 
    private SOSOptionJadeOperation objOperation = null;

    public void setUp() throws Exception {
        objOperation = new SOSOptionJadeOperation(null, "operation", "operation", enuJadeOperations.undefined.getText(), enuJadeOperations.copy.getText(), true);
    }


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

    @Test
    public void testValueEnuJadeOperations() {
        objOperation.setValue(enuJadeOperations.copy);
        assertEquals("operation send", enuJadeOperations.copy, objOperation.value());
    }

    @Test
    public void testValue() {
        objOperation.setValue(enuJadeOperations.copy);
        assertEquals("test Value", enuJadeOperations.copy.getText(), objOperation.getValue());
    }

    @Test
    public void testIsOperationSend() {
        objOperation.setValue("send");
        assertTrue("operation send", objOperation.isOperationSend());
        objOperation.setValue(enuJadeOperations.copy);
        assertFalse("operation send", objOperation.isOperationSend());
    }

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
            LOGGER.info("----" + string);
        }
        LOGGER.info(strL.toString());
    }

}
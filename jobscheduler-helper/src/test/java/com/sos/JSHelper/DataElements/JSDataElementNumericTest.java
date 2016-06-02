package com.sos.JSHelper.DataElements;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class JSDataElementNumericTest {

    @Test
    public void testSubtract() throws Exception {
        final JSDataElementNumeric objEins = new JSDataElementNumeric("1");
        final JSDataElementNumeric objDeliveryNumberIDoc = new JSDataElementNumeric("10");
        objDeliveryNumberIDoc.subtract(objEins).getValue();
        assertEquals("Eins subtrahieren => 10-1=9", "9", objDeliveryNumberIDoc.getValue());
    }

}

package com.sos.JSHelper.DataElements;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class JSDataElementTimeStampISOTest {

    public JSDataElementTimeStampISOTest() {
        //
    }

    public void testISODate() throws Exception {
        String strT;
        JSDataElementTimeStampISO ValidFromDate = new JSDataElementTimeStampISO(" ", "Date as of which the price is valid", 8, 20, " ", "ValidFromDate", 
                "ValidFromDate");
        ValidFromDate.Value("2009-08-31T15:36:53");
        strT = ValidFromDate.toXml();
        assertEquals("Date as XML is", "<ValidFromDate>2009-08-31T15:36:53</ValidFromDate>", strT);
        ValidFromDate = new JSDataElementTimeStampISO(" ", "Date as of which the price is valid", 8, 20, " ", "ValidFromDate", "ValidFromDate");
        ValidFromDate.FormatString(JSDateFormat.dfDATE_SHORT);
        ValidFromDate.Value("2009-08-31T15:36:53");
        strT = ValidFromDate.toXml();
        assertEquals("Date as XML is", "<ValidFromDate>2009-08-31</ValidFromDate>", strT);
    }

    @Test
    public void testValidFromDate() throws Exception {
        final JSDataElementValidFromDate ValidFromDate = new JSDataElementValidFromDate(" ", "Date as of which the price is valid", 8, 20, " ", 
                "ValidFromDate", "ValidFromDate");
        ValidFromDate.FormatString(JSDateFormat.dfDATE_SHORT);
        ValidFromDate.Value("2009-08-31T15:36:53");
        assertEquals("Date as XML is", "<ValidFromDate>2009-08-31</ValidFromDate>", ValidFromDate.toXml());
    }

}
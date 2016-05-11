package com.sos.JSHelper.DataElements;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

public class JSDataElementDateTimeTest {

    @Test
    public void testISO() throws Exception {
        String strT;
        JSDataElementDateTime objDateTime = new JSDataElementDateTime("", "ValidFrom Date");
        Date objD = new Date(2011 - 1900, 0, 25, 15, 15);
        objDateTime.FormatString(JSDateFormat.dfDATE_SHORT);
        objDateTime.Value(objD);
        objDateTime.XMLTagName("ValidFromDate");
        strT = objDateTime.toXml();
        assertEquals("Date as XML is", "<ValidFromDate>2011-01-25</ValidFromDate>", strT);
    }

    @Test
    public void testConstructorDate() {
        Date objD = new Date(2011 - 1900, 0, 25, 15, 15);
        System.out.println("objD =" + objD.toLocaleString());
        JSDataElementDateTime objDT = new JSDataElementDateTime(objD);
        assertEquals("Date", "2011-01-25 15:15:00", objDT.FormattedValue());
    }

}
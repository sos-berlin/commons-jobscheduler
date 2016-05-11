package com.sos.JSHelper.DataElements;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class JSDataElementDateTest {

    public JSDataElementDateTest() {
        //
    }

    @Test
    public void testDateTime() throws Exception {
        JSDataElementDate objD = new JSDataElementDate("20111220173250", "yyyyMMddHHmmss");
        System.out.println(objD.getDateTimeFormatted("yyyyMMddHHmmss"));
        System.out.println(objD.getDateObject().getTime());
    }

    @Test
    public void testISO() throws Exception {
        JSDataElementDate objDate = new JSDataElementDate(" ", "Date as of which the price is valid", 8, 20, " ", "ValidFromDate", "ValidFromDate");
        objDate.FormatString(JSDateFormat.dfDATE_SHORT);
        objDate.Value("20091101");
        assertEquals("Date as XML is", "<ValidFromDate>2009-11-01</ValidFromDate>", objDate.toXml());
    }

    @Test
    public void getLastFridayTest() {
        JSDataElementDate objDate = new JSDataElementDate(" ", "Date as of which the price is valid", 8, 20, " ", "ValidFromDate", "ValidFromDate");
        for (int i = 1; i <= 12; i++) {
            int ilastfriday = objDate.getLastFridayInAMonth(i - 1, 2011);
            System.out.println(String.format("Month %1$d: last Friday = %2$d", i, ilastfriday));
        }
    }

}
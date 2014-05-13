package com.sos.hibernate.classes;

import org.joda.time.DateTime;
import org.junit.Test;

import java.text.SimpleDateFormat;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class UtcTimeHelperTest {

    private static UtcTimeHelper h = new UtcTimeHelper();

    @Test
    public void testIsToday() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        DateTime d1 = new DateTime().minusSeconds(333);
        DateTime d2 = new DateTime().minusDays(333);
        assertTrue(h.isToday(d1));
        assertFalse(h.isToday(d2));
    }

}

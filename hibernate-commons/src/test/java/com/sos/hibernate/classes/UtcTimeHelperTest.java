package com.sos.hibernate.classes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.junit.Test;

public class UtcTimeHelperTest {

    private static UtcTimeHelper h = new UtcTimeHelper();

    @Test
    public void testIsToday() {
        DateTime d1 = new DateTime().minusSeconds(333);
        DateTime d2 = new DateTime().minusDays(333);
        assertTrue(h.isToday(d1));
        assertFalse(h.isToday(d2));
    }

}
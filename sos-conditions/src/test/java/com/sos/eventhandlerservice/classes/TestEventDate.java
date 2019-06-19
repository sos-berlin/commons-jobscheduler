package com.sos.eventhandlerservice.classes;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestEventDate {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetEventDate() {
        EventDate eventDate = new EventDate();
        String s_all = eventDate.getEventDate("*");
        String s_today = eventDate.getEventDate("today");
        String s_today_1 = eventDate.getEventDate("today-1");
        String s_today_2 = eventDate.getEventDate("today-2");
        String s_yesterday = eventDate.getEventDate("yesterday");
        String s_yesterday_1 = eventDate.getEventDate("yesterday-1");
        String s_yesterday_p1 = eventDate.getEventDate("yesterday+1");
        String s_date = eventDate.getEventDate("2019.169");
        
        assertEquals("testGetEventDate1", s_today, s_yesterday_p1);
        assertEquals("testGetEventDate2", s_today_2, s_yesterday_1);
        assertEquals("testGetEventDate3", s_today_1, s_yesterday);
        assertEquals("testGetEventDate6", s_all, "*");
        assertEquals("testGetEventDate6", s_date, "2019.169");

        

    }

}

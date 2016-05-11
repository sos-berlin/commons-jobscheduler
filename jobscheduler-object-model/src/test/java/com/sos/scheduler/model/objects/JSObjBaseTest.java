package com.sos.scheduler.model.objects;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class JSObjBaseTest {

    @Test
    public final void testConvertYesNo() {
        JSObjBase objB = new JSObjBase();
        assertEquals(true, objB.getYesOrNo("1"));
        assertEquals(true, objB.getYesOrNo("yes"));
        assertEquals(true, objB.getYesOrNo("YES"));
        assertEquals(true, objB.getYesOrNo("Yes"));
        assertEquals(true, objB.getYesOrNo("True"));
        assertEquals(true, objB.getYesOrNo("Ja"));
        assertEquals(true, objB.getYesOrNo("ja"));
    }

}
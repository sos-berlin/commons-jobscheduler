package com.sos.JSHelper.io.Files;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;

public class JSToolBoxTest {

    @Test
    public void TestConstructor() {
        JSToolBox objT = new JSToolBox();
        assertEquals("Constructor-Test", true, objT != null);
    }

}
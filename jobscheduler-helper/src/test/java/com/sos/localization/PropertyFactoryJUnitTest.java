package com.sos.localization;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

public class PropertyFactoryJUnitTest extends JSToolBox {

    protected PropertyFactoryOptions objOptions = null;
    private PropertyFactory objE = null;

    @Before
    public void setUp() throws Exception {
        objE = new PropertyFactory();
        objE.registerMessageListener(this);
        objOptions = objE.getOptions();
        objOptions.registerMessageListener(this);
        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testExecute() throws Exception {
        objE.execute();
    }

}
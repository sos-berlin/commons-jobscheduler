package com.sos.scheduler.converter.graphviz;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

public class JSObjects2GraphvizOptionsJUnitTest extends JSToolBox {

    protected JSObjects2GraphvizOptions objOptions = null;
    private JSObjects2Graphviz objE = null;

    @Before
    public void setUp() throws Exception {
        objE = new JSObjects2Graphviz();
        objE.registerMessageListener(this);
        objOptions = objE.getOptions();
        objOptions.registerMessageListener(this);
        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;
    }

    @Test
    public void testlive_folder_name() {
        objOptions.live_folder_name.Value("++----++");
        assertEquals("", "++----++" + "/", objOptions.live_folder_name.Value());
    }

    @Test
    public void testoutput_folder_name() {
        objOptions.output_folder_name.Value("++----++");
        assertEquals("", "++----++" + "/", objOptions.output_folder_name.Value());
    }

}
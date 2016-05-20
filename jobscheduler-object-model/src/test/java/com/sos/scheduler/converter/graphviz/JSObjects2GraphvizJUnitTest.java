package com.sos.scheduler.converter.graphviz;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

public class JSObjects2GraphvizJUnitTest extends JSToolBox {

    protected JSObjects2GraphvizOptions objOptions = null;
    private static final String LIVE_FOLDER_LOCATION = "/8of9_buildjars_4210/config/live/";
    private static final String LIVE_LOCAL_FOLDER_LOCATION = "Z:" + LIVE_FOLDER_LOCATION;
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
    @Ignore("Test set to Ignore for later examination")
    public void testExecute() throws Exception {
        objOptions.outputFolderName.setValue("c:/temp");
        objOptions.liveFolderName.setValue(LIVE_LOCAL_FOLDER_LOCATION);
        objE.execute();
    }

}
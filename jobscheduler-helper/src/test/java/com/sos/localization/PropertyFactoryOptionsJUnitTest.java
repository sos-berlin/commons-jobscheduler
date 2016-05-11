package com.sos.localization;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;

public class PropertyFactoryOptionsJUnitTest extends JSToolBox {

    protected PropertyFactoryOptions objOptions = null;
    private PropertyFactory objE = null;

    public PropertyFactoryOptionsJUnitTest() {
        //
    }

    @Before
    public void setUp() throws Exception {
        objE = new PropertyFactory();
        objOptions = objE.getOptions();
    }

    @Test
    public void testOperation() {
        objOptions.Operation.Value("++merge++");
        assertEquals("", objOptions.Operation.Value(), "++merge++");
    }

    @Test
    public void testPropertyFileNamePrefix() {
        objOptions.PropertyFileNamePrefix.Value("++----++");
        assertEquals("", objOptions.PropertyFileNamePrefix.Value(), "++----++");
    }

    @Test
    public void testSourceFolderName() {
        objOptions.SourceFolderName.Value("++----++");
        assertEquals("The Folder, which has all the I18N Property files.", "++----++" + "/", objOptions.SourceFolderName.Value());
    }

}
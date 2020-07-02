package com.sos.localization;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
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
        objOptions.Operation.setValue("++merge++");
        assertEquals("", objOptions.Operation.getValue(), "++merge++");
    }

    @Test
    public void testPropertyFileNamePrefix() {
        objOptions.propertyFileNamePrefix.setValue("++----++");
        assertEquals("", objOptions.propertyFileNamePrefix.getValue(), "++----++");
    }

    @Test
    public void testSourceFolderName() {
        objOptions.sourceFolderName.setValue("++----++");
        assertEquals("The Folder, which has all the I18N Property files.", "++----++" + "/", objOptions.sourceFolderName.getValue());
    }

}
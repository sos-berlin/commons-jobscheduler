package com.sos.JSHelper.Options;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

/** @author KB */
public class SOSOptionElementTest {

    private static final String CLASSNAME = "SOSOptionElementTest";
    private static final Logger LOGGER = Logger.getLogger(SOSOptionElementTest.class);
    private SOSOptionElement objOption = null;

    public SOSOptionElementTest() {
        //
    }

    @Before
    public void setUp() throws Exception {
        objOption = new SOSOptionElement(null, "key", "Description", "value", "DefaultValue", true);
    }

    @Test
    public void testSetPrefix() {
        String strPrefix = "alternate_";
        String strKey = "key";
        String strNewKey = objOption.setPrefix(strPrefix);
        assertEquals("Key has wrong prefix", strPrefix + strKey, strNewKey);
        objOption.strKey = CLASSNAME + ".key";
        strNewKey = objOption.setPrefix(strPrefix);
        assertEquals("Key has wrong prefix", CLASSNAME + "." + strPrefix + strKey, strNewKey);
    }

    @Test
    public void testHide() {
        assertEquals("toString", "key (Description): value", objOption.toString());
        objOption.setHideValue(true);
        assertEquals("toString", "key (Description): *****", objOption.toString());
        objOption.setHideOption(true);
        assertEquals("toString", "", objOption.toString());
    }

    @Test
    public void testToString() {
        assertEquals("Expected Value not got", "key (Description): value", objOption.toString());
    }

    @Test
    public void testSOSOptionElement() {
        SOSOptionElement objOpt = new SOSOptionElement(null, "key", "Description", "value", "DefaultValue", true);
        assertEquals("Key failed", "key", objOpt.getKey());
        assertEquals("Description failed", "Description", objOpt.getDescription());
        assertEquals("Value failed", "value", objOpt.getValue());
        assertEquals("DefaultValue failed", "DefaultValue", objOpt.getDefaultValue());
        assertTrue("Is Mandatory failed", objOpt.isMandatory());
    }

    @Test
    public void testColumnHeaderString() {
        assertEquals("ColumnHeader failed", "key", objOption.getColumnHeader());
        objOption.columnHeader("Column");
        assertEquals("ColumnHeader failed", "Column", objOption.getColumnHeader());
    }

    @Test
    public void testSetAlias() {
        objOption.setAlias("newKey");
        objOption.setAlias("AliasKey");
    }

    @Test
    public void testSetDirty() {
        assertFalse("Must be not Dirty", objOption.isDirty());
        objOption.setValue("Dirty");
        assertTrue("Must be Dirty", objOption.isDirty());
    }

    @Test
    public void testEnvVarAsValue() {
        SOSOptionElement objOpt = new SOSOptionElement(null, "key", "Description", "env:APPDATA", "env:LOCALAPPDATA", true);
        LOGGER.info("value = " + objOpt.getValue());
        LOGGER.info("default value = " + objOpt.getDefaultValue());
    }

    @Test
    public void testEnvVarAsValue2() {
        SOSOptionElement objOpt = new SOSOptionElement(null, "key", "Description", "file:${APPDATA}", "env:LOCALAPPDATA", true);
        LOGGER.info("value = " + objOpt.getValue());
        LOGGER.info("default value = " + objOpt.getDefaultValue());
        objOpt = new SOSOptionElement(null, "key", "Description", "file:${APPDATA}/config/live", "env:LOCALAPPDATA", true);
        LOGGER.info("value = " + objOpt.getValue());
        LOGGER.info("default value = " + objOpt.getDefaultValue());
        objOpt = new SOSOptionElement(null, "key", "Description", "${APPDATA}/config/live", "env:LOCALAPPDATA", true);
        LOGGER.info("value = " + objOpt.getValue());
        LOGGER.info("default value = " + objOpt.getDefaultValue());
        objOpt = new SOSOptionElement(null, "key", "Description", "${APPDATA}", "env:LOCALAPPDATA", true);
        LOGGER.info("value = " + objOpt.getValue());
        LOGGER.info("default value = " + objOpt.getDefaultValue());
    }

    @Test
    public void testSOSLocale() {
        SOSOptionLocale Locale = new SOSOptionLocale(null, CLASSNAME + ".Locale", "I18N is for internationalization of Application", "env:SOS_LOCALE", 
                java.util.Locale.getDefault().toString(), true);
        LOGGER.info("Locale = " + Locale.getValue());
    }

    public void testSystemProperty() {
        System.setProperty("log4j.configuration", "test-config.properties");
        JSOptionsClass objO = new JSOptionsClass();
        assertEquals("propertyfile name", "test-config.properties", objO.log4jPropertyFileName.getValue());
    }

}
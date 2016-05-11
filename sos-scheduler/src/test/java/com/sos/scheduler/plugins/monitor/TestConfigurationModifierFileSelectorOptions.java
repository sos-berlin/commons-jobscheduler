package com.sos.scheduler.plugins.monitor;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

import com.sos.scheduler.plugins.globalmonitor.ConfigurationModifierFileSelectorOptions;

public class TestConfigurationModifierFileSelectorOptions {

    @Test
    public void testGetConfigurationDirectory() {
        ConfigurationModifierFileSelectorOptions c = new ConfigurationModifierFileSelectorOptions();
        c.setConfigurationDirectory("test");
        c.getConfigurationDirectory();
        assertEquals("testGetConfigurationDirectory", "test", c.getConfigurationDirectory());
    }

    @Test
    public void testSetConfigurationDirectory() {
        ConfigurationModifierFileSelectorOptions c = new ConfigurationModifierFileSelectorOptions();
        c.setConfigurationDirectory("test");
        assertEquals("testSetConfigurationDirectory", "test", c.getConfigurationDirectory());
    }

    @Test
    public void testGetRegexSelector() {
        ConfigurationModifierFileSelectorOptions c = new ConfigurationModifierFileSelectorOptions();
        c.setRegexSelector("test");
        assertEquals("testGetRegexSelector", "test", c.getRegexSelector());
    }

    @Test
    public void testSetRegexSelector() {
        ConfigurationModifierFileSelectorOptions c = new ConfigurationModifierFileSelectorOptions();
        c.setRegexSelector("test");
        assertEquals("testSetRegexSelector", "test", c.getRegexSelector());
    }

    @Test
    public void testGetfileExclusions() {
        ConfigurationModifierFileSelectorOptions c = new ConfigurationModifierFileSelectorOptions();
        c.setFileExclusions("test");
        assertEquals("testGetfileExclusions", "test", c.getfileExclusions());
    }

    @Test
    public void testSetFileExclusions() {
        ConfigurationModifierFileSelectorOptions c = new ConfigurationModifierFileSelectorOptions();
        c.setFileExclusions("test");
        assertEquals("testSetFileExclusions", "test", c.getfileExclusions());
    }

    @Test
    public void testIsRecursiv() {
        ConfigurationModifierFileSelectorOptions c = new ConfigurationModifierFileSelectorOptions();
        c.setRecursive(true);
        assertEquals("testIsRecursiv", true, c.isRecursive());
    }

    @Test
    public void testSetRecursivBoolean() {
        ConfigurationModifierFileSelectorOptions c = new ConfigurationModifierFileSelectorOptions();
        c.setRecursive(true);
        assertEquals("testSetRecursivBoolean", true, c.isRecursive());
    }

    @Test
    public void testSetRecursivString() {
        ConfigurationModifierFileSelectorOptions c = new ConfigurationModifierFileSelectorOptions();
        c.setRecursive("true");
        assertEquals("testSetRecursivString", true, c.isRecursive());
    }

    @Test
    public void testSetDirectoryExclusions() {
        ConfigurationModifierFileSelectorOptions c = new ConfigurationModifierFileSelectorOptions();
        c.setDirectoryExclusions("/test/1,test/2");
        assertEquals("testSetDirectoryExclusions", true, c.isDirExclusion(new File("c:/test/2")));
    }

    @Test
    public void testIsFileExclusions() {
        ConfigurationModifierFileSelectorOptions c = new ConfigurationModifierFileSelectorOptions();
        c.setConfigurationDirectory("c:/temp");
        c.setFileExclusions("/job1");
        assertEquals("testIsFileExclusions", true, c.isFileExclusions(new File("c:/temp/live/job1.job.xml")));
    }

    @Test
    public void testIsDirExclusion() {
        ConfigurationModifierFileSelectorOptions c = new ConfigurationModifierFileSelectorOptions();
        c.setDirectoryExclusions("/test/1,test/2");
        assertEquals("testIsDirExclusion", true, c.isDirExclusion(new File("c:/test/1")));
    }

}
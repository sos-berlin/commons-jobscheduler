package com.sos.scheduler.plugins.globalmonitor;

import java.io.File;
import java.io.FilenameFilter;

public abstract class ConfigurationModifierFileFilter implements FilenameFilter {

    private ConfigurationModifierFileSelectorOptions options;

    abstract public String getXmlRegex();

    public ConfigurationModifierFileFilter(ConfigurationModifierFileSelectorOptions options) {
        super();
        this.options = options;
    }

    private boolean isConfiguationFile(String filename) {
        return filename.matches(getXmlRegex());
    }

    private boolean matchRegex(String filename) {
        return (options.getRegexSelector().trim().isEmpty() || filename.matches(options.getRegexSelector()));
    }

    @Override
    public boolean accept(File directory, String filename) {
        File f = new File(directory, filename);
        if (f.isDirectory()) {
            return !options.isDirExclusion(directory);
        }
        if (options.isFileExclusions(f)) {
            return false;
        } else {
            return isConfiguationFile(filename) && matchRegex(filename);
        }
    }

}
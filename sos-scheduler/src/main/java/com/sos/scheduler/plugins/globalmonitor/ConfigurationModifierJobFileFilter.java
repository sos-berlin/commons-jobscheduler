package com.sos.scheduler.plugins.globalmonitor;

public class ConfigurationModifierJobFileFilter extends ConfigurationModifierFileFilter {

    private static final String JOBXML_REGEX = "^.*\\.job\\.xml$";

    public ConfigurationModifierJobFileFilter(ConfigurationModifierFileSelectorOptions options) {
        super(options);
    }

    @Override
    public String getXmlRegex() {
        return JOBXML_REGEX;
    }

}

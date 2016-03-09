package com.sos.scheduler.plugins.globalmonitor;

public class ConfigurationModifierMonitorFileFilter extends ConfigurationModifierFileFilter {

    private static final String MONITORXML_REGEX = "^.*\\.monitor\\.xml$";

    public ConfigurationModifierMonitorFileFilter(ConfigurationModifierFileSelectorOptions options) {
        super(options);
    }

    @Override
    public String getXmlRegex() {
        return MONITORXML_REGEX;
    }

}

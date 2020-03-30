package com.sos.vfs.common.options;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.vfs.common.options.SOSBaseOptions;
import com.sos.vfs.common.options.SOSProviderOptions;

public class SOSTransfer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSOptionsClass.class);

    private SOSProviderOptions sourceOptions = null;
    private SOSProviderOptions targetOptions = null;

    public SOSTransfer(final HashMap<String, String> settings) throws Exception {
        LOGGER.trace("[provider options]source");
        sourceOptions = new SOSProviderOptions(false, true);
        setProviderOptions(settings, sourceOptions, SOSBaseOptions.SETTINGS_KEY_ALTERNATIVE_SOURCE_INCLUDE);

        LOGGER.trace("[provider options]target");
        targetOptions = new SOSProviderOptions(false, false);
        setProviderOptions(settings, targetOptions, SOSBaseOptions.SETTINGS_KEY_ALTERNATIVE_TARGET_INCLUDE);
    }

    private void setProviderOptions(HashMap<String, String> settings, SOSProviderOptions options, String settingsKey) throws Exception {
        options.setAllOptions(settings);
        if (settings.containsKey(settingsKey)) {
            options.alternateOptionsUsed.value(true);
        }
        options.setChildClasses(settings);
    }

    public SOSProviderOptions getSource() {
        return sourceOptions;
    }

    public void setSource(final SOSProviderOptions val) {
        sourceOptions = val;
        if (sourceOptions != null) {
            sourceOptions.setIsSource(true);
        }
    }

    public SOSProviderOptions getTarget() {
        return targetOptions;
    }

    public void setTarget(final SOSProviderOptions val) {
        targetOptions = val;
        if (targetOptions != null) {
            targetOptions.setIsSource(false);
        }
    }

}
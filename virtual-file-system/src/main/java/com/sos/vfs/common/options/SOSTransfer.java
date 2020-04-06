package com.sos.vfs.common.options;

import java.util.HashMap;

public class SOSTransfer {

    private SOSProviderOptions source = null;
    private SOSProviderOptions target = null;

    public SOSTransfer(final HashMap<String, String> settings) throws Exception {
        source = new SOSProviderOptions(false, true);
        setProviderOptions(settings, source, SOSBaseOptions.SETTINGS_KEY_ALTERNATIVE_SOURCE_INCLUDE);

        target = new SOSProviderOptions(false, false);
        setProviderOptions(settings, target, SOSBaseOptions.SETTINGS_KEY_ALTERNATIVE_TARGET_INCLUDE);
    }

    private void setProviderOptions(HashMap<String, String> settings, SOSProviderOptions options, String alternativeSettingsKey) throws Exception {
        options.setAllOptions(settings);
        if (settings.containsKey(alternativeSettingsKey)) {
            options.alternateOptionsUsed.value(true);
        }
        options.setChildClasses(settings);
    }

    public SOSProviderOptions getSource() {
        return source;
    }

    public void setSource(final SOSProviderOptions val) {
        source = val;
        if (source != null) {
            source.setIsSource(true);
        }
    }

    public SOSProviderOptions getTarget() {
        return target;
    }

    public void setTarget(final SOSProviderOptions val) {
        target = val;
        if (target != null) {
            target.setIsSource(false);
        }
    }

}
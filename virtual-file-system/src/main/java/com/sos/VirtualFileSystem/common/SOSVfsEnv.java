package com.sos.VirtualFileSystem.common;

import java.util.Map;

public class SOSVfsEnv {

    private Map<String, String> localEnvs;
    private Map<String, String> globalEnvs;

    public Map<String, String> getLocalEnvs() {
        return localEnvs;
    }

    public void setLocalEnvs(Map<String, String> val) {
        localEnvs = val;
    }

    public Map<String, String> getGlobalEnvs() {
        return globalEnvs;
    }

    public void setGlobalEnvs(Map<String, String> val) {
        globalEnvs = val;
    }

}

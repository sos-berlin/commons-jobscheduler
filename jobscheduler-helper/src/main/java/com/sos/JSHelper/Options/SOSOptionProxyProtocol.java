package com.sos.JSHelper.Options;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class SOSOptionProxyProtocol extends JSOptionValueList {

    private static final long serialVersionUID = 1L;

    public enum Protocol {
        http, socks4, socks5;

        public static String asString() {
            StringBuilder result = new StringBuilder();
            result.append("[");
            for (Protocol t : values()) {
                if (result.length() > 1) {
                    result.append(", ");
                }
                result.append(t.name());
            }
            result.append("]");
            return result.toString();
        }
    }

    public SOSOptionProxyProtocol(JSOptionsClass parent, String key, String description, String value, String defaultValue, boolean isMandatory) {
        super(parent, key, description, value, defaultValue, isMandatory);
    }

    public SOSOptionProxyProtocol(JSOptionsClass parent, String indexedKey, String description, String defaultValue, boolean isMandatory) {
        super(parent, indexedKey, description, defaultValue, isMandatory);
    }

    public boolean isHttp() {
        return this.Value().equalsIgnoreCase(Protocol.http.name());
    }

    public boolean isSocks4() {
        return this.Value().equalsIgnoreCase(Protocol.socks4.name());
    }

    public boolean isSocks5() {
        return this.Value().equalsIgnoreCase(Protocol.socks5.name());
    }

    public void Value(String value) {
        String testValue = value.toLowerCase();
        try {
            SOSOptionProxyProtocol.Protocol.valueOf(testValue);
        } catch (Exception e) {
            throw new JobSchedulerException(String.format("Value \"%s\" is not valid for %s - valid values are %s : %s", testValue, strKey,
                    Protocol.asString(), e.toString()));
        }
        super.Value(testValue);
    }

}
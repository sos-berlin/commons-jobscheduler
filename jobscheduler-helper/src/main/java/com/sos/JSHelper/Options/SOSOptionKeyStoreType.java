package com.sos.JSHelper.Options;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class SOSOptionKeyStoreType extends JSOptionValueList {

    private static final long serialVersionUID = 1L;

    public enum Type {
        JKS, JCEKS, PKCS12, PKCS11, DKS;

        public static String asString() {
            StringBuilder result = new StringBuilder();
            result.append("[");
            for (Type t : values()) {
                if (result.length() > 1) {
                    result.append(", ");
                }
                result.append(t.name());
            }
            result.append("]");
            return result.toString();
        }
    }

    public SOSOptionKeyStoreType(JSOptionsClass parent, String key, String description, String value, String defaultValue, boolean isMandatory) {
        super(parent, key, description, value, defaultValue, isMandatory);
    }

    public SOSOptionKeyStoreType(JSOptionsClass parent, String indexedKey, String description, String defaultValue, boolean isMandatory) {
        super(parent, indexedKey, description, defaultValue, isMandatory);
    }

    public boolean isJKS() {
        return this.getValue().equalsIgnoreCase(Type.JKS.name());
    }

    public boolean isJCEKS() {
        return this.getValue().equalsIgnoreCase(Type.JCEKS.name());
    }

    public boolean isPKCS12() {
        return this.getValue().equalsIgnoreCase(Type.PKCS12.name());
    }

    public boolean isPKCS11() {
        return this.getValue().equalsIgnoreCase(Type.PKCS11.name());
    }

    public boolean isDKS() {
        return this.getValue().equalsIgnoreCase(Type.DKS.name());
    }

    public void setValue(String value) {
        String testValue = value.toUpperCase();
        try {
            SOSOptionKeyStoreType.Type.valueOf(testValue);
        } catch (Exception e) {
            throw new JobSchedulerException(String.format("Value \"%s\" is not valid for %s - valid values are %s : %s", testValue, strKey,
                    Type.asString(), e.toString()));
        }
        super.setValue(testValue);
    }

}
package com.sos.JSHelper.Options;
import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class SOSOptionFTPSClientSecurity extends JSOptionValueList {
	private static final long serialVersionUID = 1L;

	public enum ClientSecurity {
		explicit,implicit;

        public static String asString() {
            StringBuffer result = new StringBuffer();
            result.append("[");
            for(ClientSecurity t : values()) {
                if(result.length() > 1) result.append(", ");
                result.append(t.name());
            }
            result.append("]");
            return result.toString();
        }
	}

	public SOSOptionFTPSClientSecurity(JSOptionsClass parent, String key, String description, String value, String defaultValue, boolean isMandatory) {
		super(parent, key, description, value, defaultValue, isMandatory);
	}

	public SOSOptionFTPSClientSecurity(JSOptionsClass parent, String indexedKey, String description, String defaultValue, boolean isMandatory) {
		super(parent, indexedKey, description, defaultValue, isMandatory);
	}

	public boolean isExplicit() {
		return (this.Value().equalsIgnoreCase(ClientSecurity.explicit.name()));
	}

	public boolean isImplicit() {
		return (this.Value().equalsIgnoreCase(ClientSecurity.implicit.name()));
	}
	
    public void Value(String value) {
        String testValue = value.toLowerCase();
        try {
        	SOSOptionFTPSClientSecurity.ClientSecurity.valueOf(testValue);
        } catch (Exception e) {
            throw new JobSchedulerException(String.format("Value \"%s\" is not valid for %s - valid values are %s : %s", 
            		testValue,
            		strKey,
            		ClientSecurity.asString(),
            		e.toString()));
        }
        super.Value(testValue);
    }

}

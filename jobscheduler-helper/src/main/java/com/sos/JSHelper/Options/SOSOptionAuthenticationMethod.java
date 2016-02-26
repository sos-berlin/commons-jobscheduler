package com.sos.JSHelper.Options;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/** @author KB */
public class SOSOptionAuthenticationMethod extends SOSOptionStringValueList {

    private static final long serialVersionUID = 806321970898790899L;
    private static final Logger LOGGER = Logger.getLogger(SOSOptionAuthenticationMethod.class);
    private static final String PASSWORD = "password";
    private static final String PUBLICKEY = "publickey";
    private static final String URL = "url";
    private enuAuthenticationMethods enuMethod = enuAuthenticationMethods.notDefined;

    @Override
    public String getControlType() {
        return ControlType;
    }

    public enum enuAuthenticationMethods {
        publicKey(PUBLICKEY), password(PASSWORD), url(URL), notDefined("undefined"), ppk("ppk"), privatekey("privatekey");

        public final String description;
        public final String text;

        private enuAuthenticationMethods() {
            this(null);
        }

        public String Text() {
            String strT = this.name();
            return strT;
        }

        private enuAuthenticationMethods(final String name) {
            String k;
            if (name == null) {
                k = this.name();
            } else {
                k = name;
            }
            description = k;
            text = k;
        }

        public static String[] getArray() {
            String[] strA = new String[6];
            int i = 0;
            for (enuAuthenticationMethods enuType : enuAuthenticationMethods.values()) {
                strA[i++] = enuType.name();
            }
            return strA;
        }
    }

    public SOSOptionAuthenticationMethod(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription,
            final String pPstrValue, final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
        super.valueList(enuAuthenticationMethods.getArray());
    }

    @Override
    public void Value(final String pstrAuthenticationMethod) {
        try {
            // oh 2014-10-29 NullPointer in switch if pstrAuthenticationMethod is null.
            if (isNull(pstrAuthenticationMethod)) {
                super.Value(pstrAuthenticationMethod);
                enuMethod = enuAuthenticationMethods.notDefined;
            } else {
                switch (pstrAuthenticationMethod) {
                case "ppk":
                case "privatekey":
                case PUBLICKEY:
                    super.Value(pstrAuthenticationMethod);
                    enuMethod = enuAuthenticationMethods.publicKey;
                    break;
                case PASSWORD:
                    super.Value(pstrAuthenticationMethod);
                    enuMethod = enuAuthenticationMethods.password;
                    break;
                case URL:
                    super.Value(pstrAuthenticationMethod);
                    enuMethod = enuAuthenticationMethods.url;
                    break;
                default:
                    super.Value("*invalid*: " + pstrAuthenticationMethod);
                    enuMethod = enuAuthenticationMethods.notDefined;
                    break;
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void Value(final enuAuthenticationMethods penuAuthMethod) {
        switch (penuAuthMethod) {
        case password:
            super.Value(PASSWORD);
            enuMethod = penuAuthMethod;
            break;
        case publicKey:
        case privatekey:
        case ppk:
            super.Value(PUBLICKEY);
            enuMethod = penuAuthMethod;
            break;
        case url:
            super.Value(URL);
            enuMethod = penuAuthMethod;
            break;
        default:
            throw new JobSchedulerException("Invalid AuthenticationMethod : " + penuAuthMethod);
        }
    }

    public void isPassword(final boolean flgF) {
        if (flgF) {
            this.Value(PASSWORD);
        } else {
            this.Value(PUBLICKEY);
        }
    }

    public void isURL(final boolean flgF) {
        if (flgF) {
            this.Value(URL);
        } else {
            this.Value(URL);
        }
    }

    public boolean isPassword() {
        this.Value(strValue);
        return enuMethod == enuAuthenticationMethods.password;
    }

    public boolean isURL() {
        this.Value(strValue);
        return enuMethod == enuAuthenticationMethods.url;
    }

    public boolean isPublicKey() {
        this.Value(strValue);
        return enuMethod == enuAuthenticationMethods.publicKey 
                || enuMethod == enuAuthenticationMethods.ppk
                || enuMethod == enuAuthenticationMethods.privatekey;
    }

    public enuAuthenticationMethods AuthenticationMethod() {
        return enuMethod;
    }
    
}

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

    public SOSOptionAuthenticationMethod(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription,
            final String pPstrValue, final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
        super.valueList(enuAuthenticationMethods.getArray());
    }

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

        public String getText() {
            return this.name();
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

    @Override
    public void setValue(final String pstrAuthenticationMethod) {
        try {
            if (isNull(pstrAuthenticationMethod)) {
                super.setValue(pstrAuthenticationMethod);
                enuMethod = enuAuthenticationMethods.notDefined;
            } else {
                switch (pstrAuthenticationMethod) {
                case "ppk":
                case "privatekey":
                case PUBLICKEY:
                    super.setValue(pstrAuthenticationMethod);
                    enuMethod = enuAuthenticationMethods.publicKey;
                    break;
                case PASSWORD:
                    super.setValue(pstrAuthenticationMethod);
                    enuMethod = enuAuthenticationMethods.password;
                    break;
                case URL:
                    super.setValue(pstrAuthenticationMethod);
                    enuMethod = enuAuthenticationMethods.url;
                    break;
                default:
                    super.setValue("*invalid*: " + pstrAuthenticationMethod);
                    enuMethod = enuAuthenticationMethods.notDefined;
                    break;
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void setValue(final enuAuthenticationMethods penuAuthMethod) {
        switch (penuAuthMethod) {
        case password:
            super.setValue(PASSWORD);
            enuMethod = penuAuthMethod;
            break;
        case publicKey:
        case privatekey:
        case ppk:
            super.setValue(PUBLICKEY);
            enuMethod = penuAuthMethod;
            break;
        case url:
            super.setValue(URL);
            enuMethod = penuAuthMethod;
            break;
        default:
            throw new JobSchedulerException("Invalid AuthenticationMethod : " + penuAuthMethod);
        }
    }

    public void isPassword(final boolean flgF) {
        if (flgF) {
            this.setValue(PASSWORD);
        } else {
            this.setValue(PUBLICKEY);
        }
    }

    public void isURL(final boolean flgF) {
        if (flgF) {
            this.setValue(URL);
        } else {
            this.setValue(URL);
        }
    }

    public boolean isPassword() {
        this.setValue(strValue);
        return enuMethod == enuAuthenticationMethods.password;
    }

    public boolean isURL() {
        this.setValue(strValue);
        return enuMethod == enuAuthenticationMethods.url;
    }

    public boolean isPublicKey() {
        this.setValue(strValue);
        return enuMethod == enuAuthenticationMethods.publicKey || enuMethod == enuAuthenticationMethods.ppk
                || enuMethod == enuAuthenticationMethods.privatekey;
    }

    public enuAuthenticationMethods getAuthenticationMethod() {
        return enuMethod;
    }

}
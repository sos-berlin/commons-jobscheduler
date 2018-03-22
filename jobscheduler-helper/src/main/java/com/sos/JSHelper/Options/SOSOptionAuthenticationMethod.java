package com.sos.JSHelper.Options;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class SOSOptionAuthenticationMethod extends SOSOptionStringValueList {

    private static final long serialVersionUID = 806321970898790899L;
    private static final Logger LOGGER = Logger.getLogger(SOSOptionAuthenticationMethod.class);
    private static final String PASSWORD = "password";
    private static final String PUBLICKEY = "publickey";
    private static final String URL = "url";
    private static final String KEYBOARD_INTERACTIVE = "keyboard-interactive";
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
        publicKey(PUBLICKEY), password(PASSWORD), url(URL), notDefined("undefined"), ppk("ppk"), privatekey("privatekey"), keyboardInteractive(
                KEYBOARD_INTERACTIVE);

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
            String[] arr = new String[7];
            int i = 0;
            for (enuAuthenticationMethods method : enuAuthenticationMethods.values()) {
                arr[i++] = method.name();
            }
            return arr;
        }
    }

    @Override
    public void setValue(final String method) {
        try {
            if (isNull(method)) {
                super.setValue(method);
                enuMethod = enuAuthenticationMethods.notDefined;
            } else {
                switch (method) {
                case "ppk":
                case "privatekey":
                case PUBLICKEY:
                    super.setValue(method);
                    enuMethod = enuAuthenticationMethods.publicKey;
                    break;
                case PASSWORD:
                    super.setValue(method);
                    enuMethod = enuAuthenticationMethods.password;
                    break;
                case URL:
                    super.setValue(method);
                    enuMethod = enuAuthenticationMethods.url;
                    break;
                case KEYBOARD_INTERACTIVE:
                    super.setValue(method);
                    enuMethod = enuAuthenticationMethods.keyboardInteractive;
                    break;
                default:
                    super.setValue("*invalid*: " + method);
                    enuMethod = enuAuthenticationMethods.notDefined;
                    break;
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void setValue(final enuAuthenticationMethods method) {
        switch (method) {
        case password:
            super.setValue(PASSWORD);
            enuMethod = method;
            break;
        case publicKey:
        case privatekey:
        case ppk:
            super.setValue(PUBLICKEY);
            enuMethod = method;
            break;
        case url:
            super.setValue(URL);
            enuMethod = method;
            break;
        case keyboardInteractive:
            super.setValue(KEYBOARD_INTERACTIVE);
            enuMethod = method;
            break;
        default:
            throw new JobSchedulerException("Invalid AuthenticationMethod : " + method);
        }
    }

    public void isPassword(final boolean value) {
        if (value) {
            this.setValue(PASSWORD);
        } else {
            this.setValue(PUBLICKEY);
        }
    }

    public void isURL(final boolean value) {
        if (value) {
            this.setValue(URL);
        } else {
            this.setValue(URL);
        }
    }

    public void isKeyboardInteractive(final boolean value) {
        if (value) {
            this.setValue(KEYBOARD_INTERACTIVE);
        } else {
            this.setValue(PASSWORD);
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

    public boolean isKeyboardInteractive() {
        this.setValue(strValue);
        return enuMethod == enuAuthenticationMethods.keyboardInteractive;
    }

    public enuAuthenticationMethods getAuthenticationMethod() {
        return enuMethod;
    }

}
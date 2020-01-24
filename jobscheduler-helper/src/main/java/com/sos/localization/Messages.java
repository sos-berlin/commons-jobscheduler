package com.sos.localization;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.MissingFormatArgumentException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.UnknownFormatConversionException;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** @author KB */
public class Messages implements Serializable {

    private static final long serialVersionUID = -1276188512965716159L;
    private static final Logger LOGGER = LoggerFactory.getLogger(Messages.class);
    private static final String ENCODING_KEY = "properties.file.encoding";
    private String BUNDLE_NAME = "com.sos.localization.messages";
    private ResourceBundle objResource_Bundle = null;
    private ResourceBundle objResourceBundleDefault = null;
    private Locale objCurrentLocale = Locale.getDefault();
    private enuEncodings enuEncoding = enuEncodings.ISO_8859_1;
    private final enuEncodings enuEncodingDefault = enuEncodings.ISO_8859_1;
    private String strLastKey = "";

    public static enum enuEncodings {
        ISO_8859_1, UTF_8;

        public String text() {
            String strT = this.name().toUpperCase().replace('_', '-');
            return strT;
        }
    }

    public Messages(final String pstrBundleName) {
        BUNDLE_NAME = pstrBundleName;
        String strSOSLocale = System.getenv("SOS_LOCALE");
        if (strSOSLocale == null) {
            objCurrentLocale = Locale.getDefault();
        } else {
            objCurrentLocale = new Locale(strSOSLocale);
        }
        objResource_Bundle = this.getBundle();
    }

    public Messages(final String pstrBundleName, final Locale pobjLocale) {
        BUNDLE_NAME = pstrBundleName;
        objCurrentLocale = pobjLocale;
        objResource_Bundle = this.getBundle();
    }

    public Messages(final String pstrBundleName, final Locale pobjLocale, ClassLoader loader) {
        BUNDLE_NAME = pstrBundleName;
        objCurrentLocale = pobjLocale;
        objResource_Bundle = this.getBundle(loader);
    }

    public ResourceBundle getBundle() {
        ResourceBundle objB = null;
        try {
            objB = ResourceBundle.getBundle(BUNDLE_NAME, objCurrentLocale);
            setEncoding(objB);
        } catch (MissingResourceException mb) {
            try {
                objB = ResourceBundle.getBundle(BUNDLE_NAME);
            } catch (MissingResourceException mbb) {
                objB = ResourceBundle.getBundle(BUNDLE_NAME, new Locale("en"));
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        objResourceBundleDefault = ResourceBundle.getBundle(BUNDLE_NAME, new Locale("en"));
        return objB;
    }

    public ResourceBundle getBundle(ClassLoader loader) {
        ResourceBundle objB = null;
        try {
            if (loader == null) {
                objB = ResourceBundle.getBundle(BUNDLE_NAME, objCurrentLocale);
            } else {
                objB = ResourceBundle.getBundle(BUNDLE_NAME, objCurrentLocale, loader);
            }
            setEncoding(objB);
        } catch (MissingResourceException mb) {
            try {
                if (loader == null) {
                    objB = ResourceBundle.getBundle(BUNDLE_NAME);
                } else {
                    objB = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault(), loader);
                }
            } catch (MissingResourceException mbb) {
                if (loader == null) {
                    objB = ResourceBundle.getBundle(BUNDLE_NAME, new Locale("en"));
                } else {
                    objB = ResourceBundle.getBundle(BUNDLE_NAME, new Locale("en"), loader);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        if (loader == null) {
            objResourceBundleDefault = ResourceBundle.getBundle(BUNDLE_NAME, new Locale("en"));
        } else {
            objResourceBundleDefault = ResourceBundle.getBundle(BUNDLE_NAME, new Locale("en"), loader);
        }
        return objB;
    }

    public void setLocale(final Locale pobjLocale) {
        objCurrentLocale = pobjLocale;
    }

    private void setEncoding(final ResourceBundle objBundle) {
        try {
            String strM = null;
            if (objBundle != null) {
                strM = objBundle.getString(ENCODING_KEY);
                if (strM != null && strM.equalsIgnoreCase(enuEncodings.UTF_8.text())) {
                    enuEncoding = enuEncodings.UTF_8;
                }
            }
        } catch (MissingResourceException e) {
            //
        }
    }

    private String getString(final String pstrKey, final ResourceBundle objBundle) {
        String strT = null;
        try {
            String strM = "";
            if (objResource_Bundle == null) {
                objResource_Bundle = getBundle();
            }
            enuEncodings encoding = enuEncoding;
            if (objResource_Bundle != null) {
                try {
                    strM = objResource_Bundle.getString(pstrKey);
                } catch (MissingResourceException e) {
                    strM = objResourceBundleDefault.getString(pstrKey);
                    encoding = enuEncodingDefault;
                }
                if (strM == null) {
                    strM = pstrKey;
                } else {
                    if (strM.startsWith("[") && strM.endsWith("]")) {
                        LOGGER.trace("strLastKey = " + strLastKey + ", strKey = " + strM);
                        if (strM.equalsIgnoreCase(strLastKey)) {
                            strM = "** loop: " + strM;
                        } else {
                            String strKey = strM.substring(1, strM.length() - 1);
                            String strM1 = getString(strKey, objBundle);
                            strM = strM1;
                        }
                        strLastKey = strM;
                    }
                }
            } else {
                strM = pstrKey;
            }
            try {
                if (encoding == enuEncodings.UTF_8) {
                    strT = new String(strM.getBytes(enuEncodings.ISO_8859_1.text()), enuEncodings.UTF_8.text());
                    if (strT.contains("�") == false) {
                        strM = strT;
                    }
                }
            } catch (UnsupportedEncodingException e) {
                strM = strT;
            }
            return strM;
        } catch (MissingResourceException e) {
            return null;
        }
    }

    public String getMsg(final String pstrKey) {
        String strM = "";
        try {
            strM = addKey(pstrKey, getString(pstrKey, objResource_Bundle));
        } catch (MissingResourceException e) {
            strM = '!' + pstrKey + '!';
        }
        return strM;
    }

    private String addKey(final String pstrKey, final String pstrMsgTxt) {
        String strRet = pstrMsgTxt;
        if (pstrMsgTxt != null) {
            if (pstrKey != null && !pstrKey.contains("_T_") && !pstrKey.contains("_L_")) {
                strRet = pstrKey + ": " + pstrMsgTxt;
            }
        } else {
            strRet = pstrKey;
        }
        return strRet;
    }

    public String getLabel(final String pstrKey) {
        try {
            String strM = getString(pstrKey, objResource_Bundle);
            return strM;
        } catch (MissingResourceException e) {
            return '!' + pstrKey + '!';
        }
    }

    public String getTooltip(final String pstrKey) {
        try {
            String strM = getString(pstrKey, objResource_Bundle);
            return strM;
        } catch (MissingResourceException e) {
            return '!' + pstrKey + '!';
        }
    }

    public String getMsg(final String pstrKey, final Object... pstrArgs) {
        String strM = "";
        try {
            strM = addKey(pstrKey, getString(pstrKey, objResource_Bundle));
            strM = String.format(strM, pstrArgs);
            int i = 0;
            if (strM.contains("{")) {
                for (Object object : pstrArgs) {
                    try {
                        String strT = (String) object;
                        strT = Matcher.quoteReplacement(strT);
                        strM = strM.replaceAll("\\{" + i++ + "\\}", strT);
                    } catch (ClassCastException e) {
                        i++;
                    }
                }
            }
            return strM;
        } catch (MissingFormatArgumentException e) {
            strM = String.format("%1$s (%2$s): %3$s | missing format specifer: %4$s", pstrKey, objCurrentLocale.getDisplayName(), strM, e
                    .getFormatSpecifier());
            return strM;
        } catch (UnknownFormatConversionException e) {
            strM = String.format("%1$s (%2$s): %3$s | unknown format conversion: %4$s", pstrKey, objCurrentLocale.getDisplayName(), strM, e
                    .getConversion());
            return strM;
        } catch (MissingResourceException e) {
            strM = String.format("%1$s (%2$s): %3$s", pstrKey, objCurrentLocale.getDisplayName(), strM);
            return strM;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return strM;
        }
    }

    public String getMsg(final String pstrKey, final Locale pobjLocale) {
        String strM = "";
        try {
            if (!objCurrentLocale.equals(pobjLocale)) {
                objCurrentLocale = pobjLocale;
                objResource_Bundle = getBundle();
            }
            strM = addKey(pstrKey, getString(pstrKey, objResource_Bundle));
        } catch (MissingResourceException e) {
            strM = '!' + pstrKey + '!';
        }
        return strM;
    }

}

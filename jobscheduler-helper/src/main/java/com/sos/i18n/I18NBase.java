package com.sos.i18n;

import java.util.Locale;

/** @author KB */
public class I18NBase extends com.sos.JSHelper.Basics.JSToolBox {

    static Msg.BundleBaseName BUNDLE_BASE_NAME;
    private String strLocale = Locale.getDefault().toString();
    private Locale objLocale = Locale.getDefault();

    protected I18NBase() {
        //
    }

    protected I18NBase(String strBundleBaseName) {
        super(strBundleBaseName);
        BUNDLE_BASE_NAME = new Msg.BundleBaseName(strBundleBaseName);
    }

    protected I18NBase(String strBundleBaseName, final String pstrLocale) {
        super(strBundleBaseName);
        BUNDLE_BASE_NAME = new Msg.BundleBaseName(strBundleBaseName, pstrLocale);
        strLocale = pstrLocale;
        objLocale = new Locale(strLocale);
    }

    protected String getMsg(String key, Object... varargs) {
        if (BUNDLE_BASE_NAME != null) {
            String strBundleBaseName = BUNDLE_BASE_NAME.getBundleBaseName();
            BUNDLE_BASE_NAME = new Msg.BundleBaseName(strBundleBaseName, strLocale);
        }
        return Msg.createMsg(BUNDLE_BASE_NAME, objLocale, key, varargs).toString();
    }

    public void setLocale(final String pstrLocale) {
        if (pstrLocale != null && !pstrLocale.trim().isEmpty()) {
            strLocale = pstrLocale;
            objLocale = new Locale(pstrLocale);
        }
    }

}
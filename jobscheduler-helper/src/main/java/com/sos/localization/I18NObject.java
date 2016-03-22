package com.sos.localization;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

/** @author KB */
public class I18NObject {

    private static final Logger LOGGER = Logger.getLogger(I18NObject.class);
    private String strKey = "";
    private String strLanguage = "";
    private String f1 = "";
    private String f10 = "";
    private String label = "";
    private String shorttext = "";
    private String tooltip = "";
    private String icon = "";
    private String acc = "";

    public I18NObject(final String pstrKey, final String pstrLanguage) {
        this.strKey = pstrKey;
        this.strLanguage = pstrLanguage;
    }

    public String getF1() {
        return f1;
    }

    public void setF1(String f1) {
        this.f1 = f1;
    }

    public String getF10() {
        return f10;
    }

    public void setF10(String f10) {
        this.f10 = f10;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getShorttext() {
        return shorttext;
    }

    public void setShorttext(String shorttext) {
        this.shorttext = shorttext;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getAcc() {
        return acc;
    }

    public void setAcc(String acc) {
        this.acc = acc;
    }

    public void setBeanProperty(final String pstrTooken, final String pstrValue) {
        LOGGER.debug(strKey + ": " + pstrTooken + " = " + pstrValue);
        try {
            switch (pstrTooken) {
            case "F1":
                setF1(pstrValue);
                break;
            case "F10":
                setF10(pstrValue);
                break;
            default:
                PropertyUtils.setProperty(this, pstrTooken, pstrValue.trim());
                break;
            }
        } catch (IllegalAccessException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}

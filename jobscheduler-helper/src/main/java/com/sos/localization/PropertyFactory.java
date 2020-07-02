package com.sos.localization;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;

public class PropertyFactory extends JSJobUtilitiesClass<PropertyFactoryOptions> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyFactory.class);

    public PropertyFactory() {
        super(new PropertyFactoryOptions());
    }

    @Override
    public PropertyFactoryOptions getOptions() {
        if (objOptions == null) {
            objOptions = new PropertyFactoryOptions();
        }
        return objOptions;
    }

    public PropertyFactory execute() throws Exception {
        try {
            getOptions().checkMandatory();
            LOGGER.debug(getOptions().dirtyString());
            String strPropertyFileName = objOptions.propertyFileNamePrefix.getValue();
            Map<String, Map<String, I18NObject>> allKeys = new HashMap<String, Map<String, I18NObject>>();
            Map<String, I18NObject> mapKeys = new HashMap<String, I18NObject>();
            Vector<String> vecLanguages = new Vector<>();
            for (File objFile : objOptions.sourceFolderName.listFiles()) {
                String strFileName = objFile.getName();
                if (strFileName.startsWith(strPropertyFileName) && strFileName.endsWith(".properties")) {
                    LOGGER.info("FileName = " + objFile.getName());
                    InputStream ips = new FileInputStream(objFile);
                    Properties objProps = new Properties();
                    objProps.load(ips);
                    ips.close();
                    String strLanguage = "de";
                    strLanguage = strFileName.replaceAll("\\.properties", "");
                    strLanguage = strLanguage.substring(strPropertyFileName.length() + 1);
                    vecLanguages.add(strLanguage);
                    for (String key : objProps.stringPropertyNames()) {
                        String[] strA = key.split("\\.");
                        if (strA.length < 2) {
                            LOGGER.debug(key + " = " + objProps.getProperty(key));
                        } else {
                            mapKeys = allKeys.get(strA[0]);
                            if (mapKeys == null) {
                                mapKeys = new HashMap<String, I18NObject>();
                                allKeys.put(strA[0], mapKeys);
                            }
                            String strIDent = strA[0] + "_" + strLanguage;
                            I18NObject objI18NO = mapKeys.get(strIDent);
                            if (objI18NO == null) {
                                objI18NO = new I18NObject(strIDent, strLanguage);
                                mapKeys.put(strIDent, objI18NO);
                            }
                            objI18NO.setBeanProperty(strA[1], objProps.getProperty(key));
                        }
                    }
                    for (String objK : allKeys.keySet()) {
                        LOGGER.debug(objK);
                        mapKeys = allKeys.get(objK);
                        for (String objL : mapKeys.keySet()) {
                            LOGGER.debug("_______" + objL);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
        return this;
    }

}
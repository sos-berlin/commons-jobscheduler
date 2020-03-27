package com.sos.scheduler.model.objects;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.vfs.common.interfaces.ISOSVirtualFile;
import com.sos.scheduler.model.SchedulerObjectFactory;

/** @author oh */
public class JSObjParams extends Params {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSObjParams.class);
    public static final String fileNameExtension = ".params.xml";

    public JSObjParams(final SchedulerObjectFactory schedulerObjectFactory) {
        objFactory = schedulerObjectFactory;
    }

    public JSObjParams(final SchedulerObjectFactory schedulerObjectFactory, final Params params) {
        objFactory = schedulerObjectFactory;
        setObjectFieldsFrom(params);
    }

    public JSObjParams(final SchedulerObjectFactory schedulerObjectFactory, final ISOSVirtualFile pobjVirtualFile) {
        objFactory = schedulerObjectFactory;
        Params objParams = (Params) unMarshal(pobjVirtualFile);
        setObjectFieldsFrom(objParams);
        setHotFolderSrc(pobjVirtualFile);
    }

    public HashMap<String, String> getParamList() {
        return values();
    }

    public HashMap<String, String> values() {
        HashMap<String, String> values = new HashMap<String, String>();
        for (Object o : getParamOrCopyParamsOrInclude()) {
            if (o instanceof Param) {
                Param p = (Param) o;
                values.put(p.getName(), p.getValue());
            }
        }
        return values;
    }

    public boolean hasParameterValue(final String parameterName) {
        boolean result = false;
        HashMap<String, String> values = values();
        if (values.containsKey(parameterName)) {
            String pValue = values.get(parameterName);
            if (!"".equals(pValue)) {
                result = true;
            }
        }
        return result;
    }

    public String getParameterValue(final String parameterName) {
        String result = null;
        HashMap<String, String> values = values();
        if (values.containsKey(parameterName)) {
            result = values.get(parameterName);
        }
        return result;
    }

    public void setParamsFromString(final String paramString) {
        try {
            if (!Strings.isNullOrEmpty(paramString)) {
                Params params = (Params) objFactory.unMarshall(paramString);
                setObjectFieldsFrom(params);
            }
        } catch (Exception e) {
            String msg = "Error while parsing parameter string " + paramString;
            LOGGER.error(msg);
            throw new JobSchedulerException(e);
        }
    }

    public void merge(final Params paramsToMerge) {
        for (Object o : paramsToMerge.getParamOrCopyParamsOrInclude()) {
            if (o instanceof Param) {
                Param p = (Param) o;
                if (!hasParameterValue(p.getName())) {
                    add(p);
                    LOGGER.debug("Parameter " + p.getName() + "=" + p.getValue() + " merged.");
                }
            }
        }
    }

    public void add(final Param newParam) {
        getParamOrCopyParamsOrInclude().add(newParam);
    }

    public void add(final String name, final String value) {
        Param p = objFactory.createParam();
        p.setName(name);
        p.setValue(value);
        add(p);
    }

    public int size() {
        return getParamList().size();
    }

}
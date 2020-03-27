package com.sos.scheduler.model.objects;

import java.io.File;
import java.math.BigInteger;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.vfs.common.interfaces.ISOSVirtualFile;
import com.sos.scheduler.model.SchedulerObjectFactory;

/** @author oh */
public class JSObjProcessClass extends ProcessClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSObjProcessClass.class);
    public final static String fileNameExtension = ".process_class.xml";

    public JSObjProcessClass(final SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
        super.strFileNameExtension = fileNameExtension;
    }

    public JSObjProcessClass(final SchedulerObjectFactory schedulerObjectFactory, final ProcessClass origOrder) {
        super();
        super.strFileNameExtension = fileNameExtension;
        objFactory = schedulerObjectFactory;
        setObjectFieldsFrom(origOrder);
    }

    @SuppressWarnings("unchecked")
    public JSObjProcessClass(final SchedulerObjectFactory schedulerObjectFactory, final ISOSVirtualFile pobjVirtualFile) {
        objFactory = schedulerObjectFactory;
        objJAXBElement = (JAXBElement<JSObjBase>) unMarshal(pobjVirtualFile);
        setObjectFieldsFrom(objJAXBElement.getValue());
        setHotFolderSrc(pobjVirtualFile);
    }

    public void setSpoolerIdIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setSpoolerId(value);
        }
    }

    public void setNameIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setName(value);
        }
    }

    public void setRemoteSchedulerIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setSpoolerId(value);
        }
    }

    public void setReplaceIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setReplace(value);
        }
    }

    public void setMaxProcessesIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            try {
                super.setMaxProcesses(new BigInteger(value));
            } catch (NumberFormatException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public String getObjectName() {
        String name = this.getHotFolderSrc().getName();
        int i = name.indexOf(fileNameExtension);
        if (i != -1) {
            name = name.substring(0, name.indexOf(fileNameExtension));
        }
        name = new File(name).getName();
        return name;
    }

    public void setMaxProcesses(final int value) {
        maxProcesses = int2BigInteger(value);
    }

}

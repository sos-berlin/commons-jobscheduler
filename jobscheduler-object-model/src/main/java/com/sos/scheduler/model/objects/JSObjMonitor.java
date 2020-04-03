package com.sos.scheduler.model.objects;

import javax.xml.bind.JAXBElement;

import com.sos.vfs.common.interfaces.ISOSProviderFile;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.objects.Job.Monitor;

/** @author oh */
public class JSObjMonitor extends Monitor {

    public final static String fileNameExtension = ".lock.xml";

    public JSObjMonitor(final SchedulerObjectFactory schedulerObjectFactory) {
        objFactory = schedulerObjectFactory;
    }

    public JSObjMonitor(final SchedulerObjectFactory schedulerObjectFactory, final Monitor origOrder) {
        objFactory = schedulerObjectFactory;
        setObjectFieldsFrom(origOrder);
    }

    @SuppressWarnings("unchecked")
    public JSObjMonitor(final SchedulerObjectFactory schedulerObjectFactory, final ISOSProviderFile pobjVirtualFile) {
        objFactory = schedulerObjectFactory;
        objJAXBElement = (JAXBElement<JSObjBase>) unMarshal(pobjVirtualFile);
        setObjectFieldsFrom(objJAXBElement.getValue());
        setHotFolderSrc(pobjVirtualFile);
    }

}

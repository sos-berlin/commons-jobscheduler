package com.sos.scheduler.model.objects;

import java.io.File;

import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.scheduler.model.SchedulerObjectFactory;

/** @author oh */
public class JSObjOrder extends Order {

    private static final Logger LOGGER = Logger.getLogger(JSObjOrder.class);
    public final static String fileNameExtension = ".order.xml";

    public JSObjOrder(final SchedulerObjectFactory schedulerObjectFactory) {
        try {
            objFactory = schedulerObjectFactory;
            objJAXBElement = (JAXBElement<JSObjBase>) unMarshal("<order/>");
            setObjectFieldsFrom(objJAXBElement.getValue());
            doInit();
        } catch (Exception e) {
            LOGGER.error("Could not instantiate Order.", e);
            throw new JobSchedulerException("Could not instantiate Order.", e);
        }
    }

    public JSObjOrder(final SchedulerObjectFactory schedulerObjectFactory, final String rootElementName) {
        objFactory = schedulerObjectFactory;
        objJAXBElement = (JAXBElement<JSObjBase>) unMarshal("<" + rootElementName + "/>");
        setObjectFieldsFrom(objJAXBElement.getValue());
        doInit();
    }

    public JSObjOrder(final SchedulerObjectFactory schedulerObjectFactory, final ISOSVirtualFile pobjVirtualFile) {
        objFactory = schedulerObjectFactory;
        objJAXBElement = (JAXBElement<JSObjBase>) unMarshal(pobjVirtualFile);
        setObjectFieldsFrom(objJAXBElement.getValue());
        setHotFolderSrc(pobjVirtualFile);
        doInit();
    }

    private void doInit() {
        super.strFileNameExtension = fileNameExtension;
    }

    @Override
    public RunTime getRunTime() {
        if (super.getRunTime() == null) {
            RunTime objR = new RunTime();
            super.setRunTime(objR);
        }
        return super.getRunTime();
    }

    public JSObjOrder getOrderFromXMLString(final String xmlString) {
        objJAXBElement = (JAXBElement<JSObjBase>) unMarshal(xmlString);
        Order o = (Order) objJAXBElement.getValue();
        if (o.getRunTime() != null) {
            o.getRunTime().setParent(objFactory);
        }
        setObjectFieldsFrom(o);
        return this;
    }

    public void setXmlContent(final String xmlContent) {
        getOrderFromXMLString(xmlContent);
    }

    public String createFileName(final String pstrPathName) {
        return pstrPathName + "/" + getJobChain() + "," + getId() + JSObjOrder.fileNameExtension;
    }

    public JSObjRunTime getJSObjRunTime() {
        JSObjRunTime runTime = new JSObjRunTime(objFactory);
        if (getRunTime() != null) {
            runTime.setObjectFieldsFrom(getRunTime());
        }
        runTime.setHotFolderSrc(getHotFolderSrc());
        return runTime;
    }

    public JSObjParams getJSObjParams() {
        JSObjParams params = new JSObjParams(objFactory);
        params.setObjectFieldsFrom(getParams());
        setHotFolderSrc(getHotFolderSrc());
        return params;
    }

    @Override
    public String getObjectName() {
        String name = this.getHotFolderSrc().getName();
        int i = name.indexOf(strFileNameExtension);
        if (i != -1) {
            name = name.substring(0, name.indexOf(strFileNameExtension));
        }
        name = new File(name).getName();
        i = name.indexOf(",");
        String strJobChain = "???";
        if (i > -1 && i + 1 < name.length()) {
            if (i > 0) {
                strJobChain = name.substring(0, i);
            }
            name = name.substring(i + 1) + " (" + strJobChain + ")";
        }
        return name;
    }

    public String getJobChainName() {
        String name = this.getHotFolderSrc().getName();
        int i = name.indexOf(strFileNameExtension);
        if (i != -1) {
            name = name.substring(0, name.indexOf(strFileNameExtension));
        }
        name = new File(name).getName();
        i = name.indexOf(",");
        String strJobChain = "???";
        if (i > -1 && i + 1 < name.length() && i > 0) {
            strJobChain = name.substring(0, i);
        }
        return strJobChain;
    }

}
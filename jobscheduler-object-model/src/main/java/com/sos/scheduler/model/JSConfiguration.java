package com.sos.scheduler.model;

import java.math.BigInteger;

import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.scheduler.model.objects.JSObjBase;
import com.sos.scheduler.model.objects.JSObjSpooler;
import com.sos.scheduler.model.objects.Spooler;

/** @author oh */
public class JSConfiguration extends JSObjBase {

    private final String conClassName = "JSConfiguration";
    private JSObjSpooler objSpooler;
    private Spooler.Config objConfig;

    @JSOptionDefinition(name = "Port", description = "PortNumber of JobScheduler", key = "Port", type = "SOSOptionPortNumber", mandatory = true)
    public SOSOptionPortNumber Port = new SOSOptionPortNumber(null, conClassName + ".Port", "PortNumber of JobScheduler", "0", "4444", true);

    public JSConfiguration() {
        //
    }

    public JSConfiguration(final SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
        objSpooler = objFactory.createSpooler();
        objConfig = getConfig();
    }

    public JSConfiguration(final SchedulerObjectFactory schedulerObjectFactory, final ISOSVirtualFile pobjVirtualFile) {
        super();
        objFactory = schedulerObjectFactory;
        objSpooler = objFactory.createSpooler(pobjVirtualFile);
        objConfig = getConfig();
    }

    public Spooler.Config getConfig() {
        if (objConfig == null) {
            if (objSpooler == null) {
                objConfig = new Spooler.Config();
            } else if (objSpooler.getConfig().isEmpty()) {
                objConfig = new Spooler.Config();
                objSpooler.getConfig().add(objConfig);
            } else {
                objConfig = objSpooler.getConfig().get(0);
            }
        }
        return objConfig;
    }

    public String toXMLString() {
        return objSpooler.toXMLString();
    }

    public Object toXMLFile() {
        return objSpooler.toXMLFile();
    }

    public SOSOptionPortNumber getPort() {
        Port.setValue(objConfig.getPort().toString());
        return Port;
    }

    public void setPort(String value) {
        if (value == null) {
            objConfig.setPort(BigInteger.valueOf(0));
        } else {
            objConfig.setPort(new BigInteger(value));
        }
    }

    public void setPort(SOSOptionPortNumber pobjPortNumber) {
        String value = pobjPortNumber.getValue();
        if (value == null) {
            objConfig.setPort(BigInteger.valueOf(0));
        } else {
            objConfig.setPort(new BigInteger(value));
        }
    }

}
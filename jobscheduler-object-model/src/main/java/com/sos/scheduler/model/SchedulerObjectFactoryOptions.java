package com.sos.scheduler.model;

import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionJSTransferMethod;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionString;

/** @author KB */
public class SchedulerObjectFactoryOptions extends JSOptionsClass implements ISOSSchedulerSocket {

    private static final long serialVersionUID = 1L;
    private static final String CLASSNAME = "SchedulerObjectFactoryOptions";

    @JSOptionDefinition(name = "tcp_time_out", description = "The time out in seconds for a tcp connection", key = "tcp_time_out", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger TCPTimeoutValue = new SOSOptionInteger(this, CLASSNAME + ".tcp_time_out", "The time out in seconds for a tcp connection",
            "60", "60", false);
    public SOSOptionInteger TimeOut = (SOSOptionInteger) TCPTimeoutValue.setAlias("time_out");

    @Override
    public int getTCPTimeoutValue() {
        return TCPTimeoutValue.value();
    }

    @Override
    public ISOSSchedulerSocket setTCPTimeoutValue(final String pstrValue) {
        TCPTimeoutValue.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "UDPPortNumber", description = "The scheduler communication port for UDP", key = "UDPPortNumber", type = "SOSOptionPortNumber", mandatory = true)
    public SOSOptionPortNumber UDPPortNumber = new SOSOptionPortNumber(this, CLASSNAME + ".UDPPortNumber",
            "The scheduler communication port for UDP", "4444", "4444", true);

    @Override
    public int getUDPPortNumber() {
        return UDPPortNumber.value();
    }

    @Override
    public ISOSSchedulerSocket setUDPPortNumber(final String pstrValue) {
        UDPPortNumber.setValue(pstrValue);
        return this;
    }

    
    @JSOptionDefinition(name = "basic_authorization", description = "", key = "basic_authorization", type = "SOSOptionString", mandatory = false)
    public SOSOptionString basicAuthorization = new SOSOptionString(this, conClassName + ".basic_authorization", "", " ", " ", false);

    public SOSOptionString getBasicAuthorization() {
        return basicAuthorization;
    }

    public void setBasicAuthorization(SOSOptionString basicAuthorization) {
        this.basicAuthorization = basicAuthorization;
    }

    @JSOptionDefinition(name = "command_url", description = "", key = "command_url", type = "SOSOptionString", mandatory = false)
    public SOSOptionString commandUrl = new SOSOptionString(this, conClassName + ".command_url", "", " ", " ", false);

    public SOSOptionString getCommandUrl() {
        return commandUrl;
    }

    public void setCommandUrl(SOSOptionString commandUrl) {
        this.commandUrl = commandUrl;
    }

    
    @JSOptionDefinition(name = "PortNumber", description = "The scheduler communication port", key = "PortNumber", type = "SOSOptionPortNumber", mandatory = true)
    public SOSOptionPortNumber PortNumber = new SOSOptionPortNumber(this, CLASSNAME + ".PortNumber", "The scheduler communication port", "0", "4444",
            true);
    public SOSOptionPortNumber TCPPortNumber = (SOSOptionPortNumber) PortNumber.setAlias("tcp_port_number");

    @Override
    public int getPortNumber() {
        return PortNumber.value();
    }

    @Override
    public ISOSSchedulerSocket setPortNumber(final String pstrValue) {
        PortNumber.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "ServerName", description = "The Name of the Server", key = "ServerName", type = "SOSOptionString", mandatory = true)
    public SOSOptionHostName ServerName = new SOSOptionHostName(this, CLASSNAME + ".ServerName", "The Name of the Server", "0.0.0.0", "localhost", true);
    public SOSOptionHostName ServerIPv4 = (SOSOptionHostName) ServerName.setAlias("Server_IPv4_Name");

    @JSOptionDefinition(name = "TransferMethod", description = "The technical method of how to communicate with the JobScheduler", key = "TransferMethod",
            type = "SOSOptionJSTransferMethod", mandatory = true)
    public SOSOptionJSTransferMethod TransferMethod = new SOSOptionJSTransferMethod(this, CLASSNAME + ".TransferMethod", 
            "The technical method of how to communicate with the JobScheduler", "tcp", "tcp", true);

    @Override
    public String getTransferMethod() {
        return TransferMethod.getValue();
    }

    @Override
    public ISOSSchedulerSocket setTransferMethod(final String pstrValue) {
        TransferMethod.setValue(pstrValue);
        return this;
    }

    @Override
    public String getServerName() {
        return ServerName.getValue();
    }

    @Override
    public ISOSSchedulerSocket setServerName(final String pstrValue) {
        ServerName.setValue(pstrValue);
        return this;
    }

    public SchedulerObjectFactoryOptions() {
        objParentClass = this.getClass();
    }

}
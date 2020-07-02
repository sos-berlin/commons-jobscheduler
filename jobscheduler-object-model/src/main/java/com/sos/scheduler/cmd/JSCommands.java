package com.sos.scheduler.cmd;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.scheduler.model.commands.JSCmdCommands;
import com.sos.scheduler.model.objects.JSObjBase;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/** A container class to collect commands and send them all together to an
 * JobScheduler instance. */
public class JSCommands extends JSCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSCommands.class);

    private final List<String> listOfCommands = new ArrayList<String>();
    private final JSCmdCommands cmdContainer;
    private String xmlHeader = "";

    public JSCommands(String host, Integer port) {
        super(host, port);
        this.cmdContainer = getFactory().createCmdCommands();
        setCommand(cmdContainer);
        listOfCommands.clear();
    }

    public void addCommand(JSCommand command) {
        if (xmlHeader.isEmpty()) {
            String xml = command.getCommand().toXMLString();
            xmlHeader = xml.substring(0, xml.indexOf("?>") + 2);
        }
        listOfCommands.add(command.getCommand().toXMLString().replace(xmlHeader, ""));
    }

    @Override
    public void run() {

        StringBuffer xmlCmd = new StringBuffer();
        xmlCmd.append(xmlHeader);
        xmlCmd.append("<commands>");
        for (String cmd : listOfCommands) {
            xmlCmd.append(cmd.trim());
        }
        xmlCmd.append("</commands>");
        LOGGER.info(xmlCmd.toString());
        @SuppressWarnings("unchecked")
        JAXBElement<JSObjBase> obj = (JAXBElement<JSObjBase>) cmdContainer.unMarshal(xmlCmd.toString());
        cmdContainer.setObjectFieldsFrom(obj.getValue());
        LOGGER.info(cmdContainer.toXMLString());

        try {
            super.run();
        } catch (Exception e) {
            String msg = "Error fetching commands object.";
            throw new JobSchedulerException(msg, e);
        }

    }

}

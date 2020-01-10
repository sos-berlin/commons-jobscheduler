package com.sos.scheduler.cmd;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.answers.Answer;
import com.sos.scheduler.model.answers.ERROR;
import com.sos.scheduler.model.answers.JSCmdBase;
import com.sos.scheduler.model.objects.Spooler;

abstract class JSCommand implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSCommand.class);
    private final SchedulerObjectFactory objectFactory;
    private JSCmdBase jsCommand;

    public JSCommand(String host, Integer port) {
        this.objectFactory = new SchedulerObjectFactory(host, port);
        this.objectFactory.initMarshaller(Spooler.class);
        this.jsCommand = null;
    }

    public JSCommand(SchedulerObjectFactory factory) {
        this.objectFactory = factory;
        this.objectFactory.initMarshaller(Spooler.class);
        this.jsCommand = null;
    }

    public void setCommand(JSCmdBase command) {
        this.jsCommand = command;
    }

    public JSCmdBase getCommand() {
        return this.jsCommand;
    }

    @Override
    public void run() {
        if (jsCommand == null) {
            throw new JobSchedulerException("Command not set - please call setCommand() first.");
        }
        try {
            objectFactory.getOptions().TransferMethod.setValue("tcp");
            doCommand(jsCommand);
        } catch (Exception e) {
            String msg = "Error fetching command.";
            LOGGER.info(msg);
            throw new JobSchedulerException(msg, e);
        }
    }

    private void doCommand(JSCmdBase command) {
        try {
            command.run();
            LOGGER.info(command.toXMLString());
        } catch (Exception e) {
            String msg = "Error fetching command.";
            LOGGER.error(msg);
            throw new JobSchedulerException(msg, e);
        }
        LOGGER.debug("Command submitted - waiting for answer ...");
        Answer objA = command.getAnswer();
        ERROR objE = objA.getERROR();
        LOGGER.error("JS answer received.");
        if (objE != null) {
            LOGGER.error("The answer contains an error - order not started. Errortext from JS: " + objE.getText());
        }
        try {
            objectFactory.getSocket().doClose();
        } catch (IOException e) {
            String msg = "Error closing JobScheduler socket.";
            LOGGER.error(msg);
            throw new JobSchedulerException(msg, e);
        }
    }

    public SchedulerObjectFactory getFactory() {
        return objectFactory;
    }

    public String getXml() {
        return jsCommand.toXMLString();
    }

}
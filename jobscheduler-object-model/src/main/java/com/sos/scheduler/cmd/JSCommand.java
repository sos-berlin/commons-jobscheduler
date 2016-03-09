package com.sos.scheduler.cmd;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
// import com.sos.JSHelper.Options.SOSOptionProtocol;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.answers.Answer;
import com.sos.scheduler.model.answers.ERROR;
import com.sos.scheduler.model.answers.JSCmdBase;
import com.sos.scheduler.model.objects.Spooler;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// import sos.spooler.Spooler;

abstract class JSCommand implements Runnable {

    private final Logger logger = Logger.getLogger(JSCommand.class);

    private final SchedulerObjectFactory objectFactory;
    private JSCmdBase jsCommand;
    // private SOSOptionProtocol.Type startType = SOSOptionProtocol.Type.tcp;

    private List<ICommandActionsListener> listener = new ArrayList<ICommandActionsListener>();

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

        if (jsCommand == null)
            throw new JobSchedulerException("Command not set - please call setCommand() first.");

        try {
            objectFactory.Options().TransferMethod.Value("tcp");
            doCommand(jsCommand);
        } catch (Exception e) {
            String msg = "Error fetching command.";
            notifyListener(Level.ERROR, msg);
            throw new JobSchedulerException(msg, e);
        }

    }

    private void doCommand(JSCmdBase command) {

        try {
            command.run();
            notifyListener(Level.INFO, command.toXMLString());
        } catch (Exception e) {
            String msg = "Error fetching command.";
            notifyListener(Level.ERROR, msg);
            throw new JobSchedulerException(msg, e);
        }

        notifyListener(Level.DEBUG, "Command submitted - waiting for answer ...");
        Answer objA = command.getAnswer();
        ERROR objE = objA.getERROR();
        notifyListener(Level.DEBUG, "JS answer received.");
        if (objE != null) {
            notifyListener(Level.ERROR, "The answer contains an error - order not started. Errortext from JS: " + objE.getText());
        }

        try {
            objectFactory.getSocket().doClose();
        } catch (IOException e) {
            String msg = "Error closing JobScheduler socket.";
            notifyListener(Level.ERROR, msg);
            throw new JobSchedulerException(msg, e);
        }

    }

    private void notifyListener(Level level, String message) {
        if (listener.size() == 0) {
            logger.log(level, message);
        } else {
            for (ICommandActionsListener l : listener)
                l.onMessage(level, message);
        }
    }

    public void addMessageListener(ICommandActionsListener listener) {
        this.listener.add(listener);
    }

    public SchedulerObjectFactory getFactory() {
        return objectFactory;
    }

    public String getXml() {
        return jsCommand.toXMLString();
    }

}

package com.sos.scheduler.model.commands;

import javax.xml.bind.JAXBElement;

import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.objects.JSObjBase;

public class JSCmdSubsystemShow extends SubsystemShow {

    public enum enu4What {
        STATISTICS;

        public String getText() {
            return this.name().toLowerCase();
        }
    }

    public JSCmdSubsystemShow(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
        objJAXBElement = (JAXBElement<JSObjBase>) unMarshal("<subsystem.show/>");
        setObjectFieldsFrom((SubsystemShow) objJAXBElement.getValue());
    }

    public void setWhat(enu4What penuT) {
        super.setWhat(penuT.getText());
    }

    public void setWhat(enu4What[] penuT) {
        String strT = "";
        for (enu4What enuState4What : penuT) {
            strT += enuState4What.getText() + " ";
        }
        super.setWhat(strT);
    }

}
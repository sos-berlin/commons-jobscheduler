package com.sos.scheduler.model.commands;

import java.math.BigInteger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.answers.Calendar;

public class JSCmdShowCalendar extends ShowCalendar {

    private static final Logger LOGGER = Logger.getLogger(JSCmdShowCalendar.class);
    private DatatypeFactory objDatatypeFactory = null;

    public JSCmdShowCalendar(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    public void setLimit(int i) {
        super.setLimit(BigInteger.valueOf(i));
    }

    private DatatypeFactory datatypeFactoryInstance() {
        final String conMethodName = "JSCmdShowCalendar::datatypeFactoryInstance";
        if (objDatatypeFactory == null) {
            try {
                objDatatypeFactory = DatatypeFactory.newInstance();
            } catch (DatatypeConfigurationException e) {
                LOGGER.error(e.getMessage(), e);
                throw new JobSchedulerException(String.format("%1$s: Can't get instantiate DatatypeFactory", conMethodName), e);
            }
        }
        return objDatatypeFactory;
    }

    public void setFrom(String strDateAndTime) {
        XMLGregorianCalendar objGC = datatypeFactoryInstance().newXMLGregorianCalendar(strDateAndTime);
        super.setFrom(objGC);
    }

    public void setBefore(String strDateAndTime) {
        XMLGregorianCalendar objGC = datatypeFactoryInstance().newXMLGregorianCalendar(strDateAndTime);
        super.setBefore(objGC);
    }

    public Calendar getCalendar() {
        Calendar objCalendar = null;
        objCalendar = this.getAnswer().getCalendar();
        return objCalendar;
    }

}

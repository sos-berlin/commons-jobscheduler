package com.sos.scheduler.model.commands;

import java.math.BigInteger;
import java.util.Map;

import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.objects.Params;
import com.sos.scheduler.model.objects.XmlPayload;

public class JSCmdModifyOrder extends ModifyOrder {

    private final String conClassName = "JSCmdModifyOrder";
    
    public JSCmdModifyOrder(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    /** \brief setParams
     * 
     * \details
     *
     * \return Params
     *
     * @param pstrParamArray
     * @return */
    public Params setParams(String[] pstrParamArray) {

        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::setParams";

        Params objParams = objFactory.setParams(pstrParamArray);
        super.setParams(objParams);

        return objParams;
    } // private Params setParams
    
    public Params setParams(Map<String, String> params) {
        Params objParams = objFactory.setParams(params);
        super.setParams(objParams);
        return objParams;
    }

    /** Sets the value of the xmlPayload property. */
    public void setXmlPayloadIfNotEmpty(XmlPayload value) {
        if (value != null) {
            super.setXmlPayload(value);
        }
    }

    /** Sets the value of the jobChain property. */
    public void setJobChainIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setJobChain(value);
        }
    }

    /** Sets the value of the order property. */
    public void setOrderIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setOrder(value);
        }
    }

    /** Sets the value of the priority property. */
    public void setPriorityIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            BigInteger p = new BigInteger(value);
            super.setPriority(p);
        }
    }

    /** Sets the value of the title property. */
    public void setTitleIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setTitle(value);
        }
    }

    /** Sets the value of the state property. */
    public void setStateIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setState(value);
        }
    }

    /** Sets the value of the setback property. */
    public void setSetbackIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setSetback(value);
        }
    }

    /** Sets the value of the suspended property. */
    public void setSuspendedIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setSuspended(value);
        }
    }

    /** Sets the value of the at property. */
    public void setAtIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setAt(value);
        }
    }

    /** Sets the value of the endState property. */
    public void setEndStateIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setEndState(value);
        }
    }

    /** Sets the value of the action property. */
    public void setActionIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setAction(value);
        }
    }

}

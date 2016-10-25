package com.sos.scheduler.model.commands;

import java.math.BigInteger;
import java.util.Map;

import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.objects.JSObjOrder;
import com.sos.scheduler.model.objects.Params;

public class JSCmdAddOrder extends JSObjOrder {

    private final String conClassName = "JSCmdAddOrder";

    public JSCmdAddOrder(SchedulerObjectFactory schedulerObjectFactory) {
        super(schedulerObjectFactory, "add_order");
    }

    public void setJobChainIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setJobChain(value);
        }
    }

    public void setIdIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setId(value);
        }
    }

    public void setWebService(String value) {
        if (!isEmpty(value)) {
            super.setWebService(value);
        }
    }

    public void setAtIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setAt(value);
        }
    }

    public void setEndState(String value) {
        if (!isEmpty(value)) {
            super.setEndState(value);
        }
    }

    public void setPriorityIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            BigInteger p = new BigInteger(value);
            super.setPriority(p);
        }
    }

    public void setStateIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setState(value);
        }
    }

    public void setTitleIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setTitle(value);
        }
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

}

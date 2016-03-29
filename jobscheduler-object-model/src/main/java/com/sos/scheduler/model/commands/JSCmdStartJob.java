package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.answers.Ok;
import com.sos.scheduler.model.answers.Task;
import com.sos.scheduler.model.objects.Params;

public class JSCmdStartJob extends StartJob {

    public JSCmdStartJob(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    public Task getTask() {
        Task objTask = null;
        Ok objOK = this.getAnswer().getOk();
        if (objOK != null) {
            objTask = objOK.getTask();
        }
        return objTask;
    }

    public Params setParams(String[] pstrParamArray) {
        Params objParams = objFactory.setParams(pstrParamArray);
        super.setParams(objParams);
        return objParams;
    }

    public void setJobIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setJob(value);
        }
    }

    public void setForceIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setForce(value);
        }
    }

    public void setAtIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setAt(at);
        }
    }

    public void setNameIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setName(value);
        }
    }

    public void setForce(final boolean pflgForce) {
        if (pflgForce) {
            super.setForce("yes");
        } else {
            super.setForce("no");
        }
    }

}
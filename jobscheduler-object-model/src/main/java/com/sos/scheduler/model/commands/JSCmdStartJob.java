package com.sos.scheduler.model.commands;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.answers.Ok;
import com.sos.scheduler.model.answers.Task;
import com.sos.scheduler.model.objects.Params;

 

public class JSCmdStartJob extends StartJob {

	private final String		conClassName	= "JSCmdStartJob";
	@SuppressWarnings("unused")
	private static final Logger	logger			= Logger.getLogger(JSCmdStartJob.class);

	public JSCmdStartJob(SchedulerObjectFactory schedulerObjectFactory) {
		super();
		objFactory = schedulerObjectFactory;
	}

	/**
	 * 
	 * \brief getTask
	 * 
	 * \details
	 *
	 * \return Task
	 *
	 * @return
	 */
	public Task getTask() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getTask";
		Task objTask = null;
		Ok objOK = this.getAnswer().getOk();

		if (objOK != null)
			objTask = objOK.getTask();

		return objTask;
	} // private Task getTask

	/**
	 * 
	 * \brief setParams
	 * 
	 * \details
	 *
	 * \return Params
	 *
	 * @param pstrParamArray
	 * @return
	 */
	public Params setParams(String[] pstrParamArray) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setParams";

		Params objParams = objFactory.setParams(pstrParamArray);
		super.setParams(objParams);

		return objParams;
	} // private Params setParams

	// @Override
	// public Object unMarshal(File pobjFile) {
	// return objFactory.unMarshall(pobjFile);
	// }
	//
	// @Override
	// public Object marshal(Object objO, File objF) {
	// return objFactory.marshal(objO, objF);
	// }
	//
	// @Override
	// public String toXMLString(Object objO) {
	// return objFactory.toXMLString(objO);
	// }
	//
	// @Override
	// public String toXMLString() {
	// return objFactory.toXMLString(this);
	// }
	//
	// @Override
	// public void run() {
	// objFactory.run(this);
	// }

	
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

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setForce";

		if (pflgForce) {
			super.setForce("yes");
		}
		else {
			super.setForce("no");
		}

	} // private void setForce
}

package com.sos.scheduler.model.commands;

import java.math.BigInteger;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.answers.Job;

 
public class JSCmdShowJob extends ShowJob {

	private final String		conClassName	= "JSCmdShowJob";
	@SuppressWarnings("unused")
	private static final Logger	logger			= Logger.getLogger(JSCmdShowJob.class);

	public static enum enu4What {
		task_queue, job_params, job_orders, job_commands, description, log, run_time, task_history, source

		/**/;
		public String Text() {
			String strT = this.name();
			return strT;
		}
	}

	public JSCmdShowJob(SchedulerObjectFactory schedulerObjectFactory) {
		super();
		objFactory = schedulerObjectFactory;
	}

	/**
	 * 
	 * \brief setWhat
	 * 
	 * \details
	 *
	 * @param penuT
	 */
	public void setWhat(enu4What penuT) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setWhat";

		super.setWhat(penuT.Text());

	} // private void setWhat

	/**
	 * 
	 * \brief setWhat
	 * 
	 * \details
	 *
	 * @param penuT
	 */
	public void setWhat(enu4What[] penuT) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setWhat";

		String strT = "";
		for (enu4What enuState4What : penuT) {
			strT += enuState4What.Text() + " ";
		}
		super.setWhat(strT);

	} // private JSCmdShowTask setWhat

	public Job getJobAnswer() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getAnswer";

		Job objAnswer = null;

		return objAnswer;
	} // private Job getAnswer

	public JSCmdShowJob MaxOrders(final int pintMaxOrders) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::MaxOrders";

		super.setMaxOrders(BigInteger.valueOf(pintMaxOrders));

		return this;
	} // private this MaxOrders

	public JSCmdShowJob MaxTaskHistory(final int pintMaxTaskHistory) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::MaxTaskHistory";

		super.setMaxTaskHistory(BigInteger.valueOf(pintMaxTaskHistory));

		return this;
	} // private this MaxTaskHistory

	/**
	 * \brief getjobName
	 *
	 * \details
	 * getter 
	 *
	 * @return the jobName
	 */
	public String getJobName() {
		return super.getJob();
	}

	/**
	 * \brief setjobName - 
	 *
	 * \details
	 * setter 
	 *
	 * @param jobName the value for jobName to set
	 */
	public void setJobName(String jobName) {
		super.setJob(jobName);
	}

}

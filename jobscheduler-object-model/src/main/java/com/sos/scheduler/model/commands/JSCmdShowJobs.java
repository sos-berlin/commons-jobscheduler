package com.sos.scheduler.model.commands;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.SchedulerObjectFactory.enu4What;

 

public class JSCmdShowJobs extends ShowJobs {

	private final String		conClassName	= "JSCmdShowJobs";
	@SuppressWarnings("unused")
	private static final Logger	logger			= Logger.getLogger(JSCmdShowJobs.class);

	public JSCmdShowJobs(SchedulerObjectFactory schedulerObjectFactory) {
		super();
		objFactory = schedulerObjectFactory;
	}
 
	public com.sos.scheduler.model.answers.Jobs getJobs() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getJobs";
		com.sos.scheduler.model.answers.Jobs objJobs = null;
		objJobs = this.getAnswer().getJobs();
//		objJobs.setParent(objFactory);
		return objJobs;
	} // private Calendar getJobs

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
		final String	conMethodName	= conClassName + "::setWhat";
	
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
	public void setWhat(enu4What... penuT) {
		
		@SuppressWarnings("unused")
		final String	conMethodName	= conClassName + "::setWhat";
		
		String strT = "";
		for (enu4What enuState4What : penuT) {
			strT += enuState4What.Text() + " ";
		}
		super.setWhat(strT); 
		
	} // private void setWhat

	
}

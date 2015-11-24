package com.sos.scheduler.model.commands;

import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.objects.JSObjBase;

 
public class JSCmdSubsystemShow extends SubsystemShow {

	private final String		conClassName	= "JSCmdSubsystemShow";
	@SuppressWarnings("unused")
	private static final Logger	logger			= Logger.getLogger(JSCmdSubsystemShow.class);
	
	public enum enu4What {
    	STATISTICS
    	/**/;
		public String Text() {
			String strT = this.name().toLowerCase();
			return strT;
		}
	}

	@SuppressWarnings("unchecked")
	public JSCmdSubsystemShow (SchedulerObjectFactory schedulerObjectFactory) {
		super();
		objFactory = schedulerObjectFactory;
		objJAXBElement = (JAXBElement<JSObjBase>) unMarshal("<subsystem.show/>");
		setObjectFieldsFrom((SubsystemShow) objJAXBElement.getValue());
	}
	
 
	public void setWhat(enu4What penuT) {
	
		@SuppressWarnings("unused")
		final String	conMethodName	= conClassName + "::setWhat";
	
		super.setWhat(penuT.Text()); 
	
	} // public void setWhat
 
	public void setWhat(enu4What[] penuT) {
		
		@SuppressWarnings("unused")
		final String	conMethodName	= conClassName + "::setWhat";
		
		String strT = "";
		for (enu4What enuState4What : penuT) {
			strT += enuState4What.Text() + " ";
		}
		super.setWhat(strT); 
		
	} // public void setWhat
	
}

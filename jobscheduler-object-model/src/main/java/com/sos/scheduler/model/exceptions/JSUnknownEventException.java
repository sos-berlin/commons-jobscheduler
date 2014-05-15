package com.sos.scheduler.model.exceptions;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/**
 * \file JSUnknownEventException.java
 * \brief 
 *  
 * \class JSUnknownEventException
 * \brief 
 * 
 * \details
 *
 * \code
  \endcode
 *
 * \author ss
 * \version 1.0 - 09.05.2011 16:44:08
 * <div class="sos_branding">
 *   <p>(c) 2011 SOS GmbH - Berlin (<a style='color:silver' href='http://www.sos-berlin.com'>http://www.sos-berlin.com</a>)</p>
 * </div>
 */
public class JSUnknownEventException extends JobSchedulerException {

	private static final long serialVersionUID = 162451490637611372L;
	@SuppressWarnings("unused")
	private final String		conClassName	= "JSUnknownEventException";
	@SuppressWarnings("unused")
	private static final Logger	logger					= Logger.getLogger(JSUnknownEventException.class);

	public JSUnknownEventException () {
		//
	}

	/**
	 * \brief JSCommandErrorException
	 *
	 * \details
	 *
	 * @param pstrMessage
	 */
	public JSUnknownEventException(String pstrMessage) {
		super(pstrMessage);
		// TODO Auto-generated constructor stub
	}

	/**
	 * \brief JSCommandErrorException
	 *
	 * \details
	 *
	 * @param pstrMessage
	 * @param e
	 */
	public JSUnknownEventException(String pstrMessage, Exception e) {
		super(pstrMessage, e);
		// TODO Auto-generated constructor stub
	}
}

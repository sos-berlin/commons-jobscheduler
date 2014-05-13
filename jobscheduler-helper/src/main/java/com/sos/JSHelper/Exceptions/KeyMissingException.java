package com.sos.JSHelper.Exceptions;

/*
* <p>
* Title: Exception class representing the error during processing
* </p>
* <p>
* Description:
* </p>
* <p>
* Copyright: Copyright (c) 2005
* </p>
* <p>
* Company: SOS GmbH
* </p>
* 
* @author <a href="mailto:ghassan.beydoun@sos-berlin.com">Ghassan Beydoun</a>
* @version $Id$
*/
public class KeyMissingException extends JobSchedulerException {

    /**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	/**
     * Construtor with message.
     *
     * @param message the message of the exception
     */
    public KeyMissingException(String message) {
        super(message);
        this.Status(JobSchedulerException.PENDING);		        
        this.eMailSubject("required settings-entry not found.");        		
    }

    
}

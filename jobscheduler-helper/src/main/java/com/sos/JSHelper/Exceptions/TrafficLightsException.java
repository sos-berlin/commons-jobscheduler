package com.sos.JSHelper.Exceptions;

/*
 * <p> Title: Exception class representing the error during processing </p> <p>
 * Description: </p> <p> Copyright: Copyright (c) 2005 </p> <p> Company: SOS
 * GmbH </p>
 * @author <a href="mailto:ghassan.beydoun@sos-berlin.com">Ghassan Beydoun</a>
 * @version $Id$
 */
public class TrafficLightsException extends JobSchedulerException {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    /** Constructor with message.
     *
     * @param pstrMessage the message of the exception */
    public TrafficLightsException(final String pstrMessage) {
        super(pstrMessage);
        this.message(pstrMessage);
        this.setStatus(JobSchedulerException.WARNING);
        this.setCategory(CategoryFileTransfer);
        this.setType(TypeTL);
        this.eMailSubject("Problems with Traffic-Lights: " + pstrMessage);
    }

    /** Constructor without message (Standard-Message is taken). */
    public TrafficLightsException() {
        this("file not available..the traffic light must be set to green..giving up :-(");
    }

}

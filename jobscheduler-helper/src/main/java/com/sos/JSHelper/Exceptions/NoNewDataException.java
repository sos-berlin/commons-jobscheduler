package com.sos.JSHelper.Exceptions;

/*
 * <p> Title: Exception class representing the error during processing </p> <p>
 * Description: </p> <p> Copyright: Copyright (c) 2007 </p> <p> Company: APL
 * Software GmbH </p>
 * @author <a href="mailto:rainer.buhl@schering.de">Rainer Buhl</a>
 * @version $Id$
 */
public class NoNewDataException extends JobSchedulerException {

    /**
	 * 
	 */
    private static final long serialVersionUID = -8636032062881811019L;

    /** Construtor with message.
     *
     * @param pstrMessage the message of the exception */
    public NoNewDataException(String pstrMessage) {
        super(pstrMessage);
        // this.Status(DataswitchException.PENDING);
        this.setStatus(JobSchedulerException.WARNING);
        // this.Category(CategoryJobStart);
        // this.Typ(CategoryJobStart);

    }

    /** Construtor without message. */
    public NoNewDataException() {
        this("No new Data available");
    }

}

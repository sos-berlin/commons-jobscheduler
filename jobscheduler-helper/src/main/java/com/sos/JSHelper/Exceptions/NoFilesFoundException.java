package com.sos.JSHelper.Exceptions;

/*
 * <p> Title: Exception class representing the error during processing </p> <p>
 * Description: </p> <p> Copyright: Copyright (c) 2005 </p> <p> Company: SOS
 * GmbH </p>
 * @author <a href="mailto:ghassan.beydoun@sos-berlin.com">Ghassan Beydoun</a>
 * @version $Id$
 */
public class NoFilesFoundException extends JobSchedulerException {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1203178426931970420L;

    /** Construtor with message.
     *
     * @param pstrMessage the message of the exception */
    public NoFilesFoundException(String pstrMessage) {
        super(pstrMessage);
        this.Status(JobSchedulerException.PENDING);
        // this.Category(CategoryOptions);
    }

    /** Construtor with message. */
    public NoFilesFoundException() {
        super("No Files found on the server.");
    }

}

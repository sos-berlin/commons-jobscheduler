package com.sos.JSHelper.Exceptions;

/*
* <p>
* Title: Exception class representing the error during processing
* </p>
* <p>
* Description:
* </p>
* <p>
* Copyright: Copyright (c) 2007
* </p>
* <p>
* Company: APL Software GmbH
* </p>
*
* @author <a href="mailto:rainer.buhl@schering.de">Rainer Buhl</a>
* @version $Id$
*/
public class EmptyFileException extends JobSchedulerException {

	private static final long serialVersionUID = -693689563280193120L;

	/**
     * Constructor with message.
     *
     * @param message the message of the exception
     */
    public EmptyFileException(final String message) {
        super(message);
        int i;
        this.Status(JobSchedulerException.ERROR);
        this.Category(CategoryFileHandling);
    }

    /**
     * Construtor without message.
     *
     */
    public EmptyFileException() {
        this("The file is empty");
    }

}

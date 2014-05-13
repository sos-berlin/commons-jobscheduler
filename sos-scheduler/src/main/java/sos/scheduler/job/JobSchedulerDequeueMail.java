package sos.scheduler.job;

import sos.spooler.Job_impl;


/**
 * @author andreas.pueschel@sos-berlin.com
 *
 * dequeue previously stored mails and try to send them
 */
public class JobSchedulerDequeueMail extends Job_impl {

    /** Attribut numOfMails: number of mails currently in queue */
    private int numOfMails = 0;
    
    
	public boolean spooler_open() {
    	
		this.numOfMails = spooler_log.mail().dequeue();

		return (this.numOfMails > 0);
	}

	
	public boolean spooler_process() {
    	
		if ( this.numOfMails > 0 ) {
			spooler_log.info( this.numOfMails + " previously stored mails were sent, mail queue is empty" );
		}

		return false;
	}

}
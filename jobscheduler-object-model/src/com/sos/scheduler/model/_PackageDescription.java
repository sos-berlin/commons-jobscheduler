package com.sos.scheduler.model;
// $Id$
/**
 * \package com.sos.scheduler.model 
 * 
 * \brief JobScheduler - external API Framework
 * 
 * The external API of the SOSScheduler is used to communicate via TCP, telnet or HTTP with the SOSScheduler. 
 * The reason why it is named "external" API is to distinguish it from the "internal" API, which is used to
 * interact from inside a job (coded in Java).
 * 
 * The "external" API is divided into four parts
 * <li>
 * to model the objects of SOSScheduler,
 * </li>
 * <li>
 * to control the SOSScheduler by commands, like "start_job", "kill" or something like this
 * </li>
 * <li>
 * the response, which is SOSScheduler returned, when a command was sent to the scheduler.
 * </li>
 * <li>
 * the events published by the scheduler kernel.
 * </li>
 * 
 */


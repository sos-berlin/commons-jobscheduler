package com.sos.scheduler.model.objects;

import com.sos.scheduler.model.SchedulerObjectFactory;

/** \class JSObjEnvironment
 * 
 * \brief JSObjEnvironment -
 * 
 * \details
 *
 * \section JSObjEnvironment.java_intro_sec Introduction
 *
 * \section JSObjEnvironment.java_samples Some Samples
 *
 * \code .... code goes here ... \endcode
 *
 * <p style="text-align:center">
 * <br />
 * --------------------------------------------------------------------------- <br />
 * APL/Software GmbH - Berlin <br />
 * ##### generated by ClaviusXPress (http://www.sos-berlin.com) ######### <br />
 * ---------------------------------------------------------------------------
 * </p>
 * \author oh
 * 
 * @version $Id$ \see reference
 *
 *          Created on 08.02.2011 11:56:01 */

/** @author oh */
public class JSObjEnvironment extends Environment {

    @SuppressWarnings("unused")
    private final String conClassName = "JSObjEnvironment";

    public JSObjEnvironment(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}

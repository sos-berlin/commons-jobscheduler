package com.sos.scheduler.model.objects;

import com.sos.scheduler.model.SchedulerObjectFactory;

/** \class JSObjParam
 * 
 * \brief JSObjParam -
 * 
 * \details
 *
 * \section JSObjParam.java_intro_sec Introduction
 *
 * \section JSObjParam.java_samples Some Samples
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
 *          Created on 09.02.2011 15:05:55 */

/** @author oh */
public class JSObjParam extends Param {

    @SuppressWarnings("unused")
    private final String conClassName = "JSObjParam";

    public JSObjParam(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    public JSObjParam(SchedulerObjectFactory schedulerObjectFactory, Param param) {
        objFactory = schedulerObjectFactory;
        setObjectFieldsFrom(param);
    }

}

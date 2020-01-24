package com.sos.scheduler.model.objects;

import javax.xml.bind.JAXBElement;

import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.scheduler.model.SchedulerObjectFactory;

/** \class JSObjSchedule
 * 
 * \brief JSObjSchedule -
 * 
 * \details
 *
 * \section JSObjSchedule.java_intro_sec Introduction
 *
 * \section JSObjSchedule.java_samples Some Samples
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
 *          Created on 22.02.2011 11:58:53 */

/** @author oh */
public class JSObjSchedule extends RunTime {

    @SuppressWarnings("unused")
    private final String conClassName = "JSObjSchedule";

    public final static String fileNameExtension = ".schedule.xml";

    public JSObjSchedule(SchedulerObjectFactory schedulerObjectFactory) {
        objFactory = schedulerObjectFactory;
    }

    public JSObjSchedule(SchedulerObjectFactory schedulerObjectFactory, RunTime origOrder) {
        objFactory = schedulerObjectFactory;
        setObjectFieldsFrom(origOrder);
    }

    @SuppressWarnings("unchecked")
    public JSObjSchedule(SchedulerObjectFactory schedulerObjectFactory, ISOSVirtualFile pobjVirtualFile) {
        objFactory = schedulerObjectFactory;
        this.objJAXBElement = (JAXBElement<JSObjBase>) unMarshal(pobjVirtualFile);
        setObjectFieldsFrom((RunTime) this.objJAXBElement.getValue());
        setHotFolderSrc(pobjVirtualFile);
    }
}

package com.sos.scheduler.model;

import com.sos.scheduler.model.objects.JSObjOrder;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

/** \file LiveConnectorTest.java \brief Samples for access to the live folder
 * components
 * 
 * \class LiveConnectorTest \brief Samples for access to the live folder
 * components
 * 
 * \details The LiveConnector provides a simple way to read the components
 * (order, jobs etc.) of JobScheduler. It is not necessary that the JobScheduler
 * instance is running.
 *
 * \code \endcode
 *
 * \author schaedi \version 1.0 - 05.05.2012 18:05:32 <div class="sos_branding">
 * <p>
 * © 2010 SOS GmbH - Berlin (<a style='color:silver'
 * href='http://www.sos-berlin.com'>http://www.sos-berlin.com</a>)
 * </p>
 * </div> */
public class LiveConnectorTest extends TestBase {

    private final static Logger LOGGER = LoggerFactory.getLogger(LiveConnectorTest.class);
    private static SchedulerObjectFactory factory = null;

    private static final String liveFolder = "/testdata/connector/";
    private static final HashSet<String> expected = new HashSet<String>();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        factory = new SchedulerObjectFactory();
        expected.add(liveFolder + "chain,order1.order.xml");
        expected.add(liveFolder + "folder1/chain,order1.order.xml");
        expected.add(liveFolder + "folder2/chain,order1.order.xml");
        expected.add(liveFolder + "folder2/folder2.1/chain,order1.order.xml");
    }

   

}

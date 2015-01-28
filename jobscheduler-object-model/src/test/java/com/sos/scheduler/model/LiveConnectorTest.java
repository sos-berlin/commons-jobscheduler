package com.sos.scheduler.model;

import com.sos.scheduler.model.objects.JSObjOrder;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * \file LiveConnectorTest.java
 * \brief Samples for access to the live folder components 
 *  
 * \class LiveConnectorTest
 * \brief Samples for access to the live folder components 
 * 
 * \details
 * The LiveConnector provides a simple way to read the components (order, jobs etc.) of JobScheduler. It is not necessary
 * that the JobScheduler instance is running.
 *
 * \code
  \endcode
 *
 * \author schaedi
 * \version 1.0 - 05.05.2012 18:05:32
 * <div class="sos_branding">
 *   <p>© 2010 SOS GmbH - Berlin (<a style='color:silver' href='http://www.sos-berlin.com'>http://www.sos-berlin.com</a>)</p>
 * </div>
 */
public class LiveConnectorTest extends TestBase {
	
	private final static Logger logger = Logger.getLogger(LiveConnectorTest.class);
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
	
	@Test
  @Ignore("Test set to Ignore for later examination")
	public final void testLocal() throws MalformedURLException {
		LiveConnector connector = new LiveConnector( getLiveFolder() );
		SchedulerHotFolder hotFolder = factory.createSchedulerHotFolder(connector.getHotFolderHandle());
		SchedulerHotFolderFileList fileList = hotFolder.loadRecursive();
		List<JSObjOrder> orders = fileList.getOrderList();
		for(JSObjOrder order : orders) {
			logger.debug(order.getHotFolderSrc());
		}
	}
	
	@Test
	@Ignore("Test set to Ignore for later examination, fails in Jenkins build")
	public final void testFTP() throws MalformedURLException {
		final String liveFolderName = "ftp://Administrator@8of9.sos:21" + liveFolder;
		logger.debug("liveFolderName=" + liveFolderName);
		LiveConnector connector = new LiveConnector( new URL(liveFolderName) );
		SchedulerHotFolder hotFolder = factory.createSchedulerHotFolder(connector.getHotFolderHandle());
		SchedulerHotFolderFileList fileList = hotFolder.loadRecursive();
		
		List<JSObjOrder> orders = fileList.getOrderList();
		for(int i=0; i < orders.size(); i++) {
			String name = orders.get(0).getHotFolderSrc().getName();
			assertEquals(name + " not expected",true,expected.contains(name));
		}
	}
	
}

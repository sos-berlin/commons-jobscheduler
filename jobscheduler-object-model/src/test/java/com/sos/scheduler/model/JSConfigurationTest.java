package com.sos.scheduler.model;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;
import com.sos.scheduler.model.objects.Spooler;

/** @author oh */
public class JSConfigurationTest {

    private static final Logger LOGGER = Logger.getLogger(JSConfigurationTest.class);
    private static SchedulerObjectFactory objFactory = null;
    private ISOSVFSHandler objVFS = null;
    private ISOSVfsFileTransfer objFileSystemHandler = null;
    private SOSFTPOptions objOptions = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        LOGGER.debug("test start");
        objFactory = new SchedulerObjectFactory("galadriel.sos", 4412);
        objFactory.initMarshaller(Spooler.class);
    }

    private final void prepareLocalVfs() {
        try {
            objVFS = VFSFactory.getHandler("local");
            objFileSystemHandler = (ISOSVfsFileTransfer) objVFS;
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    private final void prepareFtpVfs() {
        objOptions = new SOSFTPOptions();
        objOptions.host.setValue("galadriel.sos");
        objOptions.user.setValue("sos");
        objOptions.password.setValue("sos");
        try {
            objVFS = VFSFactory.getHandler(objOptions.protocol.getValue());
            objVFS.connect(objOptions);
            objVFS.authenticate(objOptions);
            objFileSystemHandler = (ISOSVfsFileTransfer) objVFS;
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    @Test
    public final void loadSchedulerXMLLocal() {
        prepareLocalVfs();
        String strTestHotFolder = "src/test/resources/scheduler.xml";
        ISOSVirtualFile pobjVirtualFile = objFileSystemHandler.getFileHandle(strTestHotFolder);
        JSConfiguration objJSConf = objFactory.createJSConfiguration(pobjVirtualFile);
        LOGGER.info(objJSConf.toXMLString());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public final void loadSchedulerXMLFTP() {
        prepareFtpVfs();
        String strTestHotFolder = "src/test/resources/scheduler.xml";
        ISOSVirtualFile pobjVirtualFile = objFileSystemHandler.getFileHandle(strTestHotFolder);
        JSConfiguration objJSConf = objFactory.createJSConfiguration(pobjVirtualFile);
        LOGGER.info(objJSConf.toXMLString());
    }

}
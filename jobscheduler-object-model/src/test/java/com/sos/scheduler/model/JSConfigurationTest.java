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

    public JSConfigurationTest() {
        //
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        LOGGER.debug("test start");
        objFactory = new SchedulerObjectFactory("8of9.sos", 4210);
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
        objOptions.host.setValue("8of9.sos");
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
    @Ignore("Test set to Ignore for later examination")
    public final void loadSchedulerXMLLocal() {
        prepareLocalVfs();
        String strTestHotFolder = "Z:/8of9_buildjars_4210/config/scheduler.xml";
        ISOSVirtualFile pobjVirtualFile = objFileSystemHandler.getFileHandle(strTestHotFolder);
        JSConfiguration objJSConf = objFactory.createJSConfiguration(pobjVirtualFile);
        LOGGER.info(objJSConf.toXMLString());
    }

    @Test
    public final void loadSchedulerXMLFTP() {
        prepareFtpVfs();
        String strTestHotFolder = "/8of9_buildjars_4210/config/scheduler.xml";
        ISOSVirtualFile pobjVirtualFile = objFileSystemHandler.getFileHandle(strTestHotFolder);
        JSConfiguration objJSConf = objFactory.createJSConfiguration(pobjVirtualFile);
        LOGGER.info(objJSConf.toXMLString());
    }

}
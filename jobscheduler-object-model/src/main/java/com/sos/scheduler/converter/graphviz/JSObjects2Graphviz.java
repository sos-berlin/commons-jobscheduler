package com.sos.scheduler.converter.graphviz;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.graphviz.enums.FileType;
import com.sos.scheduler.model.SchedulerHotFolder;
import com.sos.scheduler.model.SchedulerHotFolderFileList;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.objects.*;
import org.apache.log4j.Logger;
import java.io.File;

import static com.sos.scheduler.model.messages.JSMessages.*;

public class JSObjects2Graphviz extends JSJobUtilitiesClass<JSObjects2GraphvizOptions> {

    private static final String CLASSNAME = "JSObjects2Graphviz";
    private static final Logger LOGGER = Logger.getLogger(JSObjects2Graphviz.class);
    private ISOSVFSHandler objVFS = null;
    private ISOSVfsFileTransfer objFileSystemHandler = null;
    private SchedulerObjectFactory objFactory = null;
    private SchedulerHotFolderFileList objSchedulerHotFolderFileList = null;
    private String strOutputFolderName = "";

    public JSObjects2Graphviz() {
        super(new JSObjects2GraphvizOptions());
    }

    public JSObjects2Graphviz initialize() throws Exception {
        getOptions().checkMandatory();
        LOGGER.debug(getOptions().dirtyString());
        String liveFolderName = objOptions.live_folder_name.Value();
        objVFS = VFSFactory.getHandler("local");
        objFileSystemHandler = (ISOSVfsFileTransfer) objVFS;
        objFactory = new SchedulerObjectFactory();
        objFactory.initMarshaller(Spooler.class);
        ISOSVirtualFile objHotFolder = objFileSystemHandler.getFileHandle(liveFolderName);
        SchedulerHotFolder objSchedulerHotFolder = objFactory.createSchedulerHotFolder(objHotFolder);
        LOGGER.info(String.format("... load %1$s", liveFolderName));
        objSchedulerHotFolderFileList = objSchedulerHotFolder.loadRecursive();
        strOutputFolderName = objOptions.output_folder_name.Value();
        return this;
    }

    public JSObjects2Graphviz Execute() throws Exception {
        final String methodName = CLASSNAME + "::Execute";
        JOM_I_110.toLog(methodName);
        try {
            initialize();
            LOGGER.debug(String.format("%s job chains found", objSchedulerHotFolderFileList.getSortedFileList().size()));
            for (JSObjBase obj : objSchedulerHotFolderFileList.getSortedFileList()) {
                if (obj instanceof JSObjJobChain) {
                    JSObjJobChain jsObjJobChain = (JSObjJobChain) obj;
                    jsObjJobChain.setGraphVizImageType(FileType.pdf);
                    jsObjJobChain.setDotOutputPath(strOutputFolderName);
                    LOGGER.info(String.format("... call generator %1$s", strOutputFolderName));
                    jsObjJobChain.createGraphVizImageFile(new File(objOptions.live_folder_name.Value()), true);
                }
            }
        } catch (Exception e) {
            throw new JobSchedulerException(JOM_F_107.get(methodName) + ":" + e.getMessage(), e);
        }
        JOM_I_111.toLog(methodName);
        return this;
    }

}
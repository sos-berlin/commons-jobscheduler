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

/** \class JSObjects2Graphviz - Workerclass for "JSObjects2Graphviz"
 *
 * \brief AdapterClass of JSObjects2Graphviz for the SOSJobScheduler
 *
 * This Class JSObjects2Graphviz is the worker-class.
 *
 * 
 *
 * see \see
 * C:\Users\KB\AppData\Local\Temp\scheduler_editor-2781494595910967227.html for
 * (more) details.
 *
 * \verbatim ; mechanicaly created by
 * C:\Users\KB\sos-berlin.com\jobscheduler\scheduler
 * \config\JOETemplates\java\xsl\JSJobDoc2JSWorkerClass.xsl from
 * http://www.sos-berlin.com at 20121108150924 \endverbatim */
public class JSObjects2Graphviz extends JSJobUtilitiesClass<JSObjects2GraphvizOptions> {

    private final String conClassName = "JSObjects2Graphviz";											//$NON-NLS-1$
    private static Logger logger = Logger.getLogger(JSObjects2Graphviz.class);
    @SuppressWarnings("unused")
    private final String conSVNVersion = "$Id$";

    private ISOSVFSHandler objVFS = null;
    private ISOSVfsFileTransfer objFileSystemHandler = null;
    private SchedulerObjectFactory objFactory = null;
    private SchedulerHotFolderFileList objSchedulerHotFolderFileList = null;
    private String strOutputFolderName = "";

    /** \brief JSObjects2Graphviz
     *
     * \details */
    public JSObjects2Graphviz() {
        super(new JSObjects2GraphvizOptions());
    }

    public JSObjects2Graphviz initialize() throws Exception {

        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::initialize";

        getOptions().CheckMandatory();
        logger.debug(getOptions().dirtyString());

        String liveFolderName = objOptions.live_folder_name.Value();
        objVFS = VFSFactory.getHandler("local");
        // objVFS = VFSFactory.getHandler(strTestHotFolder);
        objFileSystemHandler = (ISOSVfsFileTransfer) objVFS;

        objFactory = new SchedulerObjectFactory();
        objFactory.initMarshaller(Spooler.class);

        ISOSVirtualFile objHotFolder = objFileSystemHandler.getFileHandle(liveFolderName);
        SchedulerHotFolder objSchedulerHotFolder = objFactory.createSchedulerHotFolder(objHotFolder);
        logger.info(String.format("... load %1$s", liveFolderName));
        objSchedulerHotFolderFileList = objSchedulerHotFolder.loadRecursive();
        strOutputFolderName = objOptions.output_folder_name.Value();

        return this;
    } // private JSObjects2Graphviz initialize

    /** \brief Execute - Start the Execution of JSObjects2Graphviz
     *
     * \details
     *
     * For more details see
     *
     * \see JobSchedulerAdapterClass \see JSObjects2GraphvizMain
     *
     * \return JSObjects2Graphviz
     *
     * @return */
    public JSObjects2Graphviz Execute() throws Exception {
        final String conMethodName = conClassName + "::Execute"; //$NON-NLS-1$

        JOM_I_110.toLog(conMethodName);

        try {
            initialize();
            logger.debug(String.format("%s job chains found", objSchedulerHotFolderFileList.getSortedFileList().size()));
            for (JSObjBase obj : objSchedulerHotFolderFileList.getSortedFileList()) {
                if (obj instanceof JSObjJobChain) {
                    JSObjJobChain jsObjJobChain = (JSObjJobChain) obj;
                    jsObjJobChain.setGraphVizImageType(FileType.pdf);
                    jsObjJobChain.setDotOutputPath(strOutputFolderName);
                    logger.info(String.format("... call generator %1$s", strOutputFolderName));

                    jsObjJobChain.createGraphVizImageFile(new File(objOptions.live_folder_name.Value()), true);
                }
            }
        } catch (Exception e) {
            throw new JobSchedulerException(JOM_F_107.get(conMethodName) + ":" + e.getMessage(), e);
        } finally {
        }

        JOM_I_111.toLog(conMethodName);

        return this;
    }

} // class JSObjects2Graphviz
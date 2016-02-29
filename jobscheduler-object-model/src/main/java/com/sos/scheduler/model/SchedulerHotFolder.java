package com.sos.scheduler.model;

import java.util.List;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.scheduler.model.objects.JSObjBase;
import com.sos.scheduler.model.objects.JSObjJob;
import com.sos.scheduler.model.objects.JSObjJobChain;
import com.sos.scheduler.model.objects.JSObjLock;
import com.sos.scheduler.model.objects.JSObjOrder;
import com.sos.scheduler.model.objects.JSObjParams;
import com.sos.scheduler.model.objects.JSObjProcessClass;
import com.sos.scheduler.model.objects.JSObjSchedule;

/** @author oh */
public class SchedulerHotFolder extends JSObjBase {

    private static final Logger LOGGER = Logger.getLogger(SchedulerHotFolder.class);
    private boolean isLoaded = false;
    private SchedulerHotFolderFileList objHotFolderFileList = new SchedulerHotFolderFileList();

    public SchedulerHotFolder() {
        //
    }

    public SchedulerHotFolder(final SchedulerObjectFactory schedulerObjectFactory) {
        objFactory = schedulerObjectFactory;
    }

    public SchedulerHotFolder(final SchedulerObjectFactory schedulerObjectFactory, final ISOSVirtualFile pobjVirtualFile) {
        objFactory = schedulerObjectFactory;
        setHotFolderSrc(pobjVirtualFile);
    }

    public SchedulerHotFolderFileList load() {
        if (isLoaded) {
            return getHotFolderFileList();
        }
        objHotFolderFileList = load(this.getHotFolderSrc());
        return objHotFolderFileList;
    }

    public SchedulerHotFolderFileList loadRecursive() {
        if (isLoaded) {
            return getHotFolderFileList();
        }
        objHotFolderFileList = loadRecursive(this.getHotFolderSrc());
        return objHotFolderFileList;
    }

    public SchedulerHotFolderFileList refresh() {
        objHotFolderFileList = load(this.getHotFolderSrc());
        return objHotFolderFileList;
    }

    private SchedulerHotFolderFileList loadRecursive(final ISOSVirtualFile pobjVirtualDir) {
        SchedulerHotFolderFileList result = load(pobjVirtualDir);
        List<SchedulerHotFolder> folders = result.getFolderList();
        for (SchedulerHotFolder folder : folders) {
            LOGGER.debug("reading content of " + folder.getHotFolderSrc().getName());
            SchedulerHotFolderFileList fileList = loadRecursive(folder.getHotFolderSrc());
            result.addAll(fileList);
        }
        return result;
    }

    public JSObjJob getJobByName(final String pstrJobName) {
        JSObjJob objJob = null;
        for (JSObjJob objJ : getHotFolderFileList().getJobList()) {
            String strJobName = objJ.getJobName();
            if (strJobName.equalsIgnoreCase(pstrJobName)) {
                objJob = objJ;
                break;
            }
        }
        return objJob;
    }

    private SchedulerHotFolderFileList load(final ISOSVirtualFile pobjVirtualDir) {
        final String conMethodName = "SchedulerHotFolder::load";
        SchedulerHotFolderFileList result = new SchedulerHotFolderFileList();
        try {
            if (!pobjVirtualDir.isDirectory()) {
                throw new JobSchedulerException(String.format("%1$s isn't a directory", pobjVirtualDir.getName()));
            }
        } catch (Exception e) {
            JobSchedulerException objJSException = new JobSchedulerException(conMethodName, e);
            LOGGER.error(objJSException.getMessage(), objJSException);
            throw objJSException;
        }
        ISOSVfsFileTransfer objVFSHandler = pobjVirtualDir.getHandler();
        String[] filenames = objVFSHandler.getFolderlist(pobjVirtualDir.getName(), ".*", 0, false);
        result.setHotFolderSrc(pobjVirtualDir);
        for (String filename : filenames) {
            if (!filename.contains(".svn")) {
                ISOSVirtualFile objVirtualFile1 = objVFSHandler.getFileHandle(filename);
                try {
                    if (objVirtualFile1.isDirectory()) {
                        LOGGER.debug("load SchedulerHotFolder = " + filename);
                        SchedulerHotFolder obj = objFactory.createSchedulerHotFolder(objVirtualFile1);
                        result.add(obj);
                    }
                } catch (Exception e) {
                }
            }
        }
        LOGGER.debug("getFilelist from: " + pobjVirtualDir.getName());
        filenames = objVFSHandler.getFilelist(pobjVirtualDir.getName(), ".*", 0, false, null);
        for (String filename : filenames) {
            ISOSVirtualFile objVirtualFile1 = objVFSHandler.getFileHandle(filename);
            String lowerFilename = filename.toLowerCase();
            try {
                if (objVirtualFile1.isDirectory()) {
                    LOGGER.debug("load SchedulerHotFolder = " + filename);
                    SchedulerHotFolder obj = objFactory.createSchedulerHotFolder(objVirtualFile1);
                    result.add(obj);
                } else if (lowerFilename.endsWith(JSObjJob.fileNameExtension)) {
                    LOGGER.debug("load JSObjJob = " + filename);
                    JSObjJob obj = objFactory.createJob(objVirtualFile1);
                    result.add(obj);
                } else if (lowerFilename.endsWith(JSObjJobChain.fileNameExtension)) {
                    LOGGER.debug("load JSObjJobChain = " + filename);
                    JSObjJobChain obj = objFactory.createJobChain(objVirtualFile1);
                    result.add(obj);
                } else if (lowerFilename.endsWith(JSObjOrder.fileNameExtension)) {
                    LOGGER.debug("load JSObjOrder = " + filename);
                    JSObjOrder obj = objFactory.createOrder(objVirtualFile1);
                    result.add(obj);
                } else if (lowerFilename.endsWith(JSObjLock.fileNameExtension)) {
                    LOGGER.debug("load JSObjLock = " + filename);
                    JSObjLock obj = objFactory.createLock(objVirtualFile1);
                    result.add(obj);
                } else if (lowerFilename.endsWith(JSObjProcessClass.fileNameExtension)) {
                    LOGGER.debug("load JSObjProcessClass = " + filename);
                    JSObjProcessClass obj = objFactory.createProcessClass(objVirtualFile1);
                    result.add(obj);
                } else if (lowerFilename.endsWith(JSObjSchedule.fileNameExtension)) {
                    LOGGER.debug("load JSObjSchedule = " + filename);
                    JSObjSchedule obj = objFactory.createSchedule(objVirtualFile1);
                    result.add(obj);
                } else if (lowerFilename.endsWith(JSObjParams.fileNameExtension)) {
                    LOGGER.debug("load JSObjParams = " + filename);
                    JSObjParams obj = objFactory.createParams(objVirtualFile1);
                    result.add(obj);
                } else {
                    continue;
                }
            } catch (Exception e) {
                JobSchedulerException ex = new JobSchedulerException(conMethodName, e);
                LOGGER.error(ex.getMessage(), ex);
                throw new JobSchedulerException(conMethodName, e);
            }
        }
        isLoaded = true;
        LOGGER.debug(String.format("%1$s objects found in %2$s", result.getFileList().size(), pobjVirtualDir.getName()));
        return result;
    }

    public SchedulerHotFolderFileList getHotFolderFileList() {
        return objHotFolderFileList;
    }
    
}

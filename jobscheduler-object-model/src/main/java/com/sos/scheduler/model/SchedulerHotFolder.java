package com.sos.scheduler.model;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.vfs.common.interfaces.ISOSTransferHandler;
import com.sos.vfs.common.interfaces.ISOSVirtualFile;
import com.sos.vfs.common.SOSFileEntry;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerHotFolder.class);
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

    public SchedulerHotFolderFileList loadOrderObjects() {
        if (isLoaded) {
            return getHotFolderFileList();
        }
        objHotFolderFileList = load(this.getHotFolderSrc(), ".*(" + JSObjOrder.fileNameExtension + ")");
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
        return load(pobjVirtualDir, ".*");
    }

    private SchedulerHotFolderFileList load(final ISOSVirtualFile pobjVirtualDir, String regex) {
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
        ISOSTransferHandler objVFSHandler = pobjVirtualDir.getHandler();
        List<SOSFileEntry> entries = objVFSHandler.getFolderlist(pobjVirtualDir.getName(), ".*", 0, false);
        result.setHotFolderSrc(pobjVirtualDir);
        for (SOSFileEntry entry : entries) {
            if (!entry.getFilename().contains(".svn")) {
                ISOSVirtualFile objVirtualFile1 = objVFSHandler.getFileHandle(entry.getFilename());
                try {
                    if (objVirtualFile1.isDirectory()) {
                        LOGGER.debug("load SchedulerHotFolder = " + entry.getFilename());
                        SchedulerHotFolder obj = objFactory.createSchedulerHotFolder(objVirtualFile1);
                        result.add(obj);
                    }
                } catch (Exception e) {
                    //
                }
            }
        }
        LOGGER.debug("getFilelist from: " + pobjVirtualDir.getName());
        entries = objVFSHandler.getFilelist(pobjVirtualDir.getName(), regex, 0, false, true, null);
        for (SOSFileEntry entry : entries) {
            ISOSVirtualFile objVirtualFile1 = objVFSHandler.getFileHandle(entry.getFilename());
            String lowerFilename = entry.getFilename().toLowerCase();
            try {
                if (objVirtualFile1.isDirectory()) {
                    LOGGER.debug("load SchedulerHotFolder = " + entry.getFilename());
                    SchedulerHotFolder obj = objFactory.createSchedulerHotFolder(objVirtualFile1);
                    result.add(obj);
                } else if (lowerFilename.endsWith(JSObjJob.fileNameExtension)) {
                    LOGGER.debug("load JSObjJob = " + entry.getFilename());
                    JSObjJob obj = objFactory.createJob(objVirtualFile1);
                    result.add(obj);
                } else if (lowerFilename.endsWith(JSObjJobChain.fileNameExtension)) {
                    LOGGER.debug("load JSObjJobChain = " + entry.getFilename());
                    JSObjJobChain obj = objFactory.createJobChain(objVirtualFile1);
                    result.add(obj);
                } else if (lowerFilename.endsWith(JSObjOrder.fileNameExtension)) {
                    LOGGER.debug("load JSObjOrder = " + entry.getFilename());
                    JSObjOrder obj = objFactory.createOrder(objVirtualFile1);
                    result.add(obj);
                } else if (lowerFilename.endsWith(JSObjLock.fileNameExtension)) {
                    LOGGER.debug("load JSObjLock = " + entry.getFilename());
                    JSObjLock obj = objFactory.createLock(objVirtualFile1);
                    result.add(obj);
                } else if (lowerFilename.endsWith(JSObjProcessClass.fileNameExtension)) {
                    LOGGER.debug("load JSObjProcessClass = " + entry.getFilename());
                    JSObjProcessClass obj = objFactory.createProcessClass(objVirtualFile1);
                    result.add(obj);
                } else if (lowerFilename.endsWith(JSObjSchedule.fileNameExtension)) {
                    LOGGER.debug("load JSObjSchedule = " + entry.getFilename());
                    JSObjSchedule obj = objFactory.createSchedule(objVirtualFile1);
                    result.add(obj);
                } else if (lowerFilename.endsWith(JSObjParams.fileNameExtension)) {
                    LOGGER.debug("load JSObjParams = " + entry.getFilename());
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

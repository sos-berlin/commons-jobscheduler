package com.sos.scheduler.model;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.vfs.common.interfaces.ISOSProvider;
import com.sos.vfs.common.interfaces.ISOSProviderFile;
import com.sos.vfs.common.SOSFileEntry;
import com.sos.scheduler.model.objects.JSObjBase;
import com.sos.scheduler.model.objects.JSObjJob;
import com.sos.scheduler.model.objects.JSObjJobChain;
import com.sos.scheduler.model.objects.JSObjLock;
import com.sos.scheduler.model.objects.JSObjOrder;
import com.sos.scheduler.model.objects.JSObjParams;
import com.sos.scheduler.model.objects.JSObjProcessClass;
import com.sos.scheduler.model.objects.JSObjSchedule;

public class SchedulerHotFolder extends JSObjBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerHotFolder.class);
    private boolean isLoaded = false;
    private SchedulerHotFolderFileList hotFolderFileList = new SchedulerHotFolderFileList();

    public SchedulerHotFolder(final SchedulerObjectFactory schedulerObjectFactory, final ISOSProviderFile providerFile) {
        objFactory = schedulerObjectFactory;
        setHotFolderSrc(providerFile);
    }

    // not used (used in com.sos.joe.xml.IOUtils.openHotFolder - but this function is not used)
    public SchedulerHotFolderFileList load() throws Exception {
        if (isLoaded) {
            return getHotFolderFileList();
        }
        hotFolderFileList = load(this.getHotFolderSrc());
        return hotFolderFileList;
    }

    public SchedulerHotFolderFileList loadOrderObjects() throws Exception {
        if (isLoaded) {
            return getHotFolderFileList();
        }
        hotFolderFileList = load(this.getHotFolderSrc(), ".*(" + JSObjOrder.fileNameExtension + ")");
        return hotFolderFileList;
    }

    public SchedulerHotFolderFileList loadRecursive() throws Exception {
        if (isLoaded) {
            return getHotFolderFileList();
        }
        hotFolderFileList = loadRecursive(this.getHotFolderSrc());
        return hotFolderFileList;
    }

    public SchedulerHotFolderFileList refresh() throws Exception {
        hotFolderFileList = load(this.getHotFolderSrc());
        return hotFolderFileList;
    }

    private SchedulerHotFolderFileList loadRecursive(final ISOSProviderFile providerFile) throws Exception {
        SchedulerHotFolderFileList result = load(providerFile);
        List<SchedulerHotFolder> folders = result.getFolderList();
        for (SchedulerHotFolder folder : folders) {
            LOGGER.debug("reading content of " + folder.getHotFolderSrc().getName());
            SchedulerHotFolderFileList fileList = loadRecursive(folder.getHotFolderSrc());
            result.addAll(fileList);
        }
        return result;
    }

    public JSObjJob getJobByName(final String jobName) {
        JSObjJob result = null;
        for (JSObjJob job : getHotFolderFileList().getJobList()) {
            if (job.getJobName().equalsIgnoreCase(jobName)) {
                result = job;
                break;
            }
        }
        return result;
    }

    private SchedulerHotFolderFileList load(final ISOSProviderFile providerFile) throws Exception {
        return load(providerFile, ".*");
    }

    private SchedulerHotFolderFileList load(final ISOSProviderFile providerFile, String regex) throws Exception {
        final String conMethodName = "SchedulerHotFolder::load";
        SchedulerHotFolderFileList result = new SchedulerHotFolderFileList();
        try {
            if (!providerFile.isDirectory()) {
                throw new JobSchedulerException(String.format("%1$s isn't a directory", providerFile.getName()));
            }
        } catch (Exception e) {
            JobSchedulerException exception = new JobSchedulerException(conMethodName, e);
            LOGGER.error(exception.getMessage(), exception);
            throw exception;
        }
        ISOSProvider provider = providerFile.getProvider();
        List<SOSFileEntry> entries = provider.getFolderlist(providerFile.getName(), ".*", 0, false);
        result.setHotFolderSrc(providerFile);
        for (SOSFileEntry entry : entries) {
            if (!entry.getFilename().contains(".svn")) {
                try {
                    if (entry.isDirectory()) {
                        ISOSProviderFile pf = provider.getFile(entry.getFullPath());
                        LOGGER.debug("load SchedulerHotFolder = " + pf.getName());
                        result.add(objFactory.createSchedulerHotFolder(pf));
                    }
                } catch (Exception e) {
                    //
                }
            }
        }
        LOGGER.debug("getFilelist from: " + providerFile.getName());
        entries = provider.getFilelist(providerFile.getName(), regex, 0, false, true, null);
        for (SOSFileEntry entry : entries) {
            ISOSProviderFile pf = provider.getFile(entry.getFullPath());
            String lowerFilename = entry.getFilename().toLowerCase();
            try {
                if (entry.isDirectory()) {
                    LOGGER.debug("load SchedulerHotFolder = " + pf.getName());
                    result.add(objFactory.createSchedulerHotFolder(pf));
                } else if (lowerFilename.endsWith(JSObjJob.fileNameExtension)) {
                    LOGGER.debug("load JSObjJob = " + pf.getName());
                    result.add(objFactory.createJob(pf));
                } else if (lowerFilename.endsWith(JSObjJobChain.fileNameExtension)) {
                    LOGGER.debug("load JSObjJobChain = " + pf.getName());
                    result.add(objFactory.createJobChain(pf));
                } else if (lowerFilename.endsWith(JSObjOrder.fileNameExtension)) {
                    LOGGER.debug("load JSObjOrder = " + pf.getName());
                    result.add(objFactory.createOrder(pf));
                } else if (lowerFilename.endsWith(JSObjLock.fileNameExtension)) {
                    LOGGER.debug("load JSObjLock = " + pf.getName());
                    result.add(objFactory.createLock(pf));
                } else if (lowerFilename.endsWith(JSObjProcessClass.fileNameExtension)) {
                    LOGGER.debug("load JSObjProcessClass = " + pf.getName());
                    result.add(objFactory.createProcessClass(pf));
                } else if (lowerFilename.endsWith(JSObjSchedule.fileNameExtension)) {
                    LOGGER.debug("load JSObjSchedule = " + pf.getName());
                    result.add(objFactory.createSchedule(pf));
                } else if (lowerFilename.endsWith(JSObjParams.fileNameExtension)) {
                    LOGGER.debug("load JSObjParams = " + pf.getName());
                    result.add(objFactory.createParams(pf));
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
        LOGGER.debug(String.format("%1$s objects found in %2$s", result.getFileList().size(), providerFile.getName()));
        return result;
    }

    public SchedulerHotFolderFileList getHotFolderFileList() {
        return hotFolderFileList;
    }

}

package com.sos.scheduler.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

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
public class SchedulerHotFolderFileList {

    private static final Logger LOGGER = Logger.getLogger(SchedulerHotFolderFileList.class);
    private List<JSObjBase> fileList = new ArrayList<JSObjBase>();
    private ISOSVirtualFile hotFolderSrc = null;

    public static enum HotFolderObject {
        folder, job, job_chain, order, lock, process_class, schedule, params;
    }

    public class GroupFolderAndSortByName implements Comparator<JSObjBase> {

        @Override
        public int compare(JSObjBase jsObjBase0, JSObjBase jsObjBase1) {
            int compareRet = 0;
            boolean jsObjBase0IsFolder = (jsObjBase0 instanceof SchedulerHotFolder);
            boolean jsObjBase1IsFolder = (jsObjBase1 instanceof SchedulerHotFolder);
            if (jsObjBase0IsFolder && !jsObjBase1IsFolder) {
                compareRet = -1;
            } else if (!jsObjBase0IsFolder && jsObjBase1IsFolder) {
                compareRet = 1;
            } else if (jsObjBase0.getHotFolderSrc() == null) {
                compareRet = 1;
            } else if (jsObjBase1.getHotFolderSrc() == null) {
                compareRet = -1;
            } else {
                String hotFolderSrcName0 = jsObjBase0.getHotFolderSrc().getName().toLowerCase();
                String hotFolderSrcName1 = jsObjBase1.getHotFolderSrc().getName().toLowerCase();
                compareRet = hotFolderSrcName0.compareTo(hotFolderSrcName1);
            }
            return compareRet;
        }
    }

    public List<JSObjBase> getFileList() {
        return fileList;
    }

    public List<JSObjBase> getSortedFileList() {
        Collections.sort(fileList, new GroupFolderAndSortByName());
        return fileList;
    }

    public List<JSObjBase> getSortedFileList(Comparator<JSObjBase> comp) {
        Collections.sort(fileList, comp);
        return fileList;
    }

    public void add(JSObjBase pObjHotFolderElement) {
        fileList.add(pObjHotFolderElement);
    }

    public void addAll(SchedulerHotFolderFileList pFileList) {
        fileList.addAll(pFileList.getFileList());
    }

    public List<SchedulerHotFolder> getFolderList() {
        List<SchedulerHotFolder> folders = new ArrayList<SchedulerHotFolder>();
        for (JSObjBase fileListItem : this.getFileList()) {
            if (fileListItem instanceof SchedulerHotFolder) {
                folders.add((SchedulerHotFolder) fileListItem);
            }
        }
        if (getHotFolderSrc() != null) {
            LOGGER.debug(String.format("%1$s folders found in %2$s", folders.size(), getHotFolderSrc().getName()));
        }
        return folders;
    }

    public List<JSObjJob> getJobList() {
        List<JSObjJob> jobs = new ArrayList<JSObjJob>();
        for (JSObjBase fileListItem : this.getFileList()) {
            if (fileListItem instanceof JSObjJob) {
                jobs.add((JSObjJob) fileListItem);
            }
        }
        if (getHotFolderSrc() != null) {
            LOGGER.debug(String.format("%1$s jobs found in %2$s", jobs.size(), getHotFolderSrc().getName()));
        }
        return jobs;
    }

    public List<JSObjJobChain> getJobChainList() {
        List<JSObjJobChain> jobChains = new ArrayList<JSObjJobChain>();
        for (JSObjBase fileListItem : this.getFileList()) {
            if (fileListItem instanceof JSObjJobChain) {
                jobChains.add((JSObjJobChain) fileListItem);
            }
        }
        LOGGER.debug(String.format("%1$s job chains found in %2$s", jobChains.size(), getHotFolderSrc().getName()));
        return jobChains;
    }

    public List<JSObjOrder> getOrderList() {
        List<JSObjOrder> orders = new ArrayList<JSObjOrder>();
        for (JSObjBase fileListItem : this.getFileList()) {
            if (fileListItem instanceof JSObjOrder) {
                orders.add((JSObjOrder) fileListItem);
            }
        }
        LOGGER.debug(String.format("%1$s orders found in %2$s", orders.size(), getHotFolderSrc().getName()));
        return orders;
    }

    public List<ISOSVirtualFile> getOrderList(String filter) {
        Pattern pattern = Pattern.compile(filter);
        List<JSObjOrder> orders = getOrderList();
        List<ISOSVirtualFile> result = new ArrayList<ISOSVirtualFile>();
        for (JSObjOrder object : orders) {
            File f = new File(object.getHotFolderSrc().getName());
            Matcher m = pattern.matcher(f.getName());
            if (m.matches()) {
                result.add(object.getHotFolderSrc());
            }
        }
        return result;
    }

    public List<JSObjProcessClass> getProcessClassList() {
        List<JSObjProcessClass> processClasses = new ArrayList<JSObjProcessClass>();
        for (JSObjBase fileListItem : this.getFileList()) {
            if (fileListItem instanceof JSObjProcessClass) {
                processClasses.add((JSObjProcessClass) fileListItem);
            }
        }
        LOGGER.debug(String.format("%1$s process classes found in %2$s", processClasses.size(), getHotFolderSrc().getName()));
        return processClasses;
    }

    public List<JSObjLock> getLockList() {
        List<JSObjLock> locks = new ArrayList<JSObjLock>();
        for (JSObjBase fileListItem : this.getFileList()) {
            if (fileListItem instanceof JSObjLock) {
                locks.add((JSObjLock) fileListItem);
            }
        }
        LOGGER.debug(String.format("%1$s locks found in %2$s", locks.size(), getHotFolderSrc().getName()));
        return locks;
    }

    public List<JSObjSchedule> getScheduleList() {
        List<JSObjSchedule> schedules = new ArrayList<JSObjSchedule>();
        for (JSObjBase fileListItem : this.getFileList()) {
            if (fileListItem instanceof JSObjSchedule) {
                schedules.add((JSObjSchedule) fileListItem);
            }
        }
        LOGGER.debug(String.format("%1$s schedules found in %2$s", schedules.size(), getHotFolderSrc().getName()));
        return schedules;
    }

    public List<JSObjParams> getParamsList() {
        List<JSObjParams> params = new ArrayList<JSObjParams>();
        for (JSObjBase fileListItem : this.getFileList()) {
            if (fileListItem instanceof JSObjParams) {
                params.add((JSObjParams) fileListItem);
            }
        }
        LOGGER.debug(String.format("%1$s params found in %2$s", params.size(), getHotFolderSrc().getName()));
        return params;
    }

    public List<JSObjBase> getFileList(HotFolderObject... hotFolderObject) {
        List<JSObjBase> filteredFileList = new ArrayList<JSObjBase>();
        List<HotFolderObject> hotFolderObjectList = Arrays.asList(hotFolderObject);
        for (JSObjBase fileListItem : this.getFileList()) {
            if ((hotFolderObjectList.contains(HotFolderObject.folder) && fileListItem instanceof SchedulerHotFolder)
                    || (hotFolderObjectList.contains(HotFolderObject.job) && fileListItem instanceof JSObjJob)
                    || (hotFolderObjectList.contains(HotFolderObject.job_chain) && fileListItem instanceof JSObjJobChain)
                    || (hotFolderObjectList.contains(HotFolderObject.order) && fileListItem instanceof JSObjOrder)
                    || (hotFolderObjectList.contains(HotFolderObject.lock) && fileListItem instanceof JSObjLock)
                    || (hotFolderObjectList.contains(HotFolderObject.process_class) && fileListItem instanceof JSObjProcessClass)
                    || (hotFolderObjectList.contains(HotFolderObject.schedule) && fileListItem instanceof JSObjSchedule)
                    || (hotFolderObjectList.contains(HotFolderObject.params) && fileListItem instanceof JSObjParams)) {
                filteredFileList.add(fileListItem);
            }
        }
        LOGGER.debug(String.format("%1$s objects found in %2$s", filteredFileList.size(), getHotFolderSrc().getName()));
        return filteredFileList;
    }

    public void setHotFolderSrc(ISOSVirtualFile hotFolderSrc) {
        this.hotFolderSrc = hotFolderSrc;
    }

    public ISOSVirtualFile getHotFolderSrc() {
        return hotFolderSrc;
    }

}
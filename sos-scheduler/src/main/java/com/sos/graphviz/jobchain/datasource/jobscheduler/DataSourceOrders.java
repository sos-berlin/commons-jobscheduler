package com.sos.graphviz.jobchain.datasource.jobscheduler;

import java.io.File;
import java.util.ArrayList;
import com.sos.vfs.common.SOSVFSFactory;
import com.sos.vfs.common.interfaces.ISOSProvider;
import com.sos.vfs.common.interfaces.ISOSProviderFile;
import com.sos.vfs.common.options.SOSBaseOptions;
import com.sos.graphviz.jobchain.interfaces.IDataSourceOrders;
import com.sos.scheduler.model.SchedulerHotFolder;
import com.sos.scheduler.model.SchedulerHotFolderFileList;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.objects.JSObjBase;
import com.sos.scheduler.model.objects.JSObjJobChain;
import com.sos.scheduler.model.objects.JSObjOrder;

public class DataSourceOrders implements IDataSourceOrders {

    private SchedulerObjectFactory schedulerObjectFactory;
    private ArrayList<JSObjOrder> listOfOrders;
    private File liveFolder;
    private int index;
    private JSObjJobChain jobChain;

    public DataSourceOrders(File liveFolder, SchedulerObjectFactory schedulerObjectFactory, String jobChainXml) {
        super();
        this.schedulerObjectFactory = schedulerObjectFactory;
        jobChain = schedulerObjectFactory.createJobChain();
        jobChain.loadObject(jobChainXml);
        this.liveFolder = liveFolder;
        reset();
        listOfOrders = new ArrayList<JSObjOrder>();
    }

    public DataSourceOrders(File liveFolder, SchedulerObjectFactory schedulerObjectFactory, JSObjJobChain jobChain) {
        super();
        this.schedulerObjectFactory = schedulerObjectFactory;
        this.jobChain = jobChain;
        this.liveFolder = liveFolder;
        reset();
        listOfOrders = new ArrayList<JSObjOrder>();
    }

    public void getList() {
        reset();
        String liveFolderName = liveFolder.getAbsolutePath();
        try {
            SOSBaseOptions vfsOptions = new SOSBaseOptions();
            ISOSProvider objFileSystemHandler = SOSVFSFactory.getProvider("local", vfsOptions.ssh_provider, vfsOptions.webdav_provider,
                    vfsOptions.smb_provider);
            ISOSProviderFile objHotFolder = objFileSystemHandler.getFile(liveFolderName);
            SchedulerHotFolder objSchedulerHotFolder = schedulerObjectFactory.createSchedulerHotFolder(objHotFolder);
            SchedulerHotFolderFileList objSchedulerHotFolderFileList = objSchedulerHotFolder.loadOrderObjects();
            for (JSObjBase hotFolderItem : objSchedulerHotFolderFileList.getOrderList()) {
                if (hotFolderItem instanceof JSObjOrder) {
                    JSObjOrder order = (JSObjOrder) hotFolderItem;
                    String jobchainName = order.getJobChainName();
                    if (jobchainName.equalsIgnoreCase(getName())) {
                        listOfOrders.add(order);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        String name = jobChain.getObjectName();
        if (name == null || name.isEmpty()) {
            name = jobChain.getName();
        }
        return name;
    }

    public boolean hasNext() {
        return index + 1 < listOfOrders.size();
    }

    public JSObjOrder next() {
        index = index + 1;
        return listOfOrders.get(index);
    }

    public void reset() {
        index = -1;
    }

    public JSObjJobChain getJobChain() {
        return jobChain;
    }

    public void setJobChain(JSObjJobChain jobChain) {
        this.jobChain = jobChain;
    }
}

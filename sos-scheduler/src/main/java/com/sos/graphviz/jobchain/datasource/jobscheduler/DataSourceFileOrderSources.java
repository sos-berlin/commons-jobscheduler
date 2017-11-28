package com.sos.graphviz.jobchain.datasource.jobscheduler;

import java.util.ArrayList;

import com.sos.graphviz.jobchain.interfaces.IDataSourceFileOrderSources;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.objects.JSObjJobChain;
import com.sos.scheduler.model.objects.JobChain;
import com.sos.scheduler.model.objects.JobChain.FileOrderSource;

public class DataSourceFileOrderSources implements IDataSourceFileOrderSources {
    private ArrayList<JobChain.FileOrderSource> listOfFileSources;
    private JSObjJobChain jobChain;
    private int index;

    public DataSourceFileOrderSources(SchedulerObjectFactory schedulerObjectFactory, String xml) {
        super();
        jobChain = schedulerObjectFactory.createJobChain();
        jobChain.loadObject(xml);
        listOfFileSources = new ArrayList<JobChain.FileOrderSource>();
     }

    public DataSourceFileOrderSources(SchedulerObjectFactory schedulerObjectFactory, JSObjJobChain jobChain) {
        super();
        this.jobChain = jobChain;
        listOfFileSources = new ArrayList<JobChain.FileOrderSource>();
     }

    public void getList() {

        index = -1;
        for (Object fileOrderSourceItem : jobChain.getFileOrderSourceList()) {
            if (fileOrderSourceItem instanceof FileOrderSource) {
                FileOrderSource fileOrderSource = (FileOrderSource) fileOrderSourceItem;
                listOfFileSources.add(fileOrderSource);
            }
        }
    }

    public boolean hasNext() {
        return index+1 < listOfFileSources.size();
    }

    public JobChain.FileOrderSource next() {
        index = index + 1;
        return listOfFileSources.get(index);
    }

    public void reset() {
        index = -1;
    }

    public JSObjJobChain getJobChain() {
        return jobChain;
    }

}

package com.sos.graphviz.jobchain;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.graphviz.jobchain.datasource.jobscheduler.DataSourceFileOrderSinks;
import com.sos.graphviz.jobchain.datasource.jobscheduler.DataSourceJobChainNodes;
import com.sos.graphviz.jobchain.interfaces.IDataSourceJobChainNodes;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.objects.JobChain;

public class JobChainDiagrammDataTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testIterateNodes() {
        SchedulerObjectFactory schedulerObjectFactory = new SchedulerObjectFactory();
        schedulerObjectFactory.initMarshaller(JobChain.class);
        
        String xml = "<job_chain><job_chain_node  state='100' job='job1' next_state='200' error_state='error'/><job_chain_node state='200' job='job1' next_state='300' error_state='error'/><job_chain_node  state='300' job='job1' next_state='success' error_state='error'/><job_chain_node  state='success'/><job_chain_node  state='error'/></job_chain>";        
        DataSourceJobChainNodes listOfJobChainNodes = new DataSourceJobChainNodes(schedulerObjectFactory,xml);
        JobChainDiagrammData jobChainDiagrammData = new JobChainDiagrammData();
        jobChainDiagrammData.setListOfJobChainNodes(listOfJobChainNodes);
       
            
        JobChain.JobChainNode jobChainNode;
        listOfJobChainNodes.getList();
        while (listOfJobChainNodes.hasNext()) {
            jobChainNode = listOfJobChainNodes.next();
            System.out.println(jobChainNode.getState() +  ":" + jobChainNode.getJob());
        }
    }       

    @Test
    public void testIterateSinks() {
        SchedulerObjectFactory schedulerObjectFactory = new SchedulerObjectFactory();
        schedulerObjectFactory.initMarshaller(JobChain.class);
        
        String xml = "<job_chain><job_chain_node  state='100' job='job1' next_state='200' error_state='error'/><job_chain_node state='200' job='job1' next_state='300' error_state='error'/><job_chain_node  state='300' job='job1' next_state='success' error_state='error'/><job_chain_node  state='success'/><job_chain_node  state='error'/></job_chain>";        
        DataSourceFileOrderSinks listOfFileOrderSinks = new DataSourceFileOrderSinks(schedulerObjectFactory,xml);
        JobChainDiagrammData jobChainDiagrammData = new JobChainDiagrammData();
        jobChainDiagrammData.setListOfFileOrderSinks(listOfFileOrderSinks);
             
        JobChain.FileOrderSink fileOrderSink;
        listOfFileOrderSinks.getList();
        while (listOfFileOrderSinks.hasNext()) {
            fileOrderSink = listOfFileOrderSinks.next();
            System.out.println(fileOrderSink.getState());
        }
    }       
    
    
}

package com.sos.graphviz.jobchain;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.graphviz.jobchain.diagram.JobChainDiagramCreator;

public class JobChainDiagrammCreator {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void JobChainDiagramCreatorTest() throws Exception {
        String jobChainXml = "<job_chain name='job_chain2'><job_chain_node  state='100' job='job1' next_state='200' error_state='error'/><job_chain_node state='200' job='job1' next_state='300' error_state='error'/><job_chain_node  state='300' job='job1' next_state='success' error_state='error'/><job_chain_node  state='success'/><job_chain_node  state='error'/></job_chain>";        
        File outputDirectory = new File("c:/temp");
        File liveFolder = new File("C:/Users/ur/Documents/sos-berlin.com/jobscheduler/scheduler_joc_cockpit/config/live");
        JobChainDiagramCreator jobChainDiagramCreator = new JobChainDiagramCreator(jobChainXml, outputDirectory);
        jobChainDiagramCreator.createGraphVizFile(true);
    }

}

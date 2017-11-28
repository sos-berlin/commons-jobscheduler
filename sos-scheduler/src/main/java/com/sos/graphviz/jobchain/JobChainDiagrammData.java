package com.sos.graphviz.jobchain;

 
import com.sos.graphviz.Graph;
import com.sos.graphviz.jobchain.datasource.jobscheduler.DataSourceFileOrderSinks;
import com.sos.graphviz.jobchain.interfaces.IDataSourceFileOrderSinks;
import com.sos.graphviz.jobchain.interfaces.IDataSourceFileOrderSources;
import com.sos.graphviz.jobchain.interfaces.IDataSourceJobChainNodes;
import com.sos.graphviz.jobchain.interfaces.IDataSourceOrders;
import com.sos.scheduler.model.objects.JobChain;
 
public class JobChainDiagrammData {
    private IDataSourceJobChainNodes listOfJobChainNodes;
    private IDataSourceFileOrderSinks listOfFileOrderSinks;
    private IDataSourceFileOrderSources listOfFileOrderSources;
    private IDataSourceOrders listOfOrders;
    
    private JobChain.JobChainNode firstNode;
    private Graph graph;
    
    public JobChainDiagrammData() {
        super();
    }

    public JobChain.JobChainNode getFirstNode() {
        return firstNode;
    }

    public void setFirstNode(JobChain.JobChainNode firstNode) {
        this.firstNode = firstNode;
    }

    public IDataSourceJobChainNodes getListOfJobChainNodes() {
        return listOfJobChainNodes;
    }

    public void setListOfJobChainNodes(IDataSourceJobChainNodes listOfJobChainNodes) {
        this.listOfJobChainNodes = listOfJobChainNodes;
    }

    public IDataSourceFileOrderSinks getListOfFileOrderSinks() {
        return listOfFileOrderSinks;
    }

    public void setListOfFileOrderSinks(DataSourceFileOrderSinks listOfFileOrderSinks) {
        this.listOfFileOrderSinks = listOfFileOrderSinks;
    }

    public void setListOfFileOrderSinks(IDataSourceFileOrderSinks listOfFileOrderSinks) {
        this.listOfFileOrderSinks = listOfFileOrderSinks;
    }

 
    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public IDataSourceOrders getListOfOrders() {
        return listOfOrders;
    }

    public void setListOfOrders(IDataSourceOrders listOfOrders) {
        this.listOfOrders = listOfOrders;
    }

    public IDataSourceFileOrderSources getListOfFileOrderSources() {
        return listOfFileOrderSources;
    }

    public void setListOfFileOrderSources(IDataSourceFileOrderSources listOfFileOrderSources) {
        this.listOfFileOrderSources = listOfFileOrderSources;
    }

  
    
}

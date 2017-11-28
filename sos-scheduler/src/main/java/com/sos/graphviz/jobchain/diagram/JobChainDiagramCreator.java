package com.sos.graphviz.jobchain.diagram;

import java.io.File;
import com.sos.graphviz.Edge;
import com.sos.graphviz.Graph;
import com.sos.graphviz.GraphIO;
import com.sos.graphviz.Node;
import com.sos.graphviz.SingleNodeProperties;
import com.sos.graphviz.enums.FileType;
import com.sos.graphviz.enums.RankDir;
import com.sos.graphviz.enums.SVGColor;
import com.sos.graphviz.enums.Shape;
import com.sos.graphviz.enums.Style;
import com.sos.graphviz.jobchain.JobChainDiagrammData;
import com.sos.graphviz.jobchain.datasource.jobscheduler.DataSourceFileOrderSinks;
import com.sos.graphviz.jobchain.datasource.jobscheduler.DataSourceFileOrderSources;
import com.sos.graphviz.jobchain.datasource.jobscheduler.DataSourceJobChainNodes;
import com.sos.graphviz.jobchain.datasource.jobscheduler.DataSourceOrders;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.objects.JSObjBase;
import com.sos.scheduler.model.objects.JSObjJobChain;
import com.sos.scheduler.model.objects.JSObjOrder;
import com.sos.scheduler.model.objects.JobChain;
import com.sos.scheduler.model.objects.JobChain.FileOrderSource;
import com.sos.scheduler.model.objects.JobChain.JobChainNode;
import com.sos.scheduler.model.objects.JobChain.JobChainNode.OnReturnCodes.OnReturnCode;
import com.sos.scheduler.model.objects.JobChain.JobChainNode.OnReturnCodes.OnReturnCode.ToState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobChainDiagramCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobChainDiagramCreator.class);
    private static final String HTML_BR = "<br/>";
    private File outputFile;
    private FileType graphVizImageType = FileType.png;
    private String dotOutputPath;
    private File liveFolder;
    private JobChainDiagrammData jobChainDiagrammData;
    private String jobChainXml;
    private JSObjJobChain jsObjJobChain;

    public JobChainDiagramCreator(String jobChainXml, File liveFolder) {
        super();
        this.jobChainXml = jobChainXml;
        this.liveFolder = liveFolder;
    }

    public JobChainDiagramCreator(JSObjJobChain jsObjJobChain, File liveFolder) {
        super();
        this.jsObjJobChain = jsObjJobChain;
        this.liveFolder = liveFolder;
    }
    
   
    private void newErrorNode(String state) {
        if (jobChainDiagrammData.getListOfJobChainNodes().get(state) == null) {
            Node nNotExist = jobChainDiagrammData.getGraph().getNodeOrNull(state);
            nNotExist.getSingleNodeProperties().setFillcolor(SVGColor.orangered);
            nNotExist.getSingleNodeProperties().setColor(SVGColor.blue);
            String strH = "<b>" + JSObjBase.escapeHTML(state) + "</b>" + HTML_BR;
            strH += "<i><font point-size=\"8\" color=\"yellow\" >missing</font></i>" + HTML_BR;
            nNotExist.getSingleNodeProperties().setLabel(strH);
        } else {
            Node nErrorState = jobChainDiagrammData.getGraph().getNodeOrNull(state);
            if (nErrorState != null) {
                nErrorState.getSingleNodeProperties().setFillcolor(SVGColor.orange);
                nErrorState.getSingleNodeProperties().setColor(SVGColor.blue);
                String strH = "<b>" + JSObjBase.escapeHTML(state) + "</b>";
                nErrorState.getSingleNodeProperties().setLabel(strH);
            }
        }
    }

    private Edge newEdge(String from, String to) {
        Edge e = jobChainDiagrammData.getGraph().newEdge(from, to);
        e.getEdgeProperties().setColor(SVGColor.cadetblue);
        e.getEdgeProperties().setArrowSize(0.5);
        e.getEdgeProperties().setFontSize(8);
        e.getEdgeProperties().setFontName("Arial");
        if (jobChainDiagrammData.getListOfFileOrderSinks().get(to) != null) {
            Node nFileSink = jobChainDiagrammData.getGraph().getNodeOrNull(to);
            String strH = "<b>" + JSObjBase.escapeHTML(to) + "</b>" + HTML_BR;
            strH += "<i><font point-size=\"8\" color=\"blue\" >File Sink</font></i>" + HTML_BR;
            nFileSink.getSingleNodeProperties().setLabel(strH);
            nFileSink.getSingleNodeProperties().setFillcolor(SVGColor.beige);
        } else {
            JobChainNode jobChainToNode = jobChainDiagrammData.getListOfJobChainNodes().get(to);
            if (jobChainToNode == null) {
                Node nNotExist = jobChainDiagrammData.getGraph().getNodeOrNull(to);
                String strH = "<b>" + JSObjBase.escapeHTML(to) + "</b>" + HTML_BR;
                strH += "<i><font point-size=\"8\" color=\"yellow\" >missing</font></i>" + HTML_BR;
                nNotExist.getSingleNodeProperties().setLabel(strH);
                nNotExist.getSingleNodeProperties().setFillcolor(SVGColor.lightgray);
            } else {
                Node nExist = jobChainDiagrammData.getGraph().getNodeOrNull(to);
                String strH = "<b>" + JSObjBase.escapeHTML(to) + "</b>" + HTML_BR;
                nExist.getSingleNodeProperties().setLabel(strH);
            }
        }
        return e;
    }

    private String getOrderNodeId(String n) {
        return "order:" + n;
    }

    private void creatingOrders(String firstState) throws Exception {
        jobChainDiagrammData.getListOfOrders().getList();

        while (jobChainDiagrammData.getListOfOrders().hasNext()) {
            JSObjOrder order = jobChainDiagrammData.getListOfOrders().next();
            String jobchainName = order.getJobChainName();
            if (jobchainName.equalsIgnoreCase(jobChainDiagrammData.getListOfOrders().getName())) {
                String from = order.getId();
                String to = firstState;
                LOGGER.debug("createGraphVizImageFile.order found:" + from);
                if (order.getState() != null) {
                    to = order.getState();
                }
                Node node = jobChainDiagrammData.getGraph().newNode(getOrderNodeId(from));
                SingleNodeProperties singleNodeProperties = node.getSingleNodeProperties();
                singleNodeProperties.setShape(Shape.ellipse);
                singleNodeProperties.setFillcolor(SVGColor.chartreuse);
                String label = "Order:" + order.getId() + HTML_BR;
                if (order.getTitle() != null) {
                    label += "<i><font point-size=\"8\" color=\"blue\" >" + JSObjBase.escapeHTML(order.getTitle()) + "</font></i>" + HTML_BR;
                }
                singleNodeProperties.setLabel(label);
                Edge eOrderEdge = newEdge(getOrderNodeId(from), to);
                eOrderEdge.getEdgeProperties().setConstraint(true);
                eOrderEdge.getEdgeProperties().setStyle(Style.dashed);
                eOrderEdge.getEdgeProperties().setColor(SVGColor.black);
            }
        }
    }

    private File createGraphVizImageFile(File imageOutputFolder, boolean showErrorNodes) throws Exception {
        JSObjJobChain jobchain = jobChainDiagrammData.getListOfOrders().getJobChain();
        LOGGER.debug("createGraphVizImageFile.job chain.title:" + jobchain.getTitle());
        int fileOrderSourceCnt = 0;

        jobChainDiagrammData.getListOfFileOrderSources().getList();

        while (jobChainDiagrammData.getListOfFileOrderSources().hasNext()) {
            FileOrderSource fileOrderSource = jobChainDiagrammData.getListOfFileOrderSources().next();
            String dir = fileOrderSource.getDirectory();
            LOGGER.debug("createGraphVizImageFile.file_order_source found:" + dir);
            String regExp = fileOrderSource.getRegex();
            fileOrderSourceCnt++;
            Node node = jobChainDiagrammData.getGraph().newNode("FileOrderSource" + fileOrderSourceCnt);
            SingleNodeProperties singleNodeProperties = node.getSingleNodeProperties();
            singleNodeProperties.setFillcolor(SVGColor.beige);
            String strH = "";
            strH = "<b>" + "Folder: " + dir + " </b>" + HTML_BR;
            strH += "<i><b>" + JSObjBase.escapeHTML("RegExp: " + regExp) + "</b></i>" + HTML_BR;
            singleNodeProperties.setColor(SVGColor.blue);
            singleNodeProperties.setLabel(strH);
        }

        jobChainDiagrammData.getListOfFileOrderSinks().getList();
        jobChainDiagrammData.getListOfJobChainNodes().setFirstNode(jobChainDiagrammData.getListOfFileOrderSinks().getFirstNode());
        jobChainDiagrammData.getListOfJobChainNodes().getList();

        String firstState = jobChainDiagrammData.getListOfJobChainNodes().getFirstNode();

        creatingOrders(firstState);
        String state = null;
        String nextState = null;
        fileOrderSourceCnt = 0;

        jobChainDiagrammData.getListOfFileOrderSources().reset();
        while (jobChainDiagrammData.getListOfFileOrderSources().hasNext()) {
            FileOrderSource fileOrderSource = jobChainDiagrammData.getListOfFileOrderSources().next();
            fileOrderSourceCnt++;
            nextState = fileOrderSource.getNextState();
            if (nextState.trim().isEmpty()) {
                nextState = firstState;
            }
            Edge e = newEdge("FileOrderSource" + fileOrderSourceCnt, nextState);
            e.getEdgeProperties().setConstraint(true);
        }

        jobChainDiagrammData.getListOfJobChainNodes().reset();
        while (jobChainDiagrammData.getListOfJobChainNodes().hasNext()) {
            JobChainNode jobChainNode = jobChainDiagrammData.getListOfJobChainNodes().next();
            state = jobChainNode.getState();
            int i = state.lastIndexOf(":");
            LOGGER.debug("createGraphVizImageFile.job_chain_node found:" + state);
            if (i > 0 && jobChainDiagrammData.getListOfJobChainNodes().get(state.substring(0, i)) != null) {
                String from = state.substring(0, i);
                Edge e = newEdge(from, state);
                e.getEdgeProperties().setConstraint(true);
            }
            nextState = jobChainNode.getNextState();
            if (nextState != null && !nextState.isEmpty()) {
                String strH = "";
                String suspend = "";
                boolean isSuspend = false;
                if (jobChainNode.getOnError() != null && "suspend".equals(jobChainNode.getOnError())) {
                    suspend = "on error suspend";
                    isSuspend = true;
                }
                boolean isSetback = false;
                if (jobChainNode.getOnError() != null && "setback".equals(jobChainNode.getOnError())) {
                    isSetback = true;
                }
                strH = "<b>" + JSObjBase.escapeHTML(state) + "</b>" + HTML_BR;
                strH += "<i><font point-size=\"8\" color=\"blue\" >" + JSObjBase.escapeHTML(jobChainNode.getJob()) + "</font></i>" + HTML_BR;
                if (isSuspend) {
                    strH += "<i><font point-size=\"8\" color=\"red\" >" + JSObjBase.escapeHTML(suspend) + "</font></i>" + HTML_BR;
                }
                if (isSetback) {
                    Edge eSetbackState = newEdge(state, state);
                    eSetbackState.getEdgeProperties().setConstraint(false);
                    eSetbackState.getEdgeProperties().setStyle(Style.dotted);
                    eSetbackState.getEdgeProperties().setLabel("..setback");
                }
                Node n = jobChainDiagrammData.getGraph().newNode(state);
                n.getSingleNodeProperties().setLabel(strH);
                Edge e = newEdge(state, nextState);
                e.getEdgeProperties().setLabel("..next");
                if (jobChainNode.getOnReturnCodes() != null) {
                    for (Object onReturnCodeItem : jobChainNode.getOnReturnCodes().getOnReturnCode()) {
                        if (onReturnCodeItem instanceof OnReturnCode) {
                            ToState toState = ((OnReturnCode) onReturnCodeItem).getToState();
                            if (toState != null) {
                                String toStateValue = toState.getState();
                                Edge eReturnCode = newEdge(state, toStateValue);
                                eReturnCode.getEdgeProperties().setLabel("..exit:" + ((OnReturnCode) onReturnCodeItem).getReturnCode());
                            }
                        }
                    }
                }
                String errorState = jobChainNode.getErrorState();
                if (showErrorNodes && !isSetback && !isSuspend && errorState != null) {
                    Edge eErrorState = newEdge(state, errorState);
                    eErrorState.getEdgeProperties().setConstraint(true);
                    eErrorState.getEdgeProperties().setStyle(Style.dotted);
                    if (jobChainDiagrammData.getListOfFileOrderSinks().get(errorState) == null) {
                        newErrorNode(errorState);
                    }
                }
            }
        }

        GraphIO io = new GraphIO(jobChainDiagrammData.getGraph());
        if (dotOutputPath == null || dotOutputPath.isEmpty()) {
            io.setDotDir(liveFolder.getAbsolutePath());
            LOGGER.debug("createGraphVizImageFile.dotOutputPath" + liveFolder.getAbsolutePath());
        } else {
            io.setDotDir(dotOutputPath);
            LOGGER.debug("createGraphVizImageFile.dotOutputPath" + dotOutputPath);
        }
        if (dotOutputPath == null || dotOutputPath.isEmpty()) {
            dotOutputPath = "./";
                
        }
        
        String name = jobChainDiagrammData.getListOfOrders().getName();
    
        LOGGER.debug("createGraphVizImageFile.jobchain.getObjectName()" + name);
        File output = new File(imageOutputFolder.getAbsolutePath(), name + "." + graphVizImageType);
        io.writeGraphToFile(graphVizImageType, output);
        return output;
    }

    public File createGraphVizFile(boolean showErrorNodes) throws Exception {
        SchedulerObjectFactory schedulerObjectFactory = new SchedulerObjectFactory();
        schedulerObjectFactory.initMarshaller(JobChain.class);
        jobChainDiagrammData = new JobChainDiagrammData();

        Graph graph = new Graph();
        graph.getGraphProperties().setDirection(RankDir.TB);
        graph.getGraphProperties().setFontSize("8");
        graph.getGlobalNodeProperties().setFontsize("8");
        graph.getGlobalNodeProperties().setShape(Shape.box);
        graph.getGlobalNodeProperties().setStyle(Style.rounded + "," + Style.filled);
        graph.getGlobalNodeProperties().setFillcolor(SVGColor.azure);
        graph.getGlobalNodeProperties().setFontname("Arial");
        graph.getGraphProperties().setRatio("auto");

        jobChainDiagrammData.setGraph(graph);
        DataSourceFileOrderSinks listOfFileOrderSinks;

        if (jsObjJobChain != null) {
            listOfFileOrderSinks = new DataSourceFileOrderSinks(schedulerObjectFactory, jsObjJobChain);
        } else {
            listOfFileOrderSinks = new DataSourceFileOrderSinks(schedulerObjectFactory, jobChainXml);
        }
        jobChainDiagrammData.setListOfFileOrderSinks(listOfFileOrderSinks);

        DataSourceFileOrderSources listOfFileOrderSources = new DataSourceFileOrderSources(schedulerObjectFactory, listOfFileOrderSinks.getJobChain());
        jobChainDiagrammData.setListOfFileOrderSources(listOfFileOrderSources);

        DataSourceJobChainNodes listOfJobChainNodes = new DataSourceJobChainNodes(schedulerObjectFactory, listOfFileOrderSinks.getJobChain());
        jobChainDiagrammData.setListOfJobChainNodes(listOfJobChainNodes);

        DataSourceOrders listOfOrders = new DataSourceOrders(liveFolder, schedulerObjectFactory, listOfFileOrderSinks.getJobChain());
        jobChainDiagrammData.setListOfOrders(listOfOrders);

        outputFile = createGraphVizImageFile(liveFolder, showErrorNodes);
        return outputFile;
    }

    public File getOutfile() {
        return outputFile;
    }

    public void setGraphVizImageType(FileType graphVizImageType) {
        this.graphVizImageType = graphVizImageType;
    }

    public void setDotOutputPath(String dotOutputPath) {
        this.dotOutputPath = dotOutputPath;
    }

    public void setLiveFolder(File liveFolder) {
        this.liveFolder = liveFolder;
    }

}

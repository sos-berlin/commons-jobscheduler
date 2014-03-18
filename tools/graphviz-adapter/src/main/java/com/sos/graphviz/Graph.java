package com.sos.graphviz;

import com.sos.graphviz.enums.RankType;
import com.sos.graphviz.enums.Shape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The main class to create a graph. With the factory methods new... you can create subsequent elements of the graph.
 * If you want to save the graph as file you can use the class com.sos.graphviz.GraphIO.
 *
 * See class com.sos.graphviz.GraphTest how to create a graph.
 */
public class Graph extends GraphvizObjectWithId implements IGraphvizObject {

    private final static Logger logger = LoggerFactory.getLogger(Graph.class);

	private static final String constGraph = "G";
	private static final String constPlaceHolder = "%id%";
	private static final String constProlog = "digraph " + constPlaceHolder + " {";
	private static final String constPrologSubgraph = "subgraph " + constPlaceHolder + " {";
	private static final String constEpilog = "}";

	private GraphProperties graphProperties = new GraphProperties();
	private GlobalNodeProperties globalNodeProperties = new GlobalNodeProperties(Shape.box);
	private List<Node> nodeList = new ArrayList<Node>();
	private List<Edge> edgeList = new ArrayList<Edge>();
	private List<Subgraph> subgraphList = new ArrayList<Subgraph>();

    private final static Pattern nonWordPattern = Pattern.compile("\\s|\\W");

    public Graph() {
		super("G",constProlog.replace(constPlaceHolder,constGraph), constEpilog);
        init();
	}

	protected Graph(String subgraphId) {
		super(subgraphId,constPrologSubgraph.replace(constPlaceHolder,subgraphId), constEpilog);
		init();
	}

	public GraphvizObject getProperties() {
		return this.graphProperties;
	}

    public void init() {
        edgeList.clear();
        nodeList.clear();
        subgraphList.clear();
    }

    public GlobalNodeProperties getGlobalNodeProperties() {
		return this.globalNodeProperties;
	}

	protected void setGlobalNodeProperties(GlobalNodeProperties globalNodeProperties) {
		this.globalNodeProperties = globalNodeProperties;
	}

    public Node getNodeOrNull(String id) {
        // logger.debug("Search for node with id {}",id);
        if (nodeList.isEmpty() )
            logger.debug("The graph {} contains no nodes.",this.getId());
        Node result = null;
        for(Node n : nodeList) {
            // logger.debug("Found node with id {} in graph.",n.getId());
            if(n.getId().equals(id)) {
                result = n;
                break;
            }
        }
        return result;
    }

    public Node getNodeOrNullInAllGraphs(String id) {
        Node result = getNodeOrNull(id);
        if (result == null) {
            for(Subgraph s : subgraphList) {
                result = s.getNodeOrNullInAllGraphs(id);
                if (result != null)
                    break;
            }
        }
        return result;
    }

	@Override
	public String getContent() {
		StringBuilder sb = new StringBuilder();
		sb.append( getProlog() );
		sb.append( getMainContent() );
		return sb.toString();
	}
	
	protected String getProlog() {
		StringBuilder sb = new StringBuilder();
		sb.append(graphProperties.getSource());
		sb.append(globalNodeProperties.getSource());
		return sb.toString();
	}
	
	protected String getMainContent() {
		StringBuilder sb = new StringBuilder();
		Iterator<Node> nit = nodeList.iterator();
        Iterator<Subgraph> sit = subgraphList.iterator();
        while (sit.hasNext()) {
            Subgraph s = sit.next();
            sb.append(s.getSource());
        }
		while (nit.hasNext()) {
			Node n = nit.next();
			sb.append(n.getSource());
		}
		Iterator<Edge> eit = edgeList.iterator();
		while (eit.hasNext()) {
			Edge e = eit.next();
			sb.append(e.getSource());
		}
		return sb.toString();
	}
	
	public Node newNode(String node) {
		Node n = new Node(node);
		nodeList.add(n);
		return n;
	}
	
	public Edge newEdge(Node nodeFrom, Node nodeTo) {
		Edge e = new Edge( nodeFrom, nodeTo );
		edgeList.add(e);
		return e;
	}
	
	public Subgraph newSubgraph(String subgraphId, RankType rankType) {
		Subgraph s = new Subgraph(subgraphId,rankType,this);
		subgraphList.add(s);
		return s;
	}
	
	public Subgraph newSubgraph(String subgraphId) {
		return newSubgraph(subgraphId, RankType.same);
	}
	
	public ClusterSubgraph newClusterSubgraph(String subgraphId) {
        Matcher m = nonWordPattern.matcher(subgraphId);
        if(m.find()) {
            subgraphId = m.replaceAll("");
            logger.warn("Subgraph label must not contain non word charactzers - all non word characters replaced.");
        }
		ClusterSubgraph s = new ClusterSubgraph(subgraphId, this);
		subgraphList.add(s);
		return s;
	}

	public GraphProperties getGraphProperties() {
		return graphProperties;
	}

}


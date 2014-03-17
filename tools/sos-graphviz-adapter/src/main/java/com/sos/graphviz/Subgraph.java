package com.sos.graphviz;

import com.sos.graphviz.enums.RankType;

public class Subgraph extends Graph {

	private final SubgraphProperties subgraphProperties;
    private final Graph parent;

	protected Subgraph(String subgraphId, RankType rankType, Graph parent) {
		super(subgraphId);
		subgraphProperties = new SubgraphProperties(rankType);
        this.parent = parent;
		setGlobalNodeProperties(parent.getGlobalNodeProperties());
    }
	
	@Override
	public String getContent() {
		StringBuilder sb = new StringBuilder();
		sb.append( subgraphProperties.getContent() );
		sb.append(getGlobalNodeProperties().getSource());
		sb.append( getMainContent() );
		return sb.toString();
	}

	public GraphvizObject getProperties() {
		return subgraphProperties;
	}

	/**
	 * the graph properties should be available via the Graph object only.
	 * 
	 * @see com.sos.graphviz.Graph#getGraphProperties()
	 */
	public GraphProperties getGraphProperties() {
		return null;
	}

	public SubgraphProperties getSubgraphProperties() {
		return subgraphProperties;
	}

    public Graph getParent() {
        return parent;
    }

    @Override
    /**
     * Edges are defined always at level of RootGraph
     */
    public Edge newEdge(Node nodeFrom, Node nodeTo) {
        return getRootGraph().newEdge(nodeFrom,nodeTo);
    }

    private Graph getRootGraph() {
        Graph g = this.getParent();
        while (g instanceof Subgraph) {
            g = ((Subgraph) g).getParent();
        }
        return g;
    }

}


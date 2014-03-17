package com.sos.graphviz;

/**
 * A class to represent an edge between two nodes of the graph.
 */
public class Edge extends GraphvizObject implements IGraphvizObject {

	private final Node fromNode;
	private final Node toNode;
	private EdgeProperties edgeProperties = new EdgeProperties();;
	
	protected Edge(Node from, Node to) {
		super("","");
		this.fromNode = from;
		this.toNode = to;
	}

	@Override
	public String getContent() {
		StringBuilder sb = new StringBuilder();
		if (fromNode != null && toNode != null) {
			sb.append(fromNode.getName() + " -> " + toNode.getName());
			sb.append(edgeProperties.getSource());
			if (edgeProperties.getContent().isEmpty()) sb.append(nl);
		}
		return sb.toString();
	}

	public GraphvizObject getProperties() {
		return edgeProperties;
	}

	public EdgeProperties getEdgeProperties() {
		return (EdgeProperties)getProperties();
	}

    public Node getFromNode() {
        return fromNode;
    }

    public Node getToNode() {
        return toNode;
    }

}

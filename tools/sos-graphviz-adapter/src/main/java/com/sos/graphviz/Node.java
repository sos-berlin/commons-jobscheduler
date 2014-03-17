package com.sos.graphviz;

public class Node extends GraphvizObjectWithId implements IGraphvizObject {

	private final String name;
	private SingleNodeProperties nodeProperties;
	
	protected Node(String name) {
		super(name, name + " [", "]");
		this.name = name;
		this.nodeProperties = null;
	}

	public GraphvizObject getProperties() {
		if (this.nodeProperties == null) this.nodeProperties = new SingleNodeProperties(name);
		return this.nodeProperties;
	}
	
	public String getName() {
		return name;
	}

    @Override
    public String getSource() {
        return (nodeProperties == null) ? getName() + "\n" : super.getSource();
    }

    @Override
	public String getContent() {
		StringBuilder sb = new StringBuilder();
		if (this.nodeProperties != null) {
			sb.append(nodeProperties.getContent());
		}
		return sb.toString();
	}

	public SingleNodeProperties getSingleNodeProperties() {
		return (SingleNodeProperties)getProperties();
	}
	
}

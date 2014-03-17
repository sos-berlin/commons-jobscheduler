package com.sos.graphviz.properties;

public class GraphvizHtmlProperty extends GraphvizProperty implements IGraphvizProperty {

	public GraphvizHtmlProperty(String propertyName) {
		super(propertyName);
	}
	
	@Override
	public String getContent() {
		return (isSet()) ? getPropertyName() + " = <" + getValue() + ">" + nl : "";
	}
	
}

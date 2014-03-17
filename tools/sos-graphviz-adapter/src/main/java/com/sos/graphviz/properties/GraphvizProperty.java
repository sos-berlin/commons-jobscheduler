package com.sos.graphviz.properties;

public class GraphvizProperty implements IGraphvizProperty {

	public static final String nl = "\n";

	private Object value;
	private boolean isSet = false;
	private final String propertyName;
	
	public GraphvizProperty(String propertyName) {
		this.propertyName = propertyName;
	}

	@Override
	public String getContent() {
		return (isSet()) ? this.propertyName + " = \"" + getValue() + "\"" + nl : "";
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.isSet = true;
		this.value = value;
	}
	
	public boolean isSet() {
		return this.isSet;
	}

	public String getPropertyName() {
		return propertyName;
	}
	
}

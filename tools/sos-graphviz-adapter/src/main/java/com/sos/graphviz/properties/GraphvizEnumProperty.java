package com.sos.graphviz.properties;

public class GraphvizEnumProperty extends GraphvizProperty implements IGraphvizProperty {

	public GraphvizEnumProperty(String propertyName) {
		super(propertyName);
	}
	
	@Override
	public String getContent() {
		Enum<?> e = (Enum<?>)getValue();
		return (isSet()) ? getPropertyName() + " = \"" + e.name() + "\"" + nl : "";
	}
	
}

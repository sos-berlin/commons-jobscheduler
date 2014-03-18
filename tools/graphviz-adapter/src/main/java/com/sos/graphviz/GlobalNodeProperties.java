package com.sos.graphviz;

import com.sos.graphviz.enums.Shape;

public class GlobalNodeProperties extends NodeProperties {

	private static final String constPrefix = "node ";
	
	public GlobalNodeProperties(Shape shape) {
		super(constPrefix, shape);
	}
	
	public GlobalNodeProperties() {
		super(constPrefix);
	}

}

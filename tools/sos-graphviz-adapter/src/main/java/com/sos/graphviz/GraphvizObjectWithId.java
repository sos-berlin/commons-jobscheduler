package com.sos.graphviz;

/**
 * A general implementation of an Graphviz object with an unique id.
 */
abstract class GraphvizObjectWithId extends GraphvizObject {

	public final String id;

	protected GraphvizObjectWithId(String id, String prolog, String epilog) {
		super(prolog,epilog);
		this.id = id;
	}

	public String getId() {
		return id;
	}

}

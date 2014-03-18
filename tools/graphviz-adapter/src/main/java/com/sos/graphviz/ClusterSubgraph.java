package com.sos.graphviz;

import com.sos.graphviz.enums.RankType;

public class ClusterSubgraph extends Subgraph {

	private static final String clusterPrefix = "cluster_";

	protected ClusterSubgraph(String subgraphId, Graph parent) {
		super(clusterPrefix + subgraphId, RankType.min, parent);
	}

}

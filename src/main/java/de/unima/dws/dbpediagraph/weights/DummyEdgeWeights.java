package de.unima.dws.dbpediagraph.weights;

import com.tinkerpop.blueprints.Edge;

public enum DummyEdgeWeights implements EdgeWeights{
	INSTANCE;
	
	@Override
	public Double transform(Edge e) {
		return 1.0;
	}

}

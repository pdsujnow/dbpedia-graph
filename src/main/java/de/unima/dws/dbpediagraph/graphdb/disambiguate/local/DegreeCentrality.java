package de.unima.dws.dbpediagraph.graphdb.disambiguate.local;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

import de.unima.dws.dbpediagraph.graphdb.GraphType;
import de.unima.dws.dbpediagraph.graphdb.Graphs;
import de.unima.dws.dbpediagraph.graphdb.disambiguate.GraphDisambiguator;
import de.unima.dws.dbpediagraph.graphdb.disambiguate.LocalGraphDisambiguator;
import de.unima.dws.dbpediagraph.graphdb.disambiguate.WeightedSense;

/**
 * Degree Centrality {@link GraphDisambiguator} that only takes into account the degree of edges in the subgraph.
 * 
 * @author Bernhard Schäfer
 * 
 */
public enum DegreeCentrality implements LocalGraphDisambiguator {
	IN_AND_OUT_DEGREE(Direction.BOTH), IN_DEGREE(Direction.IN), OUT_DEGREE(Direction.OUT);

	public static DegreeCentrality forGraphType(GraphType graphType) {
		switch (graphType) {
		case DIRECTED_GRAPH:
			return IN_DEGREE;
		case UNDIRECTED_GRAPH:
			return IN_AND_OUT_DEGREE;
		default:
			throw new IllegalArgumentException();
		}
	}

	private final Direction direction;

	/**
	 * The direction of edges to be used for degree measurement. E.g. Direction.BOTH means that both in- and out edges
	 * are considered for the degree calculation, whereas Direction.IN refers to the indegree of an edge.
	 */
	private DegreeCentrality(Direction direction) {
		this.direction = direction;
	}

	@Override
	public List<WeightedSense> disambiguate(Collection<String> senses, Graph subgraph) {
		int numberOfVertices = Graphs.numberOfVertices(subgraph);

		List<WeightedSense> weightedSenses = new LinkedList<>();
		for (String sense : senses) {
			Vertex v = Graphs.vertexByUri(subgraph, sense);
			double degree = Graphs.vertexDegree(v, direction);
			double centrality = degree / (numberOfVertices - 1);
			weightedSenses.add(new WeightedSense(sense, centrality));
		}

		Collections.sort(weightedSenses);
		Collections.reverse(weightedSenses);

		return weightedSenses;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " (direction: " + direction + " )";
	}

}

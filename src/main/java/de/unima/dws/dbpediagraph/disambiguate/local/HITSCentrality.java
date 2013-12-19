package de.unima.dws.dbpediagraph.disambiguate.local;

import com.tinkerpop.blueprints.*;
import com.tinkerpop.blueprints.oupls.jung.GraphJung;

import de.unima.dws.dbpediagraph.disambiguate.AbstractLocalGraphDisambiguator;
import de.unima.dws.dbpediagraph.disambiguate.LocalGraphDisambiguator;
import de.unima.dws.dbpediagraph.graph.GraphType;
import de.unima.dws.dbpediagraph.graph.Graphs;
import de.unima.dws.dbpediagraph.model.Sense;
import de.unima.dws.dbpediagraph.model.SurfaceForm;
import de.unima.dws.dbpediagraph.weights.EdgeWeights;
import edu.uci.ics.jung.algorithms.scoring.*;
import edu.uci.ics.jung.algorithms.scoring.HITS.Scores;

/**
 * @author Bernhard Schäfer
 */
public class HITSCentrality<T extends SurfaceForm, U extends Sense> extends AbstractLocalGraphDisambiguator<T, U>
		implements LocalGraphDisambiguator<T, U> {

	/** According to the founder of HITS (Kleinberg, 1999), 20 is sufficient for stable values in most cases. */
	private static final int DEFAULT_ITERATIONS = 20;

	private static final double DEFAULT_ALPHA = 0;

	private final double alpha;
	private final int iterations;

	public HITSCentrality(GraphType graphType, EdgeWeights graphWeights, boolean usePriorFallback, double alpha,
			int iterations) {
		super(graphType, graphWeights, usePriorFallback);
		this.alpha = alpha;
		this.iterations = iterations;
	}

	public HITSCentrality(GraphType graphType, EdgeWeights graphWeights, Boolean usePriorFallback) {
		this(graphType, graphWeights, usePriorFallback, DEFAULT_ALPHA, DEFAULT_ITERATIONS);
	}

	class HITSVertexScorer implements VertexScorer<Vertex, Double> {

		private final HITS<Vertex, Edge> hits;

		// private final Map<Vertex, HitsScores> hitsScores;

		public HITSVertexScorer(Graph subgraph) {
			GraphJung<Graph> graphJung = Graphs.asGraphJung(graphType, subgraph);
			hits = new HITS<Vertex, Edge>(graphJung, edgeWeights, alpha);
			hits.acceptDisconnectedGraph(true);
			hits.setMaxIterations(iterations);
			hits.evaluate();
			// hitsScores = calculateHitsScores(subgraph);
		}

		@Override
		public Double getVertexScore(Vertex v) {
			Scores scores = hits.getVertexScore(v);
			// HitsScores scores = hitsScores.get(v);
			// assign authority of 0 if v is not connected to any other vertex.
			double authority = Graphs.vertexHasNoNeighbours(v) ? 0 : scores.authority;
			return authority;
		}

		@Override
		public String toString() {
			return new StringBuilder(super.toString()).append(" (alpha: ").append(alpha).append(", iterations: ")
					.append(iterations).append(")").toString();
		}
	}

	@Override
	protected VertexScorer<Vertex, Double> getVertexScorer(Graph subgraph) {
		return new HITSVertexScorer(subgraph);
	}

}

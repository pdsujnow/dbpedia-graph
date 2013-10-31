package de.unima.dws.dbpediagraph.graphdb.disambiguate.global;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.tinkerpop.blueprints.Graph;

import de.unima.dws.dbpediagraph.graphdb.Graphs;
import de.unima.dws.dbpediagraph.graphdb.disambiguate.AbstractGlobalGraphDisambiguator;
import de.unima.dws.dbpediagraph.graphdb.disambiguate.GlobalGraphDisambiguator;
import de.unima.dws.dbpediagraph.graphdb.model.Sense;
import de.unima.dws.dbpediagraph.graphdb.model.SurfaceForm;
import de.unima.dws.dbpediagraph.graphdb.subgraph.SubgraphConstructionSettings;

/**
 * Edge density global connectivity measure implemented as described in Navigli&Lapata (2010).
 * 
 * @author Bernhard Schäfer
 * 
 */
public class EdgeDensity<T extends SurfaceForm, U extends Sense> extends AbstractGlobalGraphDisambiguator<T, U>
		implements GlobalGraphDisambiguator<T, U> {

	public EdgeDensity(SubgraphConstructionSettings subgraphConstructionSettings) {
		super(subgraphConstructionSettings);
	}

	@Override
	public double globalConnectivityMeasure(Graph sensegraph) {
		int totalEdges = Graphs.numberOfEdges(checkNotNull(sensegraph));
		checkArgument(totalEdges != 0, " the provided graph cannot contain 0 vertices.");
		int totalVertices = Graphs.numberOfVertices(sensegraph);
		// binomial (v over 2) === v * (v-1) / 2
		double edgesCompleteGraph = (totalVertices * (totalVertices - 1)) / 2.0;
		return totalEdges / edgesCompleteGraph;
	}

}

package de.unima.dws.dbpediagraph.graphdb.disambiguate;

import java.util.Collection;
import java.util.List;

import com.tinkerpop.blueprints.Graph;

public interface Disambiguator {
	List<WeightedUri> disambiguate(Collection<String> uris, Graph subGraph);
}
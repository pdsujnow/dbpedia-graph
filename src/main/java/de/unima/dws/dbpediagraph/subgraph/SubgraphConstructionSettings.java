package de.unima.dws.dbpediagraph.subgraph;

import org.apache.commons.configuration.Configuration;

import de.unima.dws.dbpediagraph.graph.GraphType;

/**
 * Immutable holder class for parameters relevant for constructing a subgraph using a {@link SubgraphConstruction}.
 * Instances are created using the internal {@link Builder} class.
 * 
 * @author Bernhard Schäfer
 * 
 */
public final class SubgraphConstructionSettings {
	// keys in configuration file
	private static final String CONFIG_BASE = "de.unima.dws.dbpediagraph.subgraph.";
	private static final String CONFIG_MAX_DISTANCE = CONFIG_BASE + "maxDistance";
	private static final String CONFIG_GRAPH_TYPE = CONFIG_BASE + "graphType";
	private static final String CONFIG_EXPLORATION_THRESHOLD = CONFIG_BASE + "explorationThreshold";
	private static final String CONFIG_PERSIST_SUBGRAPH = CONFIG_BASE + "persistSubgraph";
	private static final String CONFIG_PERSIST_SUBGRAPH_DIRECTORY = CONFIG_BASE + "persistSubgraphDirectory";

	/**
	 * Instance with default settings
	 */
	private static final SubgraphConstructionSettings DEFAULT = new SubgraphConstructionSettings.Builder().build();

	final ExplorationThreshold explorationThreshold;
	public final GraphType graphType;
	/**
	 * "the distance between two vertices in a graph is the <i>number of edges</i> in a shortest path connecting them."
	 * 
	 * @see <a
	 *      href="http://en.wikipedia.org/wiki/Distance_(graph_theory)">http://en.wikipedia.org/wiki/Distance_(graph_theory)</a>
	 */
	final int maxDistance;

	final boolean persistSubgraph; // obviously this setting should not be used in a productive system
	final String persistSubgraphDirectory;

	private SubgraphConstructionSettings(Builder builder) {
		this.explorationThreshold = builder.explorationThreshold;
		this.graphType = builder.graphType;
		this.maxDistance = builder.maxDistance;
		this.persistSubgraph = builder.persistSubgraph;
		this.persistSubgraphDirectory = builder.persistSubgraphDirectory;
	}

	/**
	 * Get the instance with default settings
	 */
	public static SubgraphConstructionSettings getDefault() {
		return DEFAULT;
	}

	public static SubgraphConstructionSettings fromConfig(Configuration config) {
		SubgraphConstructionSettings.Builder builder = new SubgraphConstructionSettings.Builder();

		Integer maxDistance = config.getInt(CONFIG_MAX_DISTANCE);
		if (maxDistance != null)
			builder.maxDistance(maxDistance);

		String graphTypeString = config.getString(CONFIG_GRAPH_TYPE);
		if (graphTypeString != null) {
			GraphType graphType = GraphType.valueOf(graphTypeString);
			builder.graphType(graphType);
		}

		String explorationThresholdClassName = config.getString(CONFIG_EXPLORATION_THRESHOLD);
		if (explorationThresholdClassName != null) {
			// TODO implement
		}

		boolean persistSubgraph = config.getBoolean(CONFIG_PERSIST_SUBGRAPH, false);
		builder.persistSubgraph(persistSubgraph);

		if (persistSubgraph) {
			String persistSubgraphDirectory = config.getString(CONFIG_PERSIST_SUBGRAPH_DIRECTORY);
			if (persistSubgraphDirectory != null)
				builder.persistSubgraphDirectory(persistSubgraphDirectory);
		}

		return builder.build();
	}

	public static class Builder {
		// parameters are initialized to default values
		private ExplorationThreshold explorationThreshold = DegreeThreshold.getDefault();
		private GraphType graphType = GraphType.DIRECTED_GRAPH;
		private int maxDistance = 4;
		private boolean persistSubgraph = false;
		private String persistSubgraphDirectory = "";

		public SubgraphConstructionSettings build() {
			return new SubgraphConstructionSettings(this);
		}

		public Builder graphType(GraphType graphType) {
			this.graphType = graphType;
			return this;
		}

		public Builder maxDistance(int maxDistance) {
			this.maxDistance = maxDistance;
			return this;
		}

		public Builder explorationThreshold(ExplorationThreshold explorationThreshold) {
			this.explorationThreshold = explorationThreshold;
			return this;
		}

		public Builder persistSubgraph(boolean persistSubgraph) {
			this.persistSubgraph = persistSubgraph;
			return this;
		}

		public Builder persistSubgraphDirectory(String persistSubgraphDirectory) {
			this.persistSubgraphDirectory = persistSubgraphDirectory;
			return this;
		}
	}

}

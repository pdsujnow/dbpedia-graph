package de.unima.dws.dbpediagraph.graphdb.demo;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import com.tinkerpop.blueprints.*;
import com.tinkerpop.blueprints.oupls.jung.GraphJung;

import de.unima.dws.dbpediagraph.graphdb.*;
import de.unima.dws.dbpediagraph.graphdb.GraphFactory;
import de.unima.dws.dbpediagraph.graphdb.disambiguate.GraphDisambiguator;
import de.unima.dws.dbpediagraph.graphdb.disambiguate.local.*;
import de.unima.dws.dbpediagraph.graphdb.model.*;
import de.unima.dws.dbpediagraph.graphdb.subgraph.*;
import de.unima.dws.dbpediagraph.graphdb.util.FileUtils;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;

/**
 * {@link SubgraphConstruction} demo for visualizing the created subgraph.
 * 
 * @author Bernhard Schäfer
 * 
 */
public class DemoSubgraphConstruction {

	private static final int MAX_DISTANCE = 4;
	private static final GraphType GRAPH_TYPE = GraphType.DIRECTED_GRAPH;

	private static final ModelFactory<DefaultSurfaceForm, DefaultSense> factory = DefaultModelFactory.INSTANCE;
	private static final Collection<GraphDisambiguator<DefaultSurfaceForm, DefaultSense>> disambiguators;
	static {
		disambiguators = new ArrayList<>();
		disambiguators.add(new BetweennessCentrality<DefaultSurfaceForm, DefaultSense>(GRAPH_TYPE, factory));
		disambiguators.add(new DegreeCentrality<DefaultSurfaceForm, DefaultSense>(GRAPH_TYPE, factory));
		disambiguators.add(new HITSCentrality<DefaultSurfaceForm, DefaultSense>(GRAPH_TYPE, factory));
		disambiguators.add(new KPPCentrality<DefaultSurfaceForm, DefaultSense>(GRAPH_TYPE, factory));
		disambiguators.add(new PageRankCentrality<DefaultSurfaceForm, DefaultSense>(GRAPH_TYPE, factory));
	}

	private static final Dimension SCREEN_DIMENSION;
	static {
		double percentageOfScreen = 0.95;
		int height = (int) (percentageOfScreen * GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getMaximumWindowBounds().height);
		int width = (int) (percentageOfScreen * GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getMaximumWindowBounds().width);
		SCREEN_DIMENSION = new Dimension(width, height);
	}

	private static <T extends SurfaceForm, U extends Sense> void demo(Graph graph, Map<T, List<U>> surfaceFormsSenses,
			Collection<GraphDisambiguator<T, U>> disambiguators) {
		SubgraphConstruction sc = SubgraphConstructionFactory.newSubgraphConstruction(graph,
				new SubgraphConstructionSettings.Builder().maxDistance(MAX_DISTANCE).graphType(GRAPH_TYPE).build());
		
		Collection<Collection<Vertex>> surfaceFormVertices = ModelTransformer.wordsVerticesFromSenses(graph,
				surfaceFormsSenses);
		Graph subGraph = sc.createSubgraph(surfaceFormVertices);

		for (GraphDisambiguator<T, U> d : disambiguators) {
			System.out.println(d);

			List<SurfaceFormSenseScore<T, U>> senseScores = d.disambiguate(surfaceFormsSenses, subGraph);
			for (SurfaceFormSenseScore<T, U> senseScore : senseScores)
				System.out
						.printf("  %s (%.2f)", UriShortener.shorten(senseScore.sense().fullUri()), senseScore.score());
			System.out.println();
		}

		visualizeGraph(subGraph, sc.getClass().getSimpleName() + " (max distance: " + MAX_DISTANCE + ")");

		subGraph.shutdown();
		graph.shutdown();
	}

	public static void main(String[] args) throws IOException, URISyntaxException {
		String sensesFileName = "/napoleon-sentence-test";
		// String sensesFileName = "/dbpedia-default-sentence-test";
		Map<DefaultSurfaceForm, List<DefaultSense>> wordsSensesString = FileUtils.surfaceFormsSensesFromFile(
				DemoSubgraphConstruction.class, sensesFileName, GraphConfig.DBPEDIA_RESOURCE_PREFIX, factory);

		Graph graph = GraphFactory.getDBpediaGraph();
		demo(graph, wordsSensesString, disambiguators);
	}

	private static void visualizeGraph(Graph graph, String frameTitle) {
		GraphJung<Graph> graphJung = new GraphJung<>(graph);
		// Layout<Vertex, Edge> layout = new CircleLayout<Vertex,
		// Edge>(graphJung);
		Layout<Vertex, Edge> layout = new ISOMLayout<Vertex, Edge>(graphJung);

		layout.setSize(SCREEN_DIMENSION);
		BasicVisualizationServer<Vertex, Edge> viz = new BasicVisualizationServer<Vertex, Edge>(layout);
		viz.setPreferredSize(SCREEN_DIMENSION);

		Transformer<Vertex, String> vertexLabelTransformer = new Transformer<Vertex, String>() {
			@Override
			public String transform(Vertex vertex) {
				return vertex.getProperty(GraphConfig.URI_PROPERTY).toString();
			}
		};

		Transformer<Edge, String> edgeLabelTransformer = new Transformer<Edge, String>() {
			@Override
			public String transform(Edge edge) {
				String uriProp = edge.getProperty(GraphConfig.URI_PROPERTY);
				return uriProp != null ? uriProp : "";
			}
		};

		viz.getRenderContext().setEdgeLabelTransformer(edgeLabelTransformer);
		viz.getRenderContext().setVertexLabelTransformer(vertexLabelTransformer);

		JFrame frame = new JFrame(frameTitle);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(viz);
		frame.pack();
		frame.setVisible(true);

	}
}

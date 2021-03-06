package de.unima.dws.dbpediagraph.loader;

import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.tinkerpop.blueprints.*;
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph;

import de.unima.dws.dbpediagraph.graph.GraphConfig;
import de.unima.dws.dbpediagraph.graph.UriTransformer;

/**
 * Blueprints compatible batch handler for creating a graph from RDF files. Uses {@link BatchGraph} and the provided
 * buffer size. See <a href="https://github.com/tinkerpop/blueprints/wiki/Batch-Implementation}"
 * >https://github.com/tinkerpop/blueprints/wiki/Batch-Implementation</a>
 * 
 * @author Bernhard Schäfer
 * 
 */
public class DBpediaBatchHandler extends RDFHandlerVerbose {

	/** Log measures every TICK_SIZE time */
	private static final int TICK_SIZE = 1_000_000;

	/** The graph to persist to */
	private final Graph bgraph;

	/** Start logging time once the object has been created */
	private final Stopwatch tickTime = Stopwatch.createStarted();

	/** the triple filter that decides if a triple is valid */
	private final Predicate<Triple> tripleFilter;

	private static final Logger logger = LoggerFactory.getLogger(DBpediaBatchHandler.class);

	/**
	 * Initialize the handler with a graph object the statements should be added to.
	 */
	public DBpediaBatchHandler(Graph graph, Predicate<Triple> tripleFilter) {
		this.bgraph = graph;
		this.tripleFilter = tripleFilter;
	}

	/**
	 * Adds a vertex with the uri as property to the graph if it does not exist yet.
	 */
	public Vertex addVertexByUriBatchIfNonExistent(String uri) {
		/*
		 * Neo4j ignores supplied ids, this means internal ids are autogenerated. However, batch graph has an id cache
		 * so that addVertex() works as expected. (https://github.com/tinkerpop/blueprints/wiki/Neo4j-Implementation)
		 */
		Vertex v = bgraph.getVertex(uri);
		if (v == null) {
			v = bgraph.addVertex(uri);
			v.setProperty(GraphConfig.URI_PROPERTY, uri);
		}
		return v;
	}

	@Override
	public void handleStatement(Statement st) {
		if (st.getObject() instanceof Literal)
			// shortcut to prevent object creation
			invalidTriples++;
		else {
			Triple triple = Triple.fromStatement(st);
			if (!tripleFilter.apply(triple))
				invalidTriples++;
			else {
				validTriples++;
				String subject = UriTransformer.shorten(triple.subject());
				String predicate = UriTransformer.shorten(triple.predicate());
				String object = UriTransformer.shorten(triple.object());

				Vertex out = addVertexByUriBatchIfNonExistent(subject);
				Vertex in = addVertexByUriBatchIfNonExistent(object);
				Edge e = bgraph.addEdge(null, out, in, GraphConfig.EDGE_LABEL);
				e.setProperty(GraphConfig.URI_PROPERTY, predicate);
			}
		}

		// logging metrics
		long totalTriples = validTriples + invalidTriples;
		if (totalTriples % TICK_SIZE == 0) {
			logger.info(String.format("triples: %,d (valid: %,d, invalid: %,d) @ %s / %,d triples.", totalTriples,
					validTriples, invalidTriples, tickTime, TICK_SIZE));
			tickTime.reset().start();
		}
	}

	@Override
	public void endRDF() throws RDFHandlerException {
	}

	@Override
	public void handleComment(String paramString) throws RDFHandlerException {
	}

	@Override
	public void handleNamespace(String paramString1, String paramString2) throws RDFHandlerException {
	}

	@Override
	public void startRDF() throws RDFHandlerException {
	}
}

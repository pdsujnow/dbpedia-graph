package de.unima.dws.dbpediagraph.loader;

import java.io.*;
import java.util.*;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.openrdf.rio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.tinkerpop.blueprints.Graph;

import de.unima.dws.dbpediagraph.graph.GraphConfig;
import de.unima.dws.dbpediagraph.graph.GraphFactory;
import de.unima.dws.dbpediagraph.util.FileUtils;
import de.unima.dws.dbpediagraph.util.LoadingMetrics;
import de.unima.dws.dbpediagraph.weights.EdgeWeightsFactory.EdgeWeightsType;
import de.unima.dws.dbpediagraph.weights.PredObjOccsCounter;

/**
 * Main class for importing DBpedia files into a {@link Graph}.
 * 
 * @author Bernhard Schäfer
 * 
 */
public final class DBpediaGraphLoader {

	/**
	 * The buffer size for batch importing. Every time the buffer fills up its vertices and edges are persisted to the
	 * graph.
	 */
	private static final long BUFFER_SIZE = 100_000;

	private static final Logger logger = LoggerFactory.getLogger(DBpediaGraphLoader.class);

	private static RDFParser getParserForFormat(RDFFormat rdfFormat) {
		RDFParser rdfParser = Rio.createParser(rdfFormat);
		rdfParser.setStopAtFirstError(false);
		rdfParser.setParseErrorListener(new ParseErrorListener() {
			@Override
			public void error(String msg, int lineNo, int colNo) {
				logger.warn("Error {} at line {} and colNo {}", msg, lineNo, colNo);
			}

			@Override
			public void fatalError(String msg, int lineNo, int colNo) {
				logger.warn("Fatal Error {} at line {} and colNo {}", msg, lineNo, colNo);
			}

			@Override
			public void warning(String msg, int lineNo, int colNo) {
				logger.debug("Warning {} at line {} and colNo {}", msg, lineNo, colNo);
			}
		});
		return rdfParser;
	}

	/**
	 * Generates a {@link Graph} by parsing the provided RDF files.
	 */
	public static void loadFromFiles(Collection<File> files, Configuration config) throws ConfigurationException {
		if (files == null || files.size() == 0) {
			throw new IllegalArgumentException("Provide one or several directories or files in RDF Format.");
		}

		// track loading metrics
		LoadingMetrics globalMetric = new LoadingMetrics("OVERALL");
		List<LoadingMetrics> metrics = new LinkedList<LoadingMetrics>();

		Graph graph = GraphFactory.getBatchGraphFromConfig(config, BUFFER_SIZE);

		for (File f : files) {
			LoadingMetrics metric = new LoadingMetrics(f.getName());

			// get appropriate handler
			Predicate<Triple> filter = TriplePredicate.fromConfig(config);
			RDFHandlerVerbose handler = new DBpediaBatchHandler(graph, filter);

			// get appropriate parser
			RDFFormat rdfFormat = RDFFormat.forFileName(f.getName());
			if (rdfFormat == null) {
				logger.warn("File {} could not be parsed since it has no valid RDF format file ending.", f.getName());
				continue;
			}
			RDFParser rdfParser = getParserForFormat(rdfFormat);

			// assign handler to parser
			rdfParser.setRDFHandler(handler);

			// parse file
			try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
				rdfParser.parse(reader, "");
			} catch (RDFParseException | RDFHandlerException | IOException e) {
				logger.error("Error while parsing file " + f.getName(), e);
			}

			// log metrics
			metric.finish(handler.getValidTriples(), handler.getInvalidTriples());
			metrics.add(metric);
		}

		graph.shutdown();

		globalMetric.finish(metrics);
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 *            each arg can be a directory containing RDF files or a RDF file itself.
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws ConfigurationException, FileNotFoundException {
		Configuration config = GraphConfig.config();
		DBpediaGraphLoader.loadFromFiles(FileUtils.extractFilesFromArgs(args), config);
		if (EdgeWeightsType.fromConfig(config) != EdgeWeightsType.DUMMY)
			PredObjOccsCounter.countAndPersistDBpediaGraphOccs(GraphFactory.getDBpediaGraph());
	}
}

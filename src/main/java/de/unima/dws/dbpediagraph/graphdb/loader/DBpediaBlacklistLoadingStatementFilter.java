package de.unima.dws.dbpediagraph.graphdb.loader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.openrdf.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unima.dws.dbpediagraph.graphdb.GraphConfig;
import de.unima.dws.dbpediagraph.graphdb.util.FileUtils;

/**
 * Custom blacklist statement filter which uses blacklists for subjects, predicates and objects.
 * 
 * @author Bernhard Schäfer
 */
public class DBpediaBlacklistLoadingStatementFilter implements LoadingStatementFilter {
	private static final Logger logger = LoggerFactory.getLogger(DBpediaBlacklistLoadingStatementFilter.class);

	// Blacklists from "Exploiting Linked Data for Semantic Document Modelling"
	private static final String KEY_CATEGORIES_FILE = "loading.filter.categories.file";
	private static final String KEY_PREDICATES_FILE = "loading.filter.predicates.file";

	private final HashSet<String> categories;
	private final HashSet<String> predicates;

	public DBpediaBlacklistLoadingStatementFilter() {
		Configuration conf = GraphConfig.config();

		categories = new HashSet<String>();
		predicates = new HashSet<String>();

		String categoriesFileName = conf.getString(KEY_CATEGORIES_FILE);
		try {
			categories.addAll(FileUtils.readRelevantLinesFromFile(getClass(), categoriesFileName));
		} catch (URISyntaxException | IOException e) {
			logger.warn("Category filter could not be loaded.", e);
		}

		String predicatesFileName = conf.getString(KEY_PREDICATES_FILE);
		try {
			predicates.addAll(FileUtils.readRelevantLinesFromFile(getClass(), predicatesFileName));
		} catch (URISyntaxException | IOException e) {
			logger.warn("Predicate filter could not be loaded.", e);
		}
	}

	private static boolean isStatementUriInBlacklist(Statement st, Set<String> blacklist) {
		return blacklist.contains(st.getSubject().stringValue()) || blacklist.contains(st.getPredicate().stringValue())
				|| blacklist.contains(st.getObject().stringValue());
	}

	@Override
	public boolean isValidStatement(Statement st) {
		// TODO check if enough to look at statement subject and object
		boolean validCategory = !(isStatementUriInBlacklist(st, categories));
		// TODO check if enough to look at statement predicate
		boolean validPredicate = !(isStatementUriInBlacklist(st, predicates));
		return validCategory && validPredicate;
	}

}

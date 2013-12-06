package de.unima.dws.dbpediagraph.loader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;

import de.unima.dws.dbpediagraph.util.FileUtils;

/**
 * Custom blacklist triple filter which uses blacklists for subjects, predicates and objects.
 * 
 * @author Bernhard Schäfer
 */
class BlacklistTriplePredicate implements Predicate<Triple> {
	private static final Logger logger = LoggerFactory.getLogger(BlacklistTriplePredicate.class);

	// Blacklists from "Exploiting Linked Data for Semantic Document Modelling"
	private static final String CONFIG_CATEGORIES_FILE = "loading.filter.categories.file";
	private static final String CONFIG_PREDICATES_FILE = "loading.filter.predicates.file";

	private final HashSet<String> categories;
	private final HashSet<String> predicates;

	BlacklistTriplePredicate(Configuration config) {
		categories = new HashSet<String>();
		predicates = new HashSet<String>();

		String categoriesFileName = config.getString(CONFIG_CATEGORIES_FILE);
		try {
			categories.addAll(FileUtils.readNonEmptyNonCommentLinesFromFile(getClass(), categoriesFileName));
		} catch (URISyntaxException | IOException e) {
			logger.warn("Category filter could not be loaded.", e);
		}

		String predicatesFileName = config.getString(CONFIG_PREDICATES_FILE);
		try {
			predicates.addAll(FileUtils.readNonEmptyNonCommentLinesFromFile(getClass(), predicatesFileName));
		} catch (URISyntaxException | IOException e) {
			logger.warn("Predicate filter could not be loaded.", e);
		}
	}

	private static boolean isTripleUriInBlacklist(Triple t, Set<String> blacklist) {
		return blacklist.contains(t.subject()) || blacklist.contains(t.predicate()) || blacklist.contains(t.object());
	}

	@Override
	public boolean apply(Triple t) {
		// TODO check if enough to look at triple subject and object
		boolean validCategory = !(isTripleUriInBlacklist(t, categories));
		// TODO check if enough to look at triple predicate
		boolean validPredicate = !(isTripleUriInBlacklist(t, predicates));
		return validCategory && validPredicate;
	}

}
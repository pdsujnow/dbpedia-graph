package de.unima.dws.dbpediagraph.graphdb.filter;

import org.openrdf.model.Statement;

/**
 * RDF Statement (Triple) filter functionalities to determine whether statements
 * are valid.
 * 
 * @author Bernhard Schäfer
 * 
 */
public interface LoadingStatementFilter {

	/**
	 * Predicate that decides if a statement is valid or not.
	 */
	boolean isValidStatement(Statement st);

}
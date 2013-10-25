package de.unima.dws.dbpediagraph.graphdb.loader;

import org.openrdf.model.Literal;
import org.openrdf.model.Statement;

/**
 * Filter that yields all statement with a DBpedia URI as object (http://dbpedia.org/*). This means that statements with
 * DBpedia resources, ontologies, or yago classes as object are included. NOTE: The class hierarchy is only partly
 * contained (e.g. foaf:Person missing)
 * 
 * @author Bernhard Schäfer
 * 
 */
class DBpediaRelaxedLoadingStatementFilter implements LoadingStatementFilter {

	@Override
	public boolean isValidStatement(Statement st) {
		// continue if object is literal
		if (st.getObject() instanceof Literal) {
			return false;
		}

		if (!st.getObject().stringValue().startsWith("http://dbpedia.org/")) {
			return false;
		}
		return true;
	}

}
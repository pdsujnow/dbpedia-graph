package de.unima.dws.dbpediagraph.graphdb.disambiguate.global;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.unima.dws.dbpediagraph.graphdb.GlobalDisambiguationTestData;
import de.unima.dws.dbpediagraph.graphdb.subgraph.SubgraphConstructionNavigliOld;

public class TestCompactness {
	private static GlobalDisambiguationTestData data;

	@BeforeClass
	public static void setUp() {
		data = new GlobalDisambiguationTestData(new Compactness(), new SubgraphConstructionNavigliOld());
	}

	@AfterClass
	public static void tearDown() {
		if (data != null)
			data.close();
	}

	@Test
	public void testDisambiguateValues() {
		data.checkDisambiguationResults();
	}

}
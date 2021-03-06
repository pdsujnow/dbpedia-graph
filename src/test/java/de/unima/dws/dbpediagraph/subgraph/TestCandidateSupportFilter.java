package de.unima.dws.dbpediagraph.subgraph;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;

import de.unima.dws.dbpediagraph.model.*;

/**
 * @author Bernhard Schäfer
 */
public class TestCandidateSupportFilter {

	private static final Sense sense1 = new DefaultSense("uri1", 0.05, 20);
	private static final Sense sense2 = new DefaultSense("uri2", 0.05, 10);
	private static final Sense sense3 = new DefaultSense("uri3", 0.1, 5);
	private static final Sense sense4 = new DefaultSense("uri4", 0.2, 3);
	private static final Sense sense5 = new DefaultSense("uri5", 0.3, 0);

	private static final SurfaceForm surfaceForm1 = new DefaultSurfaceForm("sf1");

	private static final Map<SurfaceForm, List<Sense>> sfsSenses = new HashMap<>();
	static {
		sfsSenses.put(surfaceForm1, Arrays.asList(sense1, sense2, sense3, sense4, sense5));
	}

	@Test
	public void testMaxKByPrior() {
		Map<SurfaceForm, List<Sense>> sfssSenses = CandidateFilter.maxKByPrior(sfsSenses, 3);
		assertEquals(1, sfssSenses.size());
		List<Sense> best3 = sfssSenses.get(surfaceForm1);
		assertEquals(3, best3.size());
		assertTrue(best3.contains(sense3));
		assertTrue(best3.contains(sense4));
		assertTrue(best3.contains(sense5));
	}

	@Test
	public void testByMinSupport() {
		Map<SurfaceForm, List<Sense>> sfssSenses = CandidateFilter.byMinSupport(sfsSenses, 10);
		assertEquals(1, sfssSenses.size());
		List<Sense> minSupport10 = sfssSenses.get(surfaceForm1);
		assertEquals(2, minSupport10.size());
		assertTrue(minSupport10.contains(sense1));
		assertTrue(minSupport10.contains(sense2));
	}

}

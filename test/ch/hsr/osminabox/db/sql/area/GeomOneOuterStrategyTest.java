package ch.hsr.osminabox.db.sql.area;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.entities.Area.WayRole;
import ch.hsr.osminabox.db.entities.exceptions.InvalidWayException;
import ch.hsr.osminabox.db.sql.area.exceptions.NoWayValuesException;
import ch.hsr.osminabox.db.sql.util.GeomUtil;
import ch.hsr.osminabox.db.util.AreaCompositionDetector;

public class GeomOneOuterStrategyTest extends EasyMockSupport {

	private GeomUtil util;
	private AreaCompositionDetector detector;
	private GeomOneOuterStrategy strategy;
	@Before
	public void setUp() throws Exception {
		util = createMock(GeomUtil.class);
		detector = createMock(AreaCompositionDetector.class);
		strategy = new GeomOneOuterStrategy(util);
		strategy.areaCompositionDetector = detector;
	}

	@Test
	public void testGetGeom() throws InvalidWayException, NoWayValuesException {
		Area a = new Area(1);
		Way w1 = new Way();
		Way w2 = new Way();
		Way w3 = new Way();
		a.ways.put(w1, WayRole.outer);
		a.ways.put(w2, WayRole.inner);
		a.ways.put(w3, WayRole.inner);
		expect(detector.contains(AreaCompositionDetector.OUTER_WAYROLES, WayRole.outer)).andReturn(true);
		expect(util.getLonLatForGeom(w1)).andReturn(new StringBuffer("1,2;3,4"));
		expect(detector.contains(AreaCompositionDetector.OUTER_WAYROLES, WayRole.inner)).andReturn(false);
		expect(detector.contains(AreaCompositionDetector.INNER_WAYROLES, WayRole.inner)).andReturn(true);
		expect(util.getLonLatForGeom(w2)).andReturn(new StringBuffer("5,6;7,8"));
		expect(detector.contains(AreaCompositionDetector.OUTER_WAYROLES, WayRole.inner)).andReturn(false);
		expect(detector.contains(AreaCompositionDetector.INNER_WAYROLES, WayRole.inner)).andReturn(true);
		expect(util.getLonLatForGeom(w3)).andReturn(new StringBuffer("9,10;11,12"));
		replayAll();
		String res = strategy.getGeom(a).toString();
		verifyAll();
		assertTrue(res.contains("(1,2;3,4)"));
		assertTrue(res.contains("(5,6;7,8)"));
		assertTrue(res.contains("(9,10;11,12)"));
	}

}

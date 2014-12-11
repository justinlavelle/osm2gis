package ch.hsr.osminabox.db.sql.area;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.entities.Area.WayRole;
import ch.hsr.osminabox.db.entities.exceptions.InvalidWayException;
import ch.hsr.osminabox.db.sql.area.exceptions.NoWayValuesException;
import ch.hsr.osminabox.db.sql.util.GeomUtil;
import ch.hsr.osminabox.test.WayCreator;

public class GeomOnlyOuterStrategyTest extends EasyMockSupport {

	private GeomOnlyOuterStrategy strategy;
	private GeomUtil util;
	@Before
	public void setUp() throws Exception {
		util = createMock(GeomUtil.class);
		strategy = new GeomOnlyOuterStrategy(util);
	}

	@Test(expected=NoWayValuesException.class)
	public void testGetGeomWithNoWays() throws NoWayValuesException, InvalidWayException {
		Area area = new Area(2);
		strategy.getGeom(area);
	}

	@Test
	public void testGetGeom() throws NoWayValuesException, InvalidWayException {
		Area area = new Area(1);
		Way way = WayCreator.create(1).finish();
		area.ways.put(way, WayRole.outer);
		expect(util.getLonLatForGeom(way)).andReturn(new StringBuffer("12,14;15,3"));
		replayAll();
		String res = strategy.getGeom(area).toString();
		verifyAll();
		assertEquals("GeomFromText('MULTIPOLYGON(((12,14;15,3)))',4326)", res);
	}
	
	@Test
	public void testGeoGeomWithMoreWays() throws Exception {
		Area area = new Area(1);
		Way way1 = WayCreator.create(1).finish();
		area.ways.put(way1, WayRole.outer);
		Way way2 = WayCreator.create(2).finish();
		area.ways.put(way2, WayRole.outer);
		expect(util.getLonLatForGeom(way1)).andReturn(new StringBuffer("12,14;15,3"));
		expect(util.getLonLatForGeom(way2)).andReturn(new StringBuffer("10,12;13,1"));
		replayAll();
		String res = strategy.getGeom(area).toString();
		verifyAll();
		assertTrue(res.startsWith("GeomFromText('MULTIPOLYGON((("));
		assertTrue(res.contains("((12,14;15,3))"));
		assertTrue(res.contains("((10,12;13,1))"));
	}
	
}

package ch.hsr.osminabox.db.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.AreaComposition;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.entities.Area.WayRole;
import ch.hsr.osminabox.db.entities.exceptions.InvalidWayException;

public class AreaCompositionDetectorTest {

	private AreaCompositionDetector detector;
	@Before
	public void setUp() throws Exception {
		detector = new AreaCompositionDetector();
	}

	@Test
	public void testDetectWithAreaWithoutWays() throws InvalidWayException {
		assertEquals(AreaComposition.UNKNOWN, detector.detect(new Area(1)));
	}
	
	@Test
	public void testDetectForOuterOnly() throws InvalidWayException {
		Area area = new Area(1);
		area.ways.put(new Way(), WayRole.outer);
		area.ways.put(new Way(), WayRole.none);
		area.ways.put(new Way(), WayRole.exclave);
		assertEquals(AreaComposition.ONLY_OUTER, detector.detect(area));
		area.ways.put(new Way(), WayRole.enclave);
		assertEquals(AreaComposition.UNKNOWN, detector.detect(area));
	}
	
	@Test
	public void testDetectForOneOuterNInner() throws InvalidWayException {
		Area area = new Area(1);
		area.ways.put(new Way(), WayRole.outer);
		area.ways.put(new Way(), WayRole.inner);
		area.ways.put(new Way(), WayRole.enclave);
		assertEquals(AreaComposition.ONE_OUTER_N_INNER, detector.detect(area));
		area.ways.put(new Way(), WayRole.exclave);
		assertEquals(AreaComposition.UNKNOWN, detector.detect(area));
	}
	@Test
	public void testDetectForNOuterNInner() throws InvalidWayException {
		Area area = new Area(1);
		area.ways.put(new Way(), WayRole.outer);
		area.ways.put(new Way(), WayRole.exclave);
		area.ways.put(new Way(), WayRole.enclave);
		area.ways.put(new Way(), WayRole.inner);
		area.wayIds.put(1L, WayRole.outer);
		area.wayIds.put(2L, WayRole.exclave);
		area.wayIds.put(3L, WayRole.enclave);
		area.wayIds.put(4L, WayRole.inner);
		assertEquals(AreaComposition.N_INNER_N_OUTER, detector.detect(area));

	}

	@Test
	public void testContains() {
		assertFalse(detector.contains(new WayRole[]{}, WayRole.enclave));
		assertFalse(detector.contains(new WayRole[]{WayRole.exclave}, WayRole.enclave));
		assertFalse(detector.contains(new WayRole[]{WayRole.exclave, WayRole.exclave, WayRole.inner}, WayRole.enclave));
		assertTrue(detector.contains(new WayRole[]{WayRole.exclave, WayRole.enclave}, WayRole.enclave));
		assertTrue(detector.contains(new WayRole[]{WayRole.enclave}, WayRole.enclave));
	}

}

package ch.hsr.osminabox.db.sql.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.test.NodeCreator;
import ch.hsr.osminabox.test.WayCreator;

public class GeomValueCreatorTest {

	private GeomValueCreator creator;

	@Before
	public void setUp() throws Exception {
		creator = new GeomValueCreator();
	}

	@Test
	public void testAddGeomNode() {
		String res = creator.addGeom(NodeCreator.create(1, 45, 7).finish())
				.toString();
		assertEquals("GeomFromText('POINT(7.0 45.0)',4326)", res);
	}

	@Test
	public void testSimpleAddGeomWay() {
		String res = creator.addGeom(WayCreator.create(1).node(
				NodeCreator.create(2, 45, 7).finish()).finish()).toString();
		assertEquals("GeomFromText('LINESTRING(7.0 45.0)',4326)", res);
	}
	@Test
	public void testComplexAddGeomWay() {
		String res = creator.addGeom(WayCreator.create(1)
				.node(NodeCreator.create(2, 45, 7).finish())
				.node(NodeCreator.create(3, 44, 6).finish())
				.finish()).toString();
		assertEquals("GeomFromText('LINESTRING(7.0 45.0 , 6.0 44.0)',4326)", res);
	}

}

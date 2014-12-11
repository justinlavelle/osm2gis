package ch.hsr.osminabox.db.sql.util;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.test.NodeCreator;
import ch.hsr.osminabox.test.WayCreator;

public class GeomUtilTest {

	private GeomUtil util;

	@Before
	public void setUp() throws Exception {
		util = new GeomUtil();
	}

	@Test
	public void testComplexGetLonLatFromGeom() throws Exception {
		String result = util.getLonLatForGeom(WayCreator.create(1)
				.node(NodeCreator.create(2, 45, 8).finish())
				.node(NodeCreator.create(3, 44, 7).finish())
				.node(NodeCreator.create(4, 33, 6).finish())
				.finish()).toString();
		assertEquals("8.0 45.0 , 7.0 44.0 , 6.0 33.0", result);
	}
	
	@Test
	public void testSimpleGetLonLatFromGeom() throws Exception {
		String result = util.getLonLatForGeom(WayCreator.create(1)
				.node(NodeCreator.create(2, 45, 8).finish())
				.finish()).toString();
		assertEquals("8.0 45.0", result);
	}

}

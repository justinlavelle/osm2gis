package ch.hsr.osminabox.test.db.util;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;

import org.apache.log4j.xml.DOMConfigurator;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.entities.Area.WayRole;
import ch.hsr.osminabox.db.entities.exceptions.InvalidWayException;
import ch.hsr.osminabox.db.util.AreaConstructor;
import ch.hsr.osminabox.db.util.exceptions.RingAssignementFailed;

public class AreaConstructorTest extends EasyMockSupport {

	private AreaConstructor areaConstructor;

	Way way1; // closed
	Way way2;
	Way way3;
	Way way4;

	@Before
	public void setUp() throws Exception {
		DOMConfigurator.configure("config/LogingConfigurationUnitTests.xml");
		ApplicationContext contextMock = createMock(ApplicationContext.class);
		Connection connection = createMock(Connection.class);
		areaConstructor = new AreaConstructor(connection, contextMock);
	}

	@Test
	public void ringAssignementTest() throws InvalidWayException,
			RingAssignementFailed {
		Area area1 = constructArea1();
		assertEquals(4, area1.ways.size());
		areaConstructor.assignRings(area1);
		assertEquals(2, area1.ways.size());
	}

	@Test(expected = RingAssignementFailed.class)
	public void ringAssignementTest2() throws RingAssignementFailed,
			InvalidWayException {
		Area area1 = constructArea2();
		assertEquals(3, area1.ways.size());
		areaConstructor.assignRings(area1);
	}

	private Area constructArea1() throws InvalidWayException {
		constructNodeAndWays();
		Area area1 = new Area(123);
		area1.setOsmId(123);
		area1.ways.put(way2, WayRole.outer);
		area1.ways.put(way4, WayRole.outer);
		area1.ways.put(way1, WayRole.outer);
		area1.ways.put(way3, WayRole.outer);
		return area1;

	}

	private Area constructArea2() throws InvalidWayException {
		constructNodeAndWays();
		Area area1 = new Area(234);
		area1.setOsmId(234);
		area1.ways.put(way2, WayRole.outer);
		area1.ways.put(way4, WayRole.outer);
		area1.ways.put(way1, WayRole.outer);
		return area1;
	}

	private void constructNodeAndWays() {
		Node startNode1 = createNode(1);

		Node node4 = createNode(4);

		Node node5 = createNode(5);

		Node node7 = createNode(7);

		way1 = createWay(111);
		way1.nodes.add(startNode1);
		way1.nodes.add(createNode(2));
		way1.nodes.add(createNode(3));
		way1.nodes.add(startNode1);

		way2 = createWay(222);
		way2.nodes.add(node4);
		way2.nodes.add(node5);

		way3 = createWay(333);
		way3.nodes.add(node5);
		way3.nodes.add(createNode(6));
		way3.nodes.add(node7);

		way4 = createWay(444);
		way4.nodes.add(node4);
		way4.nodes.add(createNode(8));
		way4.nodes.add(createNode(9));
		way4.nodes.add(node7);
	}

	private Way createWay(int osmId) {
		Way way = new Way();
		way.setOsmId(osmId);
		return way;
	}

	private Node createNode(int osmId) {
		Node node1 = new Node();
		node1.setOsmId(osmId);
		return node1;
	}

}

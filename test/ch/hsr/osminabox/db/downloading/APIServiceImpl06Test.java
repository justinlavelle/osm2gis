package ch.hsr.osminabox.db.downloading;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.test.NodeCreator;
import ch.hsr.osminabox.test.Util;
import ch.hsr.osminabox.test.WayCreator;

public class APIServiceImpl06Test extends EasyMockSupport {

	private APIServiceImpl06 service;
	private XMLResponseParser parser;

	@Before
	public void setUp() throws Exception {
		parser = createMock(XMLResponseParser.class);
		service = createMockBuilder(APIServiceImpl06.class).addMockedMethod(
				"doAPICall").createMock();
		service.xmlResponseParser = parser;
	}

	@Test
	public void testRetrieveNodesWithOneNode() {
		List<Node> parsedNodes = new ArrayList<Node>();
		expect(
				service
						.doAPICall("http://api.openstreetmap.org/api/0.6/nodes?nodes=1"))
				.andReturn("Test".getBytes());
		expect(parser.parseNodes("Test")).andReturn(parsedNodes);
		replayAll();
		List<Node> nodes = service.retrieveNodes(Util.asList(NodeCreator
				.create(1).finish()));
		verifyAll();
		assertEquals(parsedNodes, nodes);
	}

	@Test
	public void testRetrieveNodesWithMoreNodes() {
		List<Node> parsedNodes = new ArrayList<Node>();
		expect(
				service
						.doAPICall("http://api.openstreetmap.org/api/0.6/nodes?nodes=1,2,3"))
				.andReturn("Test".getBytes());
		expect(parser.parseNodes("Test")).andReturn(parsedNodes);
		replayAll();
		List<Node> nodes = service.retrieveNodes(Util.asList(NodeCreator
				.create(1).finish(), NodeCreator.create(2).finish(),
				NodeCreator.create(3).finish()));
		verifyAll();
		assertEquals(parsedNodes, nodes);
	}

	@Test
	public void testRetrieveWayFull() {
		expect(
				service
						.doAPICall("http://api.openstreetmap.org/api/0.6/way/1/full"))
				.andReturn("Test".getBytes());
		Way parsedWay = WayCreator.create(1).finish();
		expect(parser.parseCompleteWay("Test")).andReturn(parsedWay);
		replayAll();
		Way way = service.retrieveWayFull(1);
		verifyAll();
		assertEquals(parsedWay, way);
	}

}

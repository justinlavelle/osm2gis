package ch.hsr.osminabox.db.util;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.BasicConfigurator;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.hsr.osminabox.db.downloading.EntityConsistencyService;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.test.NodeCreator;
import ch.hsr.osminabox.test.Util;
import ch.hsr.osminabox.test.WayCreator;

public class WayConstructorTest extends EasyMockSupport {

	private WayConstructor constructor;

	@BeforeClass
	public static void init(){
		BasicConfigurator.configure();
	}
	@Before
	public void setUp() throws Exception {
		constructor = new WayConstructor(null); // no connection, will be mocked
	}

	@Test
	public void testGetIdsAsArray() throws Exception {
		assertArrayEquals(new String[] { "1", "2" }, constructor
				.getIDsAsArray(Util.asList(NodeCreator.create(1).finish(),
						NodeCreator.create(2).finish())));
	}

	@Test
	public void testGetSQLForNodeSearch() throws Exception {
		assertEquals(
				// TODO: Why not use 'WHERE osm_id IN (1,2,3)' , maybe DBMS can
				// work faster with that
				"SELECT * FROM node_temp WHERE osm_id = 1 or osm_id = 2 or osm_id = 3",
				constructor.getSQLForNodeSearch(new String[] { "1", "2", "3" }));
	}

	@Test
	public void testCopyNodes() throws Exception {
		Map<Long, Node> tempNodes = Util.asMap(2L, NodeCreator.create(2)
				.attr("k1", "v1").finish(), 3L, NodeCreator.create(3).attr("k2",
				"v2").finish());
		List<Node> missingNodes = new ArrayList<Node>();
		Way way = WayCreator.create(1).node(NodeCreator.create(2).finish())
				.node(NodeCreator.create(3).finish()).node(
						NodeCreator.create(4).finish()).finish();
		assertEquals(2, constructor.copyNodes(way, tempNodes, missingNodes));
		assertEquals(1, missingNodes.size());
		assertEquals(4, missingNodes.get(0).getOsmId());
	}

	@Test
	public void testListToMap() throws Exception {
		Node n1 = NodeCreator.create(2).attr("k1", "v1").finish();
		Node n2 = NodeCreator.create(3).attr("k2", "v2").finish();
		List<Node> nodes = Util.asList(n1, n2);
		Map<Long, Node> map = constructor.listToMap(nodes);
		assertEquals(2, map.size());
		assertSame(n1, map.get(2L));
		assertSame(n2, map.get(3L));
	}

	@Test
	public void testAddNodeData() throws Exception {
		constructor = createMockBuilder(WayConstructor.class).addMockedMethod(
				"exec").createMock();
		EntityConsistencyService consistency = createMock(EntityConsistencyService.class);

		LinkedList<Node> nodes = new LinkedList<Node>();
		Node missingNode = NodeCreator.create(4).finish();
		nodes.addAll(Util.asList(NodeCreator.create(2).finish(), NodeCreator
				.create(3).finish(), missingNode));
		Way way = WayCreator.create(1).finish();
		way.nodes = nodes;
		expect(
				constructor
						.exec("SELECT * FROM node_temp WHERE osm_id = 2 or osm_id = 3 or osm_id = 4"))
				.andReturn(createResultSet());
		consistency.addMissingNodes(Util.asList(missingNode));
		LinkedList<Node> missingNodes = new LinkedList<Node>();
		missingNodes.add(NodeCreator.create(4, 56, 44).finish());
		expect(consistency.fetchMissingNodes()).andReturn(missingNodes);
		replayAll();
		boolean returnValue = constructor.addNodeData(way, consistency);
		verifyAll();
		assertTrue(returnValue);
	}

	@Test
	public void testAddNodeDataWithList() throws Exception {
		final Stack<Call> calls = new Stack<Call>();
		constructor = new WayConstructor(null){
			protected boolean addNodeData(Way way, EntityConsistencyService consistency){
				Call c = calls.pop();
				assertSame(c.way, way);
				assertSame(c.service, consistency);
				return c.result;
			}
		};
		EntityConsistencyService consistency = createMock(EntityConsistencyService.class);
		List<Way> ways = new ArrayList<Way>();
		Way w1 = new Way();
		Way w2 = new Way();
		w2.setOsmId(2);
		Way w3 = new Way();
		ways.add(w1);
		ways.add(w2);
		ways.add(w3);
		calls.push(new Call(w3, consistency, true));
		calls.push(new Call(w2, consistency, false));
		calls.push(new Call(w1, consistency, true));
		constructor.addNodeData(ways, consistency);
		assertEquals(2, ways.size());
		assertEquals(0, calls.size());
	}

	static class Call{
		Way way;
		EntityConsistencyService service;
		boolean result;
		public Call(Way way, EntityConsistencyService service, boolean result) {
			super();
			this.way = way;
			this.service = service;
			this.result = result;
		}
		
	}
	private ResultSet createResultSet() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expectNode(rs, 2, 45f, 8f);
		expectNode(rs, 3, 35f, 7f);
		expect(rs.next()).andReturn(false);
		return rs;
	}

	private void expectNode(ResultSet rs, long osmId, double lat, double lon)
			throws SQLException {
		expect(rs.next()).andReturn(true);
		expect(rs.getLong("osm_id")).andReturn(osmId);
		expect(rs.getDouble("lat")).andReturn(lat);
		expect(rs.getDouble("lon")).andReturn(lon);
	}
}

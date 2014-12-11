package ch.hsr.osminabox.db.util;

import static org.junit.Assert.assertEquals;
import static org.easymock.EasyMock.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.boundingbox.BoundingBoxStrategy;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.test.NodeCreator;
import ch.hsr.osminabox.test.Util;
import ch.hsr.osminabox.test.WayCreator;

public class NodeUtilTest extends EasyMockSupport {

	private NodeUtil util;

	@Before
	public void setUp() throws Exception {
		util = new NodeUtil(null, null);
	}

	@Test
	public void testAddLatLonToNodes() throws SQLException {
		util = createMockBuilder(NodeUtil.class).addMockedMethod("exec")
				.createMock();
		Way way = WayCreator.create(1L).node(NodeCreator.create(2L).finish())
				.node(NodeCreator.create(3L).finish()).finish();
		expect(
				util
						.exec("SELECT * FROM node_temp WHERE osm_id = 2 or osm_id = 3"))
				.andReturn(createResultSet());
		replayAll();
		util.addLatLonToNodes(way);
		verifyAll();
		assertEquals(2, way.nodes.size());
		assertNode(way.nodes.get(0), 2, "45.0", "8.0");
		assertNode(way.nodes.get(1), 3, "35.0", "7.0");

	}

	@Test
	public void testCreateNodesFromIds() {
		Way way = new Way();
		util.createNodesFromIds(way, Util.asArray("1", "2", "3"));
		assertEquals(3, way.nodes.size());
		assertEquals(1, way.nodes.get(0).getOsmId());
		assertEquals(2, way.nodes.get(1).getOsmId());
		assertEquals(3, way.nodes.get(2).getOsmId());
	}

	@Test
	public void testDeleteNodesOutsideBBox() {
		BoundingBoxStrategy bbs = createMock(BoundingBoxStrategy.class);
		Node n1 = NodeCreator.create(1, 45, 8).finish();
		Node n2 = NodeCreator.create(2, 30, 7).finish();
		List<Node> nodes = Util.asList(n1, n2);
		expect(bbs.visit(n1)).andReturn(false);
		expect(bbs.visit(n2)).andReturn(true);
		replayAll();
		util.deleteNodesOutsideBBox(nodes, bbs);
		verifyAll();
		assertEquals(1, nodes.size());
		assertEquals(n2, nodes.get(0));
	}

	@Test
	public void testSplitToModificationGroups() {

	}

	private void assertNode(Node node, long osmId, String lon, String lat) {
		assertEquals(osmId, node.getOsmId());
		assertEquals(lon, node.attributes.get(Node.NODE_LONGITUDE));
		assertEquals(lat, node.attributes.get(Node.NODE_LATITUDE));
	}

	private ResultSet createResultSet() throws SQLException {
		ResultSet res = createMock(ResultSet.class);
		expectNode(res, 2, 45.0f, 8.0f);
		expectNode(res, 3, 35.0f, 7.0f);
		expect(res.next()).andReturn(false);
		return res;
	}

	private void expectNode(ResultSet res, long osmId, double lon, double lat)
			throws SQLException {
		expect(res.next()).andReturn(true);
		expect(res.getLong("osm_id")).andReturn(osmId);
		expect(res.getDouble("lon")).andReturn(lon);
		expect(res.getDouble("lat")).andReturn(lat);
	}

}

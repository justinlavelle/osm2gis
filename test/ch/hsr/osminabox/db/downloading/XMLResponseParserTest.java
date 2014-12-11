package ch.hsr.osminabox.db.downloading;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.test.Util;

public class XMLResponseParserTest {

	private XMLResponseParser parser;

	@Before
	public void setUp() throws Exception {
		parser = new XMLResponseParser();
	}

	@Test
	public void testParseNodes() {
		List<Node> nodes = parser
				.parseNodes(Util
						.readFileAsString("test/ch/hsr/osminabox/db/downloading/nodes.xml"));
		assertEquals(2, nodes.size());
		Node n1 = nodes.get(0);
		assertEquals(1, n1.getOsmId());
		assertEquals(2, n1.tags.size());
		assertEquals(Util.asSet("value1"), n1.tags.get("tag1"));
		assertEquals(Util.asSet("value1,value2,value3"), n1.tags.get("tag2"));
		Node n2 = nodes.get(1);
		assertEquals(2, n2.getOsmId());
	}

	@Test
	public void testParseCompleteWay() {
		Way w = parser
				.parseCompleteWay(Util
						.readFileAsString("test/ch/hsr/osminabox/db/downloading/way.xml"));
		assertEquals(3, w.nodes.size());
		assertEquals(Util.asSet("value1"), w.tags.get("tag1"));
		assertEquals(Util.asSet("value2"), w.tags.get("tag2"));
		assertNode(w.nodes.get(0), 1L, 50, 10);
		assertNode(w.nodes.get(1), 2L, 51, 11);
		Node n3 = w.nodes.get(2);
		assertNode(n3, 3L, 52, 12);
		assertEquals(Util.asSet("v1"), n3.tags.get("k1"));
	}

	private void assertNode(Node node2, long id, double lat, double lon) {
		assertEquals(id, node2.getOsmId());
		assertEquals("" + lat, node2.attributes.get(Node.NODE_LATITUDE));
		assertEquals("" + lon, node2.attributes.get(Node.NODE_LONGITUDE));
	}
}

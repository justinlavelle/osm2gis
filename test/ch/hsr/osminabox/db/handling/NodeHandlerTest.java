package ch.hsr.osminabox.db.handling;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.DBConstants;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.schemamapping.xml.MappingType;
import ch.hsr.osminabox.test.NodeCreator;
import ch.hsr.osminabox.test.Util;

public class NodeHandlerTest extends HandlerTestTemplate {

	private NodeHandler handler;
	private Node node1;
	private Node node2;
	private List<Node> nodes;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		handler = new NodeHandler(context, dbStructure);
		handler.dbUtil = util;
		handler.insertStrategy = nodeInsertStrategy;
		handler.updateStrategy = nodeUpdateStrategy;
		node1 = NodeCreator.create(1, 45, 8).finish();
		node2 = NodeCreator.create(2, 35, 7).finish();
		nodes = Util.asList(node1, node2);
	}

	@Test
	public void testCreateInsertStatements() {
		expectTablesOfGeomTypeCall(MappingType.POINT);
		expectDBQueryCall(node1, Util.asList("table1", "table2"),
				QUERY_TYPE.INSERT_NODE);
		expectDBQueryCall(node2, Util.asList("table1", "table2"),
				QUERY_TYPE.INSERT_NODE);
		expectAddEndTagsCall(Util.asList("table1", "table2"));
		replayAll();
		Map<String, StringBuffer> res = handler.createInsertStatements(nodes);
		verifyAll();
		assertEquals("<INSERT_NODE 1><INSERT_NODE 2><END TAG>", res.get(
				"table1").toString());
		assertEquals("<INSERT_NODE 1><INSERT_NODE 2><END TAG>", res.get(
				"table2").toString());
	}

	@Test
	public void testCreateUpdateStatements() {
		expectTablesOfGeomTypeCall(MappingType.POINT);
		expectDBQueryCall(node1, Util.asList("table1", "table2"),
				QUERY_TYPE.UPDATE_NODE);
		expectDBQueryCall(node2, Util.asList("table1", "table2"),
				QUERY_TYPE.UPDATE_NODE);
		replayAll();
		Map<String, StringBuffer> res = handler.createUpdateStatements(nodes);
		verifyAll();
		assertEquals("<UPDATE_NODE 1><UPDATE_NODE 2>", res.get("table1")
				.toString());
		assertEquals("<UPDATE_NODE 1><UPDATE_NODE 2>", res.get("table2")
				.toString());
	}

	@Test
	public void testCreateTempInsertStatements() {
		expectDBQueryCall(node1, Util.asList(DBConstants.NODE_TEMP),
				QUERY_TYPE.INSERT_TEMP_NODE);
		expectDBQueryCall(node2, Util.asList(DBConstants.NODE_TEMP),
				QUERY_TYPE.INSERT_TEMP_NODE);
		expectAddEndTagsCall(Util.asList(DBConstants.NODE_TEMP));
		replayAll();
		Map<String, StringBuffer> res = handler
				.createTempInsertStatements(nodes);
		verifyAll();
		assertEquals("<INSERT_TEMP_NODE 1><INSERT_TEMP_NODE 2><END TAG>", res
				.get(DBConstants.NODE_TEMP).toString());
	}

}

package ch.hsr.osminabox.db.initialimport;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.DBConstants;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.schemamapping.xml.Column;
import ch.hsr.osminabox.test.NodeCreator;
import ch.hsr.osminabox.test.Util;

public class InitialNodeHandlingStrategyTest extends InitialXXXHandlingStrategy {

	private InitialNodeHandlingStrategy strategy;
	private Node node;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		strategy = new InitialNodeHandlingStrategy(database);
		strategy.dbUtil = util;
		strategy.valueConverter = converter;
		node = NodeCreator.create(2, 45, 8).dbMapping("emptyMapping",
				new ArrayList<Column>()).finish();
		node.dbMappings.put("testtable", mappings);
	}

	@Test
	public void testAddNode() {
		expectConvertingCalls(node);
		expectInsertBeginCall();
		expectInsertValuesCall();
		replayAll();
		strategy.addNode(node, statements);
		verifyAll();
		assertGeneratedSQL();
		assertEquals(1, statements.size());
	}

	@Test
	public void testAddTemp() {
		statements.put(DBConstants.NODE_TEMP, new StringBuffer());
		expectValueConverting("%attribute_id%", node, "2");
		expectValueConverting("%attribute_timestamp%", node, "2010-21-12:13:36");
		expectValueConverting("%attribute_lat%", node, "45.0");
		expectValueConverting("%attribute_lon%", node, "8.0");
		expectInsertBeginCall("node_temp");
		expectInsertCall("node_temp", Util.asMap(
								"osm_id", "2", "lastchange", "2010-21-12:13:36", "lat",
								"45.0", "lon", "8.0"));
		replayAll();
		strategy.addTemp(node, statements);
		verifyAll();
		assertGeneratedSQL("node_temp");
	}

}

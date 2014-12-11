package ch.hsr.osminabox.db.differentialupdate;

import static org.easymock.EasyMock.expect;

import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.test.NodeCreator;
import ch.hsr.osminabox.test.Util;

public class UpdateNodeHandlingStrategyTest extends UpdateXXXHandlingStrategyTestTemplate{

	private UpdateNodeHandlingStrategy update;
	
	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();
		createMocks();
		update = new UpdateNodeHandlingStrategy(null); // is mocked!
		update.dbUtil = dbutil;
		update.valueConverter = valueConverter;
	}

	@Test
	public void testAddNode() {
		StringBuffer stringBuffer = new StringBuffer();
		Map<String, StringBuffer> statements = Util.asMap("testtable", stringBuffer);
		Node node = NodeCreator.create(1, 45, 8).finish();
		node.dbMappings.put("testtable", Util.asList(createColumn("v1", "v2"), createColumn("v3", "v4")));
		expect(valueConverter.convertValue("v2", node)).andReturn("v2c");
		expect(valueConverter.convertValue("v4", node)).andReturn("v4c");
		expectSQLGeneration(stringBuffer);
		replayAll();
		update.addNode(node, statements);
		verifyAll();
		assertSQLGenerated(statements);
	}

}

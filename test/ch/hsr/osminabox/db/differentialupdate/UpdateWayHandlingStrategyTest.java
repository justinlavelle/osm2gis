package ch.hsr.osminabox.db.differentialupdate;

import static org.easymock.EasyMock.expect;

import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.test.Util;
import ch.hsr.osminabox.test.WayCreator;

public class UpdateWayHandlingStrategyTest extends UpdateXXXHandlingStrategyTestTemplate{

private UpdateWayHandlingStrategy update;
	
	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();
		createMocks();
		update = new UpdateWayHandlingStrategy(null); // is mocked!
		update.dbUtil = dbutil;
		update.valueConverter = valueConverter;
	}

	@Test
	public void testAddWay() {
		StringBuffer stringBuffer = new StringBuffer();
		Map<String, StringBuffer> statements = Util.asMap("testtable", stringBuffer);
		Way way = WayCreator.create(1).finish();
		way.dbMappings.put("testtable", Util.asList(createColumn("v1", "v2"), createColumn("v3", "v4")));
		expect(valueConverter.convertValue("v2", way)).andReturn("v2c");
		expect(valueConverter.convertValue("v4", way)).andReturn("v4c");
		expectSQLGeneration(stringBuffer);
		replayAll();
		update.addWay(way, statements);
		verifyAll();
		assertSQLGenerated(statements);
	}

}

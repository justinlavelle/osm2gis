package ch.hsr.osminabox.db.differentialupdate;

import static org.easymock.EasyMock.expect;

import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.RelationMappingEntry;
import ch.hsr.osminabox.test.Util;

public class UpdateRelationHandlingStrategyTest extends UpdateXXXHandlingStrategyTestTemplate {

private UpdateRelationHandlingStrategy update;
	
	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();
		createMocks();
		update = new UpdateRelationHandlingStrategy(null); // is mocked!
		update.dbUtil = dbutil;
		update.valueConverter = valueConverter;
	}

	@Test
	public void testAddRealtion() {
		StringBuffer stringBuffer = new StringBuffer();
		Map<String, StringBuffer> statements = Util.asMap("testtable", stringBuffer);
		Relation realtion = new Relation();
		realtion.setOsmId(1);
		RelationMappingEntry e = new RelationMappingEntry();
		e.mappingColumns.add(createColumn("v1", "v2"));
		e.mappingColumns.add(createColumn("v3", "v4"));
		realtion.dbMappings.put("testtable", e);
		expect(valueConverter.convertValue("v2", realtion)).andReturn("v2c");
		expect(valueConverter.convertValue("v4", realtion)).andReturn("v4c");
		expectSQLGeneration(stringBuffer);
		replayAll();
		update.addRelation(realtion, statements);
		verifyAll();
		assertSQLGenerated(statements);
	}

}

package ch.hsr.osminabox.db.initialimport;

import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.DBConstants;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.util.DiffType;
import ch.hsr.osminabox.test.Util;

public class InitialWayHandlingStrategyTest extends InitialXXXHandlingStrategy {

	private InitialWayHandlingStrategy strategy;
	private Way way;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		strategy = new InitialWayHandlingStrategy(null, database);
		strategy.dbUtil = util;
		strategy.valueConverter = converter;
		way = new Way();
		way.dbMappings.put("testtable", mappings);
	}

	@Test
	public void testAddWay() {
		expectInsertBeginCall();
		expectConvertingCalls(way);
		expectInsertValuesCall();
		replayAll();
		strategy.addWay(way, statements);
		verifyAll();
		assertGeneratedSQL();
	}

	@Test
	public void testAddTemp() {
		statements.put(DBConstants.WAY_TEMP, new StringBuffer());
		expectValueConverting("%attribute_id%", way, "2");
		expectValueConverting("%attribute_timestamp%", way, "2010-21-12:13:36");
		expectValueConverting("%tags_all%", way, "all_tags");
		expectValueConverting("%nd_all%", way, "all_nodes");
		expectValueConverting("false", way, "false");
		expectInsertBeginCall(DBConstants.WAY_TEMP);
		expectInsertCall(DBConstants.WAY_TEMP, Util.asMap("keyvalue", "all_tags",
						"osm_id", "2", "nodes", "all_nodes", "usedbyrelations",
						"false", "lastchange", "2010-21-12:13:36","difftype", "create" ));
		replayAll();
		strategy.addTemp(way, DiffType.create, statements);
		verifyAll();
		assertGeneratedSQL(DBConstants.WAY_TEMP);
	}

}

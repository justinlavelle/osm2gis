package ch.hsr.osminabox.db.handling;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.DBConstants;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.util.DiffType;
import ch.hsr.osminabox.schemamapping.xml.MappingType;
import ch.hsr.osminabox.test.Util;

public class WayHandlerTest extends HandlerTestTemplate{

	private WayHandler handler;
	private List<Way> ways;
	private Way way1;
	private Way way2;
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		handler = new WayHandler(context, null, dbStructure);
		handler.dbUtil = util;
		handler.insertStrategy = wayInsertStrategy;
		handler.updateStrategy = wayUpdateStrategy;
		way1 = new Way();
		way1.setOsmId(1);
		way2 = new Way();
		way2.setOsmId(2);
		ways = Util.asList(way1, way2);
	}

	@Test
	public void testCreateInsertStatements() {
		expectTablesOfGeomTypeCall(MappingType.LINESTRING);
		expectDBQueryCall(way1, Util.asList("table1", "table2"),
				QUERY_TYPE.INSERT_WAY);
		expectDBQueryCall(way2, Util.asList("table1", "table2"),
				QUERY_TYPE.INSERT_WAY);
		expectAddEndTagsCall(Util.asList("table1", "table2"));
		replayAll();
		Map<String, StringBuffer> res = handler.createInsertStatements(ways);
		verifyAll();
		assertEquals("<INSERT_WAY 1><INSERT_WAY 2><END TAG>", res.get(
				"table1").toString());
		assertEquals("<INSERT_WAY 1><INSERT_WAY 2><END TAG>", res.get(
				"table2").toString());
	}

	@Test
	public void testCreateUpdateStatements() {
		expectTablesOfGeomTypeCall(MappingType.LINESTRING);
		expectDBQueryCall(way1, Util.asList("table1", "table2"),
				QUERY_TYPE.UPDATE_WAY);
		expectDBQueryCall(way2, Util.asList("table1", "table2"),
				QUERY_TYPE.UPDATE_WAY);
		replayAll();
		Map<String, StringBuffer> res = handler.createUpdateStatements(ways);
		verifyAll();
		assertEquals("<UPDATE_WAY 1><UPDATE_WAY 2>", res.get("table1")
				.toString());
		assertEquals("<UPDATE_WAY 1><UPDATE_WAY 2>", res.get("table2")
				.toString());
	}

	@Test
	public void testCreateTempInsertStatements() {
		expectDBQueryCall(way1, Util.asList(DBConstants.WAY_TEMP),
				QUERY_TYPE.INSERT_TEMP_WAY);
		expectDBQueryCall(way2, Util.asList(DBConstants.WAY_TEMP),
				QUERY_TYPE.INSERT_TEMP_WAY);
		expectAddEndTagsCall(Util.asList(DBConstants.WAY_TEMP));
		replayAll();
		Map<String, StringBuffer> res = handler
				.createTempInsertStatements(ways, DiffType.create);
		verifyAll();
		assertEquals("<INSERT_TEMP_WAY 1><INSERT_TEMP_WAY 2><END TAG>", res
				.get(DBConstants.WAY_TEMP).toString());
	}

}

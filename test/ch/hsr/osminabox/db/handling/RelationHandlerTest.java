package ch.hsr.osminabox.db.handling;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.DBConstants;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.util.DiffType;
import ch.hsr.osminabox.schemamapping.xml.MappingType;
import ch.hsr.osminabox.test.Util;

public class RelationHandlerTest extends HandlerTestTemplate {

	private RelationHandler handler;
	private List<Relation> relations;
	private Relation rel1;
	private Relation rel2;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		handler = new RelationHandler(context, dbStructure);
		handler.dbUtil = util;
		handler.insertStrategy = relationInsertStrategy;
		handler.updateStrategy = relationUpdateStrategy;
		rel1 = new Relation();
		rel1.setOsmId(1);
		rel2 = new Relation();
		rel2.setOsmId(2);
		relations = Util.asList(rel1, rel2);
	}

	@Test
	public void testCreateTempStatements() {
		expectDBQueryCall(rel1, Util.asList(DBConstants.RELATION_TEMP),
				QUERY_TYPE.INSERT_TEMP_RELATION);
		expectDBQueryCall(rel1, Util.asList(DBConstants.RELATION_MEMBER_TEMP),
				QUERY_TYPE.INSERT_TEMP_RELATION_MEMBER);
		expectDBQueryCall(rel2, Util.asList(DBConstants.RELATION_TEMP),
				QUERY_TYPE.INSERT_TEMP_RELATION);
		expectDBQueryCall(rel2, Util.asList(DBConstants.RELATION_MEMBER_TEMP),
				QUERY_TYPE.INSERT_TEMP_RELATION_MEMBER);
		expectAddEndTagsCall(Util.asList(DBConstants.RELATION_TEMP,
				DBConstants.RELATION_MEMBER_TEMP));
		replayAll();
		Map<String, StringBuffer> res = handler.createTempStatements(relations,
				DiffType.create);
		verifyAll();
		assertEquals(
				"<INSERT_TEMP_RELATION 1><INSERT_TEMP_RELATION 2><END TAG>",
				res.get(DBConstants.RELATION_TEMP).toString());
		assertEquals(
				"<INSERT_TEMP_RELATION_MEMBER 1><INSERT_TEMP_RELATION_MEMBER 2><END TAG>",
				res.get(DBConstants.RELATION_MEMBER_TEMP).toString());
	}

	@Test
	public void testCreateInsertStatements() {
		expectTablesOfGeomTypeCall(MappingType.RELATION);
		expectDBQueryCall(rel1, Util.asList("table1", "table2"),
				QUERY_TYPE.INSERT_RELATION);
		expectDBQueryCall(rel2, Util.asList("table1", "table2"),
				QUERY_TYPE.INSERT_RELATION);
		expectAddEndTagsCall(Util.asList("table1", "table2"));
		replayAll();
		Map<String, StringBuffer> res = handler
				.createInsertStatements(relations);
		verifyAll();
		assertEquals("<INSERT_RELATION 1><INSERT_RELATION 2><END TAG>", res
				.get("table1").toString());
		assertEquals("<INSERT_RELATION 1><INSERT_RELATION 2><END TAG>", res
				.get("table2").toString());
	}

	@Test
	public void testCreateJoinStatements() {
		expectGetJoinTablesCall();
		expectDBQueryCall(rel1, Util.asList("table1", "table2"),
				QUERY_TYPE.INSERT_RELATION_JOINS);
		expectDBQueryCall(rel2, Util.asList("table1", "table2"),
				QUERY_TYPE.INSERT_RELATION_JOINS);
		expectAddEndTagsCall(Util.asList("table1", "table2"));
		replayAll();
		Map<String, StringBuffer> res = handler.createJoinStatements(relations);
		verifyAll();
		assertEquals(
				"<INSERT_RELATION_JOINS 1><INSERT_RELATION_JOINS 2><END TAG>",
				res.get("table1").toString());
		assertEquals(
				"<INSERT_RELATION_JOINS 1><INSERT_RELATION_JOINS 2><END TAG>",
				res.get("table2").toString());
	}

	@Test
	public void testCreateUpdateStatements() {
		expectTablesOfGeomTypeCall(MappingType.RELATION);
		expectDBQueryCall(rel1, Util.asList("table1", "table2"),
				QUERY_TYPE.UPDATE_RELATION);
		expectDBQueryCall(rel2, Util.asList("table1", "table2"),
				QUERY_TYPE.UPDATE_RELATION);
		replayAll();
		Map<String, StringBuffer> res = handler.createUpdateStatements(relations);
		verifyAll();
		assertEquals("<UPDATE_RELATION 1><UPDATE_RELATION 2>", res.get("table1")
				.toString());
		assertEquals("<UPDATE_RELATION 1><UPDATE_RELATION 2>", res.get("table2")
				.toString());
	}

}

package ch.hsr.osminabox.db.initialimport;

import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.DBConstants;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.RelationMappingEntry;
import ch.hsr.osminabox.db.entities.RelationMember;
import ch.hsr.osminabox.db.util.DiffType;
import ch.hsr.osminabox.test.Util;

public class InitialRelationHandlingStrategyTest extends InitialXXXHandlingStrategy{

	private InitialRelationHandlingStrategy strategy;
	private Relation relation;
	
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		strategy = new InitialRelationHandlingStrategy(database);
		strategy.dbUtil = util;
		strategy.valueConverter = converter;
		relation = new Relation();
		RelationMappingEntry relationMappingEntry = new RelationMappingEntry();
		relationMappingEntry.mappingColumns.add(mappings.get(0));
		relationMappingEntry.mappingColumns.add(mappings.get(1));
		relation.dbMappings.put("testtable", relationMappingEntry);
	}

	@Test
	public void testAddRelation() {
		expectInsertBeginCall();
		expectConvertingCalls(relation);
		expectInsertValuesCall();
		replayAll();
		strategy.addRelation(relation, statements);
		verifyAll();
		assertGeneratedSQL();
	}

	@Test
	public void testAddTemp() {
		statements.put(DBConstants.RELATION_TEMP, new StringBuffer());
		expectValueConverting("%attribute_id%", relation, "2");
		expectValueConverting("%attribute_timestamp%", relation, "2010-21-12:13:36");
		expectValueConverting("%members_all%", relation, "all_members");
		expectValueConverting("%tags_all%", relation, "all_tags");
		expectInsertBeginCall("relation_temp");
		expectInsertCall("relation_temp", Util.asMap("relationmember", "all_members",
						"osm_id", "2", "difftype", "create", "keyvalue",
						"all_tags", "lastchange", "2010-21-12:13:36"));
		replayAll();
		strategy.addTemp(relation, DiffType.create, statements);
		verifyAll();
		assertGeneratedSQL("relation_temp");
	}

	@Test
	public void testAddTempMembers() {
		statements.put(DBConstants.RELATION_MEMBER_TEMP, new StringBuffer());
		RelationMember member1 = new RelationMember();
		relation.members.add(member1);
		expectValueConverting("%attribute_id%", relation, "2");
		expectValueConverting("%member_ref%", member1, "ref_id");
		expectValueConverting("%member_type%", member1, "mytype");
		expectValueConverting("%member_role%", member1, "myrole");
		expectInsertBeginCall("relation_member_temp");
		expectInsertCall("relation_member_temp", Util.asMap("relation_osm_id", "2",
				"member_osm_id", "ref_id",
				"type", "mytype",
				"role", "myrole"));
		replayAll();
		strategy.addTempMembers(relation, statements);
		verifyAll();
		assertGeneratedSQL("relation_member_temp");
	}

	@Test
	public void testAddJoins() {
	}

}

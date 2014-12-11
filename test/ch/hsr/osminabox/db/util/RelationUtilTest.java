package ch.hsr.osminabox.db.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.RelationMappingEntry;
import ch.hsr.osminabox.db.entities.RelationMember;
import ch.hsr.osminabox.db.entities.RelationMemberType;
import ch.hsr.osminabox.test.Util;

public class RelationUtilTest extends EasyMockSupport {

	RelationUtil util;

	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();
		util = createMockBuilder(RelationUtil.class).addMockedMethod("exec")
				.createMock();
		replayAll();
	}

	@Test
	public void testRemoveUnmappedRelations() {
		List<Relation> relations = new ArrayList<Relation>();
		Relation rValid = new Relation();
		rValid.dbMappings.put("mapping", new RelationMappingEntry());
		Relation rInvalid = new Relation();
		rInvalid.dbMappings.clear();
		relations.add(rValid);
		relations.add(rInvalid);
		util.removeUnmappedRelations(relations);
		assertEquals(1, relations.size());
		assertEquals(rValid, relations.get(0));
	}

	@Test
	public void testCountDuplicateMembers() throws Exception {
		assertEquals(0, util.countDuplicateMembers(Util.asArray(
				createRelationMember(1, RelationMemberType.NODE),
				createRelationMember(2, RelationMemberType.NODE))));
		assertEquals(1, util.countDuplicateMembers(Util.asArray(
				createRelationMember(1, RelationMemberType.NODE),
				createRelationMember(1, RelationMemberType.NODE))));
		assertEquals(2, util.countDuplicateMembers(Util.asArray(
				createRelationMember(1, RelationMemberType.NODE),
				createRelationMember(1, RelationMemberType.NODE),
				createRelationMember(2, RelationMemberType.WAY),
				createRelationMember(2, RelationMemberType.WAY))));
	}

	private RelationMember createRelationMember(int osmId,
			RelationMemberType type) {
		RelationMember m = new RelationMember();
		m.osmId = osmId;
		m.type = type;
		return m;
	}

}

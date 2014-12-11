package ch.hsr.osminabox.importing.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.test.Util;

public class RelationTagUtilTest {

	private RelationTagUtil util;
	@Before
	public void setUp() throws Exception {
		util = new RelationTagUtil();
	}

	@Test
	public void testIsAreaCandidate() {
		Relation relation = new Relation();
		assertFalse(util.isAreaCandidate(relation));
		relation.tags.put("multipolygon", Util.asSet("test"));
		assertFalse(util.isAreaCandidate(relation));
		relation.tags.put("type", Util.asSet("multipolygon", "boundary"));
		assertTrue(util.isAreaCandidate(relation));
	}

}

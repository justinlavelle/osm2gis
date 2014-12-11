package ch.hsr.osminabox.db.mapping;


import java.util.List;

import org.easymock.EasyMockSupport;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.RelationMappingEntry;
import ch.hsr.osminabox.schemamapping.xml.Column;
import ch.hsr.osminabox.schemamapping.xml.DstColumns;
import ch.hsr.osminabox.schemamapping.xml.DstTable;
import ch.hsr.osminabox.schemamapping.xml.Mapping;
import ch.hsr.osminabox.schemamapping.xml.Members;
import ch.hsr.osminabox.schemamapping.xml.RelatedTable;
import ch.hsr.osminabox.test.Util;

public class RelationMappingAppenderTest extends EasyMockSupport{

	private RelationMappingAppender mappingAppender;
	private Relation relation;
	private Mapping mapping;
	@Before
	public void setUp() throws Exception {
		mappingAppender = new RelationMappingAppender();
		relation = new Relation();
		mapping = createMock(Mapping.class);
	}

	@Test
	public void testAppendMapping() throws Exception {
		DstColumns dstColumns = new DstColumns();
		dstColumns.getColumn().add(createColumn("name", "value"));
		dstColumns.getColumn().add(createColumn("name2", "value2"));
		expect(mapping.getDstColumns()).andReturn(dstColumns);
		Members members = createMock(Members.class);
		expect(mapping.getMembers()).andReturn(members);
		List<RelatedTable> relatedTables = Util.asList(createRelatedTable("related_table_name"),createRelatedTable("related_table_name2"));
		expect(members.getRelatedTable()).andReturn(relatedTables);
		boolean allRequired = true;
		expect(members.isAllRequired()).andReturn(allRequired);
		DstTable dstTable = new DstTable();
		dstTable.setName("dest_name");
		expect(mapping.getDstTable()).andReturn(dstTable);
		replayAll();
		mappingAppender.appendMapping(relation, mapping);
		verifyAll();
		RelationMappingEntry entry = relation.dbMappings.get("dest_name");
		assertEquals(allRequired, entry.allMembersRequiered);
		assertEquals(dstColumns.getColumn(), entry.mappingColumns);
		assertEquals(relatedTables, entry.relatedTables);
		assertEquals(true, entry.dbIdsToMember.containsKey("related_table_name"));
		assertEquals(true, entry.dbIdsToMember.containsKey("related_table_name2"));
	}

	private RelatedTable createRelatedTable(String name) {
		RelatedTable relatedTable = new RelatedTable();
		relatedTable.setName(name);
		return relatedTable;
	}

	private Column createColumn(String name, String value) {
		Column column = new Column();
		column.setName(name);
		column.setValue(value);
		return column;
	}
}

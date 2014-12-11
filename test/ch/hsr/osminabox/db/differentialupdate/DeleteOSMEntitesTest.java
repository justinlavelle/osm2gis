package ch.hsr.osminabox.db.differentialupdate;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.OSMEntity;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.RelationMappingEntry;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.entities.exceptions.InvalidWayException;
import ch.hsr.osminabox.schemamapping.ConfigService;
import ch.hsr.osminabox.schemamapping.xml.Column;
import ch.hsr.osminabox.schemamapping.xml.JoinTable;
import ch.hsr.osminabox.schemamapping.xml.MappingType;
import ch.hsr.osminabox.schemamapping.xml.RelatedTable;
import ch.hsr.osminabox.test.Util;

public class DeleteOSMEntitesTest extends EasyMockSupport {

	private DeleteOSMEntites delete;
	private ConfigService configMock;
	private List<OSMEntity> entities;
	private Map<String, StringBuffer> sqlResultTable;

	@Before
	public void setUp() throws Exception {
		configMock = createStrictMock(ConfigService.class);
		delete = new DeleteOSMEntites(configMock);
		entities = new ArrayList<OSMEntity>();
	}

	@Test
	public void testSimpleDeleteByDbMappings() throws Exception {
		addOSMEntity(1, new ColumnMapping("testTable", Util.asList(
				createColumn("test", "hello"), createColumn("world", "gugus"))));
		sqlResultTable = delete.deleteByDbMappings(entities);
		assertEquals(1, sqlResultTable.size());
		assertDeleteSQL("testTable", "1");
	}

	@Test
	public void testComplexDeleteByDbMappings() throws Exception {
		addOSMEntity(1,
				new ColumnMapping("testTable", Util.asList(createColumn("test",
						"hello"), createColumn("world", "gugus"))),
				new ColumnMapping("testTable2", Util.asList(createColumn(
						"test2", "hello2"), createColumn("world2", "gugus2"))));

		addOSMEntity(2, new ColumnMapping("testTable2", Util.asList(
				createColumn("test3", "hello3"), createColumn("world3",
						"gugus3"))), new ColumnMapping("testTable2", Util
				.asList(createColumn("test2", "hello2"), createColumn("world2",
						"gugus2"))));
		sqlResultTable = delete.deleteByDbMappings(entities);
		assertEquals(2, sqlResultTable.size());
		assertDeleteSQL("testTable", "1");
		assertDeleteSQL("testTable2", "1,2");
	}

	@Test
	public void testDeleteNodesFromAllTables() throws Exception {
		expect(configMock.getTablesOfGeomType(MappingType.POINT)).andReturn(
				Util.asSet("table1", "table2"));
		replayAll();
		sqlResultTable = delete.deleteNodesFromAllTables(Util.asList(
				createNode(1), createNode(2)));
		verifyAll();
		assertEquals(2, sqlResultTable.size());
		assertDeleteSQL("table1", "1,2");
		assertDeleteSQL("table2", "1,2");
	}

	@Test
	public void testDeleteWaysFromAllTables() throws Exception {
		expect(configMock.getTablesOfGeomType(MappingType.LINESTRING))
				.andReturn(Util.asSet("table1", "table2"));
		expect(configMock.getTablesOfGeomType(MappingType.MULTIPOLYGON))
				.andReturn(Util.asSet("table3"));
		replayAll();
		sqlResultTable = delete.deleteWaysFromAllTables(Util.asList(
				createWay(1), createWay(2), createWay(3)));
		verifyAll();
		assertEquals(3, sqlResultTable.size());
		assertDeleteSQL("table1", "1,2,3");
		assertDeleteSQL("table2", "1,2,3");
		assertDeleteSQL("table3", "1,2,3");
	}

	@Test
	public void testDeleteAreasFromAllTables() throws Exception {
		expect(configMock.getTablesOfGeomType(MappingType.MULTIPOLYGON))
				.andReturn(Util.asSet("table1", "table2"));
		replayAll();
		sqlResultTable = delete.deleteAreasFromAllTables(Util.asList(
				createArea(1), createArea(2)));
		verifyAll();
		assertEquals(2, sqlResultTable.size());
		assertDeleteSQL("table1", "1,2");
		assertDeleteSQL("table2", "1,2");
	}

	@Test
	public void testDeleteRelationsFromAllTables() throws Exception {
		expect(configMock.getTablesOfGeomType(MappingType.MULTIPOLYGON))
				.andReturn(Util.asSet("table1", "table2"));
		expect(configMock.getTablesOfGeomType(MappingType.RELATION)).andReturn(
				Util.asSet("table3"));
		replayAll();
		sqlResultTable = delete.deleteRelationsFromAllTables(Util.asList(
				createRelation(1), createRelation(2)));
		verifyAll();
		assertDeleteSQL("table1", "1,2");
		assertDeleteSQL("table2", "1,2");
		assertDeleteSQL("table3", "1,2");
	}

	@Test
	public void testDeleteJoin() throws Exception {
		BasicConfigurator.configure();
		expect(configMock.getReferenceColumnName("testjt", "test")).andReturn(
				"ref1");
		expect(configMock.getReferenceColumnName("testjt2", "test")).andReturn(
				"ref2");
		expect(configMock.getReferenceColumnName("testjt3", "test")).andReturn(
		"ref3");
		replayAll();
		RelationColumnMapping relationColumnMapping1 = new RelationColumnMapping(
				"mapping1", new RelationMappingEntry(), Util.asList(
						createRelatedTable("test", "testjt"),
						createRelatedTable("test", "testjt2")));
		RelationColumnMapping relationColumnMapping2 = new RelationColumnMapping(
				"mapping1", new RelationMappingEntry(), Util.asList(
						createRelatedTable("test", "testjt2"),
						createRelatedTable("test", "testjt3")));
		sqlResultTable = delete.deleteJoinEntries(Util.asList(createRelation(1,
				relationColumnMapping1),
				createRelation(2, relationColumnMapping1),
				createRelation(3, relationColumnMapping2)));

		verifyAll();
		assertEquals(3, sqlResultTable.size());
		assertDeleteSQL("testjt", "ref1", "1,2");
		assertDeleteSQL("testjt2", "ref2", "1,2,3");
		assertDeleteSQL("testjt3", "ref3", "3");
	}

	private RelatedTable createRelatedTable(String name, String joinTableName) {
		RelatedTable t = new RelatedTable();
		t.setName(name);
		JoinTable jt = new JoinTable();
		jt.setName(joinTableName);
		t.setJoinTable(jt);
		return t;
	}

	private Relation createRelation(int osmId,
			RelationColumnMapping... mappings) {
		Relation r = new Relation();
		r.setOsmId(osmId);
		Map<String, RelationMappingEntry> hashMap = new HashMap<String, RelationMappingEntry>();
		for (RelationColumnMapping m : mappings) {
			hashMap.put(m.name, m.entry);
		}
		r.dbMappings = hashMap;
		return r;
	}

	private void assertDeleteSQL(String tableName, String inString) {
		assertDeleteSQL(tableName, "osm_id", inString);
	}
	
	private void assertDeleteSQL(String tableName, String columnName, String inString){
		assertEquals("DELETE FROM " + tableName + " WHERE "+columnName+" IN ("
				+ inString + ");", sqlResultTable.get(tableName).toString());
	}

	private Area createArea(final int osmId) throws InvalidWayException {
		Area a = null;
		a = new Area(2);
		a.setOsmId(osmId);
		return a;
	}

	private Way createWay(int osmId) {
		Way w = new Way();
		w.setOsmId(osmId);
		return w;
	}

	private Node createNode(int osm_id) {
		Node n = new Node();
		n.setOsmId(osm_id);
		return n;
	}

	private Column createColumn(String name, String value) {
		Column c = new Column();
		c.setName(name);
		c.setValue(value);
		return c;
	}

	private void addOSMEntity(int id, ColumnMapping... mappings) {
		OSMEntity e1 = new OSMEntity();
		e1.setOsmId(id);
		Map<String, List<Column>> hashMap = new HashMap<String, List<Column>>();
		for (ColumnMapping m : mappings) {
			hashMap.put(m.name, m.columns);
		}
		e1.dbMappings = hashMap;
		entities.add(e1);
	}

	public static class ColumnMapping {
		List<Column> columns;
		String name;

		public ColumnMapping(String name, List<Column> columns) {
			super();
			this.columns = columns;
			this.name = name;
		}
	}

	public static class RelationColumnMapping {
		RelationMappingEntry entry;
		String name;

		public RelationColumnMapping(String name, RelationMappingEntry entry,
				List<RelatedTable> relatedTables) {
			super();
			this.entry = entry;
			this.entry.relatedTables = relatedTables;
			this.name = name;
		}
	}
}

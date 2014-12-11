package ch.hsr.osminabox.schemamapping;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.util.List;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.schemamapping.exceptions.NoSchemaDefAndMappingConfigXMLException;
import ch.hsr.osminabox.schemamapping.xml.Column;
import ch.hsr.osminabox.schemamapping.xml.DstColumn;
import ch.hsr.osminabox.schemamapping.xml.DstColumns;
import ch.hsr.osminabox.schemamapping.xml.DstSchemaDef;
import ch.hsr.osminabox.schemamapping.xml.DstTableDef;
import ch.hsr.osminabox.schemamapping.xml.Mapping;
import ch.hsr.osminabox.schemamapping.xml.MappingType;
import ch.hsr.osminabox.schemamapping.xml.SchemaDefAndMapping;
import ch.hsr.osminabox.schemamapping.xml.SrcToDstMappings;
import ch.hsr.osminabox.schemamapping.xml.Tag;

public class ConfigServiceImplTest extends EasyMockSupport {

	private static final String CONFIG_FILE = "test/ch/hsr/osminabox/schemamapping/simple.xml";
	private ConfigService config;
	private ApplicationContext context;

	@Before
	public void setUp() throws Exception {
		context = createMock(ApplicationContext.class);
		config = new ConfigServiceImpl(CONFIG_FILE, context);
	}

	@Test
	public void testGetSchemaDefAndMapping()
			throws NoSchemaDefAndMappingConfigXMLException {
		SchemaDefAndMapping sdam = config.getSchemaDefAndMapping();
		assertNotNull(sdam);
		DstSchemaDef dst = sdam.getDstSchemaDef();
		SrcToDstMappings src = sdam.getSrcToDstMappings();
		assertDstSchemaDef(dst);
		assertSrcScemaDef(src);
	}

	@Test
	public void testGetDstSchemaDef()
			throws NoSchemaDefAndMappingConfigXMLException {
		assertDstSchemaDef(config.getDstSchemaDef());
	}

	@Test
	public void testGetSrcToDstMappings()
			throws NoSchemaDefAndMappingConfigXMLException {
		assertSrcScemaDef(config.getSrcToDstMappings());
	}

	@Test
	public void testReloadXmlFile()
			throws NoSchemaDefAndMappingConfigXMLException {
		SchemaDefAndMapping before = config.getSchemaDefAndMapping();
		config.reloadXmlFile(CONFIG_FILE);
		SchemaDefAndMapping after = config.getSchemaDefAndMapping();
		assertNotSame(before, after);
	}

	@Test
	public void testLoadXmlFile() {
		Object o = config.loadXmlFile(
				"test/ch/hsr/osminabox/schemamapping/dummyjxb/greetings.xml",
				"ch.hsr.osminabox.schemamapping.dummyjxb");
		assertNotNull(o);
	}

	@Test
	public void testGetActualConfigXmlFilePath() {
		expect(context.containsArgument("mapping")).andReturn(true);
		expect(context.getArgument("mapping")).andReturn("configPath");
		assertActualConfigXMLPath();
	}

	private void assertActualConfigXMLPath() {
		replayAll();
		String actualConfigXmlFilePath = config.getActualConfigXmlFilePath();
		verifyAll();
		assertEquals("configPath", actualConfigXmlFilePath);
	}

	@Test
	public void testGetActualConfigXMLFilePathFromConfig() {
		String actualConfigXmlFilePath;
		expect(context.containsArgument("mapping")).andReturn(false);
		expect(context.getConfigParameter("conf.mapping.file")).andReturn(
				"configPath");
		assertActualConfigXMLPath();
	}

	// @Test
	// public void testGetMappingTables() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetMappingViews() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetMappingJoinTables() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetMappingsOfTable() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetGeomTypeOfTable() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetTablesOfGeomType() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetMappings() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetXmlMapping() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetInheritedTable() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetReferenceColumnName() {
	// fail("Not yet implemented");
	// }

	private void assertDstSchemaDef(DstSchemaDef dst) {
		List<Object> res = dst.getDstTableDefOrDstTableDefUserDefined();
		assertEquals(2, res.size());
		DstTableDef d1 = (DstTableDef) res.get(0);
		DstTableDef d2 = (DstTableDef) res.get(1);
		assertEquals("gisentity", d1.getName());
		assertEquals("water", d2.getName());
		assertEquals("gisentity", d2.getInherits());
		List<DstColumn> columns = d1.getDstColumn();
		assertEquals(5, columns.size());
		assertColumn(columns.get(0), "osm_id", "bigint", true);
		assertColumn(columns.get(1), "lastchange", "TIMESTAMP");
		assertColumn(columns.get(2), "type", "VARCHAR(255)");
		assertColumn(columns.get(3), "name", "VARCHAR(255)");
		assertColumn(columns.get(4), "keyvalue", "hstore", null);
		columns = d2.getDstColumn();
		assertEquals(2, columns.size());
		assertColumn(columns.get(0), "id", "serial", null, true);
		assertColumn(columns.get(1), "geom",
				"geometry(4326, 'MULTIPOLYGON', 2)", null);
	}

	private void assertColumn(DstColumn dstColumn, String name, String type) {
		assertColumn(dstColumn, name, type, false);
	}

	private void assertColumn(DstColumn dstColumn, String name, String type,
			Boolean notNull) {
		assertColumn(dstColumn, name, type, notNull, null);
	}

	private void assertColumn(DstColumn dstColumn, String name, String type,
			Boolean notNull, Boolean pk) {
		assertEquals(name, dstColumn.getName());
		assertEquals(type, dstColumn.getType());
		assertEquals(notNull, dstColumn.isNotNull());
		assertEquals(pk, dstColumn.isPrimaryKey());
	}

	private void assertSrcScemaDef(SrcToDstMappings src) {
		List<Mapping> mappings = src.getMapping();
		assertEquals(1, mappings.size());
		Mapping m = mappings.get(0);
		assertEquals(MappingType.MULTIPOLYGON, m.getType());
		List<Tag> adEdTags = m.getAndEdConditions().getTag();
		assertEquals(1, adEdTags.size());
		Tag t = adEdTags.get(0);
		assertEquals("natural", t.getK());
		assertEquals("water", t.getV());
		assertEquals("water", m.getDstTable().getName());
		DstColumns cls = m.getDstColumns();
		List<Column> columns = cls.getColumn();
		assertColumn(columns.get(0), "osm_id", "%attribute_id%");
		assertColumn(columns.get(1), "lastchange", "%attribute_timestamp%");
		assertColumn(columns.get(2), "type", "water");
		assertColumn(columns.get(3), "name", "%tag_name%");
		assertColumn(columns.get(4), "keyvalue", "%tags_all%");
		assertColumn(columns.get(5), "geom", "%geom%");
	}

	private void assertColumn(Column column, String name, String value) {
		assertEquals(name, column.getName());
		assertEquals(value, column.getValue());
	}

}

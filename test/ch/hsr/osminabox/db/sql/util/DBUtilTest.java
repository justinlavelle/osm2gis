package ch.hsr.osminabox.db.sql.util;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import java.sql.ResultSetMetaData;
import java.util.HashMap;

import org.apache.log4j.BasicConfigurator;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.dbdefinition.Column;
import ch.hsr.osminabox.db.dbdefinition.Database;
import ch.hsr.osminabox.db.dbdefinition.Table;
import ch.hsr.osminabox.db.util.ValueValidation;
import ch.hsr.osminabox.test.Util;
import ch.hsr.osminabox.util.DateParser;

public class DBUtilTest extends EasyMockSupport {

	private DBUtil util;
	private Database db;
	private DateParser parser;
	private ValueValidation validator;
	private Table table;

	@Before
	public void setUp() throws Exception {
		parser = createMock(DateParser.class);
		validator = createMock(ValueValidation.class);
		db = createMock(Database.class);
		table = new Table("testtable", false, "Message");
		table.addColumn(new Column("pk", "int", "java.lang.Interger", false,
				true, ResultSetMetaData.columnNoNulls));
		table.addColumn(new Column("c1", "varchar", "java.lang.String", false,
				false, ResultSetMetaData.columnNoNulls));
		table.addColumn(new Column("c2", "varchar", "java.lang.String", false,
				false, ResultSetMetaData.columnNoNulls));
		util = new DBUtil(db);
		util.dateParser = parser;
		util.validator = validator;
	}

	@Test
	public void testCheckAndAppendSpacer() throws Exception {
		StringBuffer b = new StringBuffer();
		b.append("test");
		util.checkAndAppendSpacer(b, " ");
		assertEquals("test ", b.toString());
		util.checkAndAppendSpacer(b, " ");
		assertEquals("test ", b.toString());
	}

	@Test
	public void testCreateInsertBegin() throws Exception {
		expect(db.getTable("testtable")).andReturn(table);
		replayAll();
		String res = util.createInsertBegin("testtable").toString();
		verifyAll();
		assertEquals("INSERT INTO testtable (c1, c2) VALUES ", res);
	}

	@Test
	public void testAddInsertValues() throws Exception {
		expect(db.getTable("testtable")).andReturn(table);
		expect(validator.addEscape("test1")).andReturn("test1");
		expect(validator.addEscape("test2")).andReturn("test2");
		replayAll();
		String res = util.addInsertValues("testtable",
				Util.asMap("c1", "test1", "c2", "test2")).toString();
		verifyAll();
		assertEquals("('test1', 'test2')", res);
	}

	@Test
	public void testCreateUpateBegin() throws Exception {
		String res = util.createUpdateBegin("testtable").toString();
		assertEquals("UPDATE testtable SET ", res);
	}
	@Test
	public void testAddUpdateValues() throws Exception {
		BasicConfigurator.configure();
		table = new Table("testtable", false, "Message");
		table.addColumn(new Column("c1", "varchar", "java.lang.String", false,
				false, 1));
		table.addColumn(new Column("c2", "varchar", "java.lang.String", false,
				false, 1));
		expect(db.getTable("testtable")).andReturn(table);
		expect(validator.addEscape("test1")).andReturn("test1");
		expect(validator.addEscape("test2")).andReturn("test2");
		replayAll();
		String res = util.addUpdateValues("testtable",
				Util.asMap("c1", "test1", "c2", "test2")).toString();
		verifyAll();
		assertEquals("c1 = 'test1', c2 = 'test2'", res);
	}

	@Test
	public void testAddEndTags() throws Exception {
		HashMap<String, StringBuffer> map = new HashMap<String, StringBuffer>();
		map.put("test1", new StringBuffer("testcontent"));
		map.put("test2", new StringBuffer("testcontent2"));
		map = util.addEndTags(map);
		assertEquals("testcontent;", map.get("test1").toString());
		assertEquals("testcontent2;", map.get("test2").toString());
	}

	@Test
	public void testAddWhere() throws Exception {
		assertEquals(" WHERE osm_id = 2", util.addWhere(2));
	}
}

package ch.hsr.osminabox.db.dbdefinition;


import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TableTest {

	private Table table;
	private Column testcolumn;
	private Column primaryKey;
	@Before
	public void setUp() throws Exception {
		table = new Table("test", true, "A Testtable");
		testcolumn = new Column("Testcolumn", "varchar", "java.lang.String", true, false, 0);
		table.addColumn(testcolumn);
		primaryKey = new Column("primaryKey", "int", "java.lang.Integer", true, true, 0);
		table.addColumn(primaryKey);
	}
	
	
	@Test
	public void testGetNonAutoIncrementColumns() throws Exception {
		assertEquals(1, table.getNonAutoIncrementColumns().size());
		assertEquals(testcolumn, table.getNonAutoIncrementColumns().get(0));
	}
	

}

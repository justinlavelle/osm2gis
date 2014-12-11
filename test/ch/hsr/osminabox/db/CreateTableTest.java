package ch.hsr.osminabox.db;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CreateTableTest {

	private static File file = new File("testsql.sql");
	private static String lineSep = System.getProperty("line.separator");
	private CreateTable create;

	private void setupFileContent(String sql) throws IOException {
		if (file.exists()) {
			file.delete();
		}
		FileWriter w = new FileWriter(file);
		w.write(sql);
		w.flush();
		w.close();
	}

	@BeforeClass
	public static void beforeClass(){
		BasicConfigurator.configure();
	}
	@Before
	public void setUp() {
		create = new CreateTable();
	}

	@After
	public void tearDown() throws Exception {
		file.delete();
	}

	@Test
	public void testGetEmptySQL() throws Exception {
		setupFileContent("");
		assertEquals("", create.getSQL(file.getAbsolutePath()));
	}

	@Test
	public void testGetShortSQL() throws Exception {
		setupFileContent("CREATE TABLE TEST;");
		assertEquals("CREATE TABLE TEST;" + lineSep, create.getSQL(file
				.getAbsolutePath()));
	}

	@Test
	public void testGetMultilineSQL() throws Exception {
		setupFileContent("CREATE TABLE\nTEST;");
		assertEquals("CREATE TABLE" + lineSep + "TEST;" + lineSep, create
				.getSQL(file.getAbsolutePath()));
	}

	@Test
	public void testGetOnNonexistentFile() throws Exception {
		try {
			create.getSQL("nonexistentFile.sql");
		} catch (NullPointerException e) {
			assertEquals(FileNotFoundException.class, e.getCause().getClass());
		}
	}

}

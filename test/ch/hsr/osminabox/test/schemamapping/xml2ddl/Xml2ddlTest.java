package ch.hsr.osminabox.test.schemamapping.xml2ddl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.context.ApplicationContextImpl;
import ch.hsr.osminabox.schemamapping.xml2ddl.Xml2ddl;
import ch.hsr.osminabox.test.TestDatabaseTestTemplate;

public class Xml2ddlTest extends TestDatabaseTestTemplate {

	private String testFolder;
	private ApplicationContext context;
	private String configFile;
	private Xml2ddl xml2ddl;

	@Before
	public void setUp() throws Exception {
		testFolder = "test/tmp/";
		configFile = "test/config/osm2gis.properties";
		context = new ApplicationContextImpl(configFile,new String[] {});
		context.connectToDatabase();
		xml2ddl = new Xml2ddl(context) {
			@Override
			protected String getYesOrNo() throws IOException {
				return "n";
			}
		};
	}

	@After
	public void tearDown() throws Exception {
		delFolder(new File(testFolder));
	}

	@Test
	public void generateSimpleDDL() throws IOException {
		String outputFile = testFolder + "output.sql";
		xml2ddl.setOutputFile(outputFile);
		assertFalse(xml2ddl.startGeneration(
				"test/ddl_tests/water.xml", false, true));
		assertDdlSQL("test/ddl_tests/water.sql",outputFile);
	}
	
		@Test
	public void generateOSMDDL() throws IOException {
		String outputFile = testFolder + "output.sql";
		xml2ddl.setOutputFile(outputFile);
		assertFalse(xml2ddl.startGeneration(
				"test/ddl_tests/osm.xml", false, true));
		assertDdlSQL("test/ddl_tests/osm.sql",outputFile);
	}

	private void assertDdlSQL(String expectedFile, String actualFile) throws IOException {
		String exp = readFileAsString(expectedFile).replace("\r", "");
		String actaul = readFileAsString(actualFile).replace("\r", "");
		
		assertEquals(exp.substring(exp.indexOf("CREATE"), exp.length()),
				actaul.substring(exp.indexOf("CREATE"), actaul.length()));
	}

	public void delFolder(File dir) {
		if (dir.isDirectory()) {
			String[] entries = dir.list();
			for (int x = 0; x < entries.length; x++) {
				File aktFile = new File(dir.getPath(), entries[x]);
				delFolder(aktFile);
			}

		}
		dir.delete();
	}

	private static String readFileAsString(String filePath)
			throws java.io.IOException {
		StringBuilder fileData = new StringBuilder(1000);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			fileData.append(buf,0,numRead);
		}
		reader.close();
		return fileData.toString();
	}

}

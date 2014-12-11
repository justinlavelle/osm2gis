package ch.hsr.osminabox.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.postgresql.util.PSQLException;

import ch.hsr.osminabox.Main;
import ch.hsr.osminabox.context.ConfigConstants;
import ch.hsr.osminabox.updating.UpdateJob;

public class TestDatabaseTestTemplate {

	public static final String CONFIG_LOCATION = "config/osm2gis.properties";
	public static final String TEST_CONFIG_LOCATION = "test/config/osm2gis.properties";
	public static final String MAPPING_LOCATION = "config/mappingconfig.xml";
	public static final String TEST_MAPPING_LOCATION = "test/config/mappingconfig.xml";
	public static final int UPDATE_TIMEOUT = 5000;
	protected static int jobInvocationCount;

	private static String config;
	private static String mappingconfig;
	private static Connection connection;
	private static String database;

	@BeforeClass
	public static void setUpDatabase() throws IOException, SQLException {
		setDiffUpdateRoot();
		backupConfig();
		setTestConfig(Util.readFileAsString(TEST_CONFIG_LOCATION), Util
				.readFileAsString(TEST_MAPPING_LOCATION));
		// Load Database driver
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Properties p = new Properties();
		p.load(new FileReader(new File(TEST_CONFIG_LOCATION)));
		p.setProperty(ConfigConstants.CONF_DIFF_UPDATEROOT,  new File("test" + File.separatorChar
						+ "osm-dir").toURI().toURL().toString());
		System.out.println(ConfigConstants.CONF_DIFF_UPDATEROOT + ":"
				+ p.getProperty(ConfigConstants.CONF_DIFF_UPDATEROOT));
		database = p.getProperty(ConfigConstants.CONF_DB_DATABASE);
		String username = p.getProperty(ConfigConstants.CONF_DB_USERNAME);
		String host = p.getProperty(ConfigConstants.CONF_DB_HOST);
		String port = p.getProperty(ConfigConstants.CONF_DB_PORT);
		String password = p.getProperty(ConfigConstants.CONF_DB_PW);
		connection = DriverManager.getConnection("jdbc:postgresql://" + host
				+ ":" + port + "/" + database, username, password);
		System.out.println("Connected to DB: "+"jdbc:postgresql://" + host
				+ ":" + port + "/" + database);
		clearDatabase();
	}

	private static void setDiffUpdateRoot() throws IOException,
			FileNotFoundException, MalformedURLException {
		Properties p = new Properties();
		p.load(new FileReader(new File(TEST_CONFIG_LOCATION)));
		p.setProperty(ConfigConstants.CONF_DIFF_UPDATEROOT,  new File("test" + File.separatorChar
				+ "osm-dir").toURI().toURL().toString());
		p.store(new FileWriter(new File(TEST_CONFIG_LOCATION)), "");
	}

	@AfterClass
	public static void tearDownDatabase() throws IOException, SQLException {
		restoreConfig();
//		clearDatabase();
		connection.close();
		
	}

	@Before
	public void resetDatabase() throws FileNotFoundException, IOException,
			SQLException {
		clearDatabase();
	}

	@Before
	public void readCurrentJobInvocationCount() {
		jobInvocationCount = UpdateJob.invocationCount;
	}

	protected void waitForUpdateJob(boolean noTimeout) {
		long time = System.currentTimeMillis();
		while (UpdateJob.invocationCount == jobInvocationCount) {
			if(!noTimeout && System.currentTimeMillis() > time+UPDATE_TIMEOUT){
				throw new RuntimeException("The update-job did not complete within "+UPDATE_TIMEOUT+" ms.");
			}
			Thread.yield();
		}
		readCurrentJobInvocationCount();
	}

	protected static void backupConfig() throws FileNotFoundException {
		config = Util.readFileAsString(CONFIG_LOCATION);
		mappingconfig = Util.readFileAsString(MAPPING_LOCATION);
	}

	protected static void setTestConfig(String config, String mapping)
			throws IOException {
		Util.writeStringToFile(CONFIG_LOCATION, config);
		setMappingConfig(mapping);
	}

	protected static void setMappingConfig(String mapping) throws IOException {
		Util.writeStringToFile(MAPPING_LOCATION, mapping);
	}

	protected static void restoreConfig() throws IOException {
		Util.writeStringToFile(CONFIG_LOCATION, config);
		Util.writeStringToFile(MAPPING_LOCATION, mappingconfig);
		System.out.println("Restored config files!");
	}

	protected static void clearDatabase() throws FileNotFoundException,
			IOException, SQLException {

		DatabaseMetaData metadata = connection.getMetaData();
		ResultSet rs = metadata.getTables(database, null, null,
				new String[] { "TABLE" });
		List<String> tablesToDelete = new ArrayList<String>();
		while (rs.next()) {
			String tablename = rs.getString("TABLE_NAME");
			if (isDeletable(tablename)) {
				tablesToDelete.add(tablename);
			}
		}
		for (String table : tablesToDelete) {
			Statement drop = connection.createStatement();
			System.out.println("DROP " + table);
			try {
				drop.execute("DROP TABLE " + table + " CASCADE;");
			} catch (PSQLException ex) {
				System.out.println(ex);
			}
			drop.close();
		}
		rs.close();
	}

	protected ResultSet executeSQLQuery(String sql) throws SQLException {
		Statement stmt = connection.createStatement();
		return stmt.executeQuery(sql);
	}
	
	protected void performUpdate(String updateFileContent) throws IOException {
		performUpdate(updateFileContent, false);
	}

	protected void performUpdate(String updateFileContent, boolean noTimeout) throws IOException {
		Util.writeStringToFile("test/osm-dir/minute-replicate/000/000/001.osc",
				updateFileContent);
		Main.main(new String[] { "--update", "--no-consistency",
				"--generate-ddl", "y", "--frequency", "minutely",
				"--initial-diff-replicate", "000000001.osc" });
		waitForUpdateJob(noTimeout);
	}
	
	protected void performInitialImport(String updateFileContent) throws IOException {
		String initialImportFile = "test/osm-dir/initial_import.osm";
		Util.writeStringToFile(initialImportFile,
				updateFileContent);
		Main.main(new String[] { "--initial-import", "--no-consistency",
				"--generate-ddl", "y",
				"-f", new File(initialImportFile).getAbsolutePath() });
	}

	protected void assertEntity(ResultSet rs, long osmId, String keyvalue)
			throws SQLException {
				rs.next();
				assertOSMId(rs, osmId);
				assertEquals(keyvalue, rs.getString("keyvalue"));
			}

	protected void assertOSMId(ResultSet rs, long osmId) throws SQLException {
		assertEquals(osmId, rs.getLong("osm_id"));
	}

	protected void assertEndOfResultSet(ResultSet rs) throws SQLException {
		assertFalse(rs.next());
		rs.close();
	}

	protected void assertPoint(String table, int osmId, int lat,
			int lon) throws SQLException {
				ResultSet rs = executeSQLQuery("SELECT ST_asText(geom) as t FROM "+table+" where osm_id = "+osmId+";");
				rs.next();
				assertEquals("POINT("+lon+" "+lat+")", rs.getString("t"));
				assertFalse(rs.next());
				rs.close();
			}

	protected void assertLine(String table, int osmId, int...points)
			throws SQLException {
				ResultSet rs = executeSQLQuery("SELECT ST_asText(geom) as t FROM "+table+" WHERE osm_id = "+osmId);
				rs.next();
				StringBuilder expected = new StringBuilder();
				expected.append("LINESTRING(");
				for(int i = 0; i<points.length; i++){
					expected.append(points[i]);
					if(i == points.length-1){
						break;
					}
					expected.append(i%2==0 ? " ": ",");
				}
				expected.append(")");
				assertEquals(expected.toString(), rs.getString("t"));
				assertFalse(rs.next());
				rs.close();
			}

	protected void assertGeometry(String table, int osmId, String multipolygon) throws SQLException {
		ResultSet rs = executeSQLQuery("SELECT ST_asText(geom) as t FROM "+table+" WHERE osm_id = "+osmId);
		rs.next();
		assertEquals(multipolygon, rs.getString("t"));
		assertFalse(rs.next());
		rs.close();
	}

	protected void assertTableEmpty(String table) throws SQLException {
		ResultSet rs = executeSQLQuery("SELECT * FROM "+table+";");
		assertEndOfResultSet(rs);
	}

	private static boolean isDeletable(String tablename) {
		return !(tablename.equalsIgnoreCase("geometry_columns") || tablename
				.equalsIgnoreCase("spatial_ref_sys"));
	}
}

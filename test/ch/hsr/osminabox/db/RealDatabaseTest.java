package ch.hsr.osminabox.db;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;

import ch.hsr.osminabox.test.TestDatabaseTestTemplate;
import ch.hsr.osminabox.test.Util;

public class RealDatabaseTest extends TestDatabaseTestTemplate{
	
	@Test
	public void testCreationOfStateFile() throws Exception {
		File f = new File("./state.txt");
		testSimpleDataInsertation();
		String s = Util.readFileAsString(f.getAbsolutePath());
		assertEquals("{\"time\":\""+new SimpleDateFormat("dd.mm.yyyy")
			.format(new Date())+"\",\"nextUpdateFile\":\"000000002.osc\"}", s);
		f.delete();
	}
	@Test
	public void testSimpleDataInsertation() throws Exception {
		setMappingConfig(Util.readFileAsString("test/config/poi_mapping.xml"));
		performUpdate(Util.readFileAsString("test/config/add_university_and_motel.osc"));
		
		ResultSet rs = executeSQLQuery("SELECT * FROM poi ORDER BY osm_id;");
		assertEntity(rs, 999999910, "\"amenity\"=>\"university\"");
		assertEntity(rs, 999999911, "\"amenity\"=>\"motel\"");
		assertEndOfResultSet(rs);
	}
	
	@Test
	public void testDataInsertationWithNodeThatHasNoMapping() throws Exception {
		setMappingConfig(Util.readFileAsString("test/config/poi_mapping_only_university.xml"));
		performUpdate(Util.readFileAsString("test/config/add_university_and_motel.osc"));
	
		ResultSet rs = executeSQLQuery("SELECT * FROM poi ORDER BY osm_id;");
		rs.next();
		assertOSMId(rs, 999999910l);
		assertEquals("university", rs.getString("type"));
		assertEndOfResultSet(rs);
	}
	
	@Test
	public void testOSCWithSameOSMIdButDifferentVersions() throws Exception {
		setMappingConfig(Util.readFileAsString("test/config/mappingconfig.xml"));
		performUpdate(Util.readFileAsString("test/config/insertRoute.osc"));
		assertDatabaseContent();
		performUpdate(Util.readFileAsString("test/config/modifyRoute.osc"));
		assertDatabaseContent();
	}
	
	private void assertDatabaseContent() throws SQLException {
		ResultSet rs = executeSQLQuery("SELECT * FROM road ORDER BY osm_id;");
		rs.next();
		assertOSMId(rs, 4);
		assertEndOfResultSet(rs);
		rs = executeSQLQuery("SELECT * FROM route ORDER BY osm_id;");
		rs.next();
		assertOSMId(rs, 5);
		if(rs.next()){
			System.err.println("HAS NEXT");
			assertEquals(5, rs.getLong("osm_id"));
			// TODO: this is just a workaround for bug 8 to get the test to pass => result should NOT have two entries!!!
		}
		assertEndOfResultSet(rs);
	}
	
	@Test
	@Ignore
	public void testAdditionOfTagsToExistingNodesAndWays() throws Exception {
		setMappingConfig(Util.readFileAsString("test/config/mappingconfig.xml"));
		performInitialImport(Util.readFileAsString("test/config/create_university.osm"));
		assertInitialImportOfUniversity();
		performUpdate(Util.readFileAsString("test/config/add_university_tags.osc"));
		assertUniversityTagsAdded();
	}
	
	private void assertUniversityTagsAdded() throws SQLException {
		ResultSet rs = executeSQLQuery("SELECT * FROM poi ORDER BY osm_id;");
		assertEntity(rs, 1132126722, "\"btag\"=>\"Net Value\", \"name\"=>\"Uni A.\", \"amenity\"=>\"university\"");
		assertEndOfResultSet(rs);
		rs = executeSQLQuery("SELECT * FROM building ORDER BY osm_id;");
		assertEntity(rs, 97803121, "\"atag\"=>\"Remove that\", \"name\"=>\"Build C\", \"building\"=>\"yes\"");
		assertEntity(rs, 97803122, "\"name\"=>\"Uni B.\", \"amenity\"=>\"university\"");
		assertEndOfResultSet(rs);
	}

	private void assertInitialImportOfUniversity() throws SQLException {
		ResultSet rs = executeSQLQuery("SELECT * FROM poi ORDER BY osm_id;");
		assertEntity(rs, 1132126722, "\"name\"=>\"Uni A.\", \"amenity\"=>\"university\"");
		assertEndOfResultSet(rs);
		rs = executeSQLQuery("SELECT * FROM building ORDER BY osm_id;");
		assertEntity(rs, 97803121, "\"name\"=>\"Build C\", \"building\"=>\"yes\"");
		assertEntity(rs, 97803122, "\"name\"=>\"Uni B.\", \"amenity\"=>\"university\"");
		assertEndOfResultSet(rs);
	}
	
	
	@Test
	public void testTwoMappingsForSamePoint() throws Exception {
		setMappingConfig(Util.readFileAsString("test/config/double_mapping_for_same_node.xml"));
		performUpdate(Util.readFileAsString("test/config/add_university_and_motel.osc"));
		ResultSet rs = executeSQLQuery("SELECT * FROM poi ORDER BY osm_id;");
		assertEntity(rs, 999999910, "\"amenity\"=>\"university\"");
		assertEntity(rs, 999999911, "\"amenity\"=>\"motel\"");
		assertEndOfResultSet(rs);
		rs = executeSQLQuery("SELECT * FROM mypoi ORDER BY osm_id;");
		assertEntity(rs, 999999910, "\"amenity\"=>\"university\"");
		assertEndOfResultSet(rs);
	}

	@Test
	public void testImportSimplePOI() throws IOException, SQLException {
		setMappingConfig(Util.readFileAsString("test/config/simpleMapping.xml"));
		performInitialImport(Util.readFileAsString("test/config/university_and_motel.osm"));
		assertUniversityAndMotel();
		assertPoint("poi", 1, 64, -20);
		assertPoint("poi", 2, 64, -10);
	}
	
	@Test
	public void testUpdateSimplePOI() throws Exception {
		testImportSimplePOI();
		performUpdate(Util.readFileAsString("test/config/move_university.osc"));
		assertUniversityAndMotel();
		assertPoint("poi", 1, 42, 20);
		assertPoint("poi", 2, 64, -10);
	}
	
	@Test
	public void testDeleteSimplePOI() throws Exception {
		testImportSimplePOI();
		performUpdate(Util.readFileAsString("test/config/delete_university.osc"));
		ResultSet rs = executeSQLQuery("SELECT * FROM poi ORDER BY osm_id;");
		assertEntity(rs, 2, "\"amenity\"=>\"motel\"");
		assertEndOfResultSet(rs);
	}

	private void assertUniversityAndMotel() throws SQLException {
		ResultSet rs = executeSQLQuery("SELECT * FROM poi ORDER BY osm_id;");
		assertEntity(rs, 1, "\"amenity\"=>\"university\"");
		assertEntity(rs, 2, "\"amenity\"=>\"motel\"");
		assertEndOfResultSet(rs);
	}
	
	@Test
	public void testImportSimpleRoad() throws Exception {
		setMappingConfig(Util.readFileAsString("test/config/simpleMapping.xml"));
		performInitialImport(Util.readFileAsString("test/config/create_highway.osm"));
		ResultSet rs = executeSQLQuery("SELECT * FROM road ORDER BY osm_id;");
		assertEntity(rs, 7, "\"name\"=>\"Testway\", \"highway\"=>\"motorway\"");
		assertEndOfResultSet(rs);
		assertLine("road", 7, 2,1, 4,3, 6,5);
	}

	@Test
	public void testUpdateSimpleRoad() throws Exception {
		testImportSimpleRoad();
		performUpdate(Util.readFileAsString("test/config/modify_full_highway.osc"));
		ResultSet rs = executeSQLQuery("SELECT * FROM road ORDER BY osm_id;");
		assertEntity(rs, 7, "\"name\"=>\"Testway2\", \"highway\"=>\"motorway\"");
		assertEndOfResultSet(rs);
		assertLine("road", 7, 5,6, 3,4, 1,2);
	}
	
	@Test
	@Ignore
	public void testUpdateNodeReferencedByAWaySouldAdjustWayGeometry() throws Exception {
		testImportSimpleRoad();
		performUpdate(Util.readFileAsString("test/config/modify_partial_highway.osc"));
		ResultSet rs = executeSQLQuery("SELECT * FROM road ORDER BY osm_id;");
		assertEntity(rs, 7, "\"name\"=>\"Testway\", \"highway\"=>\"motorway\"");
		assertEndOfResultSet(rs);
		assertLine("road", 7, 2,1, 30,40, 6,5);
	}
	
	@Test
	public void testDeleteSimpleRoad() throws Exception {
		testImportSimpleRoad();
		performUpdate(Util.readFileAsString("test/config/delete_highway.osc"));
		assertTableEmpty("road");
	}
	
	@Test
	public void testImportInvalidRoadDoesNotCreateARoadInTheDatabase() throws Exception {
		setMappingConfig(Util.readFileAsString("test/config/simpleMapping.xml"));
		performInitialImport(Util.readFileAsString("test/config/create_invalid_road.osm"));
		assertTableEmpty("road");
	}
	
	@Test
	public void testImportSimpleArea() throws Exception {
		setMappingConfig(Util.readFileAsString("test/config/simpleMapping.xml"));
		performInitialImport(Util.readFileAsString("test/config/create_forest.osm"));
		ResultSet rs = executeSQLQuery("SELECT * FROM landuse ORDER BY osm_id;");
		assertEntity(rs, 7, "\"name\"=>\"Testwood\", \"landuse\"=>\"wood\"");
		assertEndOfResultSet(rs);
		assertGeometry("landuse", 7, "MULTIPOLYGON(((2 1,4 3,6 5,2 1)))");
	}
	
	@Test
	public void testImportInvalidArea() throws Exception {
		setMappingConfig(Util.readFileAsString("test/config/simpleMapping.xml"));
		performInitialImport(Util.readFileAsString("test/config/create_invalid_forest.osm"));
		assertTableEmpty("landuse");
	}
	
	@Test
	public void testUpdateASimpleArea() throws Exception {
		testImportSimpleArea();
		performUpdate(Util.readFileAsString("test/config/modify_forest.osc"));
		ResultSet rs = executeSQLQuery("SELECT * FROM landuse ORDER BY osm_id;");
		assertEntity(rs, 7, "\"name\"=>\"Testwood\", \"landuse\"=>\"wood\"");
		assertEndOfResultSet(rs);
		assertGeometry("landuse", 7, "MULTIPOLYGON(((3 2,5 4,7 6,3 2)))");
	}
	
	@Test
	public void testDeleteSimpleArea() throws Exception {
		testImportSimpleArea();
		performUpdate(Util.readFileAsString("test/config/delete_forest.osc"));
		assertTableEmpty("landuse");
	}

	@Test
	@Ignore
	public void testUpdateNodeReferencedByAnAreaSouldAdjustAreaGeometry() throws Exception {
		testImportSimpleArea();
		performUpdate(Util.readFileAsString("test/config/modify_partial_forest.osc"));
		ResultSet rs = executeSQLQuery("SELECT * FROM landuse ORDER BY osm_id;");
		assertEntity(rs, 7, "\"name\"=>\"Testwood\", \"landuse\"=>\"wood\"");
		assertEndOfResultSet(rs);
		assertGeometry("landuse", 7, "MULTIPOLYGON(((2 1,40 30,6 5,2 1)))");
	}
	
	@Test
	public void testAddNewTagToExistingEntityWhichSouldCreateANewMapping() throws Exception {
		testImportSimplePOI();
		performUpdate(Util.readFileAsString("test/config/add_motel_tag_to_university.osc"));
		ResultSet rs = executeSQLQuery("SELECT * FROM poi ORDER BY osm_id;");
		assertEntity(rs, 1, "\"amenity\"=>\"motel;university\"");
		assertEntity(rs, 2, "\"amenity\"=>\"motel\"");
		assertEndOfResultSet(rs);
	}
	
	@Test
	public void testRemoveTagDestroysEntity() throws Exception {
		testImportSimplePOI();
		performUpdate(Util.readFileAsString("test/config/remove_motel_tag.osc"));
		ResultSet rs = executeSQLQuery("SELECT * FROM poi ORDER BY osm_id;");
		assertEntity(rs, 1, "\"amenity\"=>\"university\"");
		assertEndOfResultSet(rs);
	}
	
	@Test
	public void testChangeTagValueDestroyOldMappingAndGeneratesNew() throws Exception {
		testImportSimplePOI();
		performUpdate(Util.readFileAsString("test/config/change_anemity_tag_to_motel.osc"));
		ResultSet rs = executeSQLQuery("SELECT * FROM poi ORDER BY osm_id;");
		assertEntity(rs, 1, "\"amenity\"=>\"motel\"");
		assertEntity(rs, 2, "\"amenity\"=>\"motel\"");
		assertEndOfResultSet(rs);
	}
	
	@Test
	public void testImportSimpleRelation() throws Exception {
		setMappingConfig(Util.readFileAsString("test/config/simpleMapping.xml"));
		performInitialImport(Util.readFileAsString("test/config/create_route.osm"));
		ResultSet rs = executeSQLQuery("SELECT * FROM road ORDER BY osm_id;");
		assertEntity(rs, 6, "\"name\"=>\"Testway1\", \"highway\"=>\"motorway\"");
		assertEntity(rs, 7, "\"name\"=>\"Testway2\", \"highway\"=>\"motorway\"");
		assertEndOfResultSet(rs);
		assertGeometry("road", 6, "LINESTRING(2 1,4 3,6 5)");
		assertGeometry("road", 7, "LINESTRING(8 7,10 9)");
		rs = executeSQLQuery("SELECT * FROM route ORDER BY osm_id;");
		assertEntity(rs, 8, "\"ref\"=>\"T1\", \"type\"=>\"route\", \"route\"=>\"highway\", \"network\"=>\"testnetwork\"");
		assertEndOfResultSet(rs);
		rs = executeSQLQuery("SELECT * FROM route_to_road ORDER BY route_id;");
		assertRouteRoad(rs, 1, 1);
		assertRouteRoad(rs, 1, 2);
		assertEndOfResultSet(rs);
	}

	@Test
	public void testImportInvalidRelation() throws Exception {
		setMappingConfig(Util.readFileAsString("test/config/simpleMapping.xml"));
		performInitialImport(Util.readFileAsString("test/config/create_invalid_route.osm"));
		assertTableEmpty("route");
		assertTableEmpty("road");
		assertTableEmpty("route_to_road");
	}
	
	@Test
	@Ignore
	public void testUpdateSimpleRelation() throws Exception {
		testImportSimpleRelation();
		performUpdate(Util.readFileAsString("test/config/modify_route.osc"));
		ResultSet rs = executeSQLQuery("SELECT * FROM road ORDER BY osm_id;");
		assertEntity(rs, 6, "\"name\"=>\"Testway1\", \"highway\"=>\"motorway\"");
		assertEntity(rs, 7, "\"name\"=>\"Testway2\", \"highway\"=>\"motorway\"");
		assertEntity(rs, 10, "\"name\"=>\"Testway3\", \"highway\"=>\"motorway\"");
		assertEndOfResultSet(rs);
		assertGeometry("road", 6, "LINESTRING(2 1,4 3,6 5)");
		assertGeometry("road", 7, "LINESTRING(8 7,10 9)");
		assertGeometry("road", 10, "LINESTRING(2 1,10 9)");
		rs = executeSQLQuery("SELECT * FROM route ORDER BY osm_id;");
		assertEntity(rs, 8, "\"ref\"=>\"T1\", \"type\"=>\"route\", \"route\"=>\"highway\", \"network\"=>\"testnetwork\"");
		assertEndOfResultSet(rs);
		rs = executeSQLQuery("SELECT * FROM route_to_road ORDER BY route_id;");
		assertRouteRoad(rs, 1, 1);
		assertRouteRoad(rs, 1, 2);
		assertRouteRoad(rs, 1, 3);
		assertEndOfResultSet(rs);
	}
	
	@Test
	@Ignore
	public void testRemoveRelation() throws Exception {
		testImportSimpleRelation();
		performUpdate(Util.readFileAsString("test/config/delete_route.osc"));
		assertTableEmpty("route");
		assertTableEmpty("route_to_road");
	}
	
	private void assertRouteRoad(ResultSet rs, int routeId, int roadId) throws SQLException {
		rs.next();
		assertEquals(routeId, rs.getInt("route_id"));
		assertEquals(roadId, rs.getInt("road_id"));
	}
	
	@Test
	public void testModifyTagWhichAffectsTwoDifferentTables() throws Exception {
		testImportSimplePOI();
		performUpdate(Util.readFileAsString("test/config/change_anemity_tag_to_bank.osc"));
		ResultSet rs = executeSQLQuery("SELECT * FROM poi ORDER BY osm_id;");
		assertEntity(rs, 2, "\"amenity\"=>\"motel\"");
		assertEndOfResultSet(rs);
		rs = executeSQLQuery("SELECT * FROM bank ORDER BY osm_id;");
		assertEntity(rs, 1, "\"amenity\"=>\"bank\"");
		assertEndOfResultSet(rs);
	}
}

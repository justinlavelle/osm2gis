package ch.hsr.osminabox.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.db.boundingbox.BoundingBoxStrategy;
import ch.hsr.osminabox.db.boundingbox.BoundingBoxStrategyFactory;
import ch.hsr.osminabox.db.dbdefinition.Column;
import ch.hsr.osminabox.db.dbdefinition.Database;
import ch.hsr.osminabox.db.dbdefinition.Table;
import ch.hsr.osminabox.db.dbdefinition.View;
import ch.hsr.osminabox.db.differentialupdate.DeleteOSMEntites;
import ch.hsr.osminabox.db.differentialupdate.ModificationType;
import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.handling.AreaHandler;
import ch.hsr.osminabox.db.handling.NodeHandler;
import ch.hsr.osminabox.db.handling.RelationHandler;
import ch.hsr.osminabox.db.handling.WayHandler;
import ch.hsr.osminabox.db.mapping.MapperService;
import ch.hsr.osminabox.db.util.AreaConstructor;
import ch.hsr.osminabox.db.util.AreaUtil;
import ch.hsr.osminabox.db.util.DiffType;
import ch.hsr.osminabox.db.util.NodeUtil;
import ch.hsr.osminabox.db.util.RelationUtil;
import ch.hsr.osminabox.db.util.WayConstructor;
import ch.hsr.osminabox.db.util.WayUtil;

/**
 * Implementation of Database Service
 * 
 * @author jzimmerm
 * 
 */
public class OSMDBService implements DBService {

	private static Logger logger = Logger.getLogger(OSMDBService.class);
	
	private ApplicationContext context;

	private Connection connection;
	private String databaseName;

	// Mapping
	private MapperService mapper;

	// Handling
	private NodeHandler nodeHandling;
	private WayHandler wayHandling;
	private RelationHandler relationHandling;
	private AreaHandler areaHandling;

	// Delete Hanlding
	private DeleteOSMEntites deleteEntites;
	
	// Util
	private NodeUtil nodeUtil;
	private WayConstructor wayConstructor;
	private WayUtil wayUtil;
	private AreaConstructor areaConstructor;
	private AreaUtil areaUtil;
	private RelationUtil relationUtil;
	
	private BoundingBoxStrategy bbox;
	
	private Database dbStructure;
	

	/**
	 * Instanciate a new OSMDBService
	 */
	public OSMDBService(ApplicationContext context) {
		
		this.context = context;

		// Load Database driver
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			logger.error("Databse Driver not found");
			e.printStackTrace();
		}
	}

	/**
	 * Connects to the database and initiates all local variables.
	 */
	public boolean connect(String host, String port, String database,
			String username, String password) {
		this.databaseName = database;
		try {
			connection = DriverManager.getConnection("jdbc:postgresql://"
					+ host + ":" + port + "/" + database, username, password);

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("Connection to Database failed.");
			System.exit(0);
			return false;
		}
		
		dbStructure = new Database(databaseName);
		updateDBStructure();
		
		nodeHandling = new NodeHandler(context, dbStructure);
		wayHandling = new WayHandler(context, connection, dbStructure);
		areaHandling = new AreaHandler(context, dbStructure);
		relationHandling = new RelationHandler(context, dbStructure);
		
		deleteEntites = new DeleteOSMEntites(context.getConfigService());
		
		mapper = new MapperService(context.getConfigService());
		
		nodeUtil = new NodeUtil(context.getConfigService(), connection);
		wayConstructor = new WayConstructor(connection);
		wayUtil = new WayUtil(context.getConfigService(), connection);
		areaConstructor = new AreaConstructor(connection, context);
		areaUtil = new AreaUtil(context.getConfigService(), connection);
		relationUtil = new RelationUtil(connection, context.getConfigService());
		
		bbox = BoundingBoxStrategyFactory.createBoundingBoxStrategy(context);

		return true;
	}
	
	/**
	 * Checks and returns the connection state to the database.
	 */
	public boolean isConnected() {
		try {
			if (connection == null) {
				return false;
			} else {
				return !connection.isClosed();
			}
		} catch (SQLException e) {
			logger.error("Database Connection is broken. Please Restart the Application!");
			return false;
		}
	}
	
	/**
	 * Executes all statements in the collection.
	 * @param statements
	 */
	private void exec(Collection<StringBuffer> statements){
		for(StringBuffer sql : statements){
			exec(sql.toString());
		}
	}

	/**
	 * Executes the given SQL-String.
	 * @param sql - The SQL-String to be executed
	 * @return
	 */
	private ResultSet exec(String sql) {
		return exec(sql, false);
	}
	
	/**
	 * Executes the given SQL-String. Reloads the DB-Structure Object if the reloadDBStructure-flag is set.
	 * @param sql - The SQL-String to be executed
	 * @param reloadDbStructure - If true, the DBStructure Object is reloaded, this is used because some Util-Classes need an up to date DBStructure Object to work.
	 * @return 
	 */
	private ResultSet exec(String sql, boolean reloadDbStructure){
		Statement st;
		try {
			st = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);

			st.setEscapeProcessing(true);
			st.execute(sql);
			
			if(reloadDbStructure)
				updateDBStructure();
			
			return st.getResultSet();
		} catch (SQLException e) {
			logger.error("SQL File could not been executed");
			logger.error(e);
			logger.error(sql);
			return null;
		}		
	}

	/**
	 * Creates the Database-Tables according to the generated SQL-File based on the Schema-Mapping-File. 
	 */
	public void createDatabase() {
		CreateTable createDB = new CreateTable();
		exec(createDB.getHStoreSQL());
		exec(createDB.getCreateTableSQL(), true);
		
		logger.debug("Database created");
	}

	
	/**
	 * Creates all needed temporary Tables for an import / update
	 */
	public boolean createTempTables() {

		removeTempTables();
		
		logger.debug("Creating Temp Tables: " + DBConstants.NODE_TEMP + ", " + DBConstants.WAY_TEMP + ", " + DBConstants.RELATION_TEMP + ", " + DBConstants.RELATION_MEMBER_TEMP);
		
		String sql = "CREATE TABLE " + DBConstants.NODE_TEMP
				+ "(id serial PRIMARY KEY," + DBConstants.ATTR_OSM_ID
				+ " bigint, " + DBConstants.ATTR_LASTCHANGE + " timestamp, "
				+ DBConstants.ATTR_NODE_TEMP_LAT + " varchar(20),"
				+ DBConstants.ATTR_NODE_TEMP_LON + " varchar(20)); "
				+ "CREATE INDEX CONCURRENTLY osm_node_id_index ON "
				+ DBConstants.NODE_TEMP + "(" + DBConstants.ATTR_OSM_ID + "); "
				+ "CREATE TABLE " + DBConstants.WAY_TEMP
				+ "(id serial PRIMARY KEY, " + DBConstants.ATTR_OSM_ID
				+ " bigint, " + DBConstants.ATTR_LASTCHANGE + " timestamp,"
				+ DBConstants.ATTR_WAY_TEMP_NODES + " text, "
				+ DBConstants.ATTR_WAY_TEMP_USEDBYRELATIONS
				+ " boolean NOT NULL DEFAULT false, "
				+ DBConstants.ATTR_KEYVALUE + " HSTORE, "
				+ DBConstants.ATTR_WAY_TEMP_DIFFTYPE + " varchar(20));"
				+ "CREATE INDEX CONCURRENTLY osm_way_id_index ON "
				+ DBConstants.WAY_TEMP + "(" + DBConstants.ATTR_OSM_ID + "); "
				+ "CREATE INDEX CONCURRENTLY osm_way_usedbyrelation_index ON "
				+ DBConstants.WAY_TEMP + "(" + DBConstants.ATTR_WAY_TEMP_USEDBYRELATIONS + "); "
				+ "CREATE INDEX CONCURRENTLY osm_way_difftype_index ON "
				+ DBConstants.WAY_TEMP + "(" + DBConstants.ATTR_WAY_TEMP_DIFFTYPE + "); "
				+ "CREATE TABLE " + DBConstants.RELATION_TEMP
				+ "(id serial PRIMARY KEY, " + DBConstants.ATTR_OSM_ID
				+ " bigint, " + DBConstants.ATTR_LASTCHANGE + " timestamp, "
				+ DBConstants.ATTR_KEYVALUE + " HSTORE, "
				+ DBConstants.ATTR_RELATION_TEMP_MEMBER + " text, "
				+ DBConstants.ATTR_RELATION_TEMP_DIFFTYPE + " varchar(20));"
				+ "CREATE INDEX CONCURRENTLY osm_relation_id_index ON "
				+ DBConstants.RELATION_TEMP + "(" + DBConstants.ATTR_OSM_ID + "); "
				+ "CREATE INDEX CONCURRENTLY osm_relation_difftype_index ON "
				+ DBConstants.WAY_TEMP + "(" + DBConstants.ATTR_RELATION_TEMP_DIFFTYPE + "); "
				+ "CREATE TABLE " + DBConstants.RELATION_MEMBER_TEMP
				+ "(" + DBConstants.ATTR_ID + " serial PRIMARY KEY, " 
				+ DBConstants.ATTR_RELATION_MEMBER_TEMP_RELATION_OSM_ID + " bigint NOT NULL, "
				+ DBConstants.ATTR_RELATION_MEMBER_TEMP_MEMBER_OSM_ID + " bigint NOT NULL, "
				+ DBConstants.ATTR_RELATION_MEMBER_TEMP_TYPE + " character varying(20) NOT NULL, "
				+ DBConstants.ATTR_RELATION_MEMBER_TEMP_ROLE + " character varying(25));";

		exec(sql, true);
		
		return true;
	}

	/**
	 * Removes all temporary tables if they exist.
	 */
	public void removeTempTables() {
		
		logger.debug("Removing Temp Tables: " + DBConstants.NODE_TEMP + ", " + DBConstants.WAY_TEMP + ", " + DBConstants.RELATION_TEMP + ", " + DBConstants.RELATION_MEMBER_TEMP);
		
		String sql = "DROP TABLE IF EXISTS " + DBConstants.NODE_TEMP
				+ "; DROP TABLE IF EXISTS " + DBConstants.WAY_TEMP
				+ "; DROP TABLE IF EXISTS " + DBConstants.RELATION_MEMBER_TEMP
				+ "; DROP TABLE IF EXISTS " + DBConstants.RELATION_TEMP;
				
		
		exec(sql, true);
	}

	/**
	 * Takes a List of Nodes from a NodeListener Class when its wakeup-Function is called.
	 */
	public void insertNodes(List<Node> nodes) {
		
		exec(nodeHandling.createTempInsertStatements(nodes).values()); 
		
		nodeUtil.deleteNodesOutsideBBox(nodes, bbox);

		mapper.addDbMappingsForNodes(nodes);		

		exec(nodeHandling.createInsertStatements(nodes).values());
	}
	
	/**
	 * Takes a List of Ways from a WayListener Class when its wakeup-Function is called.
	 */
	public void insertWays(List<Way> ways) {
		
		logger.debug("Adding Node data from " + DBConstants.NODE_TEMP + " to Ways and validate them.");
		wayConstructor.addNodeData(ways);
		
		exec(wayHandling.createTempInsertStatements(ways, DiffType.create).values());
		
		wayUtil.deleteWaysOutsideBBox(ways, bbox);
		
		mapper.addDbMappingsForWays(ways);

		exec(wayHandling.createInsertStatements(ways).values());
	}
	
	/**
	 * Takes a List of Areas from an AreaListener Class when its wakeup-Function is called.
	 * @param areas
	 */
	public void insertAreas(List<Area> areas){
		
		logger.debug("Adding Way data from " + DBConstants.WAY_TEMP + " Table to " + areas.size() + " Areas.");
		areaConstructor.addWaysAndTags(areas);
		
		logger.debug("Assigning Rings out of multiple Ways for " + areas.size() + " Areas.");
		areaConstructor.assignRings(areas);
		
		areaUtil.deleteAreasOutsideBBox(areas, bbox);
		
		mapper.addDbMappingsForAreas(areas);

		exec(areaHandling.createInsertStatements(areas).values());
		
		logger.debug("Marking all used Ways as 'usedbyrelation'");
		areaUtil.markWaysUsedByRelations(areas);
	}
	
	/**
	 * Processes all Ways in the way_temp Table which are not used by any Relation / Area and treats them like Areas if they are single-closed.
	 */
	public void insertRemainingAreas() {
		
		List<Area> areas = new LinkedList<Area>();
		
		do{
			areas.clear();
			areaUtil.getSingleClosedWays(areas);
			
			areaConstructor.addWaysAndTags(areas);
			
			areaUtil.deleteAreasOutsideBBox(areas, bbox);
			
			mapper.addDbMappingsForAreas(areas);

			exec(areaHandling.createInsertStatements(areas).values());
			
		} while (areas.size() > 0);

	}
	
	
	/**
	 * Takes a List of Relations from a RelationListener Class when its wakeup-Function is called.
	 */
	public void insertRelations(List<Relation> relations) {
		insertRelations(relations, DiffType.create);		
	}
	
	private void insertRelations(List<Relation> relations, DiffType diffType){
		
		// First, all Relations are inserted in the relation_temp Table because they might have cross-references.
		exec(relationHandling.createTempStatements(relations, diffType).values());
		
	}
	
	
	/**
	 * Processes all Relations in the relation_temp Table and inserts them in their mapped tables.
	 */
	public void insertTempRelations(){
		
		List<Relation> relations = new LinkedList<Relation>();
		
		relationUtil.deleteUnavailableRelationMembers();
		
		int relationSize = 0;
		do{
			relations.clear();
			relationUtil.getNextTempRelation(relations);
			logger.debug(relations.size() + " Relations retrieved to be precessed now.");
			
			relationSize = relations.size();
			
			mapper.addDbMappingsForRelations(relations);
			
			relationUtil.addMemberIdsFromDb(relations); // Also deletes dbMappings if required Members are not available.

			relationUtil.removeUnmappedRelations(relations);
			
			if(relations.size() > 0){
			
				exec(relationHandling.createInsertStatements(relations).values());
			
				relationUtil.addRelationIdsFromDb(relations);
				
				exec(relationHandling.createJoinStatements(relations).values());
			}
			
		} while(relationSize > 0);
	}
	
	/**
	 * 
	 */
	public void modifyNodes(List<Node> nodes){
		
		exec(nodeHandling.createTempInsertStatements(nodes).values());
		
		mapper.addDbMappingsForNodes(nodes);
		
		Map<ModificationType, List<Node>> nodeGroups = nodeUtil.splitToModificationGroups(nodes);
		
		// INSERT Nodes
		nodeUtil.deleteNodesOutsideBBox(nodeGroups.get(ModificationType.INSERT), bbox);
		exec(nodeHandling.createInsertStatements(nodeGroups.get(ModificationType.INSERT)).values());
		
		// UPDATE Nodes
		exec(nodeHandling.createUpdateStatements(nodeGroups.get(ModificationType.UPDATE)).values());
		
		// DELETE Nodes
		exec(deleteEntites.deleteByDbMappings(nodeGroups.get(ModificationType.DELETE)).values());	
	}

	/**
	 * 
	 */
	public void modifyWays(List<Way> ways){
					
		wayConstructor.addNodeData(ways, context.getApiConsistencyService());
		
		exec(wayHandling.createTempInsertStatements(ways, DiffType.modify).values());
		
		mapper.addDbMappingsForWays(ways);
		
		Map<ModificationType, List<Way>> wayGroups = wayUtil.splitToModificationGroups(ways);		
		
		// INSERT Ways
		wayUtil.deleteWaysOutsideBBox(wayGroups.get(ModificationType.INSERT), bbox);
		exec(wayHandling.createInsertStatements(wayGroups.get(ModificationType.INSERT)).values());
		
		// UPDATE Ways
		exec(wayHandling.createUpdateStatements(wayGroups.get(ModificationType.UPDATE)).values());
		
		// DELETE Ways
		exec(deleteEntites.deleteByDbMappings(wayGroups.get(ModificationType.DELETE)).values());
	}
	
	/**
	 * 
	 */
	public void modifyAreas(List<Area> areas){
		
		areaConstructor.addWaysAndTags(areas, context.getApiConsistencyService());
	
		logger.debug("Assigning Rings out of multiple Ways for " + areas.size() + " Areas.");
		areaConstructor.assignRings(areas);
		
		mapper.addDbMappingsForAreas(areas);

		Map<ModificationType, List<Area>> areaGroups = areaUtil.splitToModificationGroups(areas);
		
		// INSERT Areas
		areaUtil.deleteAreasOutsideBBox(areaGroups.get(ModificationType.INSERT), bbox);
		exec(areaHandling.createInsertStatements(areaGroups.get(ModificationType.INSERT)).values());
		
		// UPDATE Areas
		exec(areaHandling.createUpdateStatements(areaGroups.get(ModificationType.UPDATE)).values());
		
		// DELETE Areas
		exec(deleteEntites.deleteByDbMappings(areaGroups.get(ModificationType.DELETE)).values());
		
		areaUtil.markWaysUsedByRelations(areas);
	}
	
	/**
	 * Processes all Ways in the way_temp Table which are not used by any Relation / Area and treats them like Areas if they are single-closed.
	 */
	public void modifyRemainingAreas(){
		List<Area> areas = new LinkedList<Area>();
		
		do{
			areas.clear();
			DiffType diffType = areaUtil.getSingleClosedWays(areas);
			
			switch (diffType) {
			case create:
				areaConstructor.addWaysAndTags(areas, context.getApiConsistencyService());
				areaUtil.deleteAreasOutsideBBox(areas, bbox);
				mapper.addDbMappingsForAreas(areas);
				exec(areaHandling.createInsertStatements(areas).values());
				
				break;
			case modify:
				areaConstructor.addWaysAndTags(areas, context.getApiConsistencyService());
				
				mapper.addDbMappingsForAreas(areas);

				Map<ModificationType, List<Area>> areaGroups = areaUtil.splitToModificationGroups(areas);
				
				// INSERT Areas
				areaUtil.deleteAreasOutsideBBox(areaGroups.get(ModificationType.INSERT), bbox);
				exec(areaHandling.createInsertStatements(areaGroups.get(ModificationType.INSERT)).values());
				
				// UPDATE Areas
				exec(areaHandling.createUpdateStatements(areaGroups.get(ModificationType.UPDATE)).values());
				
				// DELETE Areas
				exec(deleteEntites.deleteByDbMappings(areaGroups.get(ModificationType.DELETE)).values());
				
				break;
			case delete:
				deleteAreas(areas);
				
				break;
			}
			
		} while (areas.size() > 0);
	}
	
	@Override
	public void modifyRelations(List<Relation> relations) {
		insertRelations(relations, DiffType.modify);		
	}
	
	
	@Override
	public void modifyTempRelations() {
		
		List<Relation> relations = new LinkedList<Relation>();
		
		relationUtil.deleteUnavailableRelationMembers();
		
		do{
			relations.clear();
			DiffType diffType = relationUtil.getNextTempRelation(relations);
			logger.debug(relations.size() + " Relations retrieved to be processed now (DiffType: " + diffType + ").");
			
			switch(diffType){
			case create:
				mapper.addDbMappingsForRelations(relations);
				
				relationUtil.addMemberIdsFromDb(relations); // Also deletes dbMappings if required Members are not available.

				relationUtil.removeUnmappedRelations(relations);
				
				if(relations.size() > 0){
				
					exec(relationHandling.createInsertStatements(relations).values());
					
					try{
						relationUtil.addRelationIdsFromDb(relations);
					}catch(NullPointerException e){
						logger.error("NullPointerException during relationUtil.addRelationIdsFromDb --> skip join statements", e);
						break;
					}
					
					exec(relationHandling.createJoinStatements(relations).values());
					
				}
				break;
				
			case modify:
				mapper.addDbMappingsForRelations(relations);
				
				Map<ModificationType, List<Relation>> relationGroups = relationUtil.splitToModificationGroups(relations);
				
				// INSERT Relations
				relationUtil.addMemberIdsFromDb(relationGroups.get(ModificationType.INSERT)); // Also deletes dbMappings if required Members are not available.
				relationUtil.removeUnmappedRelations(relationGroups.get(ModificationType.INSERT));
				exec(relationHandling.createInsertStatements(relationGroups.get(ModificationType.INSERT)).values());

				relationUtil.addRelationIdsFromDb(relationGroups.get(ModificationType.INSERT));
				deleteEntites.deleteJoinEntries(relationGroups.get(ModificationType.INSERT));
				exec(relationHandling.createJoinStatements(relationGroups.get(ModificationType.INSERT)).values());
				
				// UPDATE Relations
				relationUtil.addMemberIdsFromDb(relationGroups.get(ModificationType.UPDATE)); // Also deletes dbMappings if required Members are not available.
				relationUtil.removeUnmappedRelations(relationGroups.get(ModificationType.UPDATE));
				exec(relationHandling.createUpdateStatements(relationGroups.get(ModificationType.UPDATE)).values());
				relationUtil.addRelationIdsFromDb(relationGroups.get(ModificationType.UPDATE));
				exec(deleteEntites.deleteJoinEntries(relationGroups.get(ModificationType.UPDATE)).values());
				exec(relationHandling.createJoinStatements(relationGroups.get(ModificationType.UPDATE)).values());
				
				// DELETE Relations
				exec(deleteEntites.deleteByDbMappings(relationGroups.get(ModificationType.INSERT)).values());
				
				break;
				
			case delete:
				deleteRelations(relations);
				break;
			}
			
		} while(relations.size() > 0);		
	}
	
	/**
	 * Deletes the given Nodes from all Node-Tables.
	 */
	public void deleteNodes(List<Node> nodes) {
		exec(deleteEntites.deleteNodesFromAllTables(nodes).values());
	}
	
	/**
	 * Deletes the given Ways from all Way-Tables
	 */
	public void deleteWays(List<Way> ways) {
		exec(deleteEntites.deleteWaysFromAllTables(ways).values());
	}
	
	/**
	 * Deletes the given Areas from all Area-Tables
	 */
	public void deleteAreas(List<Area> areas) {
		exec(deleteEntites.deleteAreasFromAllTables(areas).values());
	}
	
	/**
	 * Deletes the given Areas and Relations from all Area- and Relation-Tables
	 * (Areas must also be deleted since only the OsmId is available in the delete-Tag 
	 * in the xml file and no Area can be created from it)
	 */
	public void deleteRelations(List<Relation> relations) {
		exec(deleteEntites.deleteRelationsFromAllTables(relations).values());
	}
	
	public Database getDBStructure() {
		return dbStructure;
	}
	
	private void updateDBStructure() {
		
		dbStructure.clear();
		
		writeTablesToDbStructure();
		
		writeViewsToDbStructure();
	}

	private void writeTablesToDbStructure() {
		try {
			DatabaseMetaData dbMetaData = connection.getMetaData();
			String[] types = { "TABLE" };
			ResultSet rsTablenames = dbMetaData.getTables(databaseName, null, null, types);
			while (rsTablenames.next()) {

				Table newTable = new Table(
						rsTablenames.getString("TABLE_NAME"), false, "");
				Statement stmt = connection.createStatement();
				ResultSet rsTable = stmt.executeQuery("SELECT * FROM "
						+ rsTablenames.getString("TABLE_NAME")+" LIMIT 0");
				ResultSetMetaData metaData = rsTable.getMetaData();
				
				for (int i = 0; i < metaData.getColumnCount(); i++) {
					newTable.addColumn(new Column(
							metaData.getColumnName(i + 1), 
							metaData.getColumnTypeName(i + 1), 
							metaData.getColumnClassName(i + 1),
							false,
							metaData.isAutoIncrement(i + 1),
							metaData.isNullable(i + 1)));
				}
				dbStructure.addTable(newTable);
			}
		} catch (SQLException e) {
			logger.error(e);
		}
	}
	
	private void writeViewsToDbStructure() {
		try {
			DatabaseMetaData dbMetaData = connection.getMetaData();
			String[] types = { "VIEW" };
			ResultSet rsViewnames = dbMetaData.getTables(databaseName, null, null, types);
			while (rsViewnames.next()) {

				View newView = new View(
						rsViewnames.getString("TABLE_NAME"), false, "");
				Statement stmt = connection.createStatement();
				ResultSet rsTable = stmt.executeQuery("SELECT * FROM "
						+ rsViewnames.getString("TABLE_NAME")+" LIMIT 0");
				ResultSetMetaData metaData = rsTable.getMetaData();
				
				for (int i = 0; i < metaData.getColumnCount(); i++) {
					newView.addColumn(new Column(
							metaData.getColumnName(i + 1), 
							metaData.getColumnTypeName(i + 1), 
							metaData.getColumnClassName(i + 1),
							false,
							metaData.isAutoIncrement(i+ 1),
							metaData.isNullable(i + 1)));
				}
				dbStructure.addView(newView);
			}
		} catch (SQLException e) {
			logger.error(e);
		}
	}
	
	/**
	 * Deletes all Tables & Views which are defined in the MappingConfig-File.
	 */
	public boolean dropTables() {
		Set<Table> tableList = dbStructure.getTables();
		for (Table t : tableList){
			if ((!t.getTablename().toLowerCase().equals(DBConstants.POSTGIS_GEOMETRY_COLUMNS)) && 
				(!t.getTablename().toLowerCase().equals(DBConstants.POSTGIS_SPATIAL_REF_SYS))) {
				exec("DROP TABLE IF EXISTS " + t.getTablename()+" CASCADE");
			}
		}
		
		Set<View> viewList = dbStructure.getViews();
		for (View v : viewList){
			exec("DROP VIEW IF EXISTS "+ v.getViewname());
		}
		exec("DELETE FROM geometry_columns");
		
		updateDBStructure();
		
		return true;
	}
}

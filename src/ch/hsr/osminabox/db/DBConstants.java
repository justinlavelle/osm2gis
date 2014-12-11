package ch.hsr.osminabox.db;
/**
 * This Constants are used for SQL Syntax. Do not change unless you also
 * change the Database schema
 * @author m2huber
 *
 */
public class DBConstants {
	
	// PostGIS Tables
	public static final String POSTGIS_GEOMETRY_COLUMNS = "geometry_columns";
	public static final String POSTGIS_SPATIAL_REF_SYS = "spatial_ref_sys";
	
	//tempTables
	public static final String NODE_TEMP = "node_temp";
	public static final String WAY_TEMP = "way_temp";
	public static final String RELATION_TEMP = "relation_temp";
	public static final String RELATION_MEMBER_TEMP = "relation_member_temp";
	
	//Attributes	
	public static final String ATTR_ID = "id";
	public static final String ATTR_OSM_ID = "osm_id";
	public static final String ATTR_LASTCHANGE = "lastchange";
	public static final String ATTR_GEOM = "geom";
	public static final String ATTR_KEYVALUE = "keyvalue";
	
	// Required Attributes
	public static final String[] ATTR_REQUIRED = {ATTR_ID, ATTR_OSM_ID, ATTR_GEOM};
	
	// Temp Table Attributes
	public static final String ATTR_NODE_TEMP_LAT = "lat";
	public static final String ATTR_NODE_TEMP_LON = "lon";
	
	public static final String ATTR_WAY_TEMP_NODES = "nodes";
	public static final String ATTR_WAY_TEMP_USEDBYRELATIONS = "usedbyrelations";
	public static final String ATTR_WAY_TEMP_DIFFTYPE = "difftype";
	
	public static final String ATTR_RELATION_TEMP_MEMBER = "relationmember";
	public static final String ATTR_TABLENAME = "tablename";
	public static final String ATTR_RELATION_TEMP_DIFFTYPE = "difftype";
	
	public static final String ATTR_RELATION_MEMBER_TEMP_RELATION_OSM_ID = "relation_osm_id";
	public static final String ATTR_RELATION_MEMBER_TEMP_MEMBER_OSM_ID = "member_osm_id";
	public static final String ATTR_RELATION_MEMBER_TEMP_TYPE = "type";
	public static final String ATTR_RELATION_MEMBER_TEMP_ROLE = "role";
	
	// Geom Types
	public static final String GEOMTYPE_POINT = "POINT";
	public static final String GEOMTYPE_LINESTRING = "LINESTRING";
	public static final String GEOMTYPE_MULTIPOLYGON = "MULTIPOLYGON";
	
	//Other constants	
	public final static String SQL_WAY_TEMP_NODE_SPACER = ";";
	public final static String SQL_RELATION_TEMP_MEMBER_SPACER = ";";
	public final static String SQL_RELATION_TEMP_MEMBER_VALUE_SPACER = ",";
	
	public final static String SQL_POINT_GEOM_START = "GeomFromText('POINT(";
	public final static String SQL_LINESTRING_GEOM_START = "GeomFromText('LINESTRING(";
	public final static String SQL_MULTIPOLYGON_GEOM_START = "GeomFromText('MULTIPOLYGON";
	public final static String SQL_GEOM_END = ")',4326)";
	
	public final static String SQL_SEARCH_NODE_TEMP_WITH_ID = "SELECT * FROM "+NODE_TEMP+" WHERE "+ATTR_OSM_ID+" = ";
	public final static String SQL_SEARCH_WAY_TEMP_WITH_ID  = "SELECT * FROM " + WAY_TEMP + " WHERE "+ATTR_OSM_ID+" = ";
		

}
package ch.hsr.osminabox.db.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.db.DBConstants;
import ch.hsr.osminabox.db.boundingbox.BoundingBoxStrategy;
import ch.hsr.osminabox.db.differentialupdate.ModificationType;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.schemamapping.ConfigService;
import ch.hsr.osminabox.schemamapping.xml.Column;
import ch.hsr.osminabox.schemamapping.xml.MappingType;

/**
 * Util for WayHandling
 * @author jzimmerm
 *
 */
public class WayUtil {
	
	private static Logger logger = Logger.getLogger(WayUtil.class);
	
	private final String TABLENAME = "[tablename]";
	private final String UNION = " UNION ";

	private final String selectSql = "SELECT " + DBConstants.ATTR_OSM_ID 
			+ ", '" + TABLENAME
			+ "' AS " + DBConstants.ATTR_TABLENAME + " FROM " + TABLENAME + " WHERE ";
	
	private ConfigService config;
	private Connection connection;
	
	public WayUtil(ConfigService config, Connection connection) {
		this.config = config;
		this.connection = connection;
	}
	
	/**
	 * Deletes every Way from the list which has no Node inside the BoundingBox.
	 * 
	 * @param ways
	 * @param bbox The BoundingBox Strategy used to validate the Nodes.
	 */
	public void deleteWaysOutsideBBox(List<Way> ways, BoundingBoxStrategy bbox) {
		
		int sizeBefore = ways.size();	
		
		for (Iterator<Way> iter = ways.iterator(); iter.hasNext();) {
			if(!bbox.visit(iter.next()))
				iter.remove();	
		}
		
		if((sizeBefore - ways.size()) > 0)
			logger.info("Deleted " + (sizeBefore - ways.size()) + " Ways which lie outside the BoundingBox.");
	}
	
	/**
	 * Executes SQL Scripts
	 * @param sql
	 * @return
	 */
	private ResultSet exec(String sql) {
		Statement st;
		try {
			st = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);

			st.execute(sql);
			return st.getResultSet();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("SQL File could not been executed");
			logger.error(sql);
			return null;
		}
	}

	public Map<ModificationType, List<Way>> splitToModificationGroups(List<Way> ways) {
		List<Way> insertWays = new LinkedList<Way>();
		List<Way> updateWays = new LinkedList<Way>();
		List<Way> deleteWays = new LinkedList<Way>();
		
		Map<ModificationType, List<Way>> wayGroups = new HashMap<ModificationType, List<Way>>();
		wayGroups.put(ModificationType.INSERT, insertWays);
		wayGroups.put(ModificationType.UPDATE, updateWays);
		wayGroups.put(ModificationType.DELETE, deleteWays);
		
		for(Way way : ways){
			
			StringBuffer sql = new StringBuffer();
			Set<String> insertTables = new HashSet<String>();
			Set<String> updateTables = new HashSet<String>();
			Set<String> deleteTables = new HashSet<String>();

			int i = 0;
			if(way.dbMappings.size() > 0){
				for(String table : way.dbMappings.keySet()){
					sql.append(selectSql.replaceAll(Pattern.quote(TABLENAME), table));
					sql.append(DBConstants.ATTR_OSM_ID);
					sql.append("=");
					sql.append(way.getOsmId());
	
					i++;
					if (i < way.dbMappings.size())
						sql.append(UNION);
				}
	
				ResultSet res = exec(sql.toString());
				
				try {
					while (res.next()) {
						// Gather Tables for UPDATE
						updateTables.add(res.getString(DBConstants.ATTR_TABLENAME));					
					}
				} catch (SQLException e) {
					logger.error("SQL Exception while splitting Way list to modification group lists.");
					e.printStackTrace();
				}
			}
			
			// Gather Tables for INSERT
			insertTables.addAll(way.dbMappings.keySet());
			insertTables.removeAll(updateTables);
			
			Set<String> unmappedTables = config.getTablesOfGeomType(MappingType.LINESTRING);
			unmappedTables.removeAll(way.dbMappings.keySet());
			
			sql = new StringBuffer();

			i = 0;
			if(unmappedTables.size() > 0){
				for(String table : unmappedTables){
					sql.append(selectSql.replaceAll(Pattern.quote(TABLENAME), table));
					sql.append(DBConstants.ATTR_OSM_ID);
					sql.append("=");
					sql.append(way.getOsmId());
	
					i++;
					if (i < unmappedTables.size())
						sql.append(UNION);
				}
	
				ResultSet res = exec(sql.toString());
				
				try {
					while (res.next()) {
						// Gather Tables for DELETE
						deleteTables.add(res.getString(DBConstants.ATTR_TABLENAME));					
					}
				} catch (SQLException e) {
					logger.error("SQL Exception while looking up Tables.");
					logger.error(sql.toString());
					e.printStackTrace();
				}
			}
			
			// Way with dbMappings for DELETION
			Way deleteWay = new Way(way);
			deleteWay.dbMappings.clear();
			for(String deleteTable : deleteTables)
				deleteWay.dbMappings.put(deleteTable, new LinkedList<Column>());
				
			// Node with dbMappings for INSERTION	
			Way insertWay = new Way(way);
			insertWay.dbMappings.clear();
			
			// Node with dbMappings for UPDATEING
			Way updateWay = new Way(way);
			updateWay.dbMappings.clear();
			
			for(String table : way.dbMappings.keySet()){
				if(insertTables.contains(table))
					insertWay.dbMappings.put(table, way.dbMappings.get(table));
				
				else if(updateTables.contains(table))
					updateWay.dbMappings.put(table, way.dbMappings.get(table));
			}
			
			if(insertWay.dbMappings.size() > 0) insertWays.add(insertWay);
			if(updateWay.dbMappings.size() > 0) updateWays.add(updateWay);
			if(deleteWay.dbMappings.size() > 0) deleteWays.add(deleteWay);
		}
		
		return wayGroups;
	}
}

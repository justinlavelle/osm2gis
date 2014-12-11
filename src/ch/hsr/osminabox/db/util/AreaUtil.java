package ch.hsr.osminabox.db.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.db.DBConstants;
import ch.hsr.osminabox.db.boundingbox.BoundingBoxStrategy;
import ch.hsr.osminabox.db.differentialupdate.ModificationType;
import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Area.WayRole;
import ch.hsr.osminabox.db.sql.Constants;
import ch.hsr.osminabox.schemamapping.ConfigService;
import ch.hsr.osminabox.schemamapping.xml.Column;
import ch.hsr.osminabox.schemamapping.xml.MappingType;

/**
 * Util Class for Area Handling
 * 
 * @author jzimmerm
 * 
 */
public class AreaUtil {

	private final int READING_SIZE = 2000;

	private static Logger logger = Logger.getLogger(AreaUtil.class);
	
	private ConfigService config;
	private Connection connection;

	private final String markWaysUsedByRelations = "UPDATE " + DBConstants.WAY_TEMP
			+ " SET " + DBConstants.ATTR_WAY_TEMP_USEDBYRELATIONS
			+ "= true WHERE " + DBConstants.ATTR_OSM_ID + " IN (";

	private final String selectWays = "SELECT " + DBConstants.ATTR_OSM_ID
			+ ", " + DBConstants.ATTR_LASTCHANGE + ", "
			+ DBConstants.ATTR_WAY_TEMP_NODES + ", "
			+ DBConstants.ATTR_KEYVALUE + " FROM " + DBConstants.WAY_TEMP
			+ " WHERE " + DBConstants.ATTR_WAY_TEMP_USEDBYRELATIONS + " = false";
	
	private final String TABLENAME = "[tablename]";
	private final String UNION = " UNION ";

	private final String selectSql = "SELECT " + DBConstants.ATTR_OSM_ID 
			+ ", '" + TABLENAME
			+ "' AS " + DBConstants.ATTR_TABLENAME + " FROM " + TABLENAME + " WHERE ";

	public AreaUtil(ConfigService config, Connection connection) {
		this.config = config;
		this.connection = connection;
	}

	/**
	 * Executes SQL Scripts
	 * 
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
			logger.error("SQL File could not been executed: " + e);
			logger.error(sql);
			return null;
		}
	}

	/**
	 * A single way is an area if it has at least 4 nodes and the first and last
	 * are the same id.
	 * 
	 * @param ids
	 * @return
	 */
	public boolean checkIfWayIsClosed(String[] ids) {
		if (ids.length < 4)
			return false;

		if (ids[0].equals(ids[ids.length - 1]))
			return true;
		else
			return false;
	}

	/**
	 * Tag all used Ways (which are not an INNER Member) by this Area in the way_temp Table as usedByRelations.
	 * 
	 * @param area
	 */
	public void markWaysUsedByRelations(List<Area> areas) {
		
		for(Area area : areas){
			
			List<Long> wayIds = new LinkedList<Long>();
		
			// Don't mark Ways which are an INNER Member since can still be an Area of its own.
			for(Long wayId : area.wayIds.keySet()){
				if(!area.wayIds.get(wayId).equals(WayRole.inner))
					wayIds.add(wayId);
			}
			
			markWaysUsedByRelation(wayIds);
		}
	}
	
	private void markWaysUsedByRelation(Collection<Long> wayIds){
		if(wayIds.size() <= 0)
			return;
		
		StringBuffer sql = new StringBuffer(markWaysUsedByRelations);

		int i = 0;
		for (Long id : wayIds) {
			sql.append(id);
			i++;

			if (i < wayIds.size()) {
				sql.append(Constants.COMMA);
			}
		}
		sql.append(Constants.CLOSE_BRACKET);

		exec(sql.toString());
	}

	/**
	 * Reads from the way_temp Table and fills the Area List from single-closed Ways. 
	 * 
	 * @return DiffType Always "create" on initial import, during a diff update this Type is either "create", "modify" or "delete"
	 */
	public DiffType getSingleClosedWays(List<Area> areas){
		DiffType diffType = getSingleClosedWays(areas, DiffType.create);
		if(areas.size() <= 0)
			diffType = getSingleClosedWays(areas, DiffType.modify);
		if(areas.size() <= 0)
			diffType = getSingleClosedWays(areas, DiffType.delete);
		
		return diffType;
	}
	
	private DiffType getSingleClosedWays(List<Area> areas, DiffType diffType) {
		ResultSet res;
		Vector<Long> usedWays = new Vector<Long>(READING_SIZE);
		
		try {
			res = exec(selectWays + " AND " + DBConstants.ATTR_WAY_TEMP_DIFFTYPE + "='" + diffType + "' LIMIT " + READING_SIZE);

			if (res.first() == false)
				return diffType;

			res.beforeFirst();

			int areaCounter = 0;
			while (res.next()) {

				try {
					String[] nodeIds = res.getString(
							DBConstants.ATTR_WAY_TEMP_NODES).split(
							DBConstants.SQL_WAY_TEMP_NODE_SPACER);

					// If the way is not closed, its no area and we ignore it
					if (!checkIfWayIsClosed(nodeIds)) {
						usedWays.add(res.getLong(DBConstants.ATTR_OSM_ID));
						continue;
					}
					
					Area area = new Area(res.getInt(DBConstants.ATTR_OSM_ID));

					usedWays.add(res.getLong(DBConstants.ATTR_OSM_ID));
					areas.add(area);
					areaCounter++;
				} catch (Exception e) {
					logger.error(e);
					continue;
				}
			}
			
			logger.info("Processed " + usedWays.size() + " Ways from " + DBConstants.WAY_TEMP + " Table. Created " + areaCounter+ " Areas from single-closed Ways (DiffType: " + diffType + ").");
			
			markWaysUsedByRelation(usedWays);
			
		} catch (SQLException e) {
			logger.error(e);
		}
		
		return diffType;
	}

	public void deleteAreasOutsideBBox(List<Area> areas, BoundingBoxStrategy bbox) {
		int sizeBefore = areas.size();	
		
		for (Iterator<Area> iter = areas.iterator(); iter.hasNext();) {
			if(!bbox.visit(iter.next()))
				iter.remove();	
		}
		
		if((sizeBefore - areas.size()) > 0)
			logger.info("Deleted " + (sizeBefore - areas.size()) + " Areas which lie outside the BoundingBox.");
		
	}

	public Map<ModificationType, List<Area>> splitToModificationGroups(List<Area> areas) {
		List<Area> insertAreas = new LinkedList<Area>();
		List<Area> updateAreas = new LinkedList<Area>();
		List<Area> deleteAreas = new LinkedList<Area>();
		
		Map<ModificationType, List<Area>> areaGroups = new HashMap<ModificationType, List<Area>>();
		areaGroups.put(ModificationType.INSERT, insertAreas);
		areaGroups.put(ModificationType.UPDATE, updateAreas);
		areaGroups.put(ModificationType.DELETE, deleteAreas);
		
		for(Area area : areas){
			
			StringBuffer sql = new StringBuffer();
			Set<String> insertTables = new HashSet<String>();
			Set<String> updateTables = new HashSet<String>();
			Set<String> deleteTables = new HashSet<String>();

			int i = 0;
			if(area.dbMappings.size() > 0){
				for(String table : area.dbMappings.keySet()){
					sql.append(selectSql.replaceAll(Pattern.quote(TABLENAME), table));
					sql.append(DBConstants.ATTR_OSM_ID);
					sql.append("=");
					sql.append(area.getOsmId());
	
					i++;
					if (i < area.dbMappings.size())
						sql.append(UNION);
				}
	
				ResultSet res = exec(sql.toString());
				
				try {
					while (res.next()) {
						// Gather Tables for UPDATE
						updateTables.add(res.getString(DBConstants.ATTR_TABLENAME));					
					}
				} catch (SQLException e) {
					logger.error("SQL Exception while splitting Area list to modification group lists.");
					e.printStackTrace();
				}
			}
			
			// Gather Tables for INSERT
			insertTables.addAll(area.dbMappings.keySet());
			insertTables.removeAll(updateTables);
			
			Set<String> unmappedTables = config.getTablesOfGeomType(MappingType.MULTIPOLYGON);
			unmappedTables.removeAll(area.dbMappings.keySet());
			
			sql = new StringBuffer();

			i = 0;
			if(unmappedTables.size() > 0){
				for(String table : unmappedTables){
					sql.append(selectSql.replaceAll(Pattern.quote(TABLENAME), table));
					sql.append(DBConstants.ATTR_OSM_ID);
					sql.append("=");
					sql.append(area.getOsmId());
	
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
			
			// Area with dbMappings for DELETION
			Area deleteArea = new Area(area);
			deleteArea.dbMappings.clear();
			for(String deleteTable : deleteTables)
				deleteArea.dbMappings.put(deleteTable, new LinkedList<Column>());
				
			// Node with dbMappings for INSERTION	
			Area insertArea = new Area(area);
			insertArea.dbMappings.clear();
			
			// Node with dbMappings for UPDATEING
			Area updateArea = new Area(area);
			updateArea.dbMappings.clear();
			
			for(String table : area.dbMappings.keySet()){
				if(insertTables.contains(table))
					insertArea.dbMappings.put(table, area.dbMappings.get(table));
				
				else if(updateTables.contains(table))
					updateArea.dbMappings.put(table, area.dbMappings.get(table));
			}
			
			if(insertArea.dbMappings.size() > 0) insertAreas.add(insertArea);
			if(updateArea.dbMappings.size() > 0) updateAreas.add(updateArea);
			if(deleteArea.dbMappings.size() > 0) deleteAreas.add(deleteArea);
		}
		
		return areaGroups;
	}
}

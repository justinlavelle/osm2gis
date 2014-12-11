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
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.schemamapping.ConfigService;
import ch.hsr.osminabox.schemamapping.xml.MappingType;
import ch.hsr.osminabox.schemamapping.xml.Column;

/**
 * Helper Class for handling Nodes.
 * 
 * @author Joram
 *
 */
public class NodeUtil {
	
	private final String TABLENAME = "[tablename]";
	private final String UNION = " UNION ";

	private final String selectSql = "SELECT " + DBConstants.ATTR_OSM_ID 
			+ ", '" + TABLENAME
			+ "' AS " + DBConstants.ATTR_TABLENAME + " FROM " + TABLENAME + " WHERE ";
	
	private static Logger logger = Logger.getLogger(NodeUtil.class);
	private Connection connection;
	private ConfigService config;
	
	public NodeUtil(ConfigService config, Connection connection){
		this.config = config;
		this.connection = connection;
	}
	
	/**
	 * Gets the Lat / Lon Values for each Node in this Way from the Database and adds it to it.
	 * @param way
	 * @throws SQLException
	 */
	public void addLatLonToNodes(Way way) throws SQLException{
			
		String sql = DBConstants.SQL_SEARCH_NODE_TEMP_WITH_ID;
		int i = 0;
		
		for (Node node : way.nodes) {
			sql += node.getOsmId();			
			i++;
			if(i < way.nodes.size()) {
				sql += " or "+DBConstants.ATTR_OSM_ID + " = ";
			}
		}
		ResultSet res = exec(sql);
		
		try{
			while(res.next()) {
				long osm_id = res.getLong(DBConstants.ATTR_OSM_ID);
				String lon = String.valueOf(res.getDouble(DBConstants.ATTR_NODE_TEMP_LON));
				String lat = String.valueOf(res.getDouble(DBConstants.ATTR_NODE_TEMP_LAT));
				for(Node node : way.nodes){
					if(node.getOsmId() == osm_id){
						node.attributes.put(Node.NODE_LONGITUDE, lon);
						node.attributes.put(Node.NODE_LATITUDE, lat);
					}
				}
				
			}
		}
		catch(SQLException e) {
			throw e;
		}
	}
	
	/**
	 * Executes SQL Scripts
	 * @param sql
	 * @return
	 */
	protected ResultSet exec(String sql) {
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

	/**
	 * Creates Nodes from the given List of Node Ids.
	 * @param way The Way which all Nodes should be added to.
	 * @param nodeIds The List of Node Ids.
	 */
	public void createNodesFromIds(Way way, String[] nodeIds) {
		for(String nodeId : nodeIds){
			Node node = new Node();
			node.setOsmId(Long.parseLong(nodeId));
			
			way.nodes.add(node);
		}
	}

	/**
	 * Deletes every Node from the list that is outside the BoundingBox.
	 * 
	 * @param nodes
	 * @param bbox The BoundingBox Strategy used to validate the Node.
	 */
	public void deleteNodesOutsideBBox(List<Node> nodes, BoundingBoxStrategy bbox) {
		
		int sizeBefore = nodes.size();
		
		for (Iterator<Node> iter = nodes.iterator(); iter.hasNext();) {
			if(!bbox.visit(iter.next()))
				iter.remove();
		}
		
		if((sizeBefore - nodes.size()) > 0)
			logger.info("Deleted " + (sizeBefore - nodes.size()) + " Nodes which lie outside the BoundingBox.");
	}

	/**
	 * Splits the Nodes in the given list into three groups according to the needed modifications.
	 * Can duplicate Nodes if for example a Node needs to be deleted in one Table and created in another.
	 * 
	 * @param nodes
	 * @return
	 */
	public Map<ModificationType, List<Node>> splitToModificationGroups(List<Node> nodes) {
		List<Node> insertNodes = new LinkedList<Node>();
		List<Node> updateNodes = new LinkedList<Node>();
		List<Node> deleteNodes = new LinkedList<Node>();
		
		Map<ModificationType, List<Node>> nodeGroups = new HashMap<ModificationType, List<Node>>();
		nodeGroups.put(ModificationType.INSERT, insertNodes);
		nodeGroups.put(ModificationType.UPDATE, updateNodes);
		nodeGroups.put(ModificationType.DELETE, deleteNodes);
		
		for(Node node : nodes){
			
			StringBuffer sql = new StringBuffer();
			Set<String> insertTables = new HashSet<String>();
			Set<String> updateTables = new HashSet<String>();
			Set<String> deleteTables = new HashSet<String>();

			int i = 0;
			if(node.dbMappings.size() > 0){
				for(String table : node.dbMappings.keySet()){
					sql.append(selectSql.replaceAll(Pattern.quote(TABLENAME), table));
					sql.append(DBConstants.ATTR_OSM_ID);
					sql.append("=");
					sql.append(node.getOsmId());
	
					i++;
					if (i < node.dbMappings.size())
						sql.append(UNION);
				}
	
				ResultSet res = exec(sql.toString());
				
				try {
					while (res.next()) {
						// Gather Tables for UPDATE
						updateTables.add(res.getString(DBConstants.ATTR_TABLENAME));					
					}
				} catch (SQLException e) {
					logger.error("SQL Exception while splitting Node list to modification group lists.");
					e.printStackTrace();
				}
			}
			
			// Gather Tables for INSERT
			insertTables.addAll(node.dbMappings.keySet());
			insertTables.removeAll(updateTables);
			
			Set<String> unmappedTables = config.getTablesOfGeomType(MappingType.POINT);
			unmappedTables.removeAll(node.dbMappings.keySet());
			
			sql = new StringBuffer();

			i = 0;
			if(unmappedTables.size() > 0){
				for(String table : unmappedTables){
					sql.append(selectSql.replaceAll(Pattern.quote(TABLENAME), table));
					sql.append(DBConstants.ATTR_OSM_ID);
					sql.append("=");
					sql.append(node.getOsmId());
	
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
					logger.error("SQL Exception while looking up Tables: ");
					logger.error(sql.toString());
					e.printStackTrace();
				}
			}
			
			// Node with dbMappings for DELETION
			Node deleteNode = new Node(node);
			deleteNode.dbMappings.clear();
			for(String deleteTable : deleteTables)
				deleteNode.dbMappings.put(deleteTable, new LinkedList<Column>());
				
			// Node with dbMappings for INSERTION	
			Node insertNode = new Node(node);
			insertNode.dbMappings.clear();
			
			// Node with dbMappings for UPDATEING
			Node updateNode = new Node(node);
			updateNode.dbMappings.clear();
			
			for(String table : node.dbMappings.keySet()){
				if(insertTables.contains(table))
					insertNode.dbMappings.put(table, node.dbMappings.get(table));
				
				else if(updateTables.contains(table))
					updateNode.dbMappings.put(table, node.dbMappings.get(table));
			}
			
			if(insertNode.dbMappings.size() > 0) insertNodes.add(insertNode);
			if(updateNode.dbMappings.size() > 0) updateNodes.add(updateNode);
			if(deleteNode.dbMappings.size() > 0) deleteNodes.add(deleteNode);
		}
		
		return nodeGroups;
	}
}

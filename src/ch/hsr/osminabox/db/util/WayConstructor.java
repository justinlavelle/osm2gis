package ch.hsr.osminabox.db.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.db.DBConstants;
import ch.hsr.osminabox.db.downloading.EntityConsistencyService;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.OSMEntity;
import ch.hsr.osminabox.db.entities.Way;

/**
 * Helper Class for gathering Way Data
 * 
 * @author jzimmerm
 *
 */
public class WayConstructor {
	
	private static Logger logger = Logger.getLogger(WayConstructor.class);
	
	private Connection connection;
	
	public WayConstructor(Connection connection){
		this.connection = connection;
	}
	
	/**
	 * Adds Node data to all Ways from the node_temp Table. Deletes Ways which have missing Nodes.
	 * @param ways
	 */
	public void addNodeData(List<Way> ways){
		addNodeData(ways, null);
	}
	
	public void addNodeData(List<Way> ways, EntityConsistencyService consistency){
		
		int sizeBefore = ways.size();
		
		for(Iterator<Way> iter = ways.iterator(); iter.hasNext();){
			Way way = iter.next();
			if(!addNodeData(way, consistency)){
				logger.debug("Way with OSM Id: " + way.getOsmId() + " didn't pass integrity check and is deleted.");
				iter.remove();
			}
		}
		if((sizeBefore - ways.size()) > 0)
			logger.info("Removed " + (sizeBefore - ways.size()) + " out of " + sizeBefore + " Ways which have no valid data.");
	}
	
	/**
	 * 
	 * @param way
	 * @return  true if all lat / lon values from every Node could be retrieved from the noed_temp table.
	 */
	protected boolean addNodeData(Way way, EntityConsistencyService consistency){
		
		if(way.nodes.size() < 2)
			return false;
		
		String sqlSearch = getSQLForNodeSearch(getIDsAsArray(way.nodes));		
		ResultSet res = exec(sqlSearch);
		
		Map<Long, Node> tmpNodes = new HashMap<Long, Node>();
		List<Node> missingNodes = new ArrayList<Node>();
		
		int changedNodes = 0;		
		try {
			while(res.next()) {
				Node tmpNode = new Node();
				tmpNode.setOsmId(res.getLong(DBConstants.ATTR_OSM_ID));
				tmpNode.attributes.put(Node.NODE_LATITUDE, String.valueOf(res.getDouble(DBConstants.ATTR_NODE_TEMP_LAT)));
				tmpNode.attributes.put(Node.NODE_LONGITUDE, String.valueOf(res.getDouble(DBConstants.ATTR_NODE_TEMP_LON)));
				tmpNodes.put(tmpNode.getOsmId(), tmpNode);
			}				
			
			changedNodes += copyNodes(way, tmpNodes, missingNodes);
			
			if(missingNodes.size() > 0 && consistency != null){
				
				consistency.addMissingNodes(missingNodes);
				List<Node> retrievedNodes = consistency.fetchMissingNodes();
				
				changedNodes += copyNodes(way, listToMap(retrievedNodes), missingNodes);
			}
			
			if(changedNodes >= way.nodes.size())
				return true;
			
			return false;
		} 
		catch(SQLException e) {
			logger.error("Failed adding Node data to Way with Osm Id: " + way.getOsmId());
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Puts all Nodes in the list into a HashMap with the Osm Id as Key.
	 * 
	 * @param nodes
	 * @return
	 */
	protected Map<Long, Node> listToMap(List<Node> nodes){
		Map<Long, Node> result = new HashMap<Long, Node>();
		
		for(Node node : nodes){
			result.put(node.getOsmId(), node);
		}
		
		return result;
	}

	/**
	 * Copies the tmpNodes to the one from the way and returns the amount of Nodes copied. 
	 * @param way
	 * @param tmpNodes
	 * @param missingNodes
	 * @return
	 */
	protected int copyNodes(Way way, Map<Long, Node> tmpNodes,	List<Node> missingNodes) {
		
		int changedNodes = 0;
		for(Node node : way.nodes){
			Node tmpNode = tmpNodes.get(node.getOsmId());
			
			if(tmpNode == null)
				missingNodes.add(node);
			else{
				node.attributes = tmpNode.attributes; // Copy attributes since they contain the only relevant data
				changedNodes++;
			}
		}
		return changedNodes;
	}
	
	protected String getSQLForNodeSearch(String[] ids) {
		StringBuffer sql = new StringBuffer(DBConstants.SQL_SEARCH_NODE_TEMP_WITH_ID);
		int i = 0;
		for (String id : ids) {
			sql.append(id);			
			i++;
			if(i < ids.length) {
				sql.append(" or ");
				sql.append(DBConstants.ATTR_OSM_ID);
				sql.append(" = ");
			}
		}
		return sql.toString();
	}	
	
	/**
	 * Splits into an Array of ids
	 * @param ways
	 * @return
	 */
	protected String[] getIDsAsArray(List<Node> node) {
		StringBuffer buffer = new StringBuffer();
		for(OSMEntity entity : node){
			buffer.append(entity.getOsmId());
			buffer.append(DBConstants.SQL_WAY_TEMP_NODE_SPACER);
		}
		
		return buffer.toString().split(DBConstants.SQL_WAY_TEMP_NODE_SPACER);
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

}

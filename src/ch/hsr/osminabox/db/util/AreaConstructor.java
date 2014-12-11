package ch.hsr.osminabox.db.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.db.DBConstants;
import ch.hsr.osminabox.db.downloading.EntityConsistencyService;
import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.entities.Area.WayRole;
import ch.hsr.osminabox.db.sql.util.HStoreUtil;
import ch.hsr.osminabox.db.util.exceptions.RingAssignementFailed;
import ch.hsr.osminabox.schemamapping.ConfigService;
import ch.hsr.osminabox.schemamapping.xml.MappingType;

/**
 * Class used for further Data gathering from referenced Ways and Nodes by Areas.
 * @author Joram
 */
public class AreaConstructor {
	
	private static Logger logger = Logger.getLogger(AreaConstructor.class);

	private HStoreUtil hstoreUtil;
	private NodeUtil nodeUtil;
	private Connection connection;
	private ConfigService config;
	private ApplicationContext context;
	
	public AreaConstructor(Connection connection, ApplicationContext context){
		this.context = context;
		this.connection = connection;
		this.config = context.getConfigService();
		this.hstoreUtil = new HStoreUtil(connection);
		this.nodeUtil = new NodeUtil(config, connection);
	}
	
	/**
	 * Gets the Way- and their Node-Values (Lat / Lon) for each Area in the List and adds them to the Area.
	 * If the Area has no own Tags yet, Tags are inherited from Ways with Role=outer or, if none are available, Role=none.
	 * @param areas
	 */
	public void addWaysAndTags(List<Area> areas){
		addWaysAndTags(areas, null);
	}
	
	/**
	 * Gets the Way- and their Node-Values (Lat / Lon) for each Area in the List and adds them to the Area.
	 * If the Area has no own Tags yet, Tags are inherited from Ways with Role=outer or, if none are available, Role=none.
	 * @param areas
	 */
	public void addWaysAndTags(List<Area> areas, EntityConsistencyService consistency){
		
		if(consistency == null)
			logger.debug("Adding Way data from " + DBConstants.WAY_TEMP + " Table to " + areas.size() + " Areas.");
		else
			logger.debug("Adding Way data from " + DBConstants.WAY_TEMP + " Table or via OSM API to " + areas.size() + " Areas.");
		
		for(Iterator<Area> iter = areas.iterator(); iter.hasNext();){
			
			Area area = iter.next();
			
			WayRole tagsNeededFromWayRole = null;
			
			// Inherit all original Tags
			area.tags = area.originalTags;
			
			// Area needs additional Tags from Ways?
			if(!hasOwnAreaTags(area.originalTags)){							
				if(hasWaysWithRole(area, WayRole.outer))
					tagsNeededFromWayRole = WayRole.outer;
				else if(hasWaysWithRole(area, WayRole.none))
					tagsNeededFromWayRole = WayRole.none;
				else
					logger.info("Area with OSM Id " + area.getOsmId() + " has no Tags to be inherited from.");
			}
			
			String sql = DBConstants.SQL_SEARCH_WAY_TEMP_WITH_ID;
			int i = 0;
			
			for (Long wayId: area.wayIds.keySet()) {			
				sql += wayId;			
				i++;
				if(i < area.wayIds.size()) {
					sql += " or " + DBConstants.ATTR_OSM_ID + " = ";
				}
			}
			
			ResultSet res = exec(sql);
			
			Set<Long> foundWayIds = new HashSet<Long>();
			if(res != null) {
				try{
					while(res.next()) {
						Way way = new Way();
						way.setOsmId(res.getInt(DBConstants.ATTR_OSM_ID));
						
						String[] nodeIds = res.getString(DBConstants.ATTR_WAY_TEMP_NODES).split(";");
						
						nodeUtil.createNodesFromIds(way, nodeIds);					
						nodeUtil.addLatLonToNodes(way);
						
						area.ways.put(way, area.wayIds.get(way.getOsmId()));
						
						// Attributes needed from Way?
						if(area.attributes.size() <= 0 && area.wayIds.size() == 1){
							area.attributes = way.attributes;
						}
						
						// Tags needed from this Way?
						if(tagsNeededFromWayRole != null && area.wayIds.get(way.getOsmId()).equals(tagsNeededFromWayRole)){

							hstoreUtil.addTagsFromTemp(way);
							
							area.addTags(way.tags);
						}
						
						foundWayIds.add(way.getOsmId());
					}
				}
				catch (SQLException e) {
					logger.error("Failed retrieving Way data from " + DBConstants.WAY_TEMP + " Table.");
					e.printStackTrace();
				}
			}
			
			if(consistency != null){
				Set<Long> missingWayIds = new HashSet<Long>(area.wayIds.keySet());
				missingWayIds.removeAll(foundWayIds);
				
				for(Long osmId : missingWayIds){
					Way way = consistency.fetchWayFull(osmId);
					if(way != null){
						area.ways.put(way, area.wayIds.get(way.getOsmId()));
						
						// Tags needed from this Way?
						if(tagsNeededFromWayRole != null && area.wayIds.get(way.getOsmId()).equals(tagsNeededFromWayRole)){
							area.addTags(way.tags);
						}
					}
					else
						logger.error("Failed retrieve Way with Osm Id: " + osmId + " to be used in Area with OsmId: " + area.getOsmId());
				}
			}
		}
	}

	/**
	 * Assigns Rings from unclosed Ways for each Area in the List.
	 * @param areas
	 */
	public void assignRings(List<Area> areas){
		
		int initialSize = areas.size();
		
		for(Iterator<Area> iter = areas.iterator(); iter.hasNext();){
			try{
				assignRings(iter.next());
			}
			catch(RingAssignementFailed e){
				logger.info(e.getMessage());
				iter.remove();
			}
		}
		
		logger.info("Ring assignement for " + initialSize + " Areas: " + areas.size() + " successfull.");
	}
	
	/**
	 * Ways in an Area are always a complete Ring.
	 * Therefore, if multiple Ways make a Ring, they need to be merged into a single Way to be inserted in the DB.
	 * 
	 * The Rings constructed have no more relation to OSM entities. No Attributes from old Ways are copied except their nodes.
	 * The Areas wayIds List is left unchanged.
	 * 
	 * See http://wiki.openstreetmap.org/wiki/Relation:multipolygon/Algorithm
	 * 
	 * @param area
	 */
	public void assignRings(Area area) throws RingAssignementFailed{
		try{
			if(area.ways.size() <= 0)
				throw new RingAssignementFailed("No Way Data available. OSM Id: " + area.getOsmId());
			
			Map<Way, Boolean> ways = new HashMap<Way, Boolean>();
			Map<Way, WayRole> rings = new HashMap<Way, WayRole>();
			
			Way currentRing = null;
			
			// Insert all Ways and mark them as unassigned. (RA-1)
			for(Way way : area.ways.keySet())
				ways.put(way, false);
			
			// As long as there are unassigned Ways left... (RA-2)
			while((currentRing = getUnassignedWay(ways)) != null){
				
				// Mark Way as assigned. (RA-2)
				ways.put(currentRing, true);
				
				// The WayRole of the first Way will be the Rings Role as well.
				WayRole currentRole = area.ways.get(currentRing);
				
				// RA-3
				if(isWayClosed(currentRing)){
					rings.put(currentRing, currentRole);
					currentRing = null;
					continue;
				}
				
				// RA-4
				else{
	
					// As long as there are unassigned Ways left, try to assign them to a Ring.
					while(searchAndAddConnectedWay(currentRing, ways) && !isWayClosed(currentRing));	
						
					// RA-3
					if(isWayClosed(currentRing)){
						rings.put(currentRing, currentRole);
						currentRing = null;
						continue;
					}
					else{
						String unassignedWayIds = "";
						for(Entry<Way, Boolean> wayEntry : ways.entrySet()){
							if(!wayEntry.getValue())
								unassignedWayIds += wayEntry.getKey().getOsmId() + ", ";
						}
						throw new RingAssignementFailed("Ring cannot be closed on Area with OSM Id: " + area.getOsmId() + ". Unassigned Way Ids: " + unassignedWayIds);
					}
				}
			}
			area.ways = rings;
		} 
		catch (OutOfMemoryError e){
			throw new RingAssignementFailed("Area with Relation Osm Id: " + area.getOsmId() + " produced an OutOfMemoryError.");
		} 
	}
	
	/**
	 * Searches an unassigned Way if it can be connected to the currentRing.
	 * If one is found, it is added to the currentRing and marked as assigned in the ways-Map.
	 * Adding to the currentRing is done in any possible way: start-start, start-end, end-start, end-end
	 * 
	 * @param currentRing
	 * @param ways
	 * @return true if a Way could be added to the currentRing, false otherwise
	 */
	private boolean searchAndAddConnectedWay(Way currentRing, Map<Way, Boolean> ways) {
		long ringStartNodeId = currentRing.nodes.getFirst().getOsmId();
		long ringEndNodeId = currentRing.nodes.getLast().getOsmId();
		
		
		// Check if any unassigned Way can be connected to the currentRing
		for(Way way : ways.keySet()){
			if(!ways.get(way)){
				
				long wayStartNodeId = way.nodes.getFirst().getOsmId();
				long wayEndNodeId = way.nodes.getLast().getOsmId();
				
				if(ringStartNodeId == wayStartNodeId){
					LinkedList<Node> nodes = way.nodes;
					
					Collections.reverse(nodes);
										
					nodes.removeLast();
					
					nodes.addAll(currentRing.nodes);
					
					currentRing.nodes = nodes;
					
					ways.put(way, true);
					
					return true;
				}
				else if(ringStartNodeId == wayEndNodeId){
					LinkedList<Node> nodes = way.nodes;
					
					nodes.removeLast();
					
					nodes.addAll(currentRing.nodes);
					
					currentRing.nodes = nodes;
					
					ways.put(way, true);
					
					return true;
				}
				else if(ringEndNodeId == wayStartNodeId){
					currentRing.nodes.removeLast();
					
					currentRing.nodes.addAll(way.nodes);
					
					ways.put(way, true);
					
					return true;
				}
				else if(ringEndNodeId == wayEndNodeId){
					LinkedList<Node> nodes = way.nodes;
					
					Collections.reverse(nodes);
					
					currentRing.nodes.removeLast();
					
					currentRing.nodes.addAll(nodes);
					
					ways.put(way, true);
					
					return true;
				}
			}
		}
		return false;
	}
	
	
	/**
	 * Checks if the first and last node-id are the same, ergo, the way is closed.
	 * @param way
	 * @return true if the way is closed.
	 */
	private boolean isWayClosed(Way ring){
		if(ring.nodes.size() < 4)
			return false;
		
		if(ring.nodes.getFirst().getOsmId() == ring.nodes.getLast().getOsmId())
			return true;
		else
			return false;
	}
	
	/**
	 * Returns an unassigned Way or null if there is none left.
	 * @param ways
	 * @return
	 */
	private Way getUnassignedWay(Map<Way, Boolean> ways){
		for(Way way : ways.keySet()){
			if(!ways.get(way)){
				return way;
			}
		}
		return null;
	}

	/**
	 * Checks every member's role.
	 * @param area
	 * @param role
	 * @return true if a role has the given role value.
	 */
	private boolean hasWaysWithRole(Area area, WayRole role) {
		if(area.wayIds.values().contains(role))
			return true;
		return false;
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
			logger.error("SQL Query could not been executed.'"+sql+"'");
			return null;
		}
	}
	
	/**
	 * Checks if there is at least 1 Mapping which's AndEdConditions Keys are all contained in the given tags-Map.
	 * 
	 * @param tags
	 * @return
	 */
	private boolean hasOwnAreaTags(Map<String, Set<String>> tags) {
		// remove unwanted Tag-Keys for this check
		String[] ignoreKeys = context.getConfigParameter("mapping.ignoreareatagkeys").split(",");
		for(String ignoreKey : ignoreKeys){
			tags.remove(ignoreKey);
		}
		
		for(Map<String, Set<String>> mapping : config.getMappings(MappingType.MULTIPOLYGON)){
			if(tags.keySet().containsAll(mapping.keySet()))
				return true;
		}
		return false;
	}
}

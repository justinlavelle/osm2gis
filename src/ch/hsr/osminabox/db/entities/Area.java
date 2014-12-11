package ch.hsr.osminabox.db.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.db.XMLConstants;
import ch.hsr.osminabox.db.entities.exceptions.InvalidRelationException;
import ch.hsr.osminabox.db.entities.exceptions.InvalidWayException;

/**
 * ATTENTION: This class will not be instantiated by the parser, since no OSM-Area Entity exists. 
 *  
 * Represents an Area which can be created by one single-closed OSM way or 
 * OSM Relations with type=multipolygon or type=boundary.
 * 
 * An Area only references other Ways (never Nodes).
 * 
 * If constructed from one single-closed Way, the Area inherits all attributes and tags.
 * If constructed from a Relation, the Area inherits all attributes and tags from the relation. If no tags other than the "type"-
 * tag are available (usual if type=multipolygon), the Area inherits all tags from every member with role="outer".
 * 
 * @author jzimmerm
 */
public class Area extends OSMEntity{
	
	private static Logger logger = Logger.getLogger(Area.class);
	
	/**
	 * Possible Roles from Relations with type=multipolygon or type=boundary.
	 * "None" if Area is constructed from a way or no role is given by the relation.
	 */
	public enum WayRole{
		none,
		outer,
		inner,
		enclave,
		exclave
	}
	
	/**
	 * A Map holding all OSM-Ids of the referenced Ways as well as their Role-Attribute.
	 */
	public Map<Long, WayRole> wayIds = new HashMap<Long, WayRole>();
	
	/**
	 * A Map holding all referenced Ways as well as their Role-Attribute. Needs to be constructed from the AreaUtil Class.
	 */
	public Map<Way, WayRole> ways = new HashMap<Way, WayRole>();
	
	/**
	 * A Map holding the OSM Tags from the original Entity it is constructed from.
	 */
	public Map<String, Set<String>> originalTags = null;
			
	
	public Area(long wayId) throws InvalidWayException{
		if(wayId <= 0) throw new InvalidWayException("wayId must be greated than 0.");
		
		wayIds.put(wayId, WayRole.outer);
		
		originalTags = new HashMap<String, Set<String>>();
	}
	
	/**
	 * Creates a new Area Entity based on the given Way.
	 * The Tags are inherited 1:1 from the Way.
	 * 
	 * @param way
	 */
	public Area(Way way) throws InvalidWayException{
		if(way == null) throw new InvalidWayException("Way must not be null.");
		if(isWayClosed(way) == false) throw new InvalidWayException("Way must be closed.");
		
		this.attributes = new HashMap<String, String>(way.attributes);
		this.tags = new HashMap<String, Set<String>>(way.tags);
		
		// They stay equal if Area is constructed from a Way
		this.originalTags = this.tags;
		
		this.wayIds.clear();
		this.wayIds.put(way.getOsmId(), WayRole.outer);
	}
	
	/**
	 * Creates a new Area Entity based on the given Relation.
	 * The Tags need to be gathered from the way_temp-Tables via the AreaConstructor-Class.
	 * 
	 * @param relation
	 * @throws InvalidRelationException 
	 */
	public Area(Relation relation) throws InvalidRelationException {
		if(relation == null) throw new InvalidRelationException("Relation must not be null.");
		if(hasValidTypeTag(relation) == false) throw new InvalidRelationException("Relation must have a type tag of multipolygon or boundary.");
		
		this.originalTags = relation.tags;		
		this.attributes = relation.attributes;
		inheritWayIds(relation);
	}
	
	public Area(Area area){
		super(area);
		
		this.wayIds = area.wayIds;
		this.ways = area.ways;
		this.originalTags = area.originalTags;
	}

	/**
	 * Collects all referenced Ways and their Role. The specific Way Data needs to be gathered from the way_temp Table later.
	 * @param relation
	 */
	private void inheritWayIds(Relation relation) {
		this.wayIds.clear();
		for(RelationMember member : relation.members){
			
			WayRole role = WayRole.none;
			try{
				role = WayRole.valueOf(member.role.toLowerCase());
				
			}
			catch(Exception e){
				logger.debug("WayRole couldn't be identified on Way with OSM Id: " + member.osmId);
			}
			finally{
				wayIds.put(member.osmId, role);
			}
		}
	}

	/**
	 * Checks whether the relation is of type=multipolygon or type=boundary
	 * @param relation
	 * @return true if type=multipolygon or type=boundary
	 */
	private boolean hasValidTypeTag(Relation relation) {
		if(relation.tags.containsKey(XMLConstants.TAG_TYPE)){
			
			Set<String> typeTagValues = relation.tags.get(XMLConstants.TAG_TYPE);
			if(typeTagValues.contains(XMLConstants.TAG_TYPE_MULTIPOLYGON) ||
				typeTagValues.contains(XMLConstants.TAG_TYPE_BOUNDARY))
				
				return true;
		}
		return false;
	}
	
	/**
	 * Checks if the first and last node-id are the same, ergo, the way is closed.
	 * @param way
	 * @return true if the way is closed.
	 */
	private boolean isWayClosed(Way way) {
		if(way.nodes.size() < 4)
			return false;
		
		if(way.nodes.getFirst().getOsmId() == way.nodes.getLast().getOsmId())
			return true;
		else
			return false;
	}
}

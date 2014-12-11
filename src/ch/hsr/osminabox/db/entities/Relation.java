package ch.hsr.osminabox.db.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

/**
 * Represents an OSM Relation
 * @author jzimmerm
 */
public class Relation extends OSMEntity {
	
	/** A Map holding the tables as keys and a RelationMappingEntry with the mapped data. */
	public Map<String, RelationMappingEntry> dbMappings = new HashMap<String, RelationMappingEntry>();	
	
	/** All referenced RelationMembers */
	public List<RelationMember> members = new Vector<RelationMember>();	
	
	public Relation(){
	}
	
	public Relation(Relation relation){		
		this.attributes = new HashMap<String, String>(relation.attributes);
		this.tags = new TreeMap<String, Set<String>>(relation.tags);
		this.dbMappings = new HashMap<String, RelationMappingEntry>(relation.dbMappings);
		this.members = relation.members;
	}
	
	
}

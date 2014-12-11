package ch.hsr.osminabox.db.entities;

/**
 * 	Represents a OSM Relation Member used by relations.
 * @author rhof
 */
public class RelationMember {
	
	/**
	 * Type of the RelationMember.
	 */
	public RelationMemberType type;
	
	/** Osm Id of the refereed Member */
	public long osmId;
	
	/** Role of this Member */
	public String role;		
}

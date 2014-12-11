package ch.hsr.osminabox.db.entities;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.hsr.osminabox.schemamapping.xml.Column;
import ch.hsr.osminabox.schemamapping.xml.RelatedTable;;

/**
 * This class holds all "per MappingConfig" needed Values. They must be gathered together in several steps.
 * @author Joram
 *
 */
public class RelationMappingEntry {
	
	/** This flag indicates if all members MUST be contained by the database in one of the relatedTables. */
	public boolean allMembersRequiered = false;
	
	/** The Primary Key of this Relation in the table of this dbMapping. */
	public int relationDbId = 0;
	
	/** This List holds all the columns for a dbMappings table. */
	public List<Column> mappingColumns = new LinkedList<Column>();
	
	/** This List holds all relatedTables Attributes */
	public List<RelatedTable> relatedTables = new LinkedList<RelatedTable>();
	
	/** This Map holds for every related Table (Key) a Map (Value), 
	 *  which associates in a Map itself the db Id (Key) to the according RelationMember (Value). 
	 *  
	 *  Map<relatedTableName, Map<dbId, RelationMember>>
	 */
	public Map<String, Map<Integer, RelationMember>> dbIdsToMember = new HashMap<String, Map<Integer,RelationMember>>();
	
	
}

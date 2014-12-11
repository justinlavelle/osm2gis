package ch.hsr.osminabox.db.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.schemamapping.xml.Column;
/**
 *  Superclass of osm2gis Entity classes
 * @author m2huber
 *
 */
public class OSMEntity {	
	
	private static Logger logger = Logger.getLogger(OSMEntity.class);
	
	// Comely used attributes.
	private static final String ATTRIBUTE_ID = "id";
	public static final String ATTRIBUTE_TIMESTAMP = "timestamp";
	
	public OSMEntity(){
	}
	
	public OSMEntity(OSMEntity entity){
		this.attributes = new HashMap<String, String>(entity.attributes);
		this.tags = new TreeMap<String, Set<String>>(entity.tags);
		this.dbMappings = new HashMap<String, List<Column>>(entity.dbMappings);
	}

	/**
	 * A Map holding the entity-attributes for the Entity
	 */
	public Map<String, String> attributes = new HashMap<String, String>();
	
	/**
	 * A Map holding the OSM Tags for the Entity
	 */
	public Map<String, Set<String>> tags = new TreeMap<String, Set<String>>(String.CASE_INSENSITIVE_ORDER);
	
	/**
	 * A Map holding the tables and their columns, according to the DB structure, that this entity is mapped to.
	 */
	public Map<String, List<Column>> dbMappings = new HashMap<String, List<Column>>();	
	
	/**
	 * Commonly used: Gets the OSM Id of this Entity
	 * @return
	 */
//	public int getOsmId(){
	public long getOsmId(){
		try{
//			return Integer.parseInt(attributes.get(ATTRIBUTE_ID));
                        return Long.parseLong(attributes.get(ATTRIBUTE_ID));
		} 
		catch(Exception e){
			logger.error("Osm Id not available.");
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * Commonly used: Sets the OSM Id of this Entity
	 * @param osmId
	 */
	public void setOsmId(long osmId){
		try{
			attributes.put(ATTRIBUTE_ID, String.valueOf(osmId));
		} 
		catch (Exception e){
			logger.error("Could not set Osm Id: '" + osmId + "'");
		}
	}
	
	/**
	 * Adds the given Tags to the existing ones of this entity. 
	 * @param tags
	 */
	public void addTags(Map<String, Set<String>> tags){
		try{
			for(String key : tags.keySet()){
				Set<String> values = this.tags.get(key);
				if(values == null){
					values = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
					this.tags.put(key, values);
				}
				values.addAll(tags.get(key));
			}
		}
		catch(Exception e){
			logger.error("Failed to add Tags: " + e.getStackTrace());
		}
	}
	
	/**
	 * Adds the value to the Set belonging to the key.
	 * 
	 * @param key
	 * @param value
	 */
	public void putTag(String key, String value){
		Set<String> values = tags.get(key);
		if(values == null) {
			values = new TreeSet<String>();
			tags.put(key, values);
		}
		values.add(value);
	}
}

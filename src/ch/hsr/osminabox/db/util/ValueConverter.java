package ch.hsr.osminabox.db.util;

import java.util.Set;

import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.OSMEntity;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.RelationMember;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.sql.Constants;
import ch.hsr.osminabox.db.sql.area.GeomStrategy;
import ch.hsr.osminabox.db.sql.area.exceptions.NoWayValuesException;
import ch.hsr.osminabox.db.sql.util.GeomValueCreator;
import ch.hsr.osminabox.db.DBConstants;

/**
 * Converts a value which is escaped by %value% in the mapping file from the OSM Entity into the real value to be inserted into the Database.
 * @author jzimmerm
 *
 */
public class ValueConverter {
	private static final String ATTRIBUTE = "attribute_";
	private static final String TAG = "tag_";
	private static final String MEMBER = "member_";
	private static final String DB = "db_";
	
	private static final String TAGS_ALL = "tags_all";
	private static final String ND_ALL = "nd_all";
	private static final String MEMBERS_ALL = "members_all";
	
	private static final String GEOM = "geom";
	
	private static final String MEMBER_TYPE = "type";
	private static final String MEMBER_REF = "ref";
	private static final String MEMBER_ROLE = "role";
	
	private static final String DB_RELATION_ID = "relation_id";
	private static final String DB_MEMBER_ID = "member_id";
	
	private static final String ESCAPE_CHAR ="%";
	private static final String DELIMITER = ";";
	private static final String HSTORE_EQUALS = "=>";
	
	protected ValueValidation validator;
	protected GeomValueCreator geomValues;
	
	public ValueConverter(){
		validator = new ValueValidation();
		geomValues = new GeomValueCreator();
	}
	
	/**
	 * If the value matches a node attribute- or tags-key, that key's value is returned.
	 * @param value The value to be checked for.
	 * @param node The entity which attributes and tags should be searched for a match.
	 * @return the converted or original value.
	 */
	public String convertValue(String value, Node node){
		
		if(!value.startsWith(ESCAPE_CHAR) && !value.endsWith(ESCAPE_CHAR))
			return value;
		
		String key = stripEscapeChar(value);
		String resultValue = "";
		
		resultValue = convertCommonKeys(key, node);
		if(resultValue.length() == 0){
			if(key.equals(GEOM))
				resultValue = geomValues.addGeom(node).toString();
		}
		
		return resultValue;
	}
	
	/**
	 * If the value matches an entities attribute-, tags- or nd_all-key, that key's value is returned.
	 * @param value The value to be checked for.
	 * @param way The entity which attributes and tags should be searched for a match.
	 * @return the converted or original value.
	 */
	public String convertValue(String value, Way way){
		
		if(!value.startsWith(ESCAPE_CHAR) && !value.endsWith(ESCAPE_CHAR))
			return value;
		
		String key = stripEscapeChar(value);
		String resultValue = "";
		
		resultValue = convertCommonKeys(key, way);
		
		if(resultValue.length() == 0){			
			if(key.equals(ND_ALL))
				resultValue = addNodesFromWay(way);
			
			else if(key.equals(GEOM))
				resultValue = geomValues.addGeom(way).toString();
		}
		
		return resultValue;
	}
	
	/**
	 * If the value matches an entities attribute-, members-, tags- or geom-key, that key's value is returned.
	 * @param value
	 * @param area
	 * @param geom
	 * @return
	 */
	public String convertValue(String value, Area area, GeomStrategy geom) throws NoWayValuesException{
		
		if(!value.startsWith(ESCAPE_CHAR) && !value.endsWith(ESCAPE_CHAR))
			return value;
		
		String key = stripEscapeChar(value);
		String resultValue = "";
		
		resultValue = convertCommonKeys(key, area);
		
		if(resultValue.length() == 0){
			if(geom != null && key.equals(GEOM))
				resultValue = geom.getGeom(area).toString();
		}
		
		return resultValue;
	}
	
	
	/**
	 * If the value matches an entities attribute-, members- or tags-key, that key's value is returned.
	 * @param value
	 * @param relation
	 * @return
	 */
	public String convertValue(String value, Relation relation){
		
		if(!value.startsWith(ESCAPE_CHAR) && !value.endsWith(ESCAPE_CHAR))
			return value;
		
		String key = stripEscapeChar(value);
		String resultValue = "";
		
		resultValue = convertCommonKeys(key, relation);
		
		if(resultValue.length() == 0){
			if(key.equals(MEMBERS_ALL))
				resultValue = addMembersFromRelation(relation);
		}
		
		return resultValue;
	}
	
	/**
	 * Returns the Value of the Ref, Type or Role from this RelationMember.
	 * @param value
	 * @param member
	 * @return
	 */
	public String convertValue(String value, RelationMember member){
		return convertValue(value, 0, 0, member);
	}
		
	/**
	 * Returns RelationMember values or dbId from the RelationMember / Relation
	 * 
	 * @param key
	 * @param dbRelationId
	 * @param dbMemberId
	 * @param member
	 * @return
	 */
	public String convertValue(String key, int dbRelationId, int dbMemberId, RelationMember member){
		
		if(!key.startsWith(ESCAPE_CHAR) && !key.endsWith(ESCAPE_CHAR))
			return key;
		
		key = stripEscapeChar(key.toLowerCase());
		
		if(key.startsWith(DB)){
			String dbKey = removePrefix(key, DB);
			
			if(dbKey.equals(DB_MEMBER_ID))
				return String.valueOf(dbMemberId);
			else if(dbKey.equals(DB_RELATION_ID))
				return String.valueOf(dbRelationId);
			
		}		
		else if(key.startsWith(MEMBER)){
			String memberKey = removePrefix(key, MEMBER);
			
			if(memberKey.equals(MEMBER_TYPE))
				return member.type.toString();
			else if(memberKey.equals(MEMBER_REF))
				return String.valueOf(member.osmId);
			else if(memberKey.equals(MEMBER_ROLE))
				return member.role;	
		}
		
		return "";
	}
	
	/**
	 * Converts a Set of Elements into a single String dividing the elements by the delimiter.
	 * @param values
	 * @return
	 */
	public String mergeSetToDelimiterString(Set<String> values){
		
		if(values == null)
			return "";
		
		String result = "";
		
		for(String value : values){
			if(result.length() > 0)
				result += DELIMITER;
			result += value;
		}
		return result;
	}
	
	/**
	 * Converts the Value of Keys valid for all OSM Entities
	 * @param key
	 * @param entity
	 * @return
	 */
	protected String convertCommonKeys(String key, OSMEntity entity){
		String resultValue = "";
		
		if(key.startsWith(ATTRIBUTE))
			resultValue = entity.attributes.get(removePrefix(key, ATTRIBUTE));
		
		else if(key.startsWith(TAG))
			resultValue = mergeSetToDelimiterString(entity.tags.get(removePrefix(key, TAG)));
		
		else if(key.equals(TAGS_ALL))
			resultValue = convertTagsToHStore(entity);
		
		if(resultValue != null)
			return resultValue;
		return "";
		
	}
	
	/**
	 * Puts all key-value-pairs from the tags Map into a hstore String. 
	 * Escapes special characters and merges duplicated keys into a delimiter separated String.
	 * @param entity
	 * @return
	 */
	public String convertTagsToHStore(OSMEntity entity){
		
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(Constants.APOSTROPHE);
		
		int index=0;
		for(String key : entity.tags.keySet()) {
			
			buffer.append(Constants.SPEECH_MARK);
			buffer.append(key);
			buffer.append(Constants.SPEECH_MARK);
			buffer.append(HSTORE_EQUALS);
			buffer.append(Constants.SPEECH_MARK);
			buffer.append(validator.addEscape(mergeSetToDelimiterString(entity.tags.get(key))));
			buffer.append(Constants.SPEECH_MARK);
			index++;
			if(index < entity.tags.size()) {
				buffer.append(Constants.SPACER);
			}
		}
		
		buffer.append(Constants.APOSTROPHE);
			
		return buffer.toString();
	}
	
	/**
	 * Merges all Node-References from a Way into a single String.
	 * @param way
	 * @return
	 */
	public String addNodesFromWay(Way way){
		StringBuffer buffer = new StringBuffer();
		
		for(Node node : way.nodes) {
			buffer.append(node.getOsmId());
			buffer.append(DBConstants.SQL_WAY_TEMP_NODE_SPACER);
		}
		
		return buffer.toString();
	}
	
	/**
	 * Merges all Members-References from a Relation into a single String.
	 * @param relation
	 * @return
	 */
	private String addMembersFromRelation(Relation relation){
		StringBuffer buffer = new StringBuffer();
		
		for(RelationMember member : relation.members) {
			if(buffer.length() > 0)
				buffer.append(DBConstants.SQL_RELATION_TEMP_MEMBER_SPACER);
			
			buffer.append(member.osmId);
			buffer.append(DBConstants.SQL_RELATION_TEMP_MEMBER_VALUE_SPACER);
			buffer.append(member.type.toString());
			buffer.append(DBConstants.SQL_RELATION_TEMP_MEMBER_VALUE_SPACER);
			buffer.append(member.role);
		}
		
		return buffer.toString();
	}
	
	private String stripEscapeChar(String value){
		if(value.startsWith(ESCAPE_CHAR) && value.endsWith(ESCAPE_CHAR))
			return value.substring(ESCAPE_CHAR.length(), value.length() - ESCAPE_CHAR.length());
		
		return value;
	}
	
	private String removePrefix(String value, String prefix){
		return value.replace(prefix, "");
	}	
}

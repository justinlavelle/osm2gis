package ch.hsr.osminabox.importing.xml;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains enums for the different xml tags occuring in an osm file
 * @author rhof
 *
 */
public enum XMLTag {
	
	OSM("osm"),
	
	NODE("node"),
	TAG("tag"),
	WAY("way"),
	NODE_REF("nd"),
	RELATION("relation"),
	RELATION_MEMBER("member"),
	
	OSM_CHANGE("osmChange"),
	CREATE("create"),
	MODIFY("modify"),
	DELETE("delete"),
	
	BOUND("bound"),
	BOUNDS("bounds"),
	CHANGESET("changeset");
	
	private final String xmlRepresentation;
	private static Map<String, XMLTag> tags;
	
	static{
		tags = new HashMap<String, XMLTag>();
		for(XMLTag xmlTag: XMLTag.values()){
			tags.put(xmlTag.getXMLRepresentation(), xmlTag);
		}
	}
	

	private XMLTag(String xmlRepresentation) {
		this.xmlRepresentation = xmlRepresentation;
	}
	
	/**
	 * @return the XML String representation for a tag
	 */
	public String getXMLRepresentation(){
		return this.xmlRepresentation;
	}
	
	/**
	 * @param xmlRepresentation
	 * @return the tag object for the xml representation
	 */
	public static XMLTag getTag(String xmlRepresentation){
		return tags.get(xmlRepresentation);
	}
	
}

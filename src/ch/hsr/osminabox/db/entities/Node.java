package ch.hsr.osminabox.db.entities;

/**
 * Represents a OSM Node
 * @author rhof
 */
public class Node extends OSMEntity {
	public static final String NODE_LATITUDE = "lat";
	public static final String NODE_LONGITUDE = "lon";
	
	public Node(){
	}
	
	public Node(Node node){
		super(node);
	}
}

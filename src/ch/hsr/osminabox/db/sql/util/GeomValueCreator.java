package ch.hsr.osminabox.db.sql.util;

import java.util.LinkedList;

import ch.hsr.osminabox.db.DBConstants;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.sql.Constants;

/**
 * Helper Class for creatng geom sql statements
 * @author m2huber
 *
 */
public class GeomValueCreator {
	
	/**
	 * Creates the sql for the geom column for a node.
	 * @param node
	 * @return
	 */
	public StringBuffer addGeom(Node node) {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(DBConstants.SQL_POINT_GEOM_START);
		buffer.append(node.attributes.get(Node.NODE_LONGITUDE));
		buffer.append(Constants.LINE_SPACE);
		buffer.append(node.attributes.get(Node.NODE_LATITUDE));
		buffer.append(DBConstants.SQL_GEOM_END);
		
		return buffer;
	}
	
	
	/**
	 * Creates the sql for the geom column for a way.
	 * @param way
	 * @return
	 */
	public StringBuffer addGeom(Way way) {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(DBConstants.SQL_LINESTRING_GEOM_START);
		buffer.append(getLatLonForGeom(way.nodes));
		buffer.append(DBConstants.SQL_GEOM_END);
		
		return buffer;
	}
	
	/**
	 * Returns a StringBuffer with Lon / Lat (' lon lat , lon lat , ...)  
	 * @param nodes The Nodes have to be filled with Lon / Lat Tags
	 * @return StringBuffer with lon lat
	 */
	private StringBuffer getLatLonForGeom(LinkedList<Node> nodes) {
		StringBuffer latLon = new StringBuffer();		
	
		int index=0;
		for(Node node : nodes) {
			latLon.append(node.attributes.get(Node.NODE_LONGITUDE));
			latLon.append(Constants.LINE_SPACE);
			latLon.append(node.attributes.get(Node.NODE_LATITUDE));
			index++;
			if(index < nodes.size()) 
				latLon.append(Constants.SPACER);
		}
		
		return latLon;
	}	

}

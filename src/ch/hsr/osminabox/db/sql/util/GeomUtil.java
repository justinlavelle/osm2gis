package ch.hsr.osminabox.db.sql.util;

import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.sql.Constants;

public class GeomUtil {
	
	/**
	 * Returns the Lon / Lat Values of all Nodes in the format: <lon> <lat>,<lon> <lat>....
	 * @param way
	 * @return
	 */
	public StringBuffer getLonLatForGeom(Way way){
		StringBuffer lonLat = new StringBuffer();
		
		int i = 0;
		for(Node node : way.nodes){
			lonLat.append(node.attributes.get(Node.NODE_LONGITUDE));
			lonLat.append(Constants.LINE_SPACE);
			lonLat.append(node.attributes.get(Node.NODE_LATITUDE));
		
			i++;
			if ( i < way.nodes.size())
				lonLat.append(Constants.SPACER);
		}
		
		return lonLat;
	}	
}

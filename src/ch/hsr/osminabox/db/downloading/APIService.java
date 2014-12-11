package ch.hsr.osminabox.db.downloading;

import java.util.List;

import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Way;

/**
 * An API Service is used to retrieve nodes or ways from the OSM Api
 * An internet Connection is nescessary for an implementation of this class to work.
 * @author rhof
 *
 */
public interface APIService {
	
	/**
	 * @param nodeIds
	 * @return
	 */
	public List<Node> retrieveNodes(List<Node> nodes);

	/**
	 * Downloads the Way to the given OsmId including all its referenced Nodes.
	 * @param way
	 */
	public Way retrieveWayFull(long osmId);
	
}

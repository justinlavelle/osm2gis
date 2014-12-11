package ch.hsr.osminabox.db.downloading;

import java.util.LinkedList;
import java.util.List;

import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Way;

/**
 * An Implementation of this class should be able to retrieve Nodes and
 * Ways via the OSM Api
 * @author rhof
 */
public interface EntityConsistencyService {

	/**
	 * @param nodes Nodes to store for the API Call
	 */
	public void addMissingNodes(List<Node> nodes);
		
	/**
	 * Executes the API Call for Nodes
	 * @return the fetched Nodes from the OSM API
	 */
	public LinkedList<Node> fetchMissingNodes();
	
	/**
	 * Downloads the Way with the given OsmId and all data from its referenced Nodes and Tags.
	 * 
	 * @param osmId
	 * @return
	 */
	public Way fetchWayFull(long osmId);
	
}

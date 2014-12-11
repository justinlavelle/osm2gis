package ch.hsr.osminabox.db.handlingstrategy;

import java.util.Map;

import ch.hsr.osminabox.db.entities.Node;

public interface NodeHandlingStrategy {
	
	/**
	 * Create SQL Statement for Node_Temp table and add it to HashMap
	 * @param node
	 * @param statements
	 */
	public void addTemp(Node node , Map<String, StringBuffer> statements);
	
	/**
	 * Create SQL Statement for every Table this Node is mapped to.
	 * @param node
	 * @param statements
	 */
	public void addNode(Node node, Map<String, StringBuffer> statements);

}

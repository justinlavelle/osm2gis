package ch.hsr.osminabox.importing.xml;

import ch.hsr.osminabox.db.entities.Node;

/**
 * A listner used by XMLTagHandlers for retrieving nodes from subhandlers
 * @author rhof
 *
 */
public interface NodeReferenceListener {

	public void addNodeReference(Node node);
	
}

package ch.hsr.osminabox.importing.listener;

import java.util.List;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.DBService;

/**
 * This listener is used for to handle modify calls for nodes and
 * delegates the buffered nodes to the database layer
 * @author rhof
 */
public class ModifyNodeListener extends EntityBufferListener<Node> {

	private static final Logger logger = Logger.getLogger(ModifyNodeListener.class);
	
	public ModifyNodeListener(DBService db, int bufferSize) {
		super(db, bufferSize);
	}

	@Override
	public void handleWakeupEvent(List<Node> entitys) {
		if(logger.isDebugEnabled())
			logger.debug("Node-Modify start");
		getDBService().modifyNodes(entitys);
		if(logger.isDebugEnabled())
			logger.debug("Node-Modify stop");		
		logger.info("Processed NodeStack with buffersize: " + entitys.size());
	}

}

package ch.hsr.osminabox.importing.listener;

import java.util.List;

import org.apache.log4j.Logger;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.DBService;


/**
 * This listener is used for to handle create calls for nodes and
 * delegates the buffered nodes to the database layer
 * @author rhof
 */
public class CreateNodeListener extends EntityBufferListener<Node> {

	private static Logger logger = Logger.getLogger(CreateNodeListener.class);
	
	public CreateNodeListener(DBService db, int bufferSize) {
		super(db, bufferSize);
	}

	@Override
	public void handleWakeupEvent(List<Node> entitys) {
		if(logger.isDebugEnabled())
			logger.debug("Point-Writing start");
		getDBService().insertNodes(entitys);
		if(logger.isDebugEnabled())
			logger.debug("Point-Writing stop");		
		logger.info("Processed PointStack with buffersize: " + entitys.size());
	}

}

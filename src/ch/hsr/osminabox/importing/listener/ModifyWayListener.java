package ch.hsr.osminabox.importing.listener;

import java.util.List;

import org.apache.log4j.Logger;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.DBService;

/**
 * This listener is used for to handle modify calls for ways and
 * delegates the buffered ways to the database layer
 * 
 * @author rhof
 */
public class ModifyWayListener extends EntityBufferListener<Way> {

	private final static Logger logger = Logger.getLogger(ModifyWayListener.class);
	
	public ModifyWayListener(DBService db, int bufferSize) {
		super(db, bufferSize);
	}

	@Override
	public void handleWakeupEvent(List<Way> entitys) {
		if(logger.isDebugEnabled())
			logger.debug("Way-Modify start");
		getDBService().modifyWays(entitys);
		if(logger.isDebugEnabled())
			logger.debug("Way-Modify stop");		
		logger.info("Processed WayStack with buffersize: " + entitys.size());
	}

}

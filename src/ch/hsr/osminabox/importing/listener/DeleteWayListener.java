package ch.hsr.osminabox.importing.listener;

import java.util.List;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.DBService;

/**
 * This listener is used for to handle delete calls for ways and
 * delegates the buffered ways to the database layer
 * 
 * @author rhof
 */
public class DeleteWayListener extends EntityBufferListener<Way> {

	private static Logger logger = Logger.getLogger(DeleteWayListener.class);
	
	public DeleteWayListener(DBService db, int bufferSize) {
		super(db, bufferSize);
	}

	@Override
	public void handleWakeupEvent(List<Way> entitys) {
		if(logger.isDebugEnabled())
			logger.debug("Way-Delete start");
		getDBService().deleteWays(entitys);
		if(logger.isDebugEnabled())
			logger.debug("Way-Delete stop");		
		logger.info("Processed WayStack with buffersize: " + entitys.size());
	}

}

package ch.hsr.osminabox.importing.listener;

import java.util.List;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.DBService;

/**
 * This listener is used for to handle delete calls for nodes and
 * delegates the buffered nodes to the database layer
 * @author rhof
 *
 */
public class DeleteAreaListener extends EntityBufferListener<Area> {

	private static Logger logger = Logger.getLogger(DeleteAreaListener.class);
	
	public DeleteAreaListener(DBService db, int bufferSize) {
		super(db, bufferSize);
	}

	@Override
	public void handleWakeupEvent(List<Area> entitys) {
		if(logger.isDebugEnabled())
			logger.debug("Point-Delete start");
		getDBService().deleteAreas(entitys);
		if(logger.isDebugEnabled())
			logger.debug("Point-Delete stop");		
		logger.info("Processed PointStack with buffersize: " + entitys.size());
	}

}

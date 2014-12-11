package ch.hsr.osminabox.importing.listener;

import java.util.List;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.DBService;


/**
 * This listener is used for to handle create calls for areas and
 * delegates the buffered areas to the database layer
 * @author jzimmerm
 *
 */
public class CreateAreaListener extends EntityBufferListener<Area> {

	private Logger logger = Logger.getLogger(CreateAreaListener.class);
	
	public CreateAreaListener(DBService db, int bufferSize) {
		super(db, bufferSize);
	}

	@Override
	public void handleWakeupEvent(List<Area> entitys) {
		logger.debug("Area-Writing start");
		
		getDBService().insertAreas(entitys);
		
		logger.debug("Area-Writing stop");		
		logger.info("Processed AreaStack with buffersize: "+ entitys.size());
	}

}

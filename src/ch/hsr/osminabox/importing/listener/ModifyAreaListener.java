package ch.hsr.osminabox.importing.listener;

import java.util.List;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.db.DBService;
import ch.hsr.osminabox.db.entities.Area;

public class ModifyAreaListener extends EntityBufferListener<Area>{
	
	private static final Logger logger = Logger.getLogger(ModifyAreaListener.class);

	public ModifyAreaListener(DBService db, int bufferSize) {
		super(db, bufferSize);
	}

	@Override
	public void handleWakeupEvent(List<Area> entitys) {
		if(logger.isDebugEnabled())
			logger.debug("Area-Modify start");
		getDBService().modifyAreas(entitys);
		if(logger.isDebugEnabled())
			logger.debug("Area-Modify stop");		
		logger.info("Processed AreaStack with buffersize: " + entitys.size());
	}

}

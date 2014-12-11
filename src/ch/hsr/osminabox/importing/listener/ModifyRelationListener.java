package ch.hsr.osminabox.importing.listener;

import java.util.List;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.DBService;

/**
 * This listener is used for to handle modify calls for relations and
 * delegates the buffered relations to the database layer
 * 
 * @author rhof
 */
public class ModifyRelationListener extends EntityBufferListener<Relation> {

	private static final Logger logger = Logger.getLogger(ModifyRelationListener.class);
	
	public ModifyRelationListener(DBService db, int bufferSize) {
		super(db, bufferSize);
	}

	@Override
	public void handleWakeupEvent(List<Relation> entitys) {
		if(logger.isDebugEnabled())
			logger.debug("Relation-Modify start");
		getDBService().modifyRelations(entitys); 
		if(logger.isDebugEnabled())
			logger.debug("Relation-Modify stop");		
		logger.info("Processed RelationStack with buffersize: "+ entitys.size());
	}

}

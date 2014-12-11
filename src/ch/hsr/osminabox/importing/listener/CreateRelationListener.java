package ch.hsr.osminabox.importing.listener;

import java.util.List;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.DBService;


/**
 * This listener is used for to handle create calls for relations and
 * delegates the buffered relations to the database layer
 * @author rhof
 *
 */
public class CreateRelationListener extends EntityBufferListener<Relation> {

	private Logger logger = Logger.getLogger(CreateRelationListener.class);
	
	public CreateRelationListener(DBService db, int bufferSize) {
		super(db, bufferSize);
	}

	@Override
	public void handleWakeupEvent(List<Relation> entitys) {
		if(logger.isDebugEnabled())
			logger.debug("Relation-Writing start");
		getDBService().insertRelations(entitys);
		if(logger.isDebugEnabled())
			logger.debug("Relation-Writing stop");		
		logger.info("Processed RelationStack with buffersize: " + entitys.size());
	}

}

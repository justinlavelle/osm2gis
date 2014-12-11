package ch.hsr.osminabox.importing.strategy;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.db.DBService;
import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.importing.listener.DeleteAreaListener;
import ch.hsr.osminabox.importing.listener.DeleteNodeListener;
import ch.hsr.osminabox.importing.listener.DeleteRelationListener;
import ch.hsr.osminabox.importing.listener.DeleteWayListener;
import ch.hsr.osminabox.importing.listener.EntityBufferListener;

/**
 * Creates the Buffers for the case if the Entitys should be deleted from the database
 * @author rhof
 */
public class DeleteBufferStrategy extends BufferStrategy {
	
	
	public DeleteBufferStrategy(ApplicationContext context) {
		super(context);
	}

	@Override
	protected EntityBufferListener<Area> createAreaListener(
			DBService dbService, int buffersize) {
		return new DeleteAreaListener(dbService, buffersize);
	}

	@Override
	protected EntityBufferListener<Node> createNodeListener(
			DBService dbService, int buffersize) {
		return new DeleteNodeListener(dbService, buffersize);
	}

	@Override
	protected EntityBufferListener<Relation> createRelationListener(
			DBService dbService, int buffersize) {
		return new DeleteRelationListener(dbService, buffersize);
	}

	@Override
	protected EntityBufferListener<Way> createWayListener(DBService dbService,
			int buffersize) {
		return new DeleteWayListener(dbService, buffersize);
	}

}

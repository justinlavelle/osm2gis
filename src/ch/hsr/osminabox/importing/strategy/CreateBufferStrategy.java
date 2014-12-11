package ch.hsr.osminabox.importing.strategy;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.db.DBService;
import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.importing.listener.CreateAreaListener;
import ch.hsr.osminabox.importing.listener.CreateNodeListener;
import ch.hsr.osminabox.importing.listener.CreateRelationListener;
import ch.hsr.osminabox.importing.listener.CreateWayListener;
import ch.hsr.osminabox.importing.listener.EntityBufferListener;

/**
 * Creates the Buffers for the case if the Entitys should be inserted to the
 * database
 * 
 * @author rhof
 */
public class CreateBufferStrategy extends BufferStrategy {

	public CreateBufferStrategy(ApplicationContext context) {
		super(context);
	}

	@Override
	protected EntityBufferListener<Area> createAreaListener(
			DBService dbService, int buffersize) {
		return new CreateAreaListener(dbService, buffersize);
	}

	@Override
	protected EntityBufferListener<Node> createNodeListener(
			DBService dbService, int buffersize) {
		return new CreateNodeListener(dbService, buffersize);
	}

	@Override
	protected EntityBufferListener<Relation> createRelationListener(
			DBService dbService, int buffersize) {
		return new CreateRelationListener(dbService, buffersize);
	}

	@Override
	protected EntityBufferListener<Way> createWayListener(DBService dbService,
			int buffersize) {
		return new CreateWayListener(dbService, buffersize);
	}

}

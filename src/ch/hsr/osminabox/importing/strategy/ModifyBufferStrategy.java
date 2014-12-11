package ch.hsr.osminabox.importing.strategy;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.db.DBService;
import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.importing.listener.EntityBufferListener;
import ch.hsr.osminabox.importing.listener.ModifyAreaListener;
import ch.hsr.osminabox.importing.listener.ModifyNodeListener;
import ch.hsr.osminabox.importing.listener.ModifyRelationListener;
import ch.hsr.osminabox.importing.listener.ModifyWayListener;

/**
 * Creates the Buffers for the case if existing Entitys should be modified in the database
 * @author rhof
 */
public class ModifyBufferStrategy extends BufferStrategy {


	public ModifyBufferStrategy(ApplicationContext context) {
		super(context);
	}

	@Override
	protected EntityBufferListener<Area> createAreaListener(
			DBService dbService, int buffersize) {
		return new ModifyAreaListener(dbService, buffersize);
	}

	@Override
	protected EntityBufferListener<Node> createNodeListener(
			DBService dbService, int buffersize) {
		return new ModifyNodeListener(dbService, buffersize);
	}

	@Override
	protected EntityBufferListener<Relation> createRelationListener(
			DBService dbService, int buffersize) {
		return new ModifyRelationListener(dbService, buffersize);
	}

	@Override
	protected EntityBufferListener<Way> createWayListener(DBService dbService,
			int buffersize) {
		return new ModifyWayListener(dbService, buffersize);
	}

}

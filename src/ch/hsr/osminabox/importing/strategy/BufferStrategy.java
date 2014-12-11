package ch.hsr.osminabox.importing.strategy;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.context.ConfigConstants;
import ch.hsr.osminabox.db.DBService;
import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.importing.EntityBuffer;
import ch.hsr.osminabox.importing.listener.EntityBufferListener;

/**
 * An Implementation of the class should create buffers for a specific situation
 * 
 * @author rhof
 */
public abstract class BufferStrategy {

	protected ApplicationContext context;
	protected EntityBuffer<Node> nodeBuffer;
	protected EntityBuffer<Way> wayBuffer;
	protected EntityBuffer<Relation> relationBuffer;
	protected EntityBuffer<Area> areaBuffer;

	public BufferStrategy(ApplicationContext context) {
		this.context = context;
		initBuffers();
	}

	protected void initBuffers() {
		nodeBuffer = new EntityBuffer<Node>();
		DBService dbService = context.getDBService();
		nodeBuffer
				.addBufferListener(createNodeListener(dbService,
						loadConfigParameter(
								ConfigConstants.CONF_BUFFERSIZE_NODE, 10000)));
		wayBuffer = new EntityBuffer<Way>();
		wayBuffer
				.addBufferListener(createWayListener(dbService,
						loadConfigParameter(
								ConfigConstants.CONF_BUFFERSIZE_WAY, 1000)));
		relationBuffer = new EntityBuffer<Relation>();
		relationBuffer.addBufferListener(createRelationListener(dbService,
				loadConfigParameter(ConfigConstants.CONF_BUFFERSIZE_RELATION,
						10)));
		areaBuffer = new EntityBuffer<Area>();
		areaBuffer.addBufferListener(createAreaListener(dbService,
				loadConfigParameter(ConfigConstants.CONF_BUFFERSIZE_AREA, 10)));
	}

	protected abstract EntityBufferListener<Node> createNodeListener(
			DBService dbService, int buffersize);
	protected abstract EntityBufferListener<Way> createWayListener(
			DBService dbService, int buffersize);
	protected abstract EntityBufferListener<Relation> createRelationListener(
			DBService dbService, int buffersize);
	protected abstract EntityBufferListener<Area> createAreaListener(
			DBService dbService, int buffersize);

	/**
	 * @return a NodeBuffer
	 */
	public EntityBuffer<Node> getNodeBuffer() {
		return nodeBuffer;
	}

	/**
	 * @return a WayBuffer
	 */
	public EntityBuffer<Way> getWayBuffer() {
		return wayBuffer;
	}

	/**
	 * @return a RelationBuffer
	 */
	public EntityBuffer<Relation> getRelationBuffer() {
		return relationBuffer;
	}

	/**
	 * @return an AreaBuffer
	 */
	public EntityBuffer<Area> getAreaBuffer() {
		return areaBuffer;
	}

	protected int loadConfigParameter(String param, int defaultValue) {
		try {
			return Integer.parseInt(context.getConfigParameter(param));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

}

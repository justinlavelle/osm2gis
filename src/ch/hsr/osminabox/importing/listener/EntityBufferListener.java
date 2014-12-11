package ch.hsr.osminabox.importing.listener;

import java.util.List;

import ch.hsr.osminabox.db.entities.OSMEntity;
import ch.hsr.osminabox.db.DBService;

/**
 * A Listener for which can be registered on an EntityBuffer
 * @author rhof
 *
 * @param <T> Any OSMEntity
 */
public abstract class EntityBufferListener<T extends OSMEntity>{
	
	private int bufferSize;
	private DBService db;
	
	public EntityBufferListener(DBService db, int bufferSize) {
		this.bufferSize = bufferSize;
		this.db = db;
	}

	public int getWakeupEventNotificationSize() {
		return bufferSize;
	}
	
	public void handleFlush(List<T> entitys){
		handleWakeupEvent(entitys);
	}
	
	protected DBService getDBService(){
		return db;
	}
	
	public abstract void handleWakeupEvent(List<T> entitys);
	
}

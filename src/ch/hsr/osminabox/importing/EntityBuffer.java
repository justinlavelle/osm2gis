package ch.hsr.osminabox.importing;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import ch.hsr.osminabox.db.entities.OSMEntity;
import ch.hsr.osminabox.importing.listener.EntityBufferListener;

/**
 * A Buffer storing osm entitys. It is able to fire events if a specified
 * buffer size is reached
 * @author rhof
 *
 * @param <T> Can be an OSMEntity
 */
public class EntityBuffer<T extends OSMEntity> {

	private static Logger logger = Logger.getLogger(EntityBuffer.class);
	
	private List<T> entities = new ArrayList<T>();	
	private List<EntityBufferListener<T>> listeners = new Vector<EntityBufferListener<T>>();
	
	public void addBufferListener(EntityBufferListener<T> listener) {
		if(logger.isTraceEnabled()){logger.trace("EntityBuffer Listener Added");}
		listeners.add(listener);
	}

	public List<EntityBufferListener<T>> getBufferListeners(){
		return listeners;
	}
	
	public List<T> getEntitys() {
		return entities;
	}
	
	/**
	 * Fires the handle flush method on the listeners
	 */
	public void flush() {
		for(EntityBufferListener<T> listener: listeners){
			listener.handleFlush(entities);
			clear();
		}
	}
	
	public void clear() {
		entities.clear();
	}

	public void put(T entity) {
		entities.add(entity);
		checkNotification();
	}
	
	/**
	 * Fires an event on the registered listeners if a specified buffer size is reached
	 */
	private void checkNotification(){
		for(EntityBufferListener<T> listener: listeners){
			if(entities.size() >= listener.getWakeupEventNotificationSize()){
				logger.trace("Notify NodeStack Listener");
				listener.handleWakeupEvent(entities);
				clear();
			}
		}
	}
}

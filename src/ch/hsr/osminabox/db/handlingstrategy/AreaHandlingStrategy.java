package ch.hsr.osminabox.db.handlingstrategy;

import java.util.Map;

import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.sql.area.GeomStrategy;

public interface AreaHandlingStrategy {
	
	public void addArea(Area area, Map<String, StringBuffer> statements, GeomStrategy geom); 

}

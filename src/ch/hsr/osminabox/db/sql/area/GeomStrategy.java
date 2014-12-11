package ch.hsr.osminabox.db.sql.area;

import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.sql.area.exceptions.NoWayValuesException;

public interface GeomStrategy {
	/**
	 * Create the Geom String
	 * @param area
	 * @return
	 */
	
	public StringBuffer getGeom(Area area) throws NoWayValuesException;

}

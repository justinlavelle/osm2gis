package ch.hsr.osminabox.db.sql.area;

import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.sql.Constants;
import ch.hsr.osminabox.db.sql.area.exceptions.NoWayValuesException;
import ch.hsr.osminabox.db.sql.util.GeomUtil;
import ch.hsr.osminabox.db.DBConstants;

/**
 * Create Geom Statement for Relation with Only Outer Ways
 * @author jzimmerm
 *
 */
public class GeomOnlyOuterStrategy implements GeomStrategy {
	
	private GeomUtil geomUtil;
	
	public GeomOnlyOuterStrategy(GeomUtil geomUtil) {
		this.geomUtil = geomUtil;
	}

	@Override
	public StringBuffer getGeom(Area area) throws NoWayValuesException {
		
		if(area.ways.size() == 0)
			throw new NoWayValuesException("Way Data not available.");
		
		StringBuffer sql = new StringBuffer();
		sql.append(DBConstants.SQL_MULTIPOLYGON_GEOM_START);
		sql.append(Constants.OPEN_BRACKET);
		int i = 0;
		for (Way way : area.ways.keySet()) {
			if (i != 0 && i < area.ways.size())
				sql.append(Constants.SPACER);

			sql.append(Constants.OPEN_BRACKET_DOUBLE);
			sql.append(geomUtil.getLonLatForGeom(way));
			sql.append(Constants.CLOSE_BRACKET_DOUBLE);
			i++;
		}
		sql.append(DBConstants.SQL_GEOM_END);
		return sql;
	}

}

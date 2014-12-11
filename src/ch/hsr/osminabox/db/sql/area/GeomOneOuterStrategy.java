package ch.hsr.osminabox.db.sql.area;

import java.util.Map.Entry;

import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.entities.Area.WayRole;
import ch.hsr.osminabox.db.sql.Constants;
import ch.hsr.osminabox.db.sql.area.exceptions.NoWayValuesException;
import ch.hsr.osminabox.db.sql.util.GeomUtil;
import ch.hsr.osminabox.db.util.AreaCompositionDetector;
import ch.hsr.osminabox.db.DBConstants;
/**
 * Create Geom Statement for Areas with One Outer and N inner Ways
 * @author jzimmerm
 *
 */
public class GeomOneOuterStrategy implements GeomStrategy{
	
	protected GeomUtil geomUtil;
	protected AreaCompositionDetector areaCompositionDetector;
	
	
	public GeomOneOuterStrategy(GeomUtil geomUtil) {
		this.geomUtil = geomUtil;
		this.areaCompositionDetector = new AreaCompositionDetector();
	}

	/**
	 * Return a string with the geom
	 * @param area
	 * @return
	 */
	public StringBuffer getGeom(Area area) throws NoWayValuesException {
		
		if(area.ways.size() == 0)
			throw new NoWayValuesException("Way Data not available.");
		
		StringBuffer sql = new StringBuffer();
		StringBuffer sqlInner = new StringBuffer();
		sql.append(DBConstants.SQL_MULTIPOLYGON_GEOM_START);
		sql.append(Constants.OPEN_BRACKET_DOUBLE);
		
		for(Entry<Way, WayRole> wayEntry : area.ways.entrySet()){
			if(areaCompositionDetector.contains(AreaCompositionDetector.OUTER_WAYROLES, wayEntry.getValue())){
				sql.append(Constants.OPEN_BRACKET);
				sql.append(geomUtil.getLonLatForGeom(wayEntry.getKey()));
				sql.append(Constants.CLOSE_BRACKET);
				sql.append(Constants.SPACER);
				continue;
			}
			else if(areaCompositionDetector.contains(AreaCompositionDetector.INNER_WAYROLES, wayEntry.getValue())){
				sqlInner.append(Constants.OPEN_BRACKET);
				sqlInner.append(geomUtil.getLonLatForGeom(wayEntry.getKey()));
				sqlInner.append(Constants.CLOSE_BRACKET);
				sqlInner.append(Constants.SPACER);
			}
		}
		
		// Remove the last Spacer from the sqlInner
		sqlInner.delete(sqlInner.length() - Constants.SPACER.length(), sqlInner.length());
		
		sql.append(sqlInner);
		sql.append(Constants.CLOSE_BRACKET);
		sql.append(DBConstants.SQL_GEOM_END);
		
		return sql;
	}

}

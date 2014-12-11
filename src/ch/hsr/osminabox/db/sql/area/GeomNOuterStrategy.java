package ch.hsr.osminabox.db.sql.area;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.geotools.geometry.jts.JTSFactoryFinder;

import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.entities.Area.WayRole;
import ch.hsr.osminabox.db.sql.Constants;
import ch.hsr.osminabox.db.sql.area.exceptions.NoWayValuesException;
import ch.hsr.osminabox.db.sql.util.GeomUtil;
import ch.hsr.osminabox.db.util.AreaCompositionDetector;
import ch.hsr.osminabox.db.DBConstants;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
/**
 * Create the geom for a Relation with N Outer and N inner Ways
 * @author jzimmerm
 *
 */
public class GeomNOuterStrategy implements GeomStrategy {

	private GeometryFactory geomFactory;
	private WKTReader WTKReader;
	private GeomUtil geomUtil;
	private AreaCompositionDetector areaCompositionDetector;
	
	private static Logger logger = Logger.getLogger(GeomNOuterStrategy.class);
	
	
	public GeomNOuterStrategy(GeomUtil geomUtil) {
		this.geomUtil = geomUtil;
		this.areaCompositionDetector = new AreaCompositionDetector();
		
		geomFactory = JTSFactoryFinder.getGeometryFactory(null);
		WTKReader = new WKTReader(geomFactory);
		
	}
	
	/**
	 * Return a string with the geom
	 * @param area
	 * @return
	 */
	public StringBuffer getGeom(Area area) throws NoWayValuesException{
		
		if(area.ways.size() == 0)
			throw new NoWayValuesException("Way Data not available.");
	
		HashMap<Long, Polygon> outerPolygons = new HashMap<Long, Polygon>();
		HashMap<Long, Polygon> innerPolygons = new HashMap<Long, Polygon>();
		//     <outer Polygon , associated inner Polygones>
		HashMap<Polygon, ArrayList<Polygon>> endPolygons = new HashMap<Polygon, ArrayList<Polygon>>();
		
		
		//Separate Inner from outer and create Polygones
		for(Entry<Way, WayRole> wayEntry : area.ways.entrySet()){
			
			Way way = wayEntry.getKey();
			WayRole role = wayEntry.getValue();
			
			LineString lineString = null;
			String line = null;
			try {
				//Create LineString
				line = createLineString(geomUtil.getLonLatForGeom(way)).toString();
				lineString = (LineString) WTKReader.read(line);
			} catch (ParseException e) {
				logger.error("Parse Error occured on Way with OSM Id: " + way.getOsmId());
				logger.info("LineString: "+ line);
				e.printStackTrace();
				throw new NoWayValuesException("Parse Error");
			}
			
			try{
				//Create the Ring
				LinearRing shell = geomFactory.createLinearRing(lineString.getCoordinates());
				if(areaCompositionDetector.contains(AreaCompositionDetector.OUTER_WAYROLES, role)){
					outerPolygons.put(way.getOsmId(), geomFactory.createPolygon(shell, new LinearRing[0]));
				}
				else if(areaCompositionDetector.contains(AreaCompositionDetector.INNER_WAYROLES, role)){
					innerPolygons.put(way.getOsmId(), geomFactory.createPolygon(shell, new LinearRing[0]));
				}
			}
			catch(IllegalArgumentException e){
				logger.error("Could not create a Ring out of Way with OSM Id: " + way.getOsmId());
				logger.error(e);
				throw new NoWayValuesException("Ring creation error.");
			}
		}
		
		//Compare inner with outer polygones
		for(Way way : area.ways.keySet()) {
			if(areaCompositionDetector.contains(AreaCompositionDetector.OUTER_WAYROLES, area.ways.get(way))){
				Polygon outer = outerPolygons.get(way.getOsmId());
				ArrayList<Polygon> arrayListInnerPolygones = new ArrayList<Polygon>();
				for(Polygon inner : innerPolygons.values()) {
					if(outer.contains(inner)) {
						arrayListInnerPolygones.add(inner);
					}
				}
				endPolygons.put(outer, arrayListInnerPolygones);
			}
		}
		
		//All Polygons have been associated.
		return createSQLScript(endPolygons);
	}
	
	private StringBuffer createSQLScript(HashMap<Polygon, ArrayList<Polygon>> data) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(DBConstants.SQL_MULTIPOLYGON_GEOM_START);
		buffer.append(Constants.OPEN_BRACKET);
		Set<Polygon> keySet = data.keySet();
		int rootIndex=0;
		for(Polygon outerPolygon : keySet) {
			buffer.append(Constants.OPEN_BRACKET_DOUBLE);
			Coordinate[] outerCords = outerPolygon.getCoordinates();
			int outerIndex =0;
			//Outer Polygon adding
			for(Coordinate cord : outerCords) {
				buffer.append(cord.x);
				buffer.append(Constants.LINE_SPACE);
				buffer.append(cord.y);
				outerIndex++;
				if(outerIndex < outerCords.length)
					buffer.append(Constants.SPACER);
			}
			buffer.append(Constants.CLOSE_BRACKET);
			//Inner ones
			ArrayList<Polygon> innerPolygons = data.get(outerPolygon);
			int innerIndex =0;
			for(Polygon innerPolygon : innerPolygons) {	
				if(innerIndex==0)
					buffer.append(Constants.SPACER);
				
				buffer.append(Constants.OPEN_BRACKET);
				Coordinate[] innerCords = innerPolygon.getCoordinates();
				int innerCordIndex =0;
				for(Coordinate cord : innerCords) {
					buffer.append(cord.x);
					buffer.append(Constants.LINE_SPACE);
					buffer.append(cord.y);
					innerCordIndex++;
					if(innerCordIndex < innerCords.length)
						buffer.append(Constants.SPACER);
				}
				buffer.append(Constants.CLOSE_BRACKET);
				innerIndex++;
				if(innerIndex < innerPolygons.size())
					buffer.append(Constants.SPACER);
			}
			buffer.append(Constants.CLOSE_BRACKET);
			rootIndex++;
			if(rootIndex < keySet.size()) 
				buffer.append(Constants.SPACER);
		}
		buffer.append(DBConstants.SQL_GEOM_END);
		
		return buffer;
	}
	
	
	/**
	 * Returns Geom for LineString geom
	 * @param nodes
	 * 			Nodes with filled lon / lat !
	 * @return
	 */
	private StringBuffer createLineString(StringBuffer buffer) {
		StringBuffer tmp = new StringBuffer();
		tmp.append("LINESTRING(");
		tmp.append(buffer);
		tmp.append(Constants.CLOSE_BRACKET);
		return tmp;
		
	}
	
}

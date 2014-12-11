package ch.hsr.osminabox.db.handling;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.db.dbdefinition.Database;
import ch.hsr.osminabox.db.differentialupdate.UpdateAreaHandlingStrategy;
import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.AreaComposition;
import ch.hsr.osminabox.db.handlingstrategy.AreaHandlingStrategy;
import ch.hsr.osminabox.db.initialimport.InitialAreaHandlingStrategy;
import ch.hsr.osminabox.db.sql.area.GeomNOuterStrategy;
import ch.hsr.osminabox.db.sql.area.GeomOneOuterStrategy;
import ch.hsr.osminabox.db.sql.area.GeomOnlyOuterStrategy;
import ch.hsr.osminabox.db.sql.area.GeomStrategy;
import ch.hsr.osminabox.db.sql.util.DBUtil;
import ch.hsr.osminabox.db.sql.util.GeomUtil;
import ch.hsr.osminabox.db.util.AreaCompositionDetector;
import ch.hsr.osminabox.db.util.DataInitialisation;
import ch.hsr.osminabox.schemamapping.xml.MappingType;
/**
 * Handling areas
 * @author jzimmerm
 *
 */
public class AreaHandler {
	
	private static Logger logger = Logger.getLogger(AreaHandler.class);
	
	protected DBUtil dbUtil;
	private ApplicationContext context;
	private DataInitialisation init;
	protected GeomUtil geomUtil;
	protected AreaCompositionDetector composition;
	private Map<AreaComposition, GeomStrategy> geomStrategy;
	protected AreaHandlingStrategy insertStrategy;
	protected AreaHandlingStrategy updateStrategy;
	
		
	public AreaHandler(ApplicationContext context, Database dbStructure) {
		this.context = context;
		dbUtil = new DBUtil(dbStructure);
		init = new DataInitialisation();
		geomUtil = new GeomUtil();
		composition = new AreaCompositionDetector();
		
		insertStrategy = new InitialAreaHandlingStrategy(dbStructure);
		updateStrategy = new UpdateAreaHandlingStrategy(dbStructure);
		
		geomStrategy = new HashMap<AreaComposition, GeomStrategy>();
		geomStrategy.put(AreaComposition.ONLY_OUTER, new GeomOnlyOuterStrategy(geomUtil));
		geomStrategy.put(AreaComposition.ONE_OUTER_N_INNER, new GeomOneOuterStrategy(geomUtil));
		geomStrategy.put(AreaComposition.N_INNER_N_OUTER, new GeomNOuterStrategy(geomUtil));
	}
	
	/**
	 * Create the initial insert SQL-Statements for Areas.
	 *  
	 * @param areas 
	 * @return
	 */
	public HashMap<String, StringBuffer> createInsertStatements(List<Area> areas) {
		 HashMap<String, StringBuffer> statements = init.initiateHashMap(context.getConfigService().getTablesOfGeomType(MappingType.MULTIPOLYGON));
		
		for(Area area : areas)
			insertStrategy.addArea(area, statements, geomStrategy.get(composition.detect(area)));
		
		logger.debug("Creating INSERT statements for mapped Tables and " + areas.size() + " Areas.");
		
		return dbUtil.addEndTags(statements);		
	}
	
	/**
	 * Create the update SQL-Statements for Areas.
	 * 
	 * @param areas
	 * @return
	 */
	public HashMap<String, StringBuffer> createUpdateStatements(List<Area> areas) {
		 HashMap<String, StringBuffer> statements = init.initiateHashMap(context.getConfigService().getTablesOfGeomType(MappingType.MULTIPOLYGON));
		
		for(Area area : areas)
			updateStrategy.addArea(area, statements, geomStrategy.get(composition.detect(area)));
		
		logger.debug("Creating UPDATE statements for mapped Tables and " + areas.size() + " Areas.");
		
		return statements;	
	}
}

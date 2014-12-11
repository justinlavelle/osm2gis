package ch.hsr.osminabox.db.handling;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.db.DBConstants;
import ch.hsr.osminabox.db.dbdefinition.Database;
import ch.hsr.osminabox.db.differentialupdate.UpdateWayHandlingStrategy;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.handlingstrategy.WayHandlingStrategy;
import ch.hsr.osminabox.db.initialimport.InitialWayHandlingStrategy;
import ch.hsr.osminabox.db.sql.util.DBUtil;
import ch.hsr.osminabox.db.util.DataInitialisation;
import ch.hsr.osminabox.db.util.DiffType;
import ch.hsr.osminabox.schemamapping.xml.MappingType;

/**
 * Way Handling
 * @author jzimmerm
 *
 */
public class WayHandler {
	
	private static Logger logger = Logger.getLogger(WayHandler.class);
	
	protected WayHandlingStrategy insertStrategy;
	protected WayHandlingStrategy updateStrategy;
	
	protected DataInitialisation init;
	protected DBUtil dbUtil;
	
	private ApplicationContext context;
	
	public WayHandler(ApplicationContext context, Connection connection, Database dbStructure) {
		this.context = context;
		init = new DataInitialisation();
		dbUtil = new DBUtil(dbStructure);
		
		insertStrategy = new InitialWayHandlingStrategy(connection, dbStructure);
		updateStrategy = new UpdateWayHandlingStrategy(dbStructure);
	}
	
	/**
	 * Create the initial insert SQL-Statements for Ways.
	 * 
	 * @param ways
	 * @return
	 */
	public HashMap<String, StringBuffer> createInsertStatements(List<Way> ways) {
		HashMap<String, StringBuffer> statements = init.initiateHashMap(context.getConfigService().getTablesOfGeomType(MappingType.LINESTRING));
		
		for (Way way : ways)
			insertStrategy.addWay(way, statements);
		
		logger.debug("Creating INSERT statements for mapped Tables and " + ways.size() + " Ways.");
		
		return dbUtil.addEndTags(statements);
	}
	
	/**
	 * Create the update SQL-Statements for Ways.
	 * 
	 * @param ways
	 * @return
	 */
	public HashMap<String, StringBuffer> createUpdateStatements(List<Way> ways) {
		HashMap<String, StringBuffer> statements = init.initiateHashMap(context.getConfigService().getTablesOfGeomType(MappingType.LINESTRING));
		
		for (Way way : ways)
			updateStrategy.addWay(way, statements);
		
		logger.debug("Creating UPDATE statements for mapped Tables and " + ways.size() + " Ways.");
		
		return statements;
	}
	
	/**
	 * Create the initial insert SQL-Statements for Ways.
	 * 
	 * @param ways
	 * @return
	 */
	public HashMap<String, StringBuffer> createTempInsertStatements(List<Way> ways, DiffType diffType) {
		HashMap<String, StringBuffer> statements = new HashMap<String, StringBuffer>();
		statements.put(DBConstants.WAY_TEMP, new StringBuffer());
		
		for (Way way : ways) 
			insertStrategy.addTemp(way, diffType, statements);
		
		logger.debug("Creating INSERT statements for " + DBConstants.WAY_TEMP + " Table and " + ways.size() + " Ways (DiffType: " + diffType + ").");
		
		return dbUtil.addEndTags(statements);
	}

	

}

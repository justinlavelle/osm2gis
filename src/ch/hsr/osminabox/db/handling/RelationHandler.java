package ch.hsr.osminabox.db.handling;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.db.DBConstants;
import ch.hsr.osminabox.db.dbdefinition.Database;
import ch.hsr.osminabox.db.differentialupdate.UpdateRelationHandlingStrategy;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.handlingstrategy.RelationHandlingStrategy;
import ch.hsr.osminabox.db.initialimport.InitialRelationHandlingStrategy;
import ch.hsr.osminabox.db.sql.util.DBUtil;
import ch.hsr.osminabox.db.util.DataInitialisation;
import ch.hsr.osminabox.db.util.DiffType;
import ch.hsr.osminabox.schemamapping.xml.MappingType;
/**
 * Handling for Relations
 * @author jzimmerm
 *
 */
public class RelationHandler {
	
	private static Logger logger = Logger.getLogger(RelationHandler.class); 
	
	protected DataInitialisation init;
	protected DBUtil dbUtil;
	protected ApplicationContext context;
	
	protected RelationHandlingStrategy insertStrategy;
	protected RelationHandlingStrategy updateStrategy;
	
	public RelationHandler(ApplicationContext context, Database dbStructure) {
		this.context = context;
		dbUtil = new DBUtil(dbStructure);
		init = new DataInitialisation();
		
		insertStrategy = new InitialRelationHandlingStrategy(dbStructure);
		updateStrategy = new UpdateRelationHandlingStrategy(dbStructure);
	}
	
	/**
	 * Creates the SQL Statement (Temp Tables only) for the given Relations
	 * @param relations
	 * @return
	 */
	public HashMap<String, StringBuffer> createTempStatements(List<Relation> relations, DiffType diffType) {
		HashMap<String, StringBuffer> statements = new HashMap<String, StringBuffer>();
		statements.put(DBConstants.RELATION_TEMP, new StringBuffer());
		statements.put(DBConstants.RELATION_MEMBER_TEMP, new StringBuffer());
		
		for(Relation relation : relations ) {
			insertStrategy.addTemp(relation, diffType, statements);
			insertStrategy.addTempMembers(relation, statements);
		}
		
		logger.debug("Creating INSERT statements for " + DBConstants.RELATION_TEMP + " and " + DBConstants.RELATION_MEMBER_TEMP + " Tables and " + relations.size() + " Relations (DiffType: " + diffType + ").");
		
		return dbUtil.addEndTags(statements);
	}
	
	/**
	 * Creates the SQL Statements for the given Relations
	 * @param relations
	 * @return
	 */
	public HashMap<String, StringBuffer> createInsertStatements(List<Relation> relations) {
		HashMap<String, StringBuffer> statements = init.initiateHashMap(context.getConfigService().getTablesOfGeomType(MappingType.RELATION));
		
		for(Relation relation : relations )
			insertStrategy.addRelation(relation, statements);	
		
		logger.debug("Creating INSERT statements for mapped Tables and " + relations.size() + " Relations.");
		
		return dbUtil.addEndTags(statements);
	}
	
	/**
	 * Creates the SQL Join Statements for the given Relations
	 * @param relations
	 * @return
	 */
	public HashMap<String, StringBuffer> createJoinStatements(List<Relation> relations) {
		HashMap<String, StringBuffer> statements = init.initiateHashMap(context.getConfigService().getMappingJoinTables());
		
		for(Relation relation : relations)
			insertStrategy.addJoins(relation, statements);
		
		logger.debug("Creating INSERT statements for mapped Join Tables and " + relations.size() + " Relations.");
		
		return dbUtil.addEndTags(statements);
	}
	
	/**
	 * Creates the SQL Update Statements for the given Relations
	 * @param relations
	 * @return
	 */
	public HashMap<String, StringBuffer> createUpdateStatements(List<Relation> relations){
		HashMap<String, StringBuffer> statements = init.initiateHashMap(context.getConfigService().getTablesOfGeomType(MappingType.RELATION));
		
		for(Relation relation : relations )
			updateStrategy.addRelation(relation, statements);
		
		logger.debug("Creating UPDATE statements for mapped Tables and " + relations.size() + " Relations.");
		
		return statements;
	}
}

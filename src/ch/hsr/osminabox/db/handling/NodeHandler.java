package ch.hsr.osminabox.db.handling;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.db.DBConstants;
import ch.hsr.osminabox.db.dbdefinition.Database;
import ch.hsr.osminabox.db.differentialupdate.UpdateNodeHandlingStrategy;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.handlingstrategy.NodeHandlingStrategy;
import ch.hsr.osminabox.db.initialimport.InitialNodeHandlingStrategy;
import ch.hsr.osminabox.db.sql.util.DBUtil;
import ch.hsr.osminabox.db.util.DataInitialisation;
import ch.hsr.osminabox.schemamapping.xml.MappingType;

/**
 * Handling of Nodes for Initial and Differential Update
 * @author m2huber
 *
 */
public class NodeHandler {
	
	private static Logger logger = Logger.getLogger(NodeHandler.class);
	
	protected NodeHandlingStrategy insertStrategy;
	protected NodeHandlingStrategy updateStrategy;
	
	private DataInitialisation init;
	protected DBUtil dbUtil;
	protected ApplicationContext context;
	
	public NodeHandler(ApplicationContext context, Database dbStructure) {
		this.context = context;
		init = new DataInitialisation();
		dbUtil = new DBUtil(dbStructure);
		
		insertStrategy = new InitialNodeHandlingStrategy(dbStructure);
		updateStrategy = new UpdateNodeHandlingStrategy(dbStructure);
	}
	
	
	/**
	 * Create the initial insert SQL statements from the given list of Nodes.
	 * 
	 * @param nodes
	 * @return HashMap with Table name @See PostgresConstants as Keys
	 */
	public HashMap<String, StringBuffer> createInsertStatements(List<Node> nodes) {
		HashMap<String, StringBuffer> statements = init.initiateHashMap(context.getConfigService().getTablesOfGeomType(MappingType.POINT));
		
		for (Node node : nodes) 
			insertStrategy.addNode(node, statements);
		
		logger.debug("Creating INSERT statements for mapped Tables and " + nodes.size() + " Nodes.");
		
		return dbUtil.addEndTags(statements);		
	}
	
	/**
	 * Create the update SQL statements from the given list of Nodes.
	 * 
	 * @param nodes
	 * @return HashMap with Table name @See PostgresConstants as Keys
	 */
	public HashMap<String, StringBuffer> createUpdateStatements(List<Node> nodes) {
		HashMap<String, StringBuffer> statements = init.initiateHashMap(context.getConfigService().getTablesOfGeomType(MappingType.POINT));
		
		for (Node node : nodes) 
			updateStrategy.addNode(node, statements);
		
		logger.debug("Creating UPDATE statements for mapped Tables and " + nodes.size() + " Nodes.");
		
		return statements;		
	}
	
	/**
	 * Create the insert SQL statements for the nodes_temp Table from the given list of Nodes.
	 * 
	 * @param nodes
	 * @return HashMap with Table name @See PostgresConstants as Keys
	 */
	public HashMap<String, StringBuffer> createTempInsertStatements(List<Node> nodes) {
		HashMap<String, StringBuffer> statements = new HashMap<String, StringBuffer>();
		statements.put(DBConstants.NODE_TEMP, new StringBuffer());
		
		for(Node node : nodes){
			insertStrategy.addTemp(node, statements);
		}
		
		logger.debug("Creating INSERT statements for " + DBConstants.NODE_TEMP + " Table and " + nodes.size() + " Nodes.");
		
		return dbUtil.addEndTags(statements);
	}
}

package ch.hsr.osminabox.db.initialimport;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.db.dbdefinition.Database;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.handlingstrategy.NodeHandlingStrategy;
import ch.hsr.osminabox.db.sql.Constants;
import ch.hsr.osminabox.db.sql.util.DBUtil;
import ch.hsr.osminabox.db.util.ValueConverter;
import ch.hsr.osminabox.db.DBConstants;
import ch.hsr.osminabox.db.XMLConstants;
import ch.hsr.osminabox.schemamapping.xml.Column;

/**
 * Create SQL Statements for Initial Insert of Nodes
 * @author m2huber
 *
 */
public class InitialNodeHandlingStrategy implements NodeHandlingStrategy{
	
	Logger logger = Logger.getLogger(InitialNodeHandlingStrategy.class);

	protected DBUtil dbUtil;
	protected ValueConverter valueConverter;
	
	public InitialNodeHandlingStrategy(Database dbStructure) {
		dbUtil = new DBUtil(dbStructure);
		valueConverter = new ValueConverter();
	}
	
	/**
	 * Creates the Insert SQL Script for each Table this node is mapped to.
	 * 
	 * @param node
	 * @return
	 */
	@Override
	public void addNode(Node node , Map<String, StringBuffer> statements) {
		
		for(String tableName : node.dbMappings.keySet()){
			try{
				StringBuffer buffer = statements.get(tableName);
				
				if(buffer == null)
					continue;
				
				Map<String, String> columns = new HashMap<String, String>();
				
				for(Column column : node.dbMappings.get(tableName))
					columns.put(column.getName(), valueConverter.convertValue(column.getValue(), node));
				
				if(buffer.length() <= 0)
					buffer.append(dbUtil.createInsertBegin(tableName));
				else
					dbUtil.checkAndAppendSpacer(buffer, Constants.SPACER);
					
				buffer.append(dbUtil.addInsertValues(tableName, columns));
			}
			catch(Exception e){
				logger.error("Exception while creating SQL-Insert for Node with OSM Id: " + node.getOsmId());
				e.printStackTrace();
				continue;
			}
		}
	}
	
	/**
	 * Creates the Insert SQL Script for the Temp Table for this node.
	 * 
	 * @param node
	 * @return
	 */
	@Override
	public void addTemp(Node node , Map<String, StringBuffer> statements) {
		StringBuffer buffer = statements.get(DBConstants.NODE_TEMP);
		if(buffer == null)
			return;

		Map<String, String> columns = new HashMap<String, String>();
		columns.put(DBConstants.ATTR_OSM_ID, valueConverter.convertValue(XMLConstants.ATTRIBUTE_ID, node));
		columns.put(DBConstants.ATTR_LASTCHANGE, valueConverter.convertValue(XMLConstants.ATTRIBUTE_TIMESTAMP, node));
		columns.put(DBConstants.ATTR_NODE_TEMP_LAT, valueConverter.convertValue(XMLConstants.ATTRIBUTE_LATITUDE, node));
		columns.put(DBConstants.ATTR_NODE_TEMP_LON, valueConverter.convertValue(XMLConstants.ATTRIBUTE_LONGITUDE, node));
		
		if (buffer.length() <= 0) 
			buffer.append(dbUtil.createInsertBegin(DBConstants.NODE_TEMP));
		else
			buffer.append(Constants.SPACER);
		
		buffer.append(dbUtil.addInsertValues(DBConstants.NODE_TEMP, columns));
	}
}

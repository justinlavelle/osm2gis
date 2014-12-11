package ch.hsr.osminabox.db.initialimport;
/**
 * Handling of Way-Tags for Inital Import
 * 
 * @author m2huber
 */
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;


import ch.hsr.osminabox.db.dbdefinition.Database;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.handlingstrategy.WayHandlingStrategy;
import ch.hsr.osminabox.db.sql.Constants;
import ch.hsr.osminabox.db.sql.util.DBUtil;
import ch.hsr.osminabox.db.util.DiffType;
import ch.hsr.osminabox.db.util.ValueConverter;
import ch.hsr.osminabox.db.DBConstants;
import ch.hsr.osminabox.db.XMLConstants;
import ch.hsr.osminabox.schemamapping.xml.Column;

public class InitialWayHandlingStrategy implements WayHandlingStrategy{
	
	protected DBUtil dbUtil;
	protected ValueConverter valueConverter;

	public InitialWayHandlingStrategy(Connection connection, Database dbStructure) {
		dbUtil = new DBUtil(dbStructure);
		valueConverter = new ValueConverter();
	}
	
	/**
	 * @param way
	 */
	public void addWay(Way way, Map<String, StringBuffer> statements) {
		for(String tableName : way.dbMappings.keySet()){
			StringBuffer buffer = statements.get(tableName);
			
			if(buffer == null)
				continue;		
			
			Map<String, String> columns = new HashMap<String, String>();
			for(Column column : way.dbMappings.get(tableName))
				columns.put(column.getName(), valueConverter.convertValue(column.getValue(), way));
			
			if(buffer.length() <= 0)
				buffer.append(dbUtil.createInsertBegin(tableName));
			else
				dbUtil.checkAndAppendSpacer(buffer, Constants.SPACER);
				
			buffer.append(dbUtil.addInsertValues(tableName, columns));
		}
	}
	
	public void addTemp(Way way, DiffType diffType, Map<String, StringBuffer> statements) {
		StringBuffer buffer = statements.get(DBConstants.WAY_TEMP);
		
		Map<String, String> columns = new HashMap<String, String>();
		columns.put(DBConstants.ATTR_OSM_ID, valueConverter.convertValue(XMLConstants.ATTRIBUTE_ID, way));
		columns.put(DBConstants.ATTR_LASTCHANGE, valueConverter.convertValue(XMLConstants.ATTRIBUTE_TIMESTAMP, way));
		columns.put(DBConstants.ATTR_KEYVALUE, valueConverter.convertValue(XMLConstants.TAGS_ALL, way));
		columns.put(DBConstants.ATTR_WAY_TEMP_NODES, valueConverter.convertValue(XMLConstants.ND_ALL, way));
		columns.put(DBConstants.ATTR_WAY_TEMP_USEDBYRELATIONS, valueConverter.convertValue("false", way));
		columns.put(DBConstants.ATTR_WAY_TEMP_DIFFTYPE, diffType.toString());
		
		if (buffer.length() <= 0) 
			buffer.append(dbUtil.createInsertBegin(DBConstants.WAY_TEMP));
		else
			buffer.append(Constants.SPACER);

		
		buffer.append(dbUtil.addInsertValues(DBConstants.WAY_TEMP, columns));
	}

	
}


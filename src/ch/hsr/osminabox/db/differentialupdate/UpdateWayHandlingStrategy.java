package ch.hsr.osminabox.db.differentialupdate;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.db.dbdefinition.Database;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.handlingstrategy.WayHandlingStrategy;
import ch.hsr.osminabox.db.sql.Constants;
import ch.hsr.osminabox.db.sql.util.DBUtil;
import ch.hsr.osminabox.db.util.DiffType;
import ch.hsr.osminabox.db.util.ValueConverter;
import ch.hsr.osminabox.schemamapping.xml.Column;

public class UpdateWayHandlingStrategy implements WayHandlingStrategy{
	
	Logger logger = Logger.getLogger(UpdateWayHandlingStrategy.class);
	
	protected DBUtil dbUtil;
	protected ValueConverter valueConverter;
	
	public UpdateWayHandlingStrategy(Database dbStructure) {
		dbUtil = new DBUtil(dbStructure);
		valueConverter = new ValueConverter();
	}

	@Override
	public void addWay(Way way, Map<String, StringBuffer> statements) {
		for(String tableName : way.dbMappings.keySet()){
			try{
				StringBuffer buffer = statements.get(tableName);
				
				if(buffer == null)
					continue;
				
				Map<String, String> columns = new HashMap<String, String>();
				
				for(Column column : way.dbMappings.get(tableName))
					columns.put(column.getName(), valueConverter.convertValue(column.getValue(), way));
				
				buffer.append(dbUtil.createUpdateBegin(tableName));					
					
				buffer.append(dbUtil.addUpdateValues(tableName, columns));
				
				buffer.append(dbUtil.addWhere(way.getOsmId()));
				
				dbUtil.checkAndAppendSpacer(buffer, Constants.SEMICOLON);
			}
			catch(Exception e){
				logger.error("Exception while creating SQL-Update for Way with OSM Id: " + way.getOsmId());
				e.printStackTrace();
				continue;
			}
		}		
	}

	@Override
	public void addTemp(Way way, DiffType diffType, Map<String, StringBuffer> statements) {
	}
}

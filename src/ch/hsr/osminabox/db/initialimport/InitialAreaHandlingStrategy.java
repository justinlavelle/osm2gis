package ch.hsr.osminabox.db.initialimport;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.db.dbdefinition.Database;
import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.handlingstrategy.AreaHandlingStrategy;
import ch.hsr.osminabox.db.sql.Constants;
import ch.hsr.osminabox.db.sql.area.GeomStrategy;
import ch.hsr.osminabox.db.sql.area.exceptions.NoWayValuesException;
import ch.hsr.osminabox.db.sql.util.DBUtil;
import ch.hsr.osminabox.db.util.ValueConverter;
import ch.hsr.osminabox.schemamapping.xml.Column;

public class InitialAreaHandlingStrategy implements AreaHandlingStrategy{
	
	private static Logger logger = Logger.getLogger(InitialAreaHandlingStrategy.class);
	
	protected DBUtil dbUtil;
	protected ValueConverter valueConverter;
	
	public InitialAreaHandlingStrategy(Database dbStructure){
		dbUtil = new DBUtil(dbStructure);
		valueConverter = new ValueConverter();
	}

	@Override
	public void addArea(Area area, Map<String, StringBuffer> statements, GeomStrategy geom) {
		for(String tableName : area.dbMappings.keySet()){
			try{
				StringBuffer buffer = statements.get(tableName);
				
				if(buffer == null)
					continue;
				
				Map<String, String> columns = new HashMap<String, String>();
				for(Column column : area.dbMappings.get(tableName))
					columns.put(column.getName(), valueConverter.convertValue(column.getValue(), area, geom));
				
				if(buffer.length() <= 0)
					buffer.append(dbUtil.createInsertBegin(tableName));
				else
					dbUtil.checkAndAppendSpacer(buffer, Constants.SPACER);
					
				buffer.append(dbUtil.addInsertValues(tableName, columns));
			}
			catch(NoWayValuesException e){
				if(logger.isDebugEnabled())
					e.printStackTrace();
			}
		}
	}
}

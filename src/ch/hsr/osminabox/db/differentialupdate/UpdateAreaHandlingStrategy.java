package ch.hsr.osminabox.db.differentialupdate;

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

public class UpdateAreaHandlingStrategy implements AreaHandlingStrategy{
	
	private static Logger logger = Logger.getLogger(UpdateAreaHandlingStrategy.class);
	
	protected DBUtil dbUtil;
	protected ValueConverter valueConverter;
	
	public UpdateAreaHandlingStrategy(Database dbStructure){
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
				
				buffer.append(dbUtil.createUpdateBegin(tableName));
				
				buffer.append(dbUtil.addUpdateValues(tableName, columns));
				
				buffer.append(dbUtil.addWhere(area.getOsmId()));
				
				dbUtil.checkAndAppendSpacer(buffer, Constants.SEMICOLON);
			}
			catch(NoWayValuesException e){
				if(logger.isDebugEnabled())
					e.printStackTrace();
			}
		}
	}

}

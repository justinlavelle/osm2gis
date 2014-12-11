package ch.hsr.osminabox.db.differentialupdate;

import java.util.HashMap;
import java.util.Map;

import ch.hsr.osminabox.db.dbdefinition.Database;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.handlingstrategy.RelationHandlingStrategy;
import ch.hsr.osminabox.db.sql.Constants;
import ch.hsr.osminabox.db.sql.util.DBUtil;
import ch.hsr.osminabox.db.util.DiffType;
import ch.hsr.osminabox.db.util.ValueConverter;
import ch.hsr.osminabox.schemamapping.xml.Column;

public class UpdateRelationHandlingStrategy implements RelationHandlingStrategy{
	
	protected DBUtil dbUtil;
	protected ValueConverter valueConverter;
	
	public UpdateRelationHandlingStrategy(Database dbStructure){
		dbUtil = new DBUtil(dbStructure);
		valueConverter = new ValueConverter();
	}

	@Override
	public void addJoins(Relation relation,
			Map<String, StringBuffer> statements) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addRelation(Relation relation, Map<String, StringBuffer> statements) {
		for(String tableName : relation.dbMappings.keySet()){
			StringBuffer buffer = statements.get(tableName);
			
			if(buffer == null)
				continue;
			
			Map<String, String> columns = new HashMap<String, String>();
			for(Column column : relation.dbMappings.get(tableName).mappingColumns)
				columns.put(column.getName(), valueConverter.convertValue(column.getValue(), relation));
			
			buffer.append(dbUtil.createUpdateBegin(tableName));
			
			buffer.append(dbUtil.addUpdateValues(tableName, columns));
			
			buffer.append(dbUtil.addWhere(relation.getOsmId()));
			
			dbUtil.checkAndAppendSpacer(buffer, Constants.SEMICOLON);
		}
	}

	@Override
	public void addTemp(Relation relation, DiffType diffType,
			Map<String, StringBuffer> statements) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addTempMembers(Relation relation,
			Map<String, StringBuffer> statements) {
		// TODO Auto-generated method stub
		
	}

}

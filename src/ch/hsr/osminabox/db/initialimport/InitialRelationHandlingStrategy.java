package ch.hsr.osminabox.db.initialimport;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ch.hsr.osminabox.db.DBConstants;
import ch.hsr.osminabox.db.XMLConstants;
import ch.hsr.osminabox.db.dbdefinition.Database;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.RelationMappingEntry;
import ch.hsr.osminabox.db.entities.RelationMember;
import ch.hsr.osminabox.db.handlingstrategy.RelationHandlingStrategy;
import ch.hsr.osminabox.db.sql.Constants;
import ch.hsr.osminabox.db.sql.util.DBUtil;
import ch.hsr.osminabox.db.util.DiffType;
import ch.hsr.osminabox.db.util.ValueConverter;
import ch.hsr.osminabox.schemamapping.xml.Column;
import ch.hsr.osminabox.schemamapping.xml.RelatedTable;
/**
 * Create SQL Statements for Initial Relation Handling (Inserts)
 * @author m2huber
 *
 */
public class InitialRelationHandlingStrategy implements RelationHandlingStrategy {
	
	protected DBUtil dbUtil;
	protected ValueConverter valueConverter;
	
	public InitialRelationHandlingStrategy(Database dbStructure) {
		 dbUtil = new DBUtil(dbStructure);
		 valueConverter = new ValueConverter();
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
			
			if(buffer.length() <= 0)
				buffer.append(dbUtil.createInsertBegin(tableName));
			else
				dbUtil.checkAndAppendSpacer(buffer, Constants.SPACER);
				
			buffer.append(dbUtil.addInsertValues(tableName, columns));
		}
	}
	
	@Override
	public void addTemp(Relation relation, DiffType diffType, Map<String, StringBuffer> statements) {
		StringBuffer buffer = statements.get(DBConstants.RELATION_TEMP);

		Map<String, String> columns = new HashMap<String, String>();
		columns.put(DBConstants.ATTR_OSM_ID, valueConverter.convertValue(XMLConstants.ATTRIBUTE_ID, relation));
		columns.put(DBConstants.ATTR_LASTCHANGE, valueConverter.convertValue(XMLConstants.ATTRIBUTE_TIMESTAMP, relation));
		columns.put(DBConstants.ATTR_RELATION_TEMP_MEMBER, valueConverter.convertValue(XMLConstants.MEMBERS_ALL, relation));
		columns.put(DBConstants.ATTR_KEYVALUE, valueConverter.convertValue(XMLConstants.TAGS_ALL, relation));
		columns.put(DBConstants.ATTR_RELATION_TEMP_DIFFTYPE, diffType.toString());
		
		if (buffer.length() <= 0) 
			buffer.append(dbUtil.createInsertBegin(DBConstants.RELATION_TEMP));
		else
			buffer.append(Constants.SPACER);
		
		buffer.append(dbUtil.addInsertValues(DBConstants.RELATION_TEMP, columns));
	}
	
	@Override
	public void addTempMembers(Relation relation, Map<String, StringBuffer> statements){
		StringBuffer buffer = statements.get(DBConstants.RELATION_MEMBER_TEMP);
		
		for(RelationMember member : relation.members){
			Map<String, String> columns = new HashMap<String, String>();
			columns.put(DBConstants.ATTR_RELATION_MEMBER_TEMP_RELATION_OSM_ID, valueConverter.convertValue(XMLConstants.ATTRIBUTE_ID, relation));
			columns.put(DBConstants.ATTR_RELATION_MEMBER_TEMP_MEMBER_OSM_ID, valueConverter.convertValue(XMLConstants.MEMBER_REF, member));
			columns.put(DBConstants.ATTR_RELATION_MEMBER_TEMP_TYPE, valueConverter.convertValue(XMLConstants.MEMBER_TYPE, member));
			columns.put(DBConstants.ATTR_RELATION_MEMBER_TEMP_ROLE, valueConverter.convertValue(XMLConstants.MEMBER_ROLE, member));
			
			if (buffer.length() <= 0) 
				buffer.append(dbUtil.createInsertBegin(DBConstants.RELATION_MEMBER_TEMP));
			else
				buffer.append(Constants.SPACER);
			
			buffer.append(dbUtil.addInsertValues(DBConstants.RELATION_MEMBER_TEMP, columns));
			
		}
	}
	
	@Override
	public void addJoins(Relation relation, Map<String, StringBuffer> statements) {
		
		// Every dbMapping...
		for(RelationMappingEntry mappingEntry : relation.dbMappings.values()){
			
			// Every relatedTable (which has exactly 1 JoinTable) in this dbMapping...
			for(RelatedTable relatedTable : mappingEntry.relatedTables){
				
				StringBuffer buffer = statements.get(relatedTable.getJoinTable().getName());
				
				if(buffer == null)
					continue;
				
				// Every RelationMember found in this relatedTable needs to be inserted in the Join Table...
				for(Entry<Integer, RelationMember> idToMember : mappingEntry.dbIdsToMember.get(relatedTable.getName()).entrySet()){
					int dbMemberId = idToMember.getKey();
					RelationMember member = idToMember.getValue();
					
					Map<String, String> columns = new HashMap<String, String>();
					for(Column column : relatedTable.getJoinTableColumns().getColumn()){
						columns.put(column.getName(), valueConverter.convertValue(column.getValue(), mappingEntry.relationDbId, dbMemberId, member));
					}
					
					if(buffer.length() <= 0)
						buffer.append(dbUtil.createInsertBegin(relatedTable.getJoinTable().getName()));
					else
						dbUtil.checkAndAppendSpacer(buffer, Constants.SPACER);
						
					buffer.append(dbUtil.addInsertValues(relatedTable.getJoinTable().getName(), columns));
				}				
			}
		}
	}
}

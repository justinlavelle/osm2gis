package ch.hsr.osminabox.db.differentialupdate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.db.DBConstants;
import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.OSMEntity;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.RelationMappingEntry;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.sql.Constants;
import ch.hsr.osminabox.schemamapping.ConfigService;
import ch.hsr.osminabox.schemamapping.xml.MappingType;
import ch.hsr.osminabox.schemamapping.xml.RelatedTable;

/**
 * Delete OSM Entities by Differential Update
 * @author m2huber, jzimmerm
 *
 */
public class DeleteOSMEntites {
	
	private static Logger logger = Logger.getLogger(DeleteOSMEntites.class);
	
	private static final String TABLENAME = "[table]";
	private static final String PKCOLUMN = "[pkcolumn]";
	private static final String deleteSql = "DELETE FROM " + TABLENAME + " WHERE " + PKCOLUMN + " IN " + Constants.OPEN_BRACKET;

	private ConfigService config;
	
	public DeleteOSMEntites(ConfigService config){
		this.config = config;
	}
	
	/**
	 * Delete the given Nodes from all Node-Tables.
	 * @param nodes
	 * @return Delete SQL Statements in a HashMap
	 */
	public HashMap<String, StringBuffer> deleteNodesFromAllTables(List<Node> nodes) {		
		Set<String> tables = config.getTablesOfGeomType(MappingType.POINT);
		
		return deleteFromAllTables(nodes, tables);
	}
	
	/**
	 * Delete the given Ways from all Way- and Area-Tables.
	 * @param ways
	 * @return Delete SQL Statements in a HashMap
	 */
	public HashMap<String, StringBuffer> deleteWaysFromAllTables(List<Way> ways) {		
		//TODO: Only delete from Multipolygon-Tables if the entry's origin is a Way (not a Relation).
		
		Set<String> tables = config.getTablesOfGeomType(MappingType.LINESTRING);
		tables.addAll(config.getTablesOfGeomType(MappingType.MULTIPOLYGON));
		
		return deleteFromAllTables(ways, tables);
	}
	
	/**
	 * Delete the given Relations from all Area- and Relation-Tables
	 * @param relations
	 * @return Delete SQL Statements in a HashMap
	 */
	public HashMap<String, StringBuffer> deleteAreasFromAllTables(List<Area> areas){
		Set<String> tables = config.getTablesOfGeomType(MappingType.MULTIPOLYGON);
		
		return deleteFromAllTables(areas, tables);		
	}
	
	/**
	 * Delete the given Relations from all Area- and Relation-Tables
	 * @param relations
	 * @return Delete SQL Statements in a HashMap
	 */
	public HashMap<String, StringBuffer> deleteRelationsFromAllTables(List<Relation> relations){
		//TODO: Only delete from Multipolygon-Tables if the entry's origin is a Relation (not a Way).
		
		Set<String> tables = config.getTablesOfGeomType(MappingType.MULTIPOLYGON);
		tables.addAll(config.getTablesOfGeomType(MappingType.RELATION));
		
		return deleteFromAllTables(relations, tables);		
	}
	
	/**
	 * Creates the SQL Statements to delete every OSM Entity from the Tables it is mapped to.
	 * @param entities
	 * @return
	 */
	public HashMap<String, StringBuffer> deleteByDbMappings(List<? extends OSMEntity> entities){
		HashMap<String, StringBuffer> statements = new HashMap<String, StringBuffer>();
		
		for(OSMEntity entity : entities){
			for(String table : entity.dbMappings.keySet()){
				StringBuffer statement = statements.get(table);
				if(statement == null){
					statement = initDeleteStatement(table, DBConstants.ATTR_OSM_ID);
					statements.put(table, statement);
				}
				statement.append(entity.getOsmId() + Constants.COMMA);
			}
		}
		
		finalizeSqlStatements(statements);
		
		return statements;	
	}
	
	/**
	 * Creates the SQL Statements to delete every OSM Entity in every given Table.
	 * @param entities
	 * @param tables
	 * @return
	 */
	private HashMap<String, StringBuffer> deleteFromAllTables(List<? extends OSMEntity> entities, Set<String> tables){
		HashMap<String, StringBuffer> statements = new HashMap<String, StringBuffer>();
		
		for(String table : tables){
			for(OSMEntity entity : entities){
				StringBuffer statement = statements.get(table);
				if(statement == null){
					statement = initDeleteStatement(table, DBConstants.ATTR_OSM_ID);
					statements.put(table, statement);
				}
				statement.append(entity.getOsmId() + Constants.COMMA);
			}
		}
		
		finalizeSqlStatements(statements);
		
		return statements;
	}
	
	/**
	 * Deletes all the Join Entries in the DB for the given Relations.
	 * @param list
	 */
	public Map<String, StringBuffer> deleteJoinEntries(List<Relation> relations) {
		
		logger.debug("Deleteing all Join Table entries for " + relations. size() + ". They are completely recreated after.");
		
		Map<String, StringBuffer> statements = new HashMap<String, StringBuffer>();
		
		// Create a delete SQL Statement for every Join Table
		for(Relation relation : relations){
			for(Entry<String, RelationMappingEntry> entry : relation.dbMappings.entrySet()){
				for(RelatedTable relatedTable : entry.getValue().relatedTables){
					StringBuffer statement = statements.get(relatedTable.getJoinTable().getName());
					if(statement == null){
						statement = initDeleteStatement(relatedTable.getJoinTable().getName(), 
														config.getReferenceColumnName(relatedTable.getJoinTable().getName(), relatedTable.getName()));
						
						statements.put(relatedTable.getJoinTable().getName(), statement);
					}
					statement.append(relation.getOsmId() + Constants.COMMA);
				}			
			}
		}
		
		finalizeSqlStatements(statements);
		
		return statements;		
	}	
	
	/**
	 * Initializes an SQL DeleteStatement for the given Table and the PrimaryKey Column.
	 * Statement must be closed with a Constants.CLOSE_BRACKET after appending all IDs.
	 * 
	 * @param table
	 * @param pkColumn
	 * @return
	 */
	private StringBuffer initDeleteStatement(String table, String pkColumn){
		String deleteInit = deleteSql.replaceAll(Pattern.quote(TABLENAME), table);
		deleteInit = deleteInit.replaceAll(Pattern.quote(PKCOLUMN), pkColumn);
		return new StringBuffer(deleteInit);
	}
	
	/**
	 * Finalizes all the given Statements by removing the last COMMA and adding a CLOSE_BRACKET and SEMICOLON
	 * @param statements
	 */
	private void finalizeSqlStatements(Map<String, StringBuffer> statements){
		for(StringBuffer statement : statements.values()){
			if(statement != null && statement.length() > 0){
				
				if(Constants.COMMA.equals(Character.toString(statement.charAt(statement.length() - 1))))
					statement.deleteCharAt(statement.length() - 1);
				
				statement.append(Constants.CLOSE_BRACKET + Constants.SEMICOLON);
			}
		}
	}
}

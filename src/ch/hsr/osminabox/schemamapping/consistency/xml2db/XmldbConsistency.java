package ch.hsr.osminabox.schemamapping.consistency.xml2db;


import java.sql.ResultSetMetaData;
import java.util.LinkedList;
import java.util.List;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.db.DBConstants;
import ch.hsr.osminabox.db.dbdefinition.Column;
import ch.hsr.osminabox.db.dbdefinition.DataType;
import ch.hsr.osminabox.db.dbdefinition.Database;
import ch.hsr.osminabox.db.dbdefinition.Table;
import ch.hsr.osminabox.schemamapping.exceptions.NoSchemaDefAndMappingConfigXMLException;
import ch.hsr.osminabox.schemamapping.xml.DstColumn;
import ch.hsr.osminabox.schemamapping.xml.DstJoinTableDef;
import ch.hsr.osminabox.schemamapping.xml.MappingType;
import ch.hsr.osminabox.schemamapping.xml.DstSchemaDef;
import ch.hsr.osminabox.schemamapping.xml.DstTableDef;
import ch.hsr.osminabox.schemamapping.xml.Mapping;
import ch.hsr.osminabox.schemamapping.xml.SrcToDstMappings;


/**
 * The Class XmldbConsistency.
 * 
 * @author ameier
 */
public class XmldbConsistency {
	
	/** The context. */
	private ApplicationContext context;
	
	/** The data types. */
	private List<DataType> dataTypes;
	
	private String message="";
	
	/**
	 * Instantiates a new xmldb consistency.
	 * 
	 * @param context the context
	 */ 
	public XmldbConsistency(ApplicationContext context){
		this.context = context;
		generatePostgresDataType();
	}
	
	/**
	 * Start consistency check.
	 * 
	 * @param theXMLDef the the xml def
	 * 
	 * @return the dB change action
	 */
	public DBChangeAction startConsistencyCheck(DstSchemaDef theXMLDef){
		Database database = context.getDBService().getDBStructure();
		DBChangeAction.ChangeAction changeAction = DBChangeAction.ChangeAction.DO_NOTHING;
		message = "";
		
		changeAction = checkDstTableDef(theXMLDef, database, changeAction);
		if (changeAction == DBChangeAction.ChangeAction.DELETE_DB_CREATE_ALL){
			return new DBChangeAction(database,changeAction,message);
		}
		
		//Check Join Tables
		changeAction = checkJoinTables(theXMLDef, database, changeAction);
		
		//Return Result
		return new DBChangeAction(database,changeAction,message);
		//TODO: wieder wechseln!!!!!!!!!
		//return new DBChangeAction(database,DBChangeAction.ChangeAction.DELETE_DB_CREATE_ALL,message);
		//return new DBChangeAction(database,DBChangeAction.ChangeAction.DO_NOTHING,message);
	}

	private DBChangeAction.ChangeAction checkDstTableDef(
			DstSchemaDef theXMLDef, Database database,
			DBChangeAction.ChangeAction changeAction) {
		for (Object xmlDstTableDef : theXMLDef.getDstTableDefOrDstTableDefUserDefined()) {
			if (xmlDstTableDef instanceof DstTableDef) {
				DstTableDef inherittedXMLTable = null;
				
				inherittedXMLTable = context.getConfigService().getInheritedTable(theXMLDef, ((DstTableDef) xmlDstTableDef).getInherits());
				
				String tablemessage="";
					
				if (context.getConfigService().getMappingTables().indexOf(((DstTableDef)xmlDstTableDef).getName())>-1){
					if (!checkRequiredFieldsInMappingTable((DstTableDef)xmlDstTableDef,inherittedXMLTable)){
						changeAction = DBChangeAction.ChangeAction.MISSING_REQUIRED_FIELDS_IN_MAPPING_CONF;
						message="One of the required field "+DBConstants.ATTR_REQUIRED+" was not found in mapping-config table: "+((DstTableDef)xmlDstTableDef).getName();
						tablemessage=message;
					}
				}
				
				boolean tableFound = false;
				for (Table tableinDatabase : database.getTables()){
					if (tableinDatabase.getTablename().toLowerCase().equals(((DstTableDef)xmlDstTableDef).getName().toLowerCase())){
						tableFound = true;
						changeAction = checkColumnsInTable(database, changeAction, ((DstTableDef)xmlDstTableDef).getDstColumn(),	inherittedXMLTable, tableinDatabase);
						if (changeAction == DBChangeAction.ChangeAction.DELETE_DB_CREATE_ALL){
							return changeAction;
						}
					}
				}
				
				if (!tableFound){
					Table newTable = new Table(((DstTableDef)xmlDstTableDef).getName(),true,tablemessage);
					System.out.println("New table in configuration: "+newTable.getTablename());
					for (DstColumn dc : ((DstTableDef)xmlDstTableDef).getDstColumn()){
						
						int isNullable = 	dc.isNotNull() != null ? 
											dc.isNotNull() ? 
												ResultSetMetaData.columnNoNulls : 
												ResultSetMetaData.columnNullable : 
											ResultSetMetaData.columnNullable;
												
						newTable.addColumn(new Column(dc.getName(),dc.getType(), "", true, dc.isPrimaryKey(), isNullable));
					}
					database.addTable(newTable);
					changeAction = DBChangeAction.ChangeAction.CREATE_NEW_TABLES;
				}
			}
		}
		return changeAction;
	}
	
	private DBChangeAction.ChangeAction checkJoinTables(
			DstSchemaDef theXMLDef, Database database,
			DBChangeAction.ChangeAction changeAction) {
		for (DstJoinTableDef xmlDstJoinTableDef : theXMLDef.getDstJoinTableDef()) {

				String tablemessage="";
				
				boolean tableFound = false;
				for (Table tableinDatabase : database.getTables()){
					if (tableinDatabase.getTablename().toLowerCase().equals(xmlDstJoinTableDef.getName().toLowerCase())){
						tableFound = true;
						changeAction = checkColumnsInTable(database, changeAction, xmlDstJoinTableDef.getDstColumn(),	null, tableinDatabase);
						if (changeAction == DBChangeAction.ChangeAction.DELETE_DB_CREATE_ALL){
							return changeAction;
						}
					}
				}
				
				if (!tableFound){
					Table newTable = new Table(xmlDstJoinTableDef.getName(),true,tablemessage);
					System.out.println("New table in configuration: "+newTable.getTablename());
					for (DstColumn dc : xmlDstJoinTableDef.getDstColumn()){
						
						int isNullable = 	dc.isNotNull() != null ? 
											dc.isNotNull() ? 
												ResultSetMetaData.columnNoNulls : 
												ResultSetMetaData.columnNullable : 
											ResultSetMetaData.columnNullable;
												
						newTable.addColumn(new Column(dc.getName(),dc.getType(), "", true, dc.isPrimaryKey(), isNullable));
					}
					database.addTable(newTable);
					changeAction = DBChangeAction.ChangeAction.CREATE_NEW_TABLES;
				}
		}
		return changeAction;
	}
	
	private DBChangeAction.ChangeAction checkColumnsInTable(Database database,
			DBChangeAction.ChangeAction changeAction,
			List<DstColumn> columns, DstTableDef inherittedXMLTable,
			Table tableinDatabase) {
		
		for (DstColumn xmlColumn : columns){	
			boolean columnFound = false;
			
			for (Column columnindatabase : tableinDatabase.getColumns()){
				String xmlColumnType = xmlColumn.getType().toLowerCase();
				if (xmlColumn.getType().indexOf("(") > 0){
					xmlColumnType = xmlColumnType.substring(0,xmlColumnType.indexOf("("));
				}
				if ((columnindatabase.getName().toLowerCase().equals(xmlColumn.getName().toLowerCase()))
						&&(isDataTypeEqual(columnindatabase.getType().toLowerCase(), xmlColumnType))){
					columnFound = true;
					break;
				}
			}
			if ((!columnFound)&&(inherittedXMLTable != null)){
				for (Column columnindatabase : tableinDatabase.getColumns()){
					String xmlColumnType = xmlColumn.getType().toLowerCase();
					if (xmlColumn.getType().indexOf("(") > 0){
						xmlColumnType = xmlColumnType.substring(0,xmlColumnType.indexOf("("));
					}
					if ((columnindatabase.getName().toLowerCase().equals(xmlColumn.getName().toLowerCase()))
							&&(isDataTypeEqual(columnindatabase.getType().toLowerCase(), xmlColumnType))){
						columnFound = true;
						break;
					}
				}
			}
			
			if (!columnFound){
				message = "Column "+xmlColumn.getName()+"("+xmlColumn.getType()+")"+" was not found!";
				changeAction = DBChangeAction.ChangeAction.DELETE_DB_CREATE_ALL;
				
				
				int isNullable = 	xmlColumn.isNotNull() != null ? 
						xmlColumn.isNotNull() ? 
							ResultSetMetaData.columnNoNulls : 
							ResultSetMetaData.columnNullable : 
						ResultSetMetaData.columnNullable;

				tableinDatabase.addColumn(new Column(xmlColumn.getName(),xmlColumn.getType(), "", true, xmlColumn.isPrimaryKey(), isNullable));
				
				return changeAction;
			}
		}
		return changeAction;
	}


	/**
	 * Check required fields in mapping table.
	 * 
	 * @param xmlDstTableDef the xml dst table def
	 * @param inherittedXMLTable the inheritted xml table
	 * 
	 * @return true, if successful
	 */
	private boolean checkRequiredFieldsInMappingTable(DstTableDef xmlDstTableDef, DstTableDef inherittedXMLTable) {
		
		for (int i = 0;i<DBConstants.ATTR_REQUIRED.length;i++){	
			boolean found = false;
			for (DstColumn dc : xmlDstTableDef.getDstColumn()){
				if(dc.getName().toLowerCase().equals(DBConstants.ATTR_REQUIRED[i].toLowerCase())){
					found = true;
					break;
				}
			}
			if ((!found)&&(inherittedXMLTable!=null)){
				for(DstColumn dc : inherittedXMLTable.getDstColumn()){
					if(dc.getName().toLowerCase().equals(DBConstants.ATTR_REQUIRED[i].toLowerCase())){
						found = true;
						break;
					}
				}
			}
			if (!found){
				if (((DBConstants.ATTR_REQUIRED[i].toLowerCase().equals(DBConstants.ATTR_GEOM.toLowerCase()))
						&& isGeomTypeReallyRequired(xmlDstTableDef.getName())) 
						|| (!DBConstants.ATTR_REQUIRED[i].toLowerCase().equals(DBConstants.ATTR_GEOM.toLowerCase())) 
						){
					System.err.println("Required field: "+DBConstants.ATTR_REQUIRED[i]+" was not found in table: "+xmlDstTableDef.getName());
					return false;
				}
			}
		}
		return true;
	}

	
	private boolean isGeomTypeReallyRequired(String tablename){
		try {
			SrcToDstMappings stdm = context.getConfigService().getSrcToDstMappings();
			for (Mapping m : stdm.getMapping()){
				if (m.getDstTable().getName().toLowerCase().equals(tablename)&&(!m.getType().equals(MappingType.RELATION))){
					return true;
				}
			}
		} catch (NoSchemaDefAndMappingConfigXMLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Checks if is data type equal.
	 * 
	 * @param datatype1 the datatype1
	 * @param datatype2 the datatype2
	 * 
	 * @return true, if is data type equal
	 */
	private boolean isDataTypeEqual(String datatype1, String datatype2){
		if (datatype1.toLowerCase().equals(datatype2.toLowerCase())){
			return true;
		}else{
			for (DataType dt : dataTypes){
				if (dt.getAliases().size() > 0){
					if (dt.getName().toLowerCase().equals(datatype1)){
						for(String dtAlias : dt.getAliases()){
							if (dtAlias.toLowerCase().equals(datatype2)){
								return true;
							}
						}
					}else if (dt.getName().toLowerCase().equals(datatype2)){
						for(String dtAlias : dt.getAliases()){
							if (dtAlias.toLowerCase().equals(datatype1)){
								return true;
							}
						}
					}else{
						for(String dtAlias : dt.getAliases()){
							if (dtAlias.toLowerCase().equals(datatype1)){
								for(String dtAlias1 : dt.getAliases()){
									if (dtAlias1.toLowerCase().equals(datatype2)){
										return true;
									}
								}
							}else if (dtAlias.toLowerCase().equals(datatype2)){
								for(String dtAlias1 : dt.getAliases()){
									if (dtAlias1.toLowerCase().equals(datatype1)){
										return true;
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Generate postgres data type.
	 */
	private void generatePostgresDataType(){
		dataTypes = new LinkedList<DataType>();
		
		DataType bigint = new DataType("bigint");
		bigint.addAlias("int8");
		
		DataType bigserial = new DataType("bigserial");
		bigserial.addAlias("int8");
		
		DataType bit = new DataType("bit");
		
		DataType bit_varying = new DataType("bit varying");
		bit_varying.addAlias("varbit");
		
		DataType boolean_ = new DataType("boolean");
		boolean_.addAlias("bool");
		
		DataType box = new DataType("box");
		
		DataType bytea = new DataType("bytea");
		
		DataType character_varying = new DataType("character varying");
		character_varying.addAlias("varchar");
		
		DataType character = new DataType("character");
		character.addAlias("char");
		
		DataType cidr = new DataType("cidr");

		DataType circle = new DataType("circle");

		DataType date = new DataType("date");
		
		DataType double_precision = new DataType("double precision");
		double_precision.addAlias("float8");
		
		DataType inet = new DataType("inet");
		
		DataType integer_ = new DataType("integer");
		integer_.addAlias("int");
		integer_.addAlias("int4");

		DataType interval = new DataType("interval");
		
		DataType line = new DataType("line");
		
		DataType lseg = new DataType("lseg");
		
		DataType macaddr = new DataType("macaddr");
		
		DataType money = new DataType("money");
		
		DataType numeric = new DataType("numeric");
		numeric.addAlias("decimal");
		
		DataType path = new DataType("path");
		
		DataType point = new DataType("point");
		
		DataType polygon = new DataType("polygon");
		
		DataType real = new DataType("real");
		real.addAlias("float4");
		
		DataType smallint = new DataType("smallint");
		smallint.addAlias("int2");
		
		DataType serial = new DataType("serial");
		serial.addAlias("int4");
		
		DataType text = new DataType("text");
		
		DataType time = new DataType("time");
		
		DataType timestamp = new DataType("timestamp");
		
		dataTypes.add(bigint);
		dataTypes.add(bigserial);
		dataTypes.add(bit);
		dataTypes.add(bit_varying);
		dataTypes.add(boolean_);
		dataTypes.add(box);
		dataTypes.add(bytea);
		dataTypes.add(character_varying);
		dataTypes.add(character);
		dataTypes.add(cidr);
		dataTypes.add(circle);
		dataTypes.add(date);
		dataTypes.add(double_precision);
		dataTypes.add(inet);
		dataTypes.add(integer_);
		dataTypes.add(interval);
		dataTypes.add(line);
		dataTypes.add(lseg);
		dataTypes.add(macaddr);
		dataTypes.add(money);
		dataTypes.add(numeric);
		dataTypes.add(path);
		dataTypes.add(point);
		dataTypes.add(polygon);
		dataTypes.add(real);
		dataTypes.add(smallint);
		dataTypes.add(serial);
		dataTypes.add(text);
		dataTypes.add(time);
		dataTypes.add(timestamp);
	}
	
	
}

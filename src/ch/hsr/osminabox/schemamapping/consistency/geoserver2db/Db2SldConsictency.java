package ch.hsr.osminabox.schemamapping.consistency.geoserver2db;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import ch.hsr.osminabox.context.ConfigConstants;
import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.db.dbdefinition.Column;
import ch.hsr.osminabox.db.dbdefinition.Database;
import ch.hsr.osminabox.db.dbdefinition.Table;
import ch.hsr.osminabox.schemamapping.consistency.geoserver2db.xml.datastore.DataStore;
import ch.hsr.osminabox.schemamapping.consistency.geoserver2db.xml.datastore.Entry;
import ch.hsr.osminabox.schemamapping.consistency.geoserver2db.xml.featuretypes.FeatureType;
import ch.hsr.osminabox.schemamapping.consistency.geoserver2db.xml.styles.BinaryComparisonOpType;
import ch.hsr.osminabox.schemamapping.consistency.geoserver2db.xml.styles.BinaryLogicOpType;
import ch.hsr.osminabox.schemamapping.consistency.geoserver2db.xml.styles.FeatureTypeStyle;
import ch.hsr.osminabox.schemamapping.consistency.geoserver2db.xml.styles.LiteralType;
import ch.hsr.osminabox.schemamapping.consistency.geoserver2db.xml.styles.NamedLayer;
import ch.hsr.osminabox.schemamapping.consistency.geoserver2db.xml.styles.PropertyNameType;
import ch.hsr.osminabox.schemamapping.consistency.geoserver2db.xml.styles.Rule;
import ch.hsr.osminabox.schemamapping.consistency.geoserver2db.xml.styles.StyledLayerDescriptor;
import ch.hsr.osminabox.schemamapping.consistency.geoserver2db.xml.styles.UserStyle;
import ch.hsr.osminabox.schemamapping.consistency.schemamappingfile.SchemamappingConsistency;
import ch.hsr.osminabox.schemamapping.consistency.xml2db.DBChangeAction;
import ch.hsr.osminabox.schemamapping.consistency.xml2db.XmldbConsistency;
import ch.hsr.osminabox.schemamapping.exceptions.NoSchemaDefAndMappingConfigXMLException;
import ch.hsr.osminabox.schemamapping.xml.Mapping;

/**
 * The Class Db2SldConsictency.
 * 
 * @author ameier
 */
public class Db2SldConsictency {
	
	/** The context. */
	private ApplicationContext context;

	/** The statistic file. */
	private ConsistencyReportFile reportFile;
	
	/** The geo server file. */
	private GeoServerFile geoServerFile;

	/** The hint counter. */
	private int hintCounter;
	
	/**
	 * Instantiates a new db2 sld consictency.
	 * 
	 * @param context the context
	 */
	public Db2SldConsictency(ApplicationContext context){
		this.context = context;
		geoServerFile = new GeoServerFile(context);
		reportFile = new ConsistencyReportFile();
	}
	
	/**
	 * Start sld consistency check.
	 */
	public void StartSLDConsistencyCheck(){
		
		reportFile = new SchemamappingConsistency(context).startSchemamappingConsistency();
		
		reportFile.writeHeadSummaryTdToDb();
		
		checkXML2DBConsistency();
		
		System.out.println("-------------------------------------------");
		
		reportFile.writeHeadSummaryFtToDb();

		checkGeoserver2DBConsistency();
		
		System.out.println("-------------------------------------------");

		reportFile.writeHeadSummaryMcToSLDTable();
		
		checkXML2GeoserverConsistency();

		System.out.println("-------------------------------------------");
		
		reportFile.saveStatisticFile();
	}

	/**
	 * Check geoserver2 db consistency.
	 */
	private void checkGeoserver2DBConsistency() {
		reportFile.writeToStatistic("\n\n\n");
		reportFile.writeToStatistic("\n3 Consistency check, GeoServer feature type (inc. SLD) to DB");
		reportFile.addLine(1);
		
		DataStore geoCatalog = geoServerFile.LoadGeoserverCatalog();
		
		if (checkGeoserverCatalog(geoCatalog)){
			checkFeatureTypes();
		}
	}

	/**
	 * Check feature types.
	 */
	private void checkFeatureTypes() {
		Database db = context.getDBService().getDBStructure();
		for (String featureTypeTableName : geoServerFile.getAllFeatureTypeNameInNamespace()){
			reportFile.writeToStatistic("\nFeature type: "+featureTypeTableName);
			reportFile.writeToStatistic("\n");
			System.out.println("Checking featureType "+featureTypeTableName);
			int errorCount = 0;
			if (!checkTablenameInDB(db,featureTypeTableName)){
				reportFile.writeToStatistic("\nTable "+featureTypeTableName+" was not found in DB!");
				errorCount++;
			}else{
				for (String columnName : geoServerFile.getColumnNameFromFeatureTypeSLD(featureTypeTableName)){
					if(!checkColumnInDB(db,featureTypeTableName,columnName)){
						reportFile.writeToStatistic("\nColumn "+columnName+" was not found in table "+featureTypeTableName+"!");
						errorCount++;
					}
				}
			}
			reportFile.writeToStatistic("\n"+errorCount+" error(s) found!");
			reportFile.addShortLine(1);
			
			reportFile.writeSummary(featureTypeTableName,String.valueOf(errorCount));
		}
		reportFile.addLine(1);
	}

	/**
	 * Check column in db.
	 * 
	 * @param db the db
	 * @param featureTypeTableName the feature type table name
	 * @param columnName the column name
	 * 
	 * @return true, if successful
	 */
	private boolean checkColumnInDB(Database db, String featureTypeTableName, String columnName) {
		if (db.getTable(featureTypeTableName) != null){
			return (db.getTable(featureTypeTableName).getColumn(columnName)!=null);
		}else{
			if (db.getView(featureTypeTableName)!=null){
				return (db.getView(featureTypeTableName).getColumn(columnName)!=null);
			}
		}
		return false;
	}

	/**
	 * Check tablename in db.
	 * 
	 * @param db the db
	 * @param tablename the tablename
	 * 
	 * @return true, if successful
	 */
	private boolean checkTablenameInDB(Database db,String tablename) {
		return db.tableOrViewExistsInDB(tablename);
	}

	/**
	 * Check xm l2 geoserver consistency.
	 */
	private void checkXML2GeoserverConsistency() {
		
		reportFile.writeToStatistic("\n\n\n");
		reportFile.writeToStatistic("\n4 Consistency check, mapping configurations to GeoServer (*.SLD)");
		reportFile.addLine(1);
		
		DataStore geoDataStore = geoServerFile.LoadGeoserverCatalog();
		
		if (checkGeoserverCatalog(geoDataStore)){
			checkMappingTables();
			checkMappingViews();
		}
	}

	/**
	 * Check mapping views.
	 */
	private void checkMappingViews() {
		reportFile.writeToStatistic("\n\nChecking views configuration against *.sld files.");
		reportFile.writeHeadSummaryMcToSLDView();
		
		for (String v : context.getConfigService().getMappingViews()){
			reportFile.addLine(1);
			reportFile.writeToStatistic("\nMappingview: "+v+
											 "\nSld-file    : "+geoServerFile.getSldFilePathOfTable(geoServerFile.getFeatureTypeFromTable(v)));
			reportFile.addShortLine(1);
			hintCounter = 0;
			
			checkTableAgainstSLD(v);
			
			reportFile.addShortLine(1);
			reportFile.writeToStatistic("\n"+hintCounter+" hint(s) found!");
			
			reportFile.addLine(1);
			reportFile.writeToStatistic("\n\n");
			
			reportFile.writeSummary(v,String.valueOf(hintCounter));
		}
	}

	/**
	 * Check mapping tables.
	 */
	private void checkMappingTables() {
		for (String mt : context.getConfigService().getMappingTables()){
			System.out.println("Checking mappingtable "+mt);
			
			reportFile.addLine(1);
			
			reportFile.writeToStatistic("\ndst_table_def in mapping configuration: "+mt+
											 "\nSLD-file    : "+geoServerFile.getSldFilePathOfTable(geoServerFile.getFeatureTypeFromTable(mt)));
			reportFile.addShortLine(1);
			
			hintCounter = 0;

			checkTableAgainstSLD(mt);
			
			reportFile.writeToStatistic("\n"+hintCounter+" hint(s) found!");
			
			reportFile.writeSummary(mt,String.valueOf(hintCounter));
			
			reportFile.addLine(1);
			
			reportFile.writeToStatistic("\n\n");
		}
	}
	
	/**
	 * Check table against sld.
	 * 
	 * @param tablename the tablename
	 */
	private void checkTableAgainstSLD(String tablename){
		FeatureType ft = geoServerFile.getFeatureTypeFromTable(tablename);
		if (ft != null){
			StyledLayerDescriptor sld = geoServerFile.LoadGeoserverStyles(geoServerFile.getSldFilePathOfTable(ft));
			if (sld != null){
				List<Mapping> tableMappings = context.getConfigService().getMappingsOfTable(tablename);
				if (tableMappings.size()>0){
					checkSldAgainstTableMappings(tableMappings,sld);
				}else{
					reportFile.writeToStatistic("\nHint: No <mapping> in mapping configuration found!");
					hintCounter++;
				}
			}else{
				reportFile.writeToStatistic("\nHint: No SLD file found!");
				hintCounter++;
			}
		}else{
			reportFile.writeToStatistic("\nHint: No feature type found on GeoServer!");
			hintCounter++;
		}
	}
	
	
	/**
	 * Check sld against table mappings.
	 * 
	 * @param tableMappings the table mappings
	 * @param sld the sld
	 */
	private void checkSldAgainstTableMappings(List<Mapping> tableMappings, StyledLayerDescriptor sld) {
		for (Mapping m : tableMappings){
			if (!checkMappingsAgainstRules(sld, m)){
				return;
			}
		}
	}


	/**
	 * Check mappings against rules.
	 * 
	 * @param sld the sld
	 * @param tableMapping the table mapping
	 * 
	 * @return true, if successful
	 */
	private boolean checkMappingsAgainstRules(StyledLayerDescriptor sld, Mapping tableMapping) {
		boolean ruleFound = false; 
		for (Object o : sld.getNamedLayerOrUserLayer()){
			if (o instanceof NamedLayer){
				for (Object o2 : ((NamedLayer) o).getNamedStyleOrUserStyle()){
					if (o2 instanceof UserStyle){
						for (FeatureTypeStyle fts : ((UserStyle)o2).getFeatureTypeStyle()){
							for (Rule r : fts.getRule()){
								if (r.getFilter() != null){
									ruleFound = checkRuleForMapping(r,tableMapping);
								}else{
									reportFile.writeToStatistic("\nHint: No <filter> found in SLD file!");
									hintCounter++;
									return false;
								}
								if (ruleFound){
									return true;
								}
							}
						}
					}
				}
			}
		}
		if (!ruleFound){
			reportFile.writeToStatistic(
							"\nHint: No <rule> in SLD file found, for mapping configuration:");
			reportFile.addMappingKeyValueToStatistic(tableMapping);
			reportFile.addShortLine(1);
			hintCounter++;
		}
		return true;
	}

	/**
	 * Check rule for mapping.
	 * 
	 * @param r the r
	 * @param m the m
	 * 
	 * @return true, if successful
	 */
	private boolean checkRuleForMapping(Rule r, Mapping m) {
			if (r.getFilter().getComparisonOps() != null){
				return checkMappingToComparisonOps(r, m);
			}else if (r.getFilter().getLogicOps() != null){					
				return checkMappingToLogicalOps(r, m);
			}else if (r.getFilter().getSpatialOps() != null){
				reportFile.writeToStatistic("SpatialOps in SLD will not be checked for consistency!");
				return false;
			}
			return false;
	}

	/**
	 * Check mapping to comparison ops.
	 * 
	 * @param r the r
	 * @param m the m
	 * 
	 * @return true, if successful
	 */
	private boolean checkMappingToComparisonOps(Rule r, Mapping m) {
		if (r.getFilter().getComparisonOps().getValue() instanceof BinaryComparisonOpType){
			BinaryComparisonOpType bcot = (BinaryComparisonOpType)r.getFilter().getComparisonOps().getValue();
					return checkExpression(m, bcot);
		}
		return false;
	}



	/**
	 * Check mapping to logical ops.
	 * 
	 * @param r the r
	 * @param m the m
	 * 
	 * @return true, if successful
	 */
	private boolean checkMappingToLogicalOps(Rule r, Mapping m) {
		if (r.getFilter().getLogicOps().getValue() instanceof BinaryLogicOpType){
			BinaryLogicOpType blot = (BinaryLogicOpType)r.getFilter().getLogicOps().getValue();
			for (Object o : blot.getComparisonOpsOrSpatialOpsOrLogicOps()){
				if (((JAXBElement<?>)o).getValue() instanceof BinaryComparisonOpType) {
					BinaryComparisonOpType bcot = (BinaryComparisonOpType)((JAXBElement<?>)o).getValue();
					return checkExpression(m,bcot);
				}
			}
		}
		return false;
	}

	
	/**
	 * Check expression.
	 * 
	 * @param m the m
	 * @param bcot the bcot
	 * 
	 * @return true, if successful
	 */
	private boolean checkExpression(Mapping m, BinaryComparisonOpType bcot) {
		String propertyname="";
		List<Object> literaltyp = new ArrayList<Object>();
		for (Object e :bcot.getExpression()){
			if (((JAXBElement<?>)e).getValue() instanceof PropertyNameType){
				PropertyNameType pnt = (PropertyNameType)((JAXBElement<?>)e).getValue();
				propertyname = pnt.getContent();
			}else if (((JAXBElement<?>)e).getValue() instanceof LiteralType){
				LiteralType lt = (LiteralType)((JAXBElement<?>)e).getValue();
				literaltyp = lt.getContent();
			}
		}
		//Check if a mapping column exists for this expression
		for (ch.hsr.osminabox.schemamapping.xml.Column c : m.getDstColumns().getColumn()){
			if ((c.getName().toLowerCase().equals(propertyname))&&
					literaltyp.indexOf(c.getValue())>-1){
				return true;
			}
		}
		
		return false;
	}	

	
	
	/**
	 * Check geoserver catalog.
	 * 
	 * @param geoDataStore the geo catalog
	 * 
	 * @return true, if successful
	 */
	private boolean checkGeoserverCatalog(DataStore geoDataStore) {
		boolean bolCatalogDBConfigFound = false;

			boolean hostFound = false;
			boolean databaseFound = false;
			for (Entry e : geoDataStore.getConnectionParameters().getEntry()){
				if ((e.getKey().toLowerCase().equals("host"))&&(e.getValue().toLowerCase().equals(context.getConfigParameter(ConfigConstants.CONF_DB_HOST).toLowerCase()))){
					hostFound=true;
				}else if ((e.getKey().toLowerCase().equals("database"))&&(e.getValue().toLowerCase().equals(context.getConfigParameter(ConfigConstants.CONF_DB_DATABASE).toLowerCase()))){
					databaseFound=true;
				}
			}
			if (hostFound&&databaseFound){
				bolCatalogDBConfigFound = true;
			}

		if (bolCatalogDBConfigFound){
			reportFile.writeToStatistic("\nDatastore name: "+geoServerFile.getGeoserverDataStoreID());
			reportFile.addShortLine(1);
			return true;
		}else{
			reportFile.writeToStatistic("\nHint: No datastore found for DB: "+context.getConfigParameter(ConfigConstants.CONF_DB_DATABASE)+" in workspace default.xml from the geoserver");
			return false;
		}
	}

	/**
	 * Check xm l2 db consistency.
	 */
	private void checkXML2DBConsistency() {
		try {
			
			reportFile.writeToStatistic("\n2 Consistency check, dst_schema_def in mapping configuration to DB");
			reportFile.addLine(1);
			
			DBChangeAction dbca = (new XmldbConsistency(context).startConsistencyCheck(context.getConfigService().getDstSchemaDef()));

			for (String tablename : dbca.getDsd().getTableNames()){
				System.out.println("Checking table "+tablename);
				Table t = dbca.getDsd().getTable(tablename);
				int iChangesCount = 0;
				reportFile.writeToStatistic("\nTable: "+t.getTablename());
				
				if (!t.getMessage().equals("")){
					reportFile.writeToStatistic("\n"+t.getMessage());
					iChangesCount++;
				}
				
				if (t.isNew()){
					reportFile.writeToStatistic("\nInconsistency: was not found in the DB!");
				    iChangesCount++;
				}else{
					for (Column c : t.getColumns()){
						if (c.isNew()){
							iChangesCount++;
							reportFile.writeToStatistic("\nInconsistency: Column name:"+c.getName()+" datatype:"+c.getType()+" was not found!");
						}
					}
				}
				reportFile.writeToStatistic("\n"+iChangesCount+" inconsistencies found!");
				
				reportFile.writeSummary(t.getTablename(),String.valueOf(iChangesCount));

				reportFile.addShortLine(1);
			}
			reportFile.addLine(1);
		} catch (NoSchemaDefAndMappingConfigXMLException e) {
			e.printStackTrace();
		}
	}
}

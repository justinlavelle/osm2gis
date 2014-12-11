package ch.hsr.osminabox.schemamapping.xml2ddl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Iterator;

import org.xml.sax.SAXException;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.context.ArgumentConstants;
import ch.hsr.osminabox.db.dbdefinition.Database;
import ch.hsr.osminabox.schemamapping.consistency.Utils;
import ch.hsr.osminabox.schemamapping.consistency.geoserver2db.ConsistencyReportFile;
import ch.hsr.osminabox.schemamapping.consistency.schemamappingfile.SchemamappingConsistency;
import ch.hsr.osminabox.schemamapping.consistency.xml2db.DBChangeAction;
import ch.hsr.osminabox.schemamapping.consistency.xml2db.XmldbConsistency;
import ch.hsr.osminabox.schemamapping.exceptions.NoSchemaDefAndMappingConfigXMLException;
import ch.hsr.osminabox.schemamapping.exceptions.XmlFileInvalidException;
import ch.hsr.osminabox.schemamapping.xml.DstColumn;
import ch.hsr.osminabox.schemamapping.xml.DstJoinTableDef;
import ch.hsr.osminabox.schemamapping.xml.DstSchemaDef;
import ch.hsr.osminabox.schemamapping.xml.DstTableDef;
import ch.hsr.osminabox.schemamapping.xml.DstViewDef;


// TODO: Auto-generated Javadoc
/**
 * The Class Xml2ddl
 * Generates a DDL for PostgreSQL from the mappingconfig.xml file
 * 
 * @author ameier
 */
public class Xml2ddl {


	/** The SQL-Comment of the addGeometry functions. */
	private String addGeometrySQL = "--Generated addGeometrySQL\n";
	
	/** The SQL-Comment of the views. */
	private String views = "\n--Generated views\n";
	
	/** The path to output file. */
	private String outputFile = "config/osm_create.sql";
	
	/** The context. */
	private ApplicationContext context;
	
	private XmlValidation xmlValidation;
	
	private Utils utils;
	
	/**
	 * Instantiates a new xml2ddl.
	 * 
	 * @param context the context
	 */
	public Xml2ddl(ApplicationContext context){
		this.context = context;
		xmlValidation = new XmlValidation();
		utils = new Utils();
	}
	
	/**
	 * Start generation.
	 * 
	 * @param pathToMappingconfigXMLFile the path to the mappingconfig xml file
	 * @param onlyViews this defines if only the views should be generated
	 * 
	 * @return true, if successful
	 */
	public boolean startGeneration(String pathToMappingconfigXMLFile, boolean onlyViews, boolean isInitialImport){
		try{
				
				xmlValidation.startValidation(pathToMappingconfigXMLFile);
				
				context.getConfigService().reloadXmlFile(pathToMappingconfigXMLFile);
				
				ConsistencyReportFile crf = new SchemamappingConsistency(context).startSchemamappingConsistency();
				if (crf.isHasErrors()){
					System.out.println("There are errors in the mappingconfig File:");
					System.out.println(crf.getDetailText());
					return false;
				}
				
				String strInput = "n";
				
				context.getConfigService().reloadXmlFile(pathToMappingconfigXMLFile);
				if (!onlyViews){
					DBChangeAction dbca = new XmldbConsistency(context).startConsistencyCheck(context.getConfigService().getDstSchemaDef());
					
					switch (dbca.getState()){
						case DO_NOTHING : { 
							System.out.println("No config changes!");
							return true; 
						}
						case CREATE_NEW_TABLES : {
							generateDDL(context.getConfigService().getDstSchemaDef(), outputFile,dbca.getDsd(),true,onlyViews);
							break;
						}
						case DELETE_DB_CREATE_ALL : {
							if (isInitialImport){
								generateDDL(context.getConfigService().getDstSchemaDef(), outputFile, dbca.getDsd(),false,onlyViews);							
								System.out.println("The consistency check has detected that your changes can only be applied");
								System.out.println("if all of the tables in your database will be deleted!");
								System.out.println("Do you want to continue?[y/n]");
								
								strInput = getYesOrNo();
								
								if (strInput.toLowerCase().equals("y")){
									context.getDBService().dropTables();
								}else{
									return false;
								}
								break;
							}else{
								System.out.println("The consistency check has detected that your database is inconsistent with your mappingconfig ("+pathToMappingconfigXMLFile+"). \n The update process stops. \n For more information run osm2gis --consistency.");
								return false;
							}
						}
						case MISSING_REQUIRED_FIELDS_IN_MAPPING_CONF :{
							System.err.println(dbca.getMessage());
							return false;
						}
					}
		
					System.out.println("The DDL is generated. Path: " + outputFile);
					System.out.println("You can customize the DDL.");
					System.out.println("Do you want to run the DDL now?[y/n]");
					strInput = getYesOrNo();
					
					if ((strInput.toLowerCase().equals("y")||onlyViews)){
						System.out.println("Creating Tables..");
						runDDL();
						System.out.println("Tables Created..");
						return true;
					}else{
						return false;
					}
				}else{
					generateDDL(context.getConfigService().getDstSchemaDef(), outputFile, null,false,onlyViews);
					System.out.println("Creating Views..");
					runDDL();
					System.out.println("Views Created..");
					return true;
				}
				

		}catch (XmlFileInvalidException e){
			System.err.println("Mappingconfig "+pathToMappingconfigXMLFile+" is Invalid!");
		} catch (NoSchemaDefAndMappingConfigXMLException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println("Could not load configurationfile: "+e);
		} catch (SAXException e) {
			e.printStackTrace();
		}
		return false;
	}

	protected String getYesOrNo() throws IOException {
		if(context.containsArgument(ArgumentConstants.OPT_GENERATE_DDL)){
			return context.getArgument(ArgumentConstants.OPT_GENERATE_DDL);
		}
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		String strInput;
		do{
			strInput = input.readLine();
		}
		while(!strInput.toLowerCase().equals("y") && !strInput.toLowerCase().equals("n"));
		return strInput;
	}



	/**
	 * Generate ddl.
	 * 
	 * @param theDef the the def
	 * @param outputfile the outputfile
	 * @param db the db
	 * @param onlyNew the only new
	 * @param onlyViews the only views
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void generateDDL(DstSchemaDef theDef, String outputfile, Database db, boolean onlyNew, boolean onlyViews) throws IOException {

			String result = "-- Generated DDL from xml2ddl (OSM-in-a-Box)\n"
					+ "-- Generated on " + utils.getActualDateTimeForLogEntry()
					+ "\n";

			if (!onlyViews){
				for (Object ob : theDef.getDstTableDefOrDstTableDefUserDefined()) {
					if (ob instanceof DstTableDef) {
						if ((onlyNew && (db.getTable(((DstTableDef)ob).getName()).isNew())) || (!onlyNew)){
							result = result + "\n" + getCreateTableSQL((DstTableDef) ob);
						}
					} else if (ob instanceof String) {
						result = result + "\n" + ((String) ob).replace("\t", "");
					}
				}
				
				result = result+"\n"+addGeometrySQL;
			}
			
			result = result+"\n--Generated Join-Tables";
			for (DstJoinTableDef ob : theDef.getDstJoinTableDef()){
				if ((onlyNew && (db.getTable(ob.getName()).isNew())) || (!onlyNew)){
					result = result + "\n" + getCreateJoinTableSQL(ob);
				}
			}
			
			result = result + "\ncommit;";

			for (DstViewDef ob : theDef.getDstViewDef()){
				views = views + "\n" + ob.getValue();
			}
			
			result = result+"\n"+views;
			
			
			String folder = outputFile.substring(0, outputfile.lastIndexOf('/'));
			if (!new File(folder).exists())
				new File(folder).mkdir();
			
			Writer fw = new FileWriter(outputfile);
			fw.write(result);
			fw.close();
	}


	/**
	 * Gets the creates the table sql.
	 * 
	 * @param dtd the dtd
	 * @param table the table
	 * 
	 * @return the creates the table sql
	 */
	private String getCreateTableSQL(DstTableDef dtd) {
		String result = "CREATE TABLE " + dtd.getName() + " (\n";
		String primaryKeys = "";
		boolean first = true;
		
		for (DstColumn column : dtd.getDstColumn()) {
			result = result + getTableColumnSQL(column,dtd.getName(),first);
			if ((column.isPrimaryKey()!=null)&&(column.isPrimaryKey()))
				primaryKeys= primaryKeys+column.getName()+",";
			
			if (first)
				first = false;
		}
		
		if (!primaryKeys.equals(""))
			primaryKeys = ",\n\tPRIMARY KEY ("+primaryKeys.substring(0,primaryKeys.length()-1)+")";
		
		result = result + primaryKeys + "\n)";
		
		if ((dtd.getInherits() != null) && (!dtd.getInherits().equals(""))) {
			result = result + " INHERITS (" + dtd.getInherits() + ")";
		}

		return result + ";\n";
	}
	
	
	private String getCreateJoinTableSQL(DstJoinTableDef djtd) {
		String result = "CREATE TABLE " + djtd.getName() + " (\n";
		String primaryKeys = "";
		Iterator<DstColumn> it = djtd.getDstColumn().iterator();
		boolean first = true;
		while (it.hasNext()) {
			DstColumn column = it.next();
			result = result + getTableColumnSQL(column,djtd.getName(),first);
			if ((column.isPrimaryKey()!=null)&&(column.isPrimaryKey()))
				primaryKeys= primaryKeys+column.getName()+",";
			
			if (first)
				first = false;
		}
		if (!primaryKeys.equals(""))
			primaryKeys = ",\n\tPRIMARY KEY ("+primaryKeys.substring(0,primaryKeys.length()-1)+")";
		
		return result+primaryKeys+"\n);\n";

	}
	
	public String getTableColumnSQL(DstColumn col, String tablename, boolean first){
		String result = "";
		if (col.getType().contains("geometry(")) {
			addGeometrySQL(tablename, col);
		} else {
			
			if (!first){
				result = result + ",\n";
			}
			
			// Fieldname
			result = result + "\t" + col.getName();
			
			// Fieldtype
			result = result + " " + col.getType();
			
			// Not-Null
			if (col.isNotNull() != null && col.isNotNull()) {
				result = result + " NOT NULL";
			}
			
			//references
			if (col.getReferences() != null && col.isPrimaryKey()){
				result = result + " REFERENCES "+col.getReferences()+" ON DELETE CASCADE";
			}
		}
		return result;
	}
	

	/**
	 * Adds the geometry sql.
	 * 
	 * @param tableName the table name
	 * @param dcol the dcol
	 */
	private void addGeometrySQL(String tableName, DstColumn dcol){
		addGeometrySQL = addGeometrySQL + "Select AddGeometryColumn"
							+"('"+tableName+"', '"+dcol.getName()+"', "
							+dcol.getType().replace("geometry(", "")+";\n";
	}
	

	/**
	 * Runs the generated ddl.
	 */
	private void runDDL(){
		context.getDBService().createDatabase();
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}
}

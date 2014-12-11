package ch.hsr.osminabox.context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Map.Entry;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import ch.hsr.osminabox.db.DBService;
import ch.hsr.osminabox.db.OSMDBService;
import ch.hsr.osminabox.db.downloading.EntityConsistencyService;
import ch.hsr.osminabox.schemamapping.ConfigServiceImpl;
import ch.hsr.osminabox.schemamapping.ConsistencyService;
import ch.hsr.osminabox.schemamapping.ConfigService;

import com.martiansoftware.jsap.JSAPResult;

/**
 * The Application Context contains all Config Parameters and Arguments for
 * the Running instance of the Application. It is used throughout the Application
 * to store global Information.
 * In addition it holds a Connection to the Database.
 * 
 * @author rhof
 */

public class ApplicationContextImpl implements ApplicationContext {
	
	private static String CONFIG_FILE = "config/osm2gis.properties";
	
	private static Logger logger = Logger.getLogger(ApplicationContextImpl.class);
	private Properties props = new Properties();
	
	/**
	 *	Stores the Arguments for the Current running Process 
	 */
	private JSAPResult arguments;
	
	/**
	 * 	Holds a Connection to the Database
	 */
	private DBService dbService;
	
	private EntityConsistencyService apiConsistencyService;
	
	private ConfigService configService;
	
	private ConsistencyService consistencyService;

	public ApplicationContextImpl(String configFile, String[] args) {
		CONFIG_FILE = configFile;
		initContext(args);
	}
	
	public ApplicationContextImpl(String[] args) {
		   
		initContext(args);
	}

	private void initContext(String[] args) throws FactoryConfigurationError {
		DOMConfigurator.configure("config/LogingConfiguration.xml");
		
		try {
				props.load(new FileInputStream(CONFIG_FILE));
			} catch (FileNotFoundException e) {
				logger.error("Configfile not found: " + CONFIG_FILE);
				System.exit(0);
			} catch (IOException e) {
				logger.error("Error while reading configfile: "+ CONFIG_FILE);
				System.exit(0);
			}
			
		arguments = ArgumentParser.parse(args);	
		//ArgumentHandler.updateConfigParameter(arguments, this);
		dbService = new OSMDBService(this);
		apiConsistencyService = null; // Will be initialized by the UpdateScheduler if needed.
		configService = new ConfigServiceImpl("",this);
		consistencyService = new ConsistencyService(this);
	}
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.context.IApplicationContext#getJSAPArguments()
	 */
	public JSAPResult getJSAPArguments(){
		return arguments;
	}
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.context.IApplicationContext#getDBService()
	 */
	public DBService getDBService(){
		return dbService;
	}	
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.context.IApplicationContext#setDBService(ch.hsr.osminabox.db.DBService)
	 */
	public void setDBService(DBService dbService){
		this.dbService = dbService;
	}
	
	public EntityConsistencyService getApiConsistencyService(){
		return this.apiConsistencyService;
	}
	
	public void setApiConsistencyService(EntityConsistencyService consistencyService){
		this.apiConsistencyService = consistencyService;
	}
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.context.IApplicationContext#getConfigService()
	 */
	public ConfigService getConfigService(){
		return configService;
	}
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.context.IApplicationContext#setConfigService(ch.hsr.osminabox.schemamapping.ConfigService)
	 */
	public void setConfigService(ConfigService cs){
		this.configService = cs;
	}
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.context.IApplicationContext#getConsistencyService()
	 */
	public ConsistencyService getConsistencyService() {
		return consistencyService;
	}

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.context.IApplicationContext#connectToDatabase()
	 */
	public void connectToDatabase(){
		dbService.connect(props.getProperty(ConfigConstants.CONF_DB_HOST), 
				props.getProperty(ConfigConstants.CONF_DB_PORT), 
				props.getProperty(ConfigConstants.CONF_DB_DATABASE), 
				props.getProperty(ConfigConstants.CONF_DB_USERNAME), 
				props.getProperty(ConfigConstants.CONF_DB_PW));
	}
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.context.IApplicationContext#isConnected()
	 */
	public boolean isConnected(){
		return dbService.isConnected();
	}
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.context.IApplicationContext#createTempTables()
	 */
	public boolean createTempTables(){
		return dbService.createTempTables();
	}	
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.context.IApplicationContext#removeTempTables()
	 */
	public void removeTempTables(){
		dbService.removeTempTables();
	}
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.context.IApplicationContext#setConfigParameter(java.lang.String, java.lang.String)
	 */
	public Object setConfigParameter(String name, String value){
		return props.setProperty(name, value);
	}
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.context.IApplicationContext#getConfigParameter(java.lang.String)
	 */
	public String getConfigParameter(String name){
		return props.getProperty(name);
	}
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.context.IApplicationContext#containsConfigParameter(java.lang.String)
	 */
	public boolean containsConfigParameter(String name){
		return props.containsKey(name);
	}
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.context.IApplicationContext#isSwitchOn(java.lang.String)
	 */
	public boolean isSwitchOn(String name){
		return arguments.getBoolean(name);
	}
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.context.IApplicationContext#getArgument(java.lang.String)
	 */
	public String getArgument(String name){
		return arguments.getString(name);
	}
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.context.IApplicationContext#containsArgument(java.lang.String)
	 */
	public boolean containsArgument(String name){
		return arguments.contains(name);
	}
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.context.IApplicationContext#save()
	 */
	public void save(){
		try {
			props.store(new FileOutputStream(CONFIG_FILE), "osm2gis properties");
		} catch (FileNotFoundException e) {
			logger.error("Configfile not found: " + CONFIG_FILE);
		} catch (IOException e) {
			logger.error("Error while saving configfile: "+ CONFIG_FILE);
		}
	}
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.context.IApplicationContext#refresh()
	 */
	public void refresh(){
		   props = new Properties();
		   try {
				props.load(new FileInputStream(CONFIG_FILE));
			} catch (FileNotFoundException e) {
				logger.error("Configfile not found: " + CONFIG_FILE);
				System.exit(0);
			} catch (IOException e) {
				logger.error("Error while reading configfile: "+ CONFIG_FILE);
				System.exit(0);
			} 
	}
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.context.IApplicationContext#debug()
	 */
	public void debug(){
		
		if(logger.isDebugEnabled()){
			logger.debug("*****ConfigFile*******");
			for(Entry<Object, Object> entry: props.entrySet()){
				logger.debug(entry.getKey() + "=" + entry.getValue());
			}
			logger.debug("*****EndConfigFile****");
		}
	}	
}

package ch.hsr.osminabox.context;

import ch.hsr.osminabox.db.DBService;
import ch.hsr.osminabox.schemamapping.ConsistencyService;
import ch.hsr.osminabox.schemamapping.ConfigService;

import com.martiansoftware.jsap.JSAPResult;

public interface ApplicationContext {

	public JSAPResult getJSAPArguments();

	public DBService getDBService();

	public void setDBService(DBService dbService);
	
	public ch.hsr.osminabox.db.downloading.EntityConsistencyService getApiConsistencyService();
	
	public void setApiConsistencyService(ch.hsr.osminabox.db.downloading.EntityConsistencyService consistencyService);

	public ConfigService getConfigService();

	public void setConfigService(ConfigService cs);

	public ConsistencyService getConsistencyService();

	public void connectToDatabase();

	public boolean isConnected();

	public boolean createTempTables();

	public void removeTempTables();

	public Object setConfigParameter(String name, String value);

	public String getConfigParameter(String name);

	public boolean containsConfigParameter(String name);

	public boolean isSwitchOn(String name);

	public String getArgument(String name);

	public boolean containsArgument(String name);

	/**
	 * 	Stores the Arguments and Config Parameters to the Applications Property File
	 */
	public void save();

	public void refresh();

	public void debug();

}
package ch.hsr.osminabox.schemamapping;

import ch.hsr.osminabox.context.ConfigConstants;
import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.schemamapping.consistency.geoserver2db.Db2SldConsictency;
import ch.hsr.osminabox.schemamapping.xml2ddl.Xml2ddl;

/**
 * Implementation of Config Service
 * 
 * @author ameier
 * 
 */
public class ConsistencyService {
	protected ApplicationContext context;
	
	protected Xml2ddl xml2ddl;
	
	protected Db2SldConsictency db2sldconsistency;

	public ConsistencyService(ApplicationContext context){
		this.context = context;
		xml2ddl = new Xml2ddl(this.context);
		db2sldconsistency = new Db2SldConsictency(this.context);
	}
	
	public boolean checkConsistencyForInitialImportAndUpdate(boolean isInitialImport){
		String xmlFilePath = getXMLConfigFilePath();
		if (xml2ddl.startGeneration(xmlFilePath, false, isInitialImport)){
			context.setConfigParameter(ConfigConstants.CONF_MAPPING_CONFIGFILE, xmlFilePath);
			context.save();
			return true;
		}
		return false;
	}

	protected String getXMLConfigFilePath() {
		return context.getConfigService().getActualConfigXmlFilePath();
	}
	
	public void checkWholeConsistency(){
		db2sldconsistency.StartSLDConsistencyCheck();
	}
	
	public void createViewsOnly(){
		xml2ddl.startGeneration(getXMLConfigFilePath(), true, true);
	}
}

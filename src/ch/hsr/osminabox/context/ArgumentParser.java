package ch.hsr.osminabox.context;

import org.apache.log4j.Logger;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;

/**
 * The ArgumentParser is responsible for Parsing the Arguments given to
 * the Application via command line. Its using the JSAP Library to Handle the
 * arguments.
 * 
 * @author rhof
 */

public class ArgumentParser {

	private static Logger logger = Logger.getLogger(ArgumentParser.class);
	
	public static JSAPResult parse(String[] args){
        JSAP jsap = new JSAP();
        
        try {
        	addSwitches(jsap);
			addScheduleOptions(jsap);
	        addDatabaseOptions(jsap);
	        addInitialImportOptions(jsap);
	        addAdditionalOptions(jsap);
		} catch (JSAPException e) {
			logger.error("Error Setting up the ArgumentParser");
			e.printStackTrace();
			return null;
		}
		
		if(logger.isDebugEnabled()){
			logger.debug(jsap.getUsage());
		}
		
        return jsap.parse(args);
	}
		

	private static void addAdditionalOptions(JSAP jsap) throws JSAPException {
		FlaggedOption optGenerateDDL = new FlaggedOption(ArgumentConstants.OPT_GENERATE_DDL)
	        .setStringParser(JSAP.STRING_PARSER)
	        .setRequired(false)
	        .setLongFlag(ArgumentConstants.OPT_GENERATE_DDL_LFLAG);
		jsap.registerParameter(optGenerateDDL);
	}


	private static void addInitialImportOptions(JSAP jsap) throws JSAPException {
		
		FlaggedOption optLat1 = new FlaggedOption(ArgumentConstants.OPT_LAT_MAX)
			.setStringParser(JSAP.STRING_PARSER)
	        .setRequired(false)
	        .setLongFlag(ArgumentConstants.OPT_LAT_MAX_LFLAG);

		FlaggedOption optLon1 = new FlaggedOption(ArgumentConstants.OPT_LON_MIN)
		.setStringParser(JSAP.STRING_PARSER)
        .setRequired(false)
        .setLongFlag(ArgumentConstants.OPT_LON_MIN_LFLAG);
		
		FlaggedOption optLat2 = new FlaggedOption(ArgumentConstants.OPT_LAT_MIN)
		.setStringParser(JSAP.STRING_PARSER)
        .setRequired(false)
        .setLongFlag(ArgumentConstants.OPT_LAT_MIN_LFLAG);
		
		FlaggedOption optLon2 = new FlaggedOption(ArgumentConstants.OPT_LON_MAX)
		.setStringParser(JSAP.STRING_PARSER)
        .setRequired(false)
        .setLongFlag(ArgumentConstants.OPT_LON_MAX_LFLAG);
		
		FlaggedOption optPlanetFile = new FlaggedOption(ArgumentConstants.OPT_PLANET_FILE)
	        .setStringParser(JSAP.STRING_PARSER)
	        .setRequired(false)
	        .setShortFlag(ArgumentConstants.OPT_PLANET_FILE_SFLAG)
	        .setLongFlag(ArgumentConstants.OPT_PLANET_FILE_LFLAG);
    
        FlaggedOption optPlanetUrl = new FlaggedOption(ArgumentConstants.OPT_PLANET_URL)
	        .setStringParser(JSAP.STRING_PARSER)
	        .setRequired(false)
	        .setShortFlag(ArgumentConstants.OPT_PLANET_URL_SFLAG)
	        .setLongFlag(ArgumentConstants.OPT_PLANET_URL_LFLAG);
        
        FlaggedOption optMapping = new FlaggedOption(ArgumentConstants.OPT_MAPPING)
		.setStringParser(JSAP.STRING_PARSER)
        .setRequired(false)
        .setShortFlag(ArgumentConstants.OPT_MAPPING_SFLAG)
	    .setLongFlag(ArgumentConstants.OPT_MAPPING_LFLAG);
        
        jsap.registerParameter(optLat1);
        jsap.registerParameter(optLon1);
        jsap.registerParameter(optLat2);
        jsap.registerParameter(optLon2);
        
        jsap.registerParameter(optPlanetFile);
        jsap.registerParameter(optPlanetUrl);
        jsap.registerParameter(optMapping);
	}

	private static void addDatabaseOptions(JSAP jsap) throws JSAPException {
		FlaggedOption optHost = new FlaggedOption(ArgumentConstants.OPT_HOST)
	        .setStringParser(JSAP.STRING_PARSER)
	        .setRequired(false)
	        .setShortFlag(ArgumentConstants.OPT_HOST_SFLAG)
	        .setLongFlag(ArgumentConstants.OPT_HOST_LFLAG);
		
		FlaggedOption optPort = new FlaggedOption(ArgumentConstants.OPT_PORT)
        .setStringParser(JSAP.INTEGER_PARSER)
        .setRequired(false)
        .setShortFlag(ArgumentConstants.OPT_PORT_SFLAG)
        .setLongFlag(ArgumentConstants.OPT_PORT_LFLAG);
	    
        FlaggedOption optDatabase = new FlaggedOption(ArgumentConstants.OPT_DATABASE)
	        .setStringParser(JSAP.STRING_PARSER)
	        .setRequired(false)
	        .setShortFlag(ArgumentConstants.OPT_DATABASE_SFLAG)
	        .setLongFlag(ArgumentConstants.OPT_DATABASE_LFLAG);
    
        FlaggedOption optUser = new FlaggedOption(ArgumentConstants.OPT_USER)
	        .setStringParser(JSAP.STRING_PARSER)
	        .setRequired(false)
	        .setShortFlag(ArgumentConstants.OPT_USER_SFLAG)
	        .setLongFlag(ArgumentConstants.OPT_USER_LFLAG);
        
        FlaggedOption optPassword = new FlaggedOption(ArgumentConstants.OPT_PASSWORD)
	        .setStringParser(JSAP.STRING_PARSER)
	        .setRequired(false)
	        .setShortFlag(ArgumentConstants.OPT_PASSWORD_SFLAG)
	        .setLongFlag(ArgumentConstants.OPT_PASSWORD_LFLAG);
        
        jsap.registerParameter(optHost);
        jsap.registerParameter(optPort);
        jsap.registerParameter(optDatabase);
        jsap.registerParameter(optUser);
        jsap.registerParameter(optPassword);
        
	}
	


	private static void addScheduleOptions(JSAP jsap) throws JSAPException {
		FlaggedOption optFrequency = new FlaggedOption(ArgumentConstants.OPT_FREQUENCY)
	        .setStringParser(JSAP.STRING_PARSER)
	        .setRequired(false)
	        .setShortFlag(ArgumentConstants.OPT_FREQUENCY_SFLAG)
	        .setLongFlag(ArgumentConstants.OPT_FREQUENCY_LFLAG);
        
        FlaggedOption optUpdateRoot = new FlaggedOption(ArgumentConstants.OPT_UPDATE_ROOT)
	        .setStringParser(JSAP.URL_PARSER)
	        .setRequired(false)
	        .setShortFlag(ArgumentConstants.OPT_UPDATE_ROOT_SFLAG)
	        .setLongFlag(ArgumentConstants.OPT_UPDATE_ROOT_LFLAG);

        FlaggedOption optInitialDiff = new FlaggedOption(ArgumentConstants.OPT_INITIAL_DIFF)
	        .setStringParser(JSAP.STRING_PARSER)
	        .setRequired(false)
	        .setShortFlag(ArgumentConstants.OPT_INITIAL_DIFF_SFLAG)
	        .setLongFlag(ArgumentConstants.OPT_INITIAL_DIFF_LFLAG);
        
        FlaggedOption optInitialDiffReplicate = new FlaggedOption(ArgumentConstants.OPT_INITIAL_DIFF_REPLICATE)
        	.setStringParser(JSAP.STRING_PARSER)
	        .setRequired(false)
	        .setShortFlag(ArgumentConstants.OPT_INITIAL_DIFF_REPLICATE_SFLAG)
	        .setLongFlag(ArgumentConstants.OPT_INITIAL_DIFF_REPLICATE_LFLAG);
        
        jsap.registerParameter(optFrequency);
        jsap.registerParameter(optUpdateRoot);
        jsap.registerParameter(optInitialDiff);
        jsap.registerParameter(optInitialDiffReplicate);
        
	}
	
	private static void addSwitches(JSAP jsap) throws JSAPException {
		
		Switch swtHelp = new Switch(ArgumentConstants.SWT_HELP)
						.setLongFlag(ArgumentConstants.SWT_HELP_LFLAG);
		
		Switch swtConfig = new Switch(ArgumentConstants.SWT_CONFIG)
        				.setShortFlag(ArgumentConstants.SWT_CONFIG_SFLAG)
        				.setLongFlag(ArgumentConstants.SWT_CONFIG_LFLAG);
        
        Switch swtInitialImport = new Switch(ArgumentConstants.SWT_INITIAL_IMPORT)
        				.setShortFlag(ArgumentConstants.SWT_INITIAL_IMPORT_SFLAG)
        				.setLongFlag(ArgumentConstants.SWT_INITIAL_IMPORT_LFLAG);
        
        Switch swtScheduleUpdate = new Switch(ArgumentConstants.SWT_SCHEDULE_UPDATE)
        				.setShortFlag(ArgumentConstants.SWT_SCHEDULE_UPDATE_SFLAG)
        				.setLongFlag(ArgumentConstants.SWT_SCHEDULE_UPDATE_LFLAG);
        
        Switch swtUpdate = new Switch(ArgumentConstants.SWT_UPDATE)
						.setShortFlag(ArgumentConstants.SWT_UPDATE_SFLAG)
						.setLongFlag(ArgumentConstants.SWT_UPDATE_LFLAG);
        
        Switch swtViews = new Switch(ArgumentConstants.SWT_CREATEVIEWS)
						.setLongFlag(ArgumentConstants.SWT_CREATEVIEWS_LFLAG);
        
        Switch swtGeoserverConsistency = new Switch(ArgumentConstants.SWT_GEOSERVERCONSISTENCY)
						.setShortFlag(ArgumentConstants.SWT_GEOSERVERCONSISTENCY_SFLAG)
						.setLongFlag(ArgumentConstants.SWT_GEOSERVERCONSISTENCY_LFLAG);
        
        Switch swtNoConsistency = new Switch(ArgumentConstants.SWT_NO_CONSISTENCY)
		.setLongFlag(ArgumentConstants.SWT_NO_CONSISTENCY_LFLAG);
        
        jsap.registerParameter(swtHelp);
        jsap.registerParameter(swtConfig);
        jsap.registerParameter(swtInitialImport);
        jsap.registerParameter(swtUpdate);
        jsap.registerParameter(swtScheduleUpdate);
        jsap.registerParameter(swtViews);
        jsap.registerParameter(swtGeoserverConsistency);
        jsap.registerParameter(swtNoConsistency);
	}
	
	
}

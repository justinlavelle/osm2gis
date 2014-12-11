package ch.hsr.osminabox.context;


import com.martiansoftware.jsap.JSAPResult;

public class ArgumentHandler {

	public static void updateConfigParameter(JSAPResult arguments, ApplicationContext context){
		updateDatabaseParameters(arguments, context);
		updateScheduleParameters(arguments, context);
		updatePlanetParameters(arguments, context);
	}

	private static void updatePlanetParameters(JSAPResult arguments,
			ApplicationContext context) {
		
		if(arguments.contains(ArgumentConstants.OPT_PLANET_FILE)){
			context.setConfigParameter(ConfigConstants.CONF_PLANET_FILE, arguments.getString(ArgumentConstants.OPT_PLANET_FILE));
		}
		
		if(arguments.contains(ArgumentConstants.OPT_PLANET_URL)){
			context.setConfigParameter(ConfigConstants.CONF_PLANET_URL, arguments.getString(ArgumentConstants.OPT_PLANET_URL));
		}
	}
	
	private static void updateScheduleParameters(JSAPResult arguments,
			ApplicationContext context) {
		
		if(arguments.contains(ArgumentConstants.OPT_FREQUENCY))
			context.setConfigParameter(ConfigConstants.CONF_DIFF_FREQUENCY, arguments.getString(ArgumentConstants.OPT_FREQUENCY));
		
		if(arguments.contains(ArgumentConstants.OPT_UPDATE_ROOT))
			context.setConfigParameter(ConfigConstants.CONF_DIFF_UPDATEROOT, arguments.getString(ArgumentConstants.OPT_UPDATE_ROOT));
		
		if(arguments.contains(ArgumentConstants.OPT_INITIAL_DIFF))
			context.setConfigParameter(ConfigConstants.CONF_DIFF_CURRENT_FILE, arguments.getString(ArgumentConstants.OPT_INITIAL_DIFF));
		
		if(arguments.contains(ArgumentConstants.OPT_INITIAL_DIFF_REPLICATE))
			context.setConfigParameter(ConfigConstants.CONF_DIFF_CURRENT_FILE_REPLICATE, arguments.getString(ArgumentConstants.OPT_INITIAL_DIFF_REPLICATE));
		
		if(arguments.contains(ArgumentConstants.OPT_LAT_MAX))
			context.setConfigParameter(ConfigConstants.CONF_LAT_MAX, arguments.getString(ArgumentConstants.OPT_LAT_MAX));
		
		if(arguments.contains(ArgumentConstants.OPT_LAT_MIN))
			context.setConfigParameter(ConfigConstants.CONF_LAT_MIN, arguments.getString(ArgumentConstants.OPT_LAT_MIN));
		
		if(arguments.contains(ArgumentConstants.OPT_LON_MIN))
			context.setConfigParameter(ConfigConstants.CONF_LON_MIN, arguments.getString(ArgumentConstants.OPT_LON_MIN));
		
		if(arguments.contains(ArgumentConstants.OPT_LON_MAX))
			context.setConfigParameter(ConfigConstants.CONF_LON_MAX, arguments.getString(ArgumentConstants.OPT_LON_MAX));
		
	}

	private static void updateDatabaseParameters(JSAPResult arguments,
			ApplicationContext context) {
		
		if(arguments.contains(ArgumentConstants.OPT_DATABASE)){
			context.setConfigParameter(ConfigConstants.CONF_DB_DATABASE, arguments.getString(ArgumentConstants.OPT_DATABASE));
		}
		
		if(arguments.contains(ArgumentConstants.OPT_PASSWORD)){
			context.setConfigParameter(ConfigConstants.CONF_DB_PW, arguments.getString(ArgumentConstants.OPT_PASSWORD));
		}
		
		if(arguments.contains(ArgumentConstants.OPT_USER)){
			context.setConfigParameter(ConfigConstants.CONF_DB_USERNAME, arguments.getString(ArgumentConstants.OPT_USER));
		}
		
		if(arguments.contains(ArgumentConstants.OPT_HOST)){
			context.setConfigParameter(ConfigConstants.CONF_DB_HOST, arguments.getString(ArgumentConstants.OPT_HOST));
		}
		
		if(arguments.contains(ArgumentConstants.OPT_PORT)){
			context.setConfigParameter(ConfigConstants.CONF_DB_PORT, String.valueOf(arguments.getInt(ArgumentConstants.OPT_PORT)));
		}
	}
	
}

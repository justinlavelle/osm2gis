package ch.hsr.osminabox;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ch.hsr.osminabox.context.ApplicationContextImpl;
import ch.hsr.osminabox.context.ArgumentConstants;
import ch.hsr.osminabox.context.ArgumentHandler;
import ch.hsr.osminabox.context.ApplicationContext;

/**
 * The osm2gis Applications Main Class
 * 
 * @author rhof
 *
 */
public class Main {

	private static Logger logger = Logger.getLogger(Main.class);
	
	/**
	 * Main Function
	 * 	delegates the Tasks
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		logger.setLevel(Level.ALL);
		ApplicationContext context = new ApplicationContextImpl(args);
		logger.debug("created Application Context");
		ArgumentHandler.updateConfigParameter(context.getJSAPArguments(), context);
		logger.debug("created Config Parameter");
		handleHelp(context);
		handleSaveConfiguration(context);
		handleInitialImport(context);
		logger.debug("Handeled: help, saveConfiguration and initial import");
		handleScheduleUpdate(context);
		logger.debug("Handeled schedule update");
		handleUpdate(context);	
		logger.debug("Update handeled");
		handleCreateViews(context);
		logger.debug("created views");
		handleGeoserverConsistency(context);
		logger.debug("Handeled Geoserver Consistency");
	}

	

	/**
	 * 	Checks if the Update Scheduling Option is set and delegates
	 * 	the Processing to the UpdateScheduler
	 * 
	 * 	@param context	The Application Context
	 */
	private static void handleScheduleUpdate(ApplicationContext context) {
		if(context.isSwitchOn(ArgumentConstants.SWT_SCHEDULE_UPDATE)){
			if(!context.isConnected()){context.connectToDatabase();}
			if(logger.isDebugEnabled()){logger.debug("Scheduling update..");}
			new UpdateScheduler(context).scheduleUpdate(false);
		}
	}
	
	/**
	 * Checks if the Update Option is set an delegates the Update
	 * Task to the Update Scheduler. (The Update will only be executed once)
	 * 
	 * @param context	The Application Context
	 */
	private static void handleUpdate(ApplicationContext context){
		if(context.isSwitchOn(ArgumentConstants.SWT_UPDATE)){
			if(!context.isConnected()){context.connectToDatabase();}
			logger.debug("in handle Update");
			if(logger.isDebugEnabled()){logger.debug("Starting Cron Update..");}
			new UpdateScheduler(context).scheduleUpdate(true);
		}
	}
	
	/**
	 * 	Checks if the Initial Import Option is set and delegates
	 * 	the Processing to the InitialImportHandler
	 * 
	 * @param context	The Application Context
	 */
	private static void handleInitialImport(ApplicationContext context) {
		if(context.isSwitchOn(ArgumentConstants.SWT_INITIAL_IMPORT)){
			if(!context.isConnected()){context.connectToDatabase();}
			if(logger.isDebugEnabled()){logger.debug("Launching initial import..");}
			new InitialImportHandler(context).initialImport();
		}
	}

	/**
	 * 	Checks if the Save Configuration Option is set and delegates
	 * 	the saving process to the Application Context
	 * 
	 * @param context	The Application Context
	 */
	private static void handleSaveConfiguration(ApplicationContext context) {
		if(context.isSwitchOn(ArgumentConstants.SWT_CONFIG)){
			logger.debug("Updating config file..");
			context.save();
		}
	}
	
	/**
	 * 	Checks if the Help Option is set and calls the HelpHandler
	 * 	who will Print the Manpage to the stdout
	 * 
	 * @param context
	 */
	private static void handleHelp(ApplicationContext context){
		if(     context.isSwitchOn(ArgumentConstants.SWT_HELP) 
		    && !context.isSwitchOn(ArgumentConstants.SWT_CONFIG)
			&& !context.isSwitchOn(ArgumentConstants.SWT_SCHEDULE_UPDATE)){
			HelpHandler.handleHelpCall();
		}
	}

	private static void handleGeoserverConsistency(ApplicationContext context) {
		if(!context.isConnected()){context.connectToDatabase();}
		if(context.isSwitchOn(ArgumentConstants.SWT_GEOSERVERCONSISTENCY)){
			context.getConsistencyService().checkWholeConsistency();
		}
	}

	private static void handleCreateViews(ApplicationContext context) {
		if(!context.isConnected()){context.connectToDatabase();}
		if( context.isSwitchOn(ArgumentConstants.SWT_CREATEVIEWS)){		
			context.getConsistencyService().createViewsOnly();
		}
	}
}

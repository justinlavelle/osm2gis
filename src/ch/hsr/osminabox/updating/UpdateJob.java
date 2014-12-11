package ch.hsr.osminabox.updating;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.StatefulJob;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.context.ConfigConstants;
import ch.hsr.osminabox.downloading.Downloader;
import ch.hsr.osminabox.parsing.OSMParser;
import ch.hsr.osminabox.util.PathUtil;
import ch.hsr.osminabox.util.StreamFactory;

/**
 * This job is scheduled by quartz. The UpdateStrategy is used to determine different
 * execution options.
 * @author rhof
 *
 */
public class UpdateJob implements StatefulJob {

	private final static Logger logger = Logger.getLogger(UpdateJob.class);
	private final static String FILE_SEPARATOR = System.getProperty("file.separator");
	public static int invocationCount = 0;
	
	@Override
	public void execute(JobExecutionContext jobContext) throws JobExecutionException {
		
		logger.info("Starting Update Job");
		
		JobDataMap map = jobContext.getJobDetail().getJobDataMap();
		
		ApplicationContext context = (ApplicationContext) map.get(JobDetailMapConstants.APPLICATION_CONTEXT);
		UpdateStrategy strategy = (UpdateStrategy) map.get(JobDetailMapConstants.UPDATE_STRATEGY);
		Boolean terminateScheduler = (Boolean) map.get(JobDetailMapConstants.TERMINATE_SCHEDULER);
		
		UpdateFrequency frequency = UpdateFrequency.getFrequency(context.getConfigParameter(ConfigConstants.CONF_DIFF_FREQUENCY));
		String nextUpdateFile, updateFileType;
		switch(frequency){
		case DAILY:
			nextUpdateFile = context.getConfigParameter(ConfigConstants.CONF_DIFF_CURRENT_FILE);
			updateFileType = ConfigConstants.CONF_DIFF_CURRENT_FILE;
			break;
			
		case HOURLY:
		case MINUTELY:
			nextUpdateFile = context.getConfigParameter(ConfigConstants.CONF_DIFF_CURRENT_FILE_REPLICATE);
			updateFileType = ConfigConstants.CONF_DIFF_CURRENT_FILE_REPLICATE;
			break;
			
		default:
			logger.error("Invalid Update Frequency set.");
			invocationCount++;
			return;
		}
		
		if(nextUpdateFile.length() <= 0){
			logger.error("No Update File Name set. Check your Arguments or the properties File. Note the different commands for daily and hourly / minutely updates.");
			System.exit(0);
		}
		
		if(terminateScheduler){
			try {
				jobContext.getScheduler().shutdown();
			} catch (SchedulerException e) {
				logger.error("Coult not terminate Scheduler. Please terminate Application manualy");
			}
		}			
		
		try{
			do{
				nextUpdateFile = importNextUpdateFile(context, strategy, nextUpdateFile);
				context.setConfigParameter(updateFileType, nextUpdateFile);
				context.save();
				writeStateFile(context, updateFileType);
			} while(!terminateScheduler);
		} catch (FileNotFoundException e){
			logger.error("File not found while downloading! No update was performed!!!");
			logger.error("make sure that you entered the filename right; a file at hour-replicate/000/000/003.osc.gz becomes 000000003.osc.gz and NOT 000/000/003.osc.gz");
			e.printStackTrace();
			logger.info("Scheduling for the same update File: " + strategy.getUpdateFileAsUrl(nextUpdateFile));
		}	
		invocationCount++;
	}

	private void writeStateFile(ApplicationContext context, String updateFileType) {
		try{
			File state = new File(context.getConfigParameter(ConfigConstants.CONF_OSM2GIS_STATEFILE));
			FileWriter out = new FileWriter(state);
			String dateString = new Date().toString();
			String dateFormat = context.getConfigParameter(ConfigConstants.CONF_OSM2GIS_STATEFILE_DATE_FROMAT);
			try{
				dateString = new SimpleDateFormat(dateFormat).format(new Date());
			}catch(Exception e){
				logger.warn("<"+dateFormat+"> is not a valid date setting for "
						+ConfigConstants.CONF_OSM2GIS_STATEFILE_DATE_FROMAT+
						" in osm2gis.properties. Look here: ");
			}

			out.write("{\"time\":\""+dateString
					+"\",\"nextUpdateFile\":\""+context.getConfigParameter(updateFileType)+"\"}");
			out.flush();
			out.close();
		}catch(Exception e){
			logger.warn("Could not write state file...", e);
		}
	}

	private String importNextUpdateFile(ApplicationContext context,
			UpdateStrategy strategy, String updateFile) throws FileNotFoundException {
		
		String urlRoot = context.getConfigParameter(ConfigConstants.CONF_DIFF_UPDATEROOT);
		String subDir = context.getConfigParameter(strategy.getSubDirectoryConfigConstant());
		
		String downloadURL = urlRoot + PathUtil.urlSeparator
							+ subDir + PathUtil.urlSeparator
							+ strategy.getUpdateFileAsUrl(updateFile);
		
		String tempDir = context.getConfigParameter(ConfigConstants.CONF_DOWNLOAD_DIR);
		String downloadLocation = tempDir + FILE_SEPARATOR + updateFile;
		
		logger.info("Downloading update file from url: " + downloadURL);
		
		try{
			Downloader.download(downloadURL, downloadLocation);
		} catch (FileNotFoundException e){
			throw e;
		}
		
		logger.info("Parsing Update File: " + strategy.getUpdateFileAsUrl(updateFile));
		InputStream in = StreamFactory.createInputStream(downloadLocation);
		OSMParser parser = new OSMParser(context);
		parser.parseUpdate(in);
		
		return strategy.getNextUpdateFile(updateFile);
	}

}

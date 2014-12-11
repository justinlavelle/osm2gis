package ch.hsr.osminabox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.filechooser.FileSystemView;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.context.ArgumentConstants;
import ch.hsr.osminabox.context.ConfigConstants;
import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.downloading.Downloader;
import ch.hsr.osminabox.parsing.OSMParser;
import ch.hsr.osminabox.util.PathUtil;
import ch.hsr.osminabox.util.StreamFactory;

/**
 * Handles an initial import call
 * @author rhof
 *
 */
public class InitialImportHandler {

	private static Logger logger = Logger.getLogger(InitialImportHandler.class);
	private ApplicationContext context;
	
	public InitialImportHandler(ApplicationContext context) {
		this.context = context;
	}
	
	
	/**
	 * Handles the Initial Import Call
	 */
	public void initialImport() {
		
		if(logger.isDebugEnabled()){logger.debug("Switch Initial Import active");}
		
		if (context.getConsistencyService().checkConsistencyForInitialImportAndUpdate(true)){
			String localPath;
			if(context.containsArgument(ArgumentConstants.OPT_PLANET_URL)){
				localPath = downloadFileFromURL();			
			} else if(context.containsArgument(ArgumentConstants.OPT_PLANET_FILE)){
				localPath = importFile();
			} else {
				logger.info("No Planet File specified. Reading from STDIN..");
				localPath = "";
			}
			parseFile(localPath);
		}
	}


	/**
	 * @return A String referencing the File to Import
	 */
	private String importFile() {
		logger.info("Starting parser..");
		String localPath = context.getConfigParameter(ConfigConstants.CONF_PLANET_FILE);
		if(!PathUtil.fileExists(localPath)){
			logger.error("Specified File does not exist: " + localPath);
			System.exit(0);
		}
		return localPath;
	}

	/**
	 * @return A String referencint the File to Import
	 */
	private String downloadFileFromURL() {
		String localPath = downloadPlanetFile();
		if(!PathUtil.fileExists(localPath)){
			logger.error("Error while downloading Planet File, try downloading manualy and import from direct from the file");
			System.exit(0);
		}
		return localPath;
	}

	/**
	 * @return A String referencing the Downloaded File
	 */
	private String downloadPlanetFile() {
		
		String url = context.getConfigParameter(ConfigConstants.CONF_PLANET_URL);
		String fileName = PathUtil.getFileName(url);
		String localPath = System.getProperty("user.dir") 
					+ File.separator 
					+ context.getConfigParameter(ConfigConstants.CONF_DOWNLOAD_DIR)
					+ File.separator 
					+ fileName;
		
		File downloadDir = new File(context.getConfigParameter(ConfigConstants.CONF_DOWNLOAD_DIR));
		if(!downloadDir.exists()){createFolder(downloadDir);}
		
		try {
			Downloader.download(url, localPath);
		} catch (FileNotFoundException e) {
			logger.error("Planet File not found on location:" + url);
		}

		return localPath;
	}


	private void createFolder(File downloadDir) {
		logger.info("Creating download directory:" + downloadDir);
		try {
			FileSystemView.getFileSystemView().createNewFolder(downloadDir);
		} catch (IOException e) {
			logger.error("Error creating download directory: " + downloadDir);
			System.exit(0);
		}
	}
	
	/**
	 * Creates A Parser for the given File.
	 * @param file The File to Parse. If no File is given the STDIN will be used.
	 */
	private void parseFile(String file){
		InputStream in = null;
		if(!file.equals("")){
			logger.debug("Parsing xml file..");
			in = StreamFactory.createInputStream(file);
		} else {
			logger.debug("Parsing STDIN Stream");
			in = System.in;
		}
		OSMParser parser = new OSMParser(context);
		parser.parseInitialImport(in);
	}
}

package ch.hsr.osminabox.parsing;

import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.db.DBService;
import ch.hsr.osminabox.importing.strategy.BufferStrategy;
import ch.hsr.osminabox.importing.strategy.CreateBufferStrategy;
import ch.hsr.osminabox.importing.xml.OSMChangeTagHandler;
import ch.hsr.osminabox.importing.xml.OSMTagHandler;
import ch.hsr.osminabox.importing.xml.XMLTagHandler;
import ch.hsr.osminabox.util.StopWatch;

/**
 * The Main Class starting the Parsing Process of an osm file
 * @author rhof
 *
 */
public class OSMParser {
	
	private static Logger logger = Logger.getLogger(OSMParser.class);
	
	private static StopWatch timer;
	protected ApplicationContext context;
	
	public OSMParser(ApplicationContext context) {
		this.context = context;
	}
	
	public void parse(InputStream inputStream, XMLTagHandler rootHandler){
		timer  = new StopWatch();
		timer.start();
		
		if(!context.createTempTables()) {
			logger.fatal("Could not create temporary Tables.");
			System.exit(0);
		}
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXEventHandler handler = new SAXEventHandler(rootHandler);
		
		logger.info("Start Parsing OSM File");
		
		try{
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(inputStream, handler);
		}catch (Exception e) { e.printStackTrace(); }
	}
	
	private void endParsing(){
		context.removeTempTables();
		logger.info("Parsing finished  in: " + timer.stop() + " seconds");
	}
	
	/**
	 * Parses an Initial Import File (eg Planet.osm)
	 * @param inputStream Stream from which the xml data flows
	 */
	public void parseInitialImport(InputStream inputStream){
		BufferStrategy strategy = new CreateBufferStrategy(context);
		XMLTagHandler rootHandler = new OSMTagHandler(context, strategy);
		parse(inputStream, rootHandler);
		
		// Additional work after the parsing is finished...
		DBService dbService = context.getDBService();
		dbService.insertTempRelations();
		dbService.insertRemainingAreas();
		
		endParsing();
	}
	
	/**
	 * Parse an Update File holding diff information
	 * @param inputStream Stream from which the xml data flows
	 */
	public void parseUpdate(InputStream inputStream){
		XMLTagHandler rootHandler = new OSMChangeTagHandler(context);
		parse(inputStream, rootHandler);
	
		// Additional work after the parsing is finished...
		DBService dbService = context.getDBService();
		dbService.modifyTempRelations();
		dbService.modifyRemainingAreas();
		
		endParsing();
	}
}

package ch.hsr.osminabox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.log4j.Logger;

/**
 * Handles the Help Argument Call
 * @author rhof
 */
public class HelpHandler {

	private final static Logger logger = Logger.getLogger(HelpHandler.class);

	protected static PrintStream out = System.out;
	/**
	 *  Path to the Manpage
	 */
	private final static String HELP_FILE = System.getProperty("user.dir") + System.getProperty("file.separator") + "manpage.txt";
	
	/**
	 * Handles the Call
	 */
	public static void handleHelpCall(){
		
		File file = new File(HELP_FILE);
		try {
			BufferedReader is = new BufferedReader(new FileReader(file));

			while (is.ready()) {
			       writeLine(is.readLine());
			}
			
		} catch (FileNotFoundException e) {
			logger.error("Help File not found: " + HELP_FILE);
		} catch (IOException e) {
			logger.error("Could not read helpfile: " + HELP_FILE);
		}
	}

	private static void writeLine(String line) throws IOException {
		out.println(line);
	}
}

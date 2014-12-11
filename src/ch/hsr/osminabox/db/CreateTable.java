package ch.hsr.osminabox.db;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * Creates and imports needed data(-schemas) for a osm2gis Database
 * 
 * @author m2huber
 * 
 */
public class CreateTable {

	private static final Logger logger = Logger.getLogger(CreateTable.class);
	/**
	 * Returns the SQL Statements for the creation of the whole Database shema
	 * defines in config/osm_create.sql
	 * 
	 * @return SQL Create Statements
	 */
	protected String getSQL(String pathToFile) {
		StringBuffer buffer = new StringBuffer();
		String lineSep = System.getProperty("line.separator");
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(pathToFile));
		} catch (FileNotFoundException e) {
			logger.error(e);
			NullPointerException nullPointerException = new NullPointerException("File "+pathToFile+" does not exist!");
			nullPointerException.initCause(e);
			throw nullPointerException;
		}

		try {
			String tmp;
			while ((tmp = reader.readLine()) != null) {
				buffer.append(tmp);
				buffer.append(lineSep);
			}
		} catch (IOException e) {
			logger.error(e);
			return null;
		}
		return buffer.toString();
	}
	
	public String getCreateTableSQL(){
		return getSQL("config/osm_create.sql");
	}
	
	public String getHStoreSQL(){
		return getSQL("config/hstore.sql");
	}
}

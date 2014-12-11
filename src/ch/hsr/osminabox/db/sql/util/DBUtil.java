package ch.hsr.osminabox.db.sql.util;

import java.lang.reflect.Constructor;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.db.DBConstants;
import ch.hsr.osminabox.db.dbdefinition.Column;
import ch.hsr.osminabox.db.dbdefinition.Database;
import ch.hsr.osminabox.db.sql.Constants;
import ch.hsr.osminabox.db.util.ValueValidation;
import ch.hsr.osminabox.util.DateParser;
import java.lang.reflect.InvocationTargetException;
/**
 * Util Class for the Database Management
 * @author m2huber
 *
 */
public class DBUtil {
				
	private static Logger logger = Logger.getLogger(DBUtil.class);
	
	protected DateParser dateParser;
	protected ValueValidation validator;
	protected Database dbStructure;
	
	public DBUtil(Database dbStructure) {
		dateParser = new DateParser();
		validator = new ValueValidation();
		this.dbStructure = dbStructure;
	}
	
	/**
	 * Checks if the given Spacer-String is already appended at the end of the buffer. Appends it if not.
	 * @param buffer
	 * @param spacer
	 */
	public void checkAndAppendSpacer(StringBuffer buffer, String spacer){
		if(buffer.lastIndexOf(spacer) < buffer.length() - spacer.length())
			buffer.append(spacer);
	}
	
	/**
	 * Create the INSERT Statement Start 'INSERT INTO .... VALUES ' for the given Table.
	 * @param tableName Database tablename
	 * @return
	 * 			Insert String
	 */	
	public StringBuffer createInsertBegin(String tableName){
		
		StringBuffer sql = new StringBuffer();
		
		sql.append(Constants.INSERT);
		sql.append(Constants.INTO);
		sql.append(tableName);
		sql.append(Constants.LINE_SPACE);
		sql.append(Constants.OPEN_BRACKET);
		
		int index =0;
		List<Column> nonAutoIncrementColumns = dbStructure.getTable(tableName).getNonAutoIncrementColumns();
		for(Column column : nonAutoIncrementColumns){
			sql.append(column.getName());
			index++;
			if(index < nonAutoIncrementColumns.size()) { 
				sql.append(Constants.COMMA);
				sql.append(Constants.LINE_SPACE);
			}
		}
		
		sql.append(Constants.CLOSE_BRACKET);
		sql.append(Constants.LINE_SPACE);
		sql.append(Constants.VALUES);
	
		return sql;
	}
	
	/**
	 * Creates the VALUES (...) part from the SQL INSERT statement for the Table.
	 * @param columns
	 * @return
	 */
	public StringBuffer addInsertValues(String tableName, Map<String, String> columns) {
		StringBuffer sql = new StringBuffer();
		
		sql.append(Constants.OPEN_BRACKET);
		
		int index = 0;
		List<Column> nonAutoIncrementColumns = dbStructure.getTable(tableName).getNonAutoIncrementColumns();
		for(Column column : nonAutoIncrementColumns){
			String castedValue = validateValue(column, columns.get(column.getName()));
			
			// If value couldn't be converted, skip this node!
			if(castedValue == null){
				logger.info("InsertConvertionError: value [" + columns.get(column.getName()) + "] in column [" + column.getName() + "] for table [" + tableName + "] couldn't be converted - skipping this entry!");
				return new StringBuffer();
			}
			
			sql.append(castedValue);
			
			if(++index < nonAutoIncrementColumns.size()) { 
				sql.append(Constants.COMMA);
				sql.append(Constants.LINE_SPACE);
			}
		}
		
		sql.append(Constants.CLOSE_BRACKET);
		
		return sql;
	}
	
	/**
	 * Create the UPDATE Statement Start 'UPDATE .... VALUES ' for the given columns and Table
	 * @param tableName Database tablename
	 * @return Update String
	 */	
	public StringBuffer createUpdateBegin(String tableName){
		
		StringBuffer sql = new StringBuffer();
		
		sql.append(Constants.UPDATE);
		sql.append(tableName);
		sql.append(Constants.LINE_SPACE);
		sql.append(Constants.SET);
	
		return sql;
	}
	
	/**
	 * Creates the Column=Value part from the SQL UPDATE statement for each Column.
	 * @param columnValues
	 * @return
	 */
	public StringBuffer addUpdateValues(String tableName, Map<String, String> columns) {
		StringBuffer sql = new StringBuffer();
		
		int index = 0;
		List<Column> nonAutoIncrementColumns = dbStructure.getTable(tableName).getNonAutoIncrementColumns();
		for(Column column : nonAutoIncrementColumns){
			String castedValue = validateValue(column, columns.get(column.getName()));
			
			// If value couldn't be converted, skip this node!
			if(castedValue == null){
				logger.error("UpdateConvertionError: Value [" + columns.get(column.getName()) + "] in column [ " + column.getName() + " ] for table [" + tableName + "] couldn't be converted - skipping this entry!");
				return new StringBuffer();
			}
			
			sql.append(column.getName());
			sql.append(Constants.EQUAL);
			sql.append(castedValue);

			if(++index < nonAutoIncrementColumns.size()) { 
				sql.append(Constants.COMMA);
				sql.append(Constants.LINE_SPACE);
			}
		}
		
		return sql;
	}
	
	/**
	 * Add the ";" at the end of sql scripts
	 * @param map
	 * @return
	 */
	public  HashMap<String, StringBuffer> addEndTags(HashMap<String, StringBuffer> map) {
		for (StringBuffer b : map.values()) {
			if (b.length() > 0)
				b.append(Constants.SEMICOLON);
		}
		return map;
	}
	
	private String validateValue(Column column, String value){
		
		try {				
			String className = column.getJavaType();
			
			// Check for empty values and "nullable"
			if((value == null || value.equals("")) && column.isNullable() == ResultSetMetaData.columnNullable)
				return "null";
			
			// Check for empty values and "not null"
			if((value == null || value.equals("")) && column.isNullable() == ResultSetMetaData.columnNoNulls)
				return null;
			
			// Parse OSM timestamps because they can be in different formats.
			if(className.equals(Timestamp.class.getCanonicalName()))
				return Constants.APOSTROPHE + new Timestamp(dateParser.parse(value).getTime()).toString() + Constants.APOSTROPHE;
		
			// HStore & Geom return as java.lang.Object
			if(className.equals(Object.class.getCanonicalName()))
				return value;
			
                        Class<?> cls = Class.forName(className);
			try {
                            
                            // Cast the Value to the desired class to see if its valid.
                            value = castValue(value, cls).toString();
                        } catch (InvocationTargetException e){
                            try {
                                if(cls.equals(Integer.class)){
                                    logger.warn("Conversion from "+value.getClass()+" to "+cls
                                            +" failed for value "+value+". Convert to "+Long.class.getName() +" !");
                                    Class<?> cls_new = Class.forName(Long.class.getName());
                                    // Cast the Value to the desired class to see if its valid.
                                    value = castValue(value, cls_new).toString();
                                } else {
                                    logger.error("Conversion from "+value.getClass()+" to "+cls+" failed for value "+value);
                                    value = null;
                                }
                            } catch (InvocationTargetException ee){
                                logger.error("Conversion from "+value.getClass()+" to "+cls+" failed for value "+value);
                                value = null;
                            }
                        }
			
			if(value != null)
				return Constants.APOSTROPHE + validator.addEscape(value) + Constants.APOSTROPHE;
			else
				return null;
		
		} catch (Exception e) {
			return null;
		}
	}
	
	private <T> T castValue(Object value, Class<T> to) throws InvocationTargetException {
        try {
            
        	// First try valueOf Method...
            return to.cast(to.getDeclaredMethod("valueOf", new Class[] { String.class }).invoke(null, value.toString()));
            
        } catch (Exception e) {
            
        	// If that fails
            try {
                
            	// Try String based Constructor
                Constructor<T> constructor = to.getDeclaredConstructor(String.class);
                return constructor.newInstance(value.toString());
                
            } catch (InvocationTargetException ee) {
            	throw new InvocationTargetException(ee);
            } catch (Exception ee) {
            	logger.error("Conversion from "+value.getClass()+" to "+to+" failed for value "+value);
            	return null;
            }
        }
    }

	public String addWhere(long osmId) {
		return Constants.LINE_SPACE + Constants.WHERE + Constants.LINE_SPACE + DBConstants.ATTR_OSM_ID + Constants.EQUAL + osmId;		
	}
}

package ch.hsr.osminabox.db.util;

/**
 * Value Validator class
 * @author m2huber
 *
 */
public class ValueValidation {
		
	/**
	 * Add's Escape chars if needed for the given string
	 * @param value
	 * @return
	 */
	public String addEscape(String value) {
		if(value == null)
			return null;		
		
		else if(value.contains("'")){
			//value = value.replace("''", "'");
			value = value.replace("\\'", "'");
			value = value.replace("'", "''");
		}
		
		if(value.contains("\""))
			value = value.replace("\"", "");		
		
		return value;		
	}	
}

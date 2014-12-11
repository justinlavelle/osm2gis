package ch.hsr.osminabox.schemamapping.consistency.xml2db;

import ch.hsr.osminabox.db.dbdefinition.Database;

/**
 * The Class DBChangeAction.
 * 
 * @author ameier
 */
public class DBChangeAction {
	
	public enum ChangeAction{DO_NOTHING,CREATE_NEW_TABLES,DELETE_DB_CREATE_ALL,MISSING_REQUIRED_FIELDS_IN_MAPPING_CONF}

	ChangeAction state;

	Database db;

	String message;
	
	/**
	 * Instantiates a new dB change action.
	 * 
	 * @param db the db
	 * @param state the state
	 * @param message the message
	 */
	public DBChangeAction(Database db, ChangeAction state, String message){
		this.db = db;
		this.state = state;
		this.message = message;
	}

	/**
	 * Gets the state.
	 * 
	 * @return the state
	 */
	public ChangeAction getState() {
		return state;
	}

	/**
	 * Gets the dsd.
	 * 
	 * @return the dsd
	 */
	public Database getDsd() {
		return db;
	}

	/**
	 * Gets the message.
	 * 
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the message.
	 * 
	 * @param message the new message
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	

}

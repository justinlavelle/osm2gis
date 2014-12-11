package ch.hsr.osminabox.db.dbdefinition;

import java.util.LinkedList;
import java.util.List;


/**
 * The Class DataType.
 * 
 * @author ameier
 */
public class DataType {
	
	/** The name. */
	private String name;
	
	/** The aliases. */
	private List<String> aliases = new LinkedList<String>();
	
	/**
	 * Instantiates a new data type.
	 * 
	 * @param name the name
	 */
	public DataType(String name){
		this.name = name;
	}
	
	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 * 
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the aliases.
	 * 
	 * @return the aliases
	 */
	public List<String> getAliases() {
		return aliases;
	}
	
	/**
	 * Adds the alias.
	 * 
	 * @param alias the alias
	 */
	public void addAlias(String alias){
		aliases.add(alias);
	}
	
}

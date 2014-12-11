package ch.hsr.osminabox.db.dbdefinition;

/**
 * Implementation of Column
 * 
 * @author ameier
 * 
 */
public class Column {
	
	/** The name. */
	private String name;
	
	/** The type. */
	private String type;
	
	/** The java type. */
	private String javaType;
	
	/** The is new. */
	private boolean isNew;
	
	/** Weather the column is auto incremented or not. */
	private boolean isAutoIncrement;
	
	/** the nullability status: one of columnNoNulls, columnNullable or columnNullableUnknown */
	private int isNullable;
	
	/**
	 * Instantiates a new Column.
	 * @param name
	 * @param type
	 * @param javaType
	 * @param isNew
	 * @param isAutoIncrement
	 * @param isNullable
	 */
	public Column(String name, String type, String javaType, boolean isNew, Boolean isAutoIncrement, int isNullable){
		this.name = name;
		this.type = type;
		this.javaType = javaType;
		this.isNew = isNew;
		if(isAutoIncrement == null)
			this.isAutoIncrement = false;
		else
			this.isAutoIncrement = isAutoIncrement;
		this.isNullable = isNullable;
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
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Gets the java type.
	 * 
	 * @return the java type
	 */
	public String getJavaType(){
		return javaType;
	}
	
	/**
	 * Checks if is new.
	 * 
	 * @return true, if is new
	 */
	public boolean isNew() {
		return isNew;
	}
	
	/**
	 * If this Column is auto incremented.
	 * @return true if it is.
	 */
	public boolean isAutoIncrement(){
		return isAutoIncrement;
	}
	
	/**
	 * Nullability of this column
	 * 
	 * @return true, if it is.
	 */
	public int isNullable() {
		return isNullable;
	}
	
}

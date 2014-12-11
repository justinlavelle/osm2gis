package ch.hsr.osminabox.db.dbdefinition;

import java.util.LinkedList;

import java.util.List;


/**
 * Implementation of Table.
 * 
 * @author ameier
 */
public class Table {
	
	/** The tablename. */
	private String tablename;
	
	/** The is new. */
	private boolean isNew;
	
	/** The columns. */
	private List<Column> columns;
	
	/** The message. */
	private String message;
	
	/**
	 * Instantiates a new table.
	 * 
	 * @param tablename the tablename
	 * @param isNew the is new
	 * @param message the message
	 */
	public Table(String tablename, boolean isNew, String message){
		this.tablename = tablename;
		this.isNew = isNew;
		this.message = message;
		columns = new LinkedList<Column>();
	}
	
	/**
	 * Gets the tablename.
	 * 
	 * @return the tablename
	 */
	public String getTablename() {
		return tablename;
	}
	
	/**
	 * Sets the tablename.
	 * 
	 * @param tablename the new tablename
	 */
	public void setTablename(String tablename) {
		this.tablename = tablename;
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
	 * Sets the new.
	 * 
	 * @param isNew the new new
	 */
	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}
	
	/**
	 * Gets the columns.
	 * 
	 * @return the columns
	 */
	public List<Column> getColumns() {
		return columns;
	}
	
	/**
	 * Gets the columns which are not auto incremented (non PKs).
	 * 
	 * @return the columns
	 */
	public List<Column> getNonAutoIncrementColumns() {
		List<Column> nonAutoIncrementColumns = new LinkedList<Column>();
		
		for(Column column : columns){
			if(column.isAutoIncrement())
				continue;
			
			nonAutoIncrementColumns.add(column);
		}
		
		return nonAutoIncrementColumns;
	}
	
	/**
	 * Adds the column.
	 * 
	 * @param column the column
	 */
	public void addColumn(Column column){
		columns.add(column);
	}
	
	/**
	 * Sets the columns.
	 * 
	 * @param columns the new columns
	 */
	public void setColumns(List<Column> columns) {
		this.columns = columns;
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
	 * Gets the column.
	 * 
	 * @param columnName the column name
	 * 
	 * @return the column
	 */
	public Column getColumn(String columnName){
		for (Column c : columns){
			if (c.getName().toLowerCase().equals(columnName)){
				return c;
			}
		}
		return null;
	}
	
}

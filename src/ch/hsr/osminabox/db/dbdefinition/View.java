package ch.hsr.osminabox.db.dbdefinition;

import java.util.LinkedList;
import java.util.List;

public class View {
	/** The viewname. */
	private String viewname;
	
	/** The is new. */
	private boolean isNew;
	
	/** The columns. */
	private List<Column> columns;
	
	/** The message. */
	private String message;
	
	/**
	 * Instantiates a new view.
	 * 
	 * @param viewname the viewname
	 * @param isNew the is new
	 * @param message the message
	 */
	public View(String viewname, boolean isNew, String message){
		this.viewname = viewname;
		this.isNew = isNew;
		this.message = message;
		columns = new LinkedList<Column>();
	}
	
	/**
	 * Gets the viewname.
	 * 
	 * @return the viewname
	 */
	public String getViewname() {
		return viewname;
	}
	
	/**
	 * Sets the viewname.
	 * 
	 * @param viewname the new viewname
	 */
	public void setViewname(String viewname) {
		this.viewname = viewname;
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

package ch.hsr.osminabox.db.dbdefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * The Class Database.
 * 
 * @author ameier
 */
public class Database {
	
	/** The tables. */
	Set<Table> tables;
	
	/** The vies. */
	Set<View> views;
	
	/** The name. */
	String	name;
	
	/**
	 * Instantiates a new database.
	 * 
	 * @param name the name
	 */
	public Database(String name){
		this.name = name;
		tables = new HashSet<Table>();
		views = new HashSet<View>();
	}
	
	/**
	 * Gets the tables.
	 * 
	 * @return the tables
	 */
	public Set<Table> getTables() {
		return tables;
	}

	/**
	 * Adds the table.
	 * 
	 * @param table the table
	 */
	public void addTable(Table table) {
		tables.add(table);
	}
	
	/**
	 * Gets the table.
	 * 
	 * @param tablename the tablename
	 * 
	 * @return the table
	 */
	public Table getTable(String tablename){
		for (Table t : tables){
			if (t.getTablename().toLowerCase().equals(tablename.toLowerCase())){
				return t;
			}
		}
		return null;
	}
	
	/**
	 * Gets the table names.
	 * 
	 * @return the table names
	 */
	public List<String> getTableNames(){
		List<String> result = new ArrayList<String>();
		for (Table t : tables){
			result.add(t.getTablename());
		}
		Collections.sort(result);
		return result;
	}
	
	public void clear(){
		tables.clear();
		views.clear();
	}
	
	/**
	 * Gets the views.
	 * 
	 * @return the views
	 */
	public Set<View> getViews() {
		return views;
	}

	/**
	 * Adds the view.
	 * 
	 * @param view the view
	 */
	public void addView(View view) {
		views.add(view);
	}
	
	/**
	 * Gets the view.
	 * 
	 * @param viewname the viewname
	 * 
	 * @return the view
	 */
	public View getView(String viewname){
		for (View v : views){
			if (v.getViewname().toLowerCase().equals(viewname.toLowerCase())){
				return v;
			}
		}
		return null;
	}
	
	/**
	 * Gets the view names.
	 * 
	 * @return the view names
	 */
	public List<String> getViewNames(){
		List<String> result = new ArrayList<String>();
		for (View v : views){
			result.add(v.getViewname());
		}
		Collections.sort(result);
		return result;
	}

	
	public boolean tableOrViewExistsInDB(String name){
		
		if (getTable(name)!=null)
				return true;
		if (getView(name)!=null)
			return true;
		
		return false;
	}
}

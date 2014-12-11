package ch.hsr.osminabox.db.sql.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.db.DBConstants;
import ch.hsr.osminabox.db.entities.OSMEntity;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.Way;

/**
 * Helping Class for handling the HStore PGSQL Datatype
 * @author jzimmerm
 *
 */
public class HStoreUtil {
	
	private static Logger logger = Logger.getLogger(HStoreUtil.class);
	
	private final String selectHstore1 = "SELECT * FROM each((SELECT "+ DBConstants.ATTR_KEYVALUE +" FROM ";
	private final String selectHstore2 = " WHERE " + DBConstants.ATTR_OSM_ID + "=";
	private final String selectHstore3 = " LIMIT 1));";
	
	protected static final String HSTORE_KEY = "key";
	protected static final String HSTORE_VALUE = "value";
	
	private Connection connection;
	
	public HStoreUtil(Connection connection) {
		this.connection = connection;
	}
	
	/**
	 * Adds the Tags from the way_temp Table to the given Way
	 * 
	 * @param way
	 */
	public void addTagsFromTemp(Way way){
		addTagsFromTemp(way, DBConstants.WAY_TEMP);
	}
	
	/**
	 * Adds the Tags from the relation_temp Table to the given Relation
	 * 
	 * @param relation
	 */
	public void addTagsFromTemp(Relation relation){
		addTagsFromTemp(relation, DBConstants.RELATION_TEMP);
	}
	
	/**
	 * Adds all KeyValue-Pairs from the given tempTable to the OSMEntity.
	 * 
	 * @param entity
	 * @param tempTable
	 */
	protected void addTagsFromTemp(OSMEntity entity, String tempTable){
		
		StringBuffer sql = new StringBuffer();
		
		try {
			
			sql.append(selectHstore1);
			sql.append(tempTable);
			sql.append(selectHstore2);
			sql.append(entity.getOsmId());
			sql.append(selectHstore3);
			
			ResultSet res = exec(sql.toString());
			
			while(res != null && res.next()){
				entity.putTag(res.getString(HSTORE_KEY), res.getString(HSTORE_VALUE));
			}
		}
		catch (Exception e) {
			logger.error("Failed to add HStore Values from " + tempTable);
		}
	}
	
	/**
	 * Executes SQL Scripts
	 * @param sql
	 * @return
	 */
	protected ResultSet exec(String sql) {
		Statement st;
		try {
			st = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);

			st.execute(sql);
			return st.getResultSet();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("SQL File could not been executed");
			logger.error(sql);
			return null;
		}
	}

}

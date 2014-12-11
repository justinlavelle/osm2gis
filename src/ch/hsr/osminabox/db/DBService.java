package ch.hsr.osminabox.db;

import java.util.List;

import ch.hsr.osminabox.db.dbdefinition.Database;
import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.Way;

public interface DBService {

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.db.DBService#connect(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean connect(String host, String port, String database,
			String username, String password);

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.db.DBService#isConnected()
	 */
	public boolean isConnected();

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.db.DBService#createDatabase()
	 */
	public void createDatabase();

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.db.DBService#createTempTables()
	 */
	public boolean createTempTables();

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.db.DBService#removeTempTables()
	 */
	public void removeTempTables();

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.db.DBService#insertPoints(java.util.List)
	 */
	public void insertNodes(List<Node> nodes);

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.db.DBService#insertWays(java.util.List)
	 */
	public void insertWays(List<Way> ways);

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.db.DBService#insertRelations(java.util.List)
	 */
	public void insertRelations(List<Relation> relations);
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.db.DBService#insertAreas(java.util.List)
	 */
	public void insertAreas(List<Area> areas);
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.db.DBService#insertRemainingRelations()
	 */
	public void insertTempRelations();

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.db.DBService#insertRemainingAreas()
	 */
	public void insertRemainingAreas();

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.db.DBService#deletePoints(java.util.List)
	 */
	public void deleteNodes(List<Node> nodes);

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.db.DBService#deleteRelations(java.util.List)
	 */
	public void deleteRelations(List<Relation> relations);

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.db.DBService#deleteWays(java.util.List)
	 */
	public void deleteWays(List<Way> ways);

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.db.DBService#modifyPoints(java.util.List)
	 */
	public void modifyNodes(List<Node> nodes);
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.db.DBService#modifyWays(java.util.List)
	 */
	public void modifyWays(List<Way> ways);
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.db.DBService#modifyAreas(java.util.List)
	 */
	public void modifyAreas(List<Area> areas);

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.db.DBService#modifyRelations(java.util.List)
	 */
	public void modifyRelations(List<Relation> relations);

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.db.DBService#getDBStructure()
	 */
	public Database getDBStructure();

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.db.DBService#dropTables()
	 */
	public boolean dropTables();

	public void deleteAreas(List<Area> entitys);

	public void modifyTempRelations();

	public void modifyRemainingAreas();
}
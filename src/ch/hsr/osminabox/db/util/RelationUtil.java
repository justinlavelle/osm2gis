package ch.hsr.osminabox.db.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.db.DBConstants;
import ch.hsr.osminabox.db.differentialupdate.ModificationType;
import ch.hsr.osminabox.db.entities.OSMEntity;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.RelationMappingEntry;
import ch.hsr.osminabox.db.entities.RelationMember;
import ch.hsr.osminabox.db.entities.RelationMemberType;
import ch.hsr.osminabox.db.sql.util.HStoreUtil;
import ch.hsr.osminabox.schemamapping.ConfigService;
import ch.hsr.osminabox.schemamapping.xml.MappingType;
import ch.hsr.osminabox.schemamapping.xml.RelatedTable;

/**
 * Helper Class for the relation handling
 * 
 * @author jzimmerm
 * 
 */
public class RelationUtil {

	private static final int RELATION_BUFFER_SIZE = 10;

	private Connection connection;
	private ConfigService config;
	private HStoreUtil hstoreUtil;

	private static Logger logger = Logger.getLogger(RelationUtil.class);

	private final String TABLENAME = "[tablename]";
	private final String UNION = " UNION ";
	private final String DIFFTYPE = "[difftype]";

	private final String selectSql = "SELECT " + DBConstants.ATTR_ID + ", "
			+ DBConstants.ATTR_OSM_ID + ", '" + TABLENAME
			+ "' AS tablename FROM " + TABLENAME + " WHERE ";

	private final String selectTempSql = "SELECT * FROM "
			+ DBConstants.RELATION_TEMP + " WHERE " + DBConstants.ATTR_OSM_ID
			+ " NOT IN(" + "SELECT "
			+ DBConstants.ATTR_RELATION_MEMBER_TEMP_RELATION_OSM_ID + " FROM "
			+ DBConstants.RELATION_MEMBER_TEMP + " WHERE "
			+ DBConstants.ATTR_RELATION_TEMP_DIFFTYPE + "='" + DIFFTYPE
			+ "' AND " + DBConstants.ATTR_RELATION_MEMBER_TEMP_MEMBER_OSM_ID
			+ " IN (" + "SELECT " + DBConstants.ATTR_OSM_ID + " FROM "
			+ DBConstants.RELATION_TEMP + ") " + "AND LOWER("
			+ DBConstants.ATTR_RELATION_MEMBER_TEMP_TYPE + ") = LOWER('"
			+ RelationMemberType.RELATION + "') GROUP BY "
			+ DBConstants.ATTR_RELATION_MEMBER_TEMP_RELATION_OSM_ID + ")"
			+ " LIMIT " + RELATION_BUFFER_SIZE + ";";

	private final String deleteTempMemberSql = "DELETE FROM "
			+ DBConstants.RELATION_MEMBER_TEMP + " WHERE "
			+ DBConstants.ATTR_RELATION_MEMBER_TEMP_MEMBER_OSM_ID + " NOT IN "
			+ "(SELECT " + DBConstants.ATTR_OSM_ID + " FROM "
			+ DBConstants.RELATION_TEMP + ")" + " AND UPPER("
			+ DBConstants.ATTR_RELATION_MEMBER_TEMP_TYPE + ") LIKE UPPER('"
			+ RelationMemberType.RELATION + "');";

	private final String deleteTempSql = "DELETE FROM "
			+ DBConstants.RELATION_TEMP + " WHERE " + DBConstants.ATTR_OSM_ID
			+ " NOT IN (SELECT "
			+ DBConstants.ATTR_RELATION_MEMBER_TEMP_RELATION_OSM_ID + " FROM "
			+ DBConstants.RELATION_MEMBER_TEMP + ")";

	private final String deleteTempByIdSql = "DELETE FROM "
			+ DBConstants.RELATION_TEMP + " WHERE " + DBConstants.ATTR_OSM_ID
			+ " IN (";

	private final String deleteTempMemberByIdSql = "DELETE FROM "
			+ DBConstants.RELATION_MEMBER_TEMP + " WHERE "
			+ DBConstants.ATTR_RELATION_MEMBER_TEMP_RELATION_OSM_ID + " IN (";

	/**
	 * 
	 * @param connection
	 *            Connection to the Database
	 */
	public RelationUtil(Connection connection, ConfigService config) {
		this.connection = connection;
		this.config = config;
		this.hstoreUtil = new HStoreUtil(connection);
	}

	/**
	 * Executes SQL Scripts
	 * 
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
			logger.error("SQL File could not been executed.: " + sql);
			return null;
		}
	}

	/**
	 * Removes all Relations from the List which have no dbMapping
	 * 
	 * @param relations
	 */
	public void removeUnmappedRelations(List<Relation> relations) {

		int sizeBefore = relations.size();

		for (Iterator<Relation> iter = relations.iterator(); iter.hasNext();) {
			Relation relation = iter.next();
			if (relation.dbMappings.size() <= 0)
				iter.remove();
		}

		if (sizeBefore > relations.size())
			logger.info("Deleted " + (sizeBefore - relations.size())
					+ " Relation(s) without a Mapping.");
	}

	/**
	 * Gets all Member Ids for each dbMapping and every Member. (Members can
	 * have multiple Ids, one for every dbMapping)
	 * 
	 * If, for any Mapping, all Members are required to be in one of the related
	 * tables and one is missing, the Mapping is removed!!!
	 * 
	 * @param relations
	 */
	public void addMemberIdsFromDb(List<Relation> relations) {
		logger.debug("Retrieving all referenced Member PKs from any Member Table. Deleting Relations with required, but missing, Members");
		for (Relation relation : relations) {
			Map<MappingType, List<RelationMember>> membersByType = groupMembersByType(relation);
			for (Iterator<Entry<String, RelationMappingEntry>> iter = relation.dbMappings
					.entrySet().iterator(); iter.hasNext();) {
				RelationMappingEntry relationMappingEntry = iter.next()
						.getValue();
				Map<MappingType, List<String>> groupedRelatedTablesByType = groupRelatedTablesByMappingType(relationMappingEntry);

				int membersFound = countMembers(membersByType,
						relationMappingEntry, groupedRelatedTablesByType);

				// If all members must be found, remove the mappingEntry if some
				// are missing.
				if (relationMappingEntry.allMembersRequiered) {
					RelationMember[] memberArray = new RelationMember[relation.members.size()];
					memberArray = relation.members.toArray(memberArray);
					int duplicateMembers = countDuplicateMembers(memberArray);
					if (membersFound < (relation.members.size() - duplicateMembers)) {
						logger.info("Found "
										+ membersFound
										+ " referenced Members in database. Total amount of RelationMembers: "
										+ (relation.members.size() - duplicateMembers)
										+ " in Relation with Osm Id: "
										+ relation.getOsmId());
						logger.info("Required RelationMembers missing, removing Mapping from Relation with Osm Id: "
										+ relation.getOsmId());
						iter.remove();
					}
				}
			}
		}
	}

	protected int countDuplicateMembers(RelationMember[] memberArray) {
		int duplicateMembers = 0;
		for (int i = 0; i < memberArray.length; i++) {
			for (int j = i + 1; j < memberArray.length; j++) {
				if (memberArray[i].osmId == memberArray[j].osmId
						&& memberArray[i].type.equals(memberArray[j].type))
					duplicateMembers++;
			}
		}
		return duplicateMembers;
	}

	protected int countMembers(
			Map<MappingType, List<RelationMember>> membersByType,
			RelationMappingEntry relationMappingEntry,
			Map<MappingType, List<String>> groupedRelatedTablesByType) {
		int membersFound = 0;
		for (Entry<MappingType, List<String>> groupedTableEntry : groupedRelatedTablesByType
				.entrySet()) {
			if (membersByType.get(groupedTableEntry.getKey()).size() > 0)
				membersFound += addIdsOfMembers(membersByType
						.get(groupedTableEntry.getKey()),
						relationMappingEntry.dbIdsToMember, groupedTableEntry
								.getValue());
		}
		return membersFound;
	}

	protected Map<MappingType, List<String>> groupRelatedTablesByMappingType(
			RelationMappingEntry relationMappingEntry) {
		Map<MappingType, List<String>> groupedRelatedTablesByType = new HashMap<MappingType, List<String>>();

		for (RelatedTable table : relationMappingEntry.relatedTables) {
			List<String> values = groupedRelatedTablesByType.get(config
					.getGeomTypeOfTable(table.getName()));
			if (values == null) {
				values = new LinkedList<String>();
				groupedRelatedTablesByType.put(config.getGeomTypeOfTable(table
						.getName()), values);
			}
			values.add(table.getName());
		}
		return groupedRelatedTablesByType;
	}

	protected Map<MappingType, List<RelationMember>> groupMembersByType(
			Relation relation) {
		Map<MappingType, List<RelationMember>> membersByType = new HashMap<MappingType, List<RelationMember>>();
		membersByType.put(MappingType.POINT, new LinkedList<RelationMember>());
		membersByType.put(MappingType.LINESTRING,
				new LinkedList<RelationMember>());
		membersByType.put(MappingType.RELATION,
				new LinkedList<RelationMember>());

		for (RelationMember member : relation.members) {
			if (member.type.equals(RelationMemberType.NODE))
				membersByType.get(MappingType.POINT).add(member);
			else if (member.type.equals(RelationMemberType.WAY))
				membersByType.get(MappingType.LINESTRING).add(member);
			else if (member.type.equals(RelationMemberType.RELATION))
				membersByType.get(MappingType.RELATION).add(member);
		}
		return membersByType;
	}

	/**
	 * Adds the Ids of the found members to the idsPerTable Map.
	 * 
	 * @param members
	 * @param idsPerTable
	 * @return Number of found Ids
	 */
	protected int addIdsOfMembers(List<RelationMember> members,
			Map<String, Map<Integer, RelationMember>> dbIdsToMember,
			List<String> tables) {
		StringBuffer sqlMemberOsmIds = new StringBuffer();

		int i = 0;
		for (RelationMember member : members) {
			sqlMemberOsmIds.append(DBConstants.ATTR_OSM_ID);
			sqlMemberOsmIds.append("=");
			sqlMemberOsmIds.append(member.osmId);

			i++;
			if (i < members.size())
				sqlMemberOsmIds.append(" OR ");

		}

		StringBuffer sql = new StringBuffer();

		i = 0;
		for (String relatedTable : tables) {
			sql.append(selectSql.replaceAll(Pattern.quote(TABLENAME),
					relatedTable));
			sql.append(sqlMemberOsmIds);

			i++;
			if (i < tables.size())
				sql.append(UNION);
		}

		ResultSet res = exec(sql.toString());

		int membersFound = 0;
		try {
			while (res.next()) {
				String relatedTableName = res
						.getString(DBConstants.ATTR_TABLENAME);
				int dbId = res.getInt(DBConstants.ATTR_ID);
				int osmId = res.getInt(DBConstants.ATTR_OSM_ID);

				// Assign the dbId to the RelationMember with the Osm Id of this
				// entry.
				Map<Integer, RelationMember> idsOfMembers = dbIdsToMember
						.get(relatedTableName);

				for (RelationMember member : members) {
					if (member.osmId == osmId)
						idsOfMembers.put(dbId, member);
				}

				membersFound++;
			}
		} catch (SQLException e) {
			logger.error("SQL Exception while gathering RelationMember Ids."
					+ e);
			return 0;
		}
		return membersFound;
	}

	/**
	 * Looks up all the auto incremented primary keys from the given relations
	 * and adds them to their mappingEntryies.
	 * 
	 * @param relations
	 */
	public void addRelationIdsFromDb(List<Relation> relations) {

		logger
				.debug("Retrieving PKs for every Relation and every Table it was inserted before.");

		StringBuffer sqlOsmIds = new StringBuffer();
		Set<String> tables = new HashSet<String>();

		int i = 0;
		for (Relation relation : relations) {

			sqlOsmIds.append(DBConstants.ATTR_OSM_ID);
			sqlOsmIds.append("=");
			sqlOsmIds.append(relation.getOsmId());

			i++;

			if (i < relations.size())
				sqlOsmIds.append(" OR ");

			for (String tableName : relation.dbMappings.keySet()) {
				tables.add(tableName);
			}
		}

		StringBuffer sql = new StringBuffer();

		i = 0;
		for (String tableName : tables) {
			sql.append(selectSql
					.replaceAll(Pattern.quote(TABLENAME), tableName));
			sql.append(sqlOsmIds);

			i++;
			if (i < tables.size())
				sql.append(UNION);
		}

		ResultSet res = exec(sql.toString());

		try {
			while (res.next()) {
				int osmId = res.getInt(DBConstants.ATTR_OSM_ID);
				int dbId = res.getInt(DBConstants.ATTR_ID);
				String tableName = res.getString(DBConstants.ATTR_TABLENAME);

				for (Relation relation : relations) {
					if (relation.getOsmId() == osmId) {
						RelationMappingEntry mappingEntry = relation.dbMappings
								.get(tableName);
						mappingEntry.relationDbId = dbId;
						break;
					}
				}
			}
		} catch (SQLException e) {
			logger.error("SQL Exception while gathering Relation Ids." + e);
		}

	}

	/**
	 * Reads the next Relations from the relation_temp Table and deletes them
	 * afterwards.
	 * 
	 * @return The DiffType of the parent tag from the xml file (create, modify
	 *         or delete)
	 */
	public DiffType getNextTempRelation(List<Relation> relations) {
		DiffType diffType = getNextTempRelation(relations, DiffType.create);
		if (relations.size() <= 0)
			diffType = getNextTempRelation(relations, DiffType.modify);
		if (relations.size() <= 0)
			diffType = getNextTempRelation(relations, DiffType.delete);

		return diffType;
	}

	protected DiffType getNextTempRelation(List<Relation> relations,
			DiffType diffType) {

		StringBuffer deleteIds = new StringBuffer();

		try {

			ResultSet res = exec(selectTempSql.replaceAll(Pattern
					.quote(DIFFTYPE), diffType.toString()));

			while (res.next()) {
				String relationmember = res
						.getString(DBConstants.ATTR_RELATION_TEMP_MEMBER);
				if (!(relationmember == null || relationmember.length() <= 0)){
					Relation relation = new Relation();
					relation.setOsmId(res.getInt(DBConstants.ATTR_OSM_ID));
					relation.attributes
							.put(OSMEntity.ATTRIBUTE_TIMESTAMP, String.valueOf(res
									.getTimestamp(DBConstants.ATTR_LASTCHANGE)));
					addRelationMembers(relation, res
							.getString(DBConstants.ATTR_RELATION_TEMP_MEMBER));
					hstoreUtil.addTagsFromTemp(relation);
					relations.add(relation);

					deleteIds.append(relation.getOsmId());
				}

				if (!res.isLast())
					deleteIds.append(",");
				else {
					deleteIds.append(")");
				}
			}
			String sql = deleteIds.toString().replace(",)", ")");
			if(sql.equals("")){
				return diffType;
			}
			// Delete the Relations so we don't read them twice
			exec(deleteTempByIdSql + sql);
			exec(deleteTempMemberByIdSql + sql);
		} catch (Exception e) {
			logger
					.error("Failed retrieving Relation data from the relation_temp Table.");
			e.printStackTrace();
		}
		return diffType;
	}

	/**
	 * Creates RelationMembers out of a single String and adds them to the
	 * Relation.
	 * 
	 * @param relation
	 * @param memberString
	 */
	protected void addRelationMembers(Relation relation, String memberString) {
		String[] membersString = memberString
				.split(DBConstants.SQL_RELATION_TEMP_MEMBER_SPACER);

		for (String member : membersString) {
			String[] memberValues = member
					.split(DBConstants.SQL_RELATION_TEMP_MEMBER_VALUE_SPACER);
			try {
				RelationMember m = new RelationMember();
				m.osmId = Long.parseLong(memberValues[0]);
				m.type = RelationMemberType.valueOf(memberValues[1]);
				if (memberValues.length > 2)
					m.role = memberValues[2];
				else
					m.role = "";

				relation.members.add(m);
			} catch (Exception e) {
				logger.error("Could not retreive RelationMember data from "
						+ DBConstants.RELATION_TEMP
						+ " Table for Relation with Osm Id: "
						+ relation.getOsmId());
				logger.error(e.getMessage());
			}
		}
	}

	/**
	 * Splits the Relations in the given list into three groups according to the
	 * needed modifications. Can duplicate Relations if, for example, a Relation
	 * needs to be deleted in one Table and created in another.
	 * 
	 * @param relations
	 * @return
	 */
	public Map<ModificationType, List<Relation>> splitToModificationGroups(
			List<Relation> relations) {
		List<Relation> insertRelations = new LinkedList<Relation>();
		List<Relation> updateRelations = new LinkedList<Relation>();
		List<Relation> deleteRelations = new LinkedList<Relation>();

		Map<ModificationType, List<Relation>> relationGroups = new HashMap<ModificationType, List<Relation>>();
		relationGroups.put(ModificationType.INSERT, insertRelations);
		relationGroups.put(ModificationType.UPDATE, updateRelations);
		relationGroups.put(ModificationType.DELETE, deleteRelations);

		for (Relation relation : relations) {

			StringBuffer sql = new StringBuffer();
			Set<String> insertTables = new HashSet<String>();
			Set<String> updateTables = new HashSet<String>();
			Set<String> deleteTables = new HashSet<String>();

			int i = 0;
			if (relation.dbMappings.size() > 0) {
				for (String table : relation.dbMappings.keySet()) {
					sql.append(selectSql.replaceAll(Pattern.quote(TABLENAME),
							table));
					sql.append(DBConstants.ATTR_OSM_ID);
					sql.append("=");
					sql.append(relation.getOsmId());

					i++;
					if (i < relation.dbMappings.size())
						sql.append(UNION);
				}

				ResultSet res = exec(sql.toString());

				try {
					while (res.next()) {
						// Gather Tables for UPDATE
						updateTables.add(res
								.getString(DBConstants.ATTR_TABLENAME));
					}
				} catch (SQLException e) {
					logger
							.error("SQL Exception while splitting Relation list to modification group lists.");
					e.printStackTrace();
				}
			}

			// Gather Tables for INSERT
			insertTables.addAll(relation.dbMappings.keySet());
			insertTables.removeAll(updateTables);

			Set<String> unmappedTables = config
					.getTablesOfGeomType(MappingType.RELATION);
			unmappedTables.removeAll(relation.dbMappings.keySet());

			sql = new StringBuffer();

			i = 0;
			if (unmappedTables.size() > 0) {
				for (String table : unmappedTables) {
					sql.append(selectSql.replaceAll(Pattern.quote(TABLENAME),
							table));
					sql.append(DBConstants.ATTR_OSM_ID);
					sql.append("=");
					sql.append(relation.getOsmId());

					i++;
					if (i < unmappedTables.size())
						sql.append(UNION);
				}

				ResultSet res = exec(sql.toString());

				try {
					while (res.next()) {
						// Gather Tables for DELETE
						deleteTables.add(res
								.getString(DBConstants.ATTR_TABLENAME));
					}
				} catch (SQLException e) {
					logger.error("SQL Exception while looking up Tables: ");
					logger.error(sql.toString());
					e.printStackTrace();
				}
			}

			// Relation with dbMappings for DELETION
			Relation deleteRelation = new Relation(relation);
			deleteRelation.dbMappings.clear();
			for (String deleteTable : deleteTables)
				deleteRelation.dbMappings.put(deleteTable,
						new RelationMappingEntry());

			// Relation with dbMappings for INSERTION
			Relation insertRelation = new Relation(relation);
			insertRelation.dbMappings.clear();

			// Relation with dbMappings for UPDATEING
			Relation updateRelation = new Relation(relation);
			updateRelation.dbMappings.clear();

			for (String table : relation.dbMappings.keySet()) {
				if (insertTables.contains(table))
					insertRelation.dbMappings.put(table, relation.dbMappings
							.get(table));

				else if (updateTables.contains(table))
					updateRelation.dbMappings.put(table, relation.dbMappings
							.get(table));
			}

			if (insertRelation.dbMappings.size() > 0)
				insertRelations.add(insertRelation);
			if (updateRelation.dbMappings.size() > 0)
				updateRelations.add(updateRelation);
			if (deleteRelation.dbMappings.size() > 0)
				deleteRelations.add(deleteRelation);
		}

		return relationGroups;
	}

	/**
	 * Deletes all entries of type=relation in relation_member_temp where the
	 * member_id is not available in the relation_temp Table. Deletes all
	 * relations which have no members.
	 */
	public void deleteUnavailableRelationMembers() {
		logger.info("Deleting all invalid Relations in "
				+ DBConstants.RELATION_TEMP
				+ " for the rest to be processed normally.");
		exec(deleteTempMemberSql);
		exec(deleteTempSql);
	}
}

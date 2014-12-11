package ch.hsr.osminabox.db.differentialupdate;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.db.dbdefinition.Database;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.handlingstrategy.NodeHandlingStrategy;
import ch.hsr.osminabox.db.sql.Constants;
import ch.hsr.osminabox.db.sql.util.DBUtil;
import ch.hsr.osminabox.db.util.ValueConverter;
import ch.hsr.osminabox.schemamapping.xml.Column;

public class UpdateNodeHandlingStrategy implements NodeHandlingStrategy {

	Logger logger = Logger.getLogger(UpdateNodeHandlingStrategy.class);

	protected DBUtil dbUtil;
	protected ValueConverter valueConverter;

	public UpdateNodeHandlingStrategy(Database dbStructure) {
		dbUtil = new DBUtil(dbStructure);
		valueConverter = new ValueConverter();
	}

	/**
	 * Creates the Update SQL Script for each Table this node is mapped to.
	 * 
	 * @param node
	 * @return
	 */
	@Override
	public void addNode(Node node, Map<String, StringBuffer> statements) {

		for (String tableName : node.dbMappings.keySet()) {
			try {
				StringBuffer buffer = statements.get(tableName);

				if (buffer == null)
					continue;

				Map<String, String> columns = new HashMap<String, String>();

				for (Column column : node.dbMappings.get(tableName)) {
					logger.debug(""+column);
					logger.debug(""+valueConverter);
					columns.put(column.getName(), valueConverter.convertValue(
							column.getValue(), node));
				}

				buffer.append(dbUtil.createUpdateBegin(tableName));

				buffer.append(dbUtil.addUpdateValues(tableName, columns));

				buffer.append(dbUtil.addWhere(node.getOsmId()));

				dbUtil.checkAndAppendSpacer(buffer, Constants.SEMICOLON);
			} catch (Exception e) {
				logger
						.error("Exception while creating SQL-Update for Node with OSM Id: "
								+ node.getOsmId());
				e.printStackTrace();
				continue;
			}
		}
	}

	@Override
	public void addTemp(Node node, Map<String, StringBuffer> statements) {
	}

}

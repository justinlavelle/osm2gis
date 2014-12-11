package ch.hsr.osminabox.db.handling;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.easymock.Capture;
import org.easymock.EasyMockSupport;
import org.easymock.IAnswer;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.db.dbdefinition.Database;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.OSMEntity;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.handlingstrategy.AreaHandlingStrategy;
import ch.hsr.osminabox.db.handlingstrategy.NodeHandlingStrategy;
import ch.hsr.osminabox.db.handlingstrategy.RelationHandlingStrategy;
import ch.hsr.osminabox.db.handlingstrategy.WayHandlingStrategy;
import ch.hsr.osminabox.db.sql.util.DBUtil;
import ch.hsr.osminabox.db.util.DiffType;
import ch.hsr.osminabox.schemamapping.ConfigService;
import ch.hsr.osminabox.schemamapping.xml.MappingType;
import ch.hsr.osminabox.test.Util;

public class HandlerTestTemplate extends EasyMockSupport {

	protected NodeHandlingStrategy nodeInsertStrategy;
	protected NodeHandlingStrategy nodeUpdateStrategy;
	protected WayHandlingStrategy wayInsertStrategy;
	protected WayHandlingStrategy wayUpdateStrategy;
	protected RelationHandlingStrategy relationInsertStrategy;
	protected RelationHandlingStrategy relationUpdateStrategy;
	protected AreaHandlingStrategy areaInsertStrategy;
	protected AreaHandlingStrategy areaUpdateStrategy;
	protected ApplicationContext context;
	protected Database dbStructure;
	protected DBUtil util;

	static enum QUERY_TYPE {
		INSERT_NODE, INSERT_TEMP_NODE, UPDATE_NODE, 
		INSERT_WAY, INSERT_TEMP_WAY, UPDATE_WAY, 
		INSERT_RELATION, INSERT_TEMP_RELATION, INSERT_TEMP_RELATION_MEMBER, UPDATE_RELATION, INSERT_RELATION_JOINS;
	}

	public void setUp() throws Exception {
		BasicConfigurator.configure();
		context = createMock(ApplicationContext.class);
		dbStructure = createMock(Database.class);
		util = createMock(DBUtil.class);
		nodeInsertStrategy = createMock(NodeHandlingStrategy.class);
		nodeUpdateStrategy = createMock(NodeHandlingStrategy.class);
		wayInsertStrategy = createMock(WayHandlingStrategy.class);
		wayUpdateStrategy = createMock(WayHandlingStrategy.class);
		relationInsertStrategy = createMock(RelationHandlingStrategy.class);
		relationUpdateStrategy = createMock(RelationHandlingStrategy.class);
		areaInsertStrategy = createMock(AreaHandlingStrategy.class);
		areaUpdateStrategy = createMock(AreaHandlingStrategy.class);
	}

	protected void expectAddEndTagsCall(final List<String> tables) {
		final Capture<HashMap<String, StringBuffer>> mapCapture = new Capture<HashMap<String, StringBuffer>>();
		expect(util.addEndTags(capture(mapCapture))).andAnswer(
				new IAnswer<HashMap<String, StringBuffer>>() {

					@Override
					public HashMap<String, StringBuffer> answer()
							throws Throwable {
						HashMap<String, StringBuffer> map = mapCapture
								.getValue();
						for (String table : tables) {
							assertTrue(map.containsKey(table));
							map.get(table).append("<END TAG>");
						}
						return map;
					}
				});
	}

	protected void expectTablesOfGeomTypeCall(MappingType mappingType) {
		ConfigService config = createMock(ConfigService.class);
		expect(context.getConfigService()).andReturn(config);
		expect(config.getTablesOfGeomType(mappingType)).andReturn(
				Util.asSet("table1", "table2"));
	}

	protected void expectGetJoinTablesCall() {
		ConfigService config = createMock(ConfigService.class);
		expect(context.getConfigService()).andReturn(config);
		expect(config.getMappingJoinTables()).andReturn(
				Util.asSet("table1", "table2"));
	}

	protected void expectDBQueryCall(final OSMEntity osmEntity,
			final List<String> tables, final QUERY_TYPE type) {
		final Capture<Map<String, StringBuffer>> mapCapture = new Capture<Map<String, StringBuffer>>();
		switch (type) {
		case INSERT_NODE:
			nodeInsertStrategy.addNode(eq((Node) osmEntity),
					capture(mapCapture));
			break;
		case INSERT_TEMP_NODE:
			nodeInsertStrategy.addTemp(eq((Node) osmEntity),
					capture(mapCapture));
			break;
		case UPDATE_NODE:
			nodeUpdateStrategy.addNode(eq((Node) osmEntity),
					capture(mapCapture));
			break;
		case INSERT_WAY:
			wayInsertStrategy.addWay(eq((Way) osmEntity), capture(mapCapture));
			break;
		case UPDATE_WAY:
			wayUpdateStrategy.addWay(eq((Way) osmEntity), capture(mapCapture));
			break;
		case INSERT_TEMP_WAY:
			wayInsertStrategy.addTemp(eq((Way) osmEntity), eq(DiffType.create),
					capture(mapCapture));
			break;
		case INSERT_RELATION:
			relationInsertStrategy.addRelation(eq((Relation) osmEntity),
					capture(mapCapture));
			break;
		case INSERT_TEMP_RELATION:
			relationInsertStrategy.addTemp(eq((Relation) osmEntity),
					eq(DiffType.create), capture(mapCapture));
			break;
		case INSERT_TEMP_RELATION_MEMBER:
			relationInsertStrategy.addTempMembers(eq((Relation) osmEntity),
					capture(mapCapture));
			break;
		case INSERT_RELATION_JOINS:
			relationInsertStrategy.addJoins(eq((Relation) osmEntity),
					capture(mapCapture));
			break;
		case UPDATE_RELATION:
			relationUpdateStrategy.addRelation(eq((Relation) osmEntity),
					capture(mapCapture));
			break;
		default:
			throw new RuntimeException("no type " + type);
		}
		expectLastCall().andAnswer(new IAnswer<Object>() {

			@Override
			public Object answer() throws Throwable {
				Map<String, StringBuffer> map = mapCapture.getValue();
				for (String table : tables) {
					assertTrue(map.containsKey(table));
					map.get(table).append(
							"<" + type + " " + osmEntity.getOsmId() + ">");
				}
				return null;
			}
		});
	}
}

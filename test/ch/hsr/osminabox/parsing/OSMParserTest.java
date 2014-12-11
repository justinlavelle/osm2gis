package ch.hsr.osminabox.parsing;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.BasicConfigurator;
import org.easymock.Capture;
import org.easymock.EasyMockSupport;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.db.DBService;
import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.OSMEntity;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.RelationMember;
import ch.hsr.osminabox.db.entities.RelationMemberType;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.importing.strategy.BufferStrategy;
import ch.hsr.osminabox.importing.strategy.CreateBufferStrategy;
import ch.hsr.osminabox.importing.xml.OSMChangeTagHandler;
import ch.hsr.osminabox.importing.xml.OSMTagHandler;
import ch.hsr.osminabox.importing.xml.XMLTagHandler;
import ch.hsr.osminabox.test.Util;

public class OSMParserTest extends EasyMockSupport {

	private OSMParser parser;
	private ApplicationContext context;
	private DBService db;

	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();
		context = createMock(ApplicationContext.class);
		parser = new OSMParser(context);
	}

	@Test
	public void testParse() throws FileNotFoundException {
		InputStream in = new FileInputStream(new File(
				"test/ch/hsr/osminabox/parsing/testosm.osm"));
		expectSettingUpOfParser(true);
		expectFirstNodeInsert();
		expectSecondNodeInsert();
		expectInsertWays();
		expectInsertAreas();
		expectInsertRelations();
		replayAll();
		BufferStrategy strategy = new CreateBufferStrategy(context);
		parser.parse(in, new OSMTagHandler(context, strategy));
	}

	@Test
	public void testParseInitialImport() {
		parser = createMockBuilder(OSMParser.class).addMockedMethod("parse")
				.createMock();
		parser.context = context;
		InputStream in = createMock(InputStream.class);
		expectSettingUpOfParser(false);
		final Capture<XMLTagHandler> handlerCapture = new Capture<XMLTagHandler>();
		parser.parse(eq(in), capture(handlerCapture));
		expectLastCall().andAnswer(new IAnswer<Object>() {

			@Override
			public Object answer() throws Throwable {
				assertEquals(OSMTagHandler.class, handlerCapture.getValue()
						.getClass());
				return null;
			}
		});
		expect(context.getDBService()).andReturn(db);
		db.insertTempRelations();
		db.insertRemainingAreas();
		context.removeTempTables();
		replayAll();
		parser.parseInitialImport(in);
		verifyAll();
	}

	@Test
	public void testParseUpdate() {
		parser = createMockBuilder(OSMParser.class).addMockedMethod("parse")
				.createMock();
		parser.context = context;
		InputStream in = createMock(InputStream.class);
		db = createMock(DBService.class);
		expect(context.getDBService()).andReturn(db);
		for (int i = 0; i < 3; i++) {
			expect(context.getConfigParameter("buffersize.node"))
					.andReturn("2");
			expect(context.getConfigParameter("buffersize.way")).andReturn("2");
			expect(context.getConfigParameter("buffersize.relation"))
					.andReturn("2");
			expect(context.getConfigParameter("buffersize.area"))
					.andReturn("2");
			expect(context.getDBService()).andReturn(db);
		}
		final Capture<XMLTagHandler> handlerCapture = new Capture<XMLTagHandler>();
		parser.parse(eq(in), capture(handlerCapture));
		expectLastCall().andAnswer(new IAnswer<Object>() {

			@Override
			public Object answer() throws Throwable {
				assertEquals(OSMChangeTagHandler.class, handlerCapture
						.getValue().getClass());
				return null;
			}
		});
		db.modifyTempRelations();
		db.modifyRemainingAreas();
		context.removeTempTables();
		replayAll();
		parser.parseUpdate(in);
		verifyAll();
	}

	private void assertTag(Map<String, Set<String>> tags, String key, String value) {
		assertEquals(Util.asSet(value), tags.get(key));
	}

	private void expectInsertAreas() {
		final Capture<List<Area>> areaCapture = new Capture<List<Area>>();
		db.insertAreas(capture(areaCapture));
		expectLastCall().andAnswer(new IAnswer<Object>() {
			@Override
			public Object answer() throws Throwable {
				List<Area> areas = areaCapture.getValue();
				assertEquals(1, areas.size());
				Area a = areas.get(0);
				assertEquals(142, a.getOsmId());
				assertEquals(a.attributes, Util.asMap(
				"timestamp", "2001-01-01T00:00:00.0+11:00", "user", "Testing",
				"uid", "345", "id", "142", "visible",
				"true"));
				assertEquals(Util.asSet(131L, 132L), a.wayIds.keySet());
				return null;
			}
		});
	}
	
	private void expectInsertRelations() {
		final Capture<List<Relation>> relationCapture = new Capture<List<Relation>>();
		db.insertRelations(capture(relationCapture));
		expectLastCall().andAnswer(new IAnswer<Object>() {
			@Override
			public Object answer() throws Throwable {
				List<Relation> relations = relationCapture.getValue();
				assertEquals(2, relations.size());
				Relation r1 = relations.get(0);
				assertOSMEntity(r1, 141, Util.asMap("timestamp", "2001-01-01T00:00:00.0+11:00", "user", "Testing",
						"uid", "345", "id", "141", "visible",
						"true"), Util.asMap("restriction", "no_right_turn", "created_by", "Testing", "type", "restriction"));
				assertEquals(3, r1.members.size());
				assertRelationMember(r1.members.get(0), 124, "via", RelationMemberType.NODE);
				assertRelationMember(r1.members.get(1), 131, "to", RelationMemberType.WAY);
				assertRelationMember(r1.members.get(2), 132, "from", RelationMemberType.WAY);
				Relation r2 = relations.get(1);
				assertOSMEntity(r2, 142, Util.asMap("timestamp", "2001-01-01T00:00:00.0+11:00", "user", "Testing",
						"uid", "345", "id", "142", "visible",
						"true"), Util.asMap("created_by", "Testing", "type", "multipolygon"));
				assertEquals(2, r2.members.size());
				assertRelationMember(r2.members.get(0), 131, "outer", RelationMemberType.WAY);
				assertRelationMember(r2.members.get(1), 132, "inner", RelationMemberType.WAY);
				return null;
			}
		});
	}
	
	private void assertOSMEntity(OSMEntity entity, int osmId,
			Map<String, String> attributes, Map<String, String> tags) {
		assertEquals(osmId, entity.getOsmId());
		assertEquals(tags.size(), entity.tags.size());
		assertEquals(attributes.size(), entity.attributes.size());
		assertEquals(entity.attributes, attributes);
		assertTags(entity.tags, tags);
	}

	private void assertTags(Map<String, Set<String>> entityTags, Map<String, String> tags) {
		for (Entry<String, String> entry : tags.entrySet()) {
			assertTag(entityTags, entry.getKey(), entry.getValue());
		}
	}

	private void expectInsertWays() {
		final Capture<List<Way>> waysCapture = new Capture<List<Way>>();
		db.insertWays(capture(waysCapture));
		expectLastCall().andAnswer(new IAnswer<Object>() {
			@Override
			public Object answer() throws Throwable {
				List<Way> ways = waysCapture.getValue();
				assertEquals(2, ways.size());
				Way w1 = ways.get(0);
				assertOSMEntity(w1, 131, Util.asMap(
						"timestamp", "2001-01-01T00:00:00.0+11:00", "user", "Testing",
						"uid", "345", "id", "131", "visible",
						"true"), Util.asMap("created_by", "Testing", "name", "Teststrasse"));
				assertEquals(2, w1.nodes.size());
				assertEquals(123, w1.nodes.get(0).getOsmId());
				assertEquals(124, w1.nodes.get(1).getOsmId());
				Way w2 = ways.get(1);
				assertOSMEntity(w2, 132, Util.asMap(
						"timestamp", "2001-01-01T00:00:00.0+11:00", "user", "Testing",
						"uid", "345", "id", "132", "visible",
						"true"), Util.asMap("created_by", "Testing", "name", "Musterweg"));
				assertEquals(2, w2.nodes.size());
				assertEquals(124, w2.nodes.get(0).getOsmId());
				assertEquals(125, w2.nodes.get(1).getOsmId());
				return null;
			}
		});
	}

	private void expectFirstNodeInsert() {
		final Capture<List<Node>> nodesCapture = new Capture<List<Node>>();
		db.insertNodes(capture(nodesCapture));
		expectLastCall().andAnswer(new IAnswer<Object>() {

			@Override
			public Object answer() throws Throwable {
				List<Node> nodes = nodesCapture.getValue();
				assertEquals(2, nodes.size());
				assertOSMEntity(nodes.get(0), 123, Util.asMap(
						Node.NODE_LATITUDE, "45.0", Node.NODE_LONGITUDE, "8.0",
						"uid", "345", "id", "123", "timestamp",
						"2001-01-01T00:00:00.0+11:00"), Util.asMap(
						"created_by", "Testing"));
				assertOSMEntity(nodes.get(1), 124, Util.asMap(
						Node.NODE_LATITUDE, "35.0", Node.NODE_LONGITUDE, "7.0",
						"uid", "345", "id", "124", "timestamp",
						"2001-01-01T00:00:00.0+11:00"), Util.asMap(
						"created_by", "Testing"));
				return null;
			}

		});
	}

	private void expectSecondNodeInsert() {
		final Capture<List<Node>> nodesCaprute = new Capture<List<Node>>();
		db.insertNodes(capture(nodesCaprute));
		expectLastCall().andAnswer(new IAnswer<Object>() {

			@Override
			public Object answer() throws Throwable {
				List<Node> nodes = nodesCaprute.getValue();
				assertEquals(1, nodes.size());
				assertOSMEntity(nodes.get(0), 125, Util.asMap(
						Node.NODE_LATITUDE, "25.0", Node.NODE_LONGITUDE, "6.0",
						"uid", "345", "id", "125", "timestamp",
						"2001-01-01T00:00:00.0+11:00"), Util.asMap(
						"created_by", "Testing"));
				return null;
			}

		});
	}

	private void expectSettingUpOfParser(boolean createTempTables) {
		db = createMock(DBService.class);
		expect(context.getDBService()).andReturn(db);
		expect(context.getConfigParameter("buffersize.node")).andReturn("2");
		expect(context.getConfigParameter("buffersize.way")).andReturn("10");
		expect(context.getConfigParameter("buffersize.relation"))
				.andReturn("10");
		expect(context.getConfigParameter("buffersize.area")).andReturn("10");
		if (createTempTables) {
			expect(context.createTempTables()).andReturn(true);
		}
	}

	private void assertRelationMember(RelationMember member, int osmId, String role, RelationMemberType type) {
		assertEquals(osmId, member.osmId);
		assertEquals(role, member.role);
		assertEquals(type, member.type);
	}

}

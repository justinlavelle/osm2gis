package ch.hsr.osminabox.db.util;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import java.util.HashSet;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.OSMEntity;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.RelationMember;
import ch.hsr.osminabox.db.entities.RelationMemberType;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.entities.exceptions.InvalidWayException;
import ch.hsr.osminabox.db.sql.area.GeomStrategy;
import ch.hsr.osminabox.db.sql.area.exceptions.NoWayValuesException;
import ch.hsr.osminabox.db.sql.util.GeomValueCreator;
import ch.hsr.osminabox.test.NodeCreator;
import ch.hsr.osminabox.test.Util;
import ch.hsr.osminabox.test.WayCreator;

public class ValueConverterTest extends EasyMockSupport {

	private static final String CONVERTED_VALUE = "convertedValue";
	private static final String PLAIN_INPUT_VALUE = "inputValue";
	private static final String UNCOMMON_INPUT_VALUE = "%geom%";
	private static final String COMMON_INPUT_VALUE = "%commonInputValue%";
	private static final String ALL_NODES = "%nd_all%";
	private static final String ALL_MEMBERS = "%members_all%";
	private static final String GEOM_VALUE = "geomValue";
	private ValueConverter converter;
	private GeomValueCreator creator;

	@Before
	public void setUp() throws Exception {
		creator = createMock(GeomValueCreator.class);
		converter = new ValueConverter() {
			@Override
			protected String convertCommonKeys(String key, OSMEntity entity) {
				if (key.equals("geom") || key.equals("nd_all")
						|| key.equals("members_all")) {
					return "";
				} else {
					return CONVERTED_VALUE;
				}

			}
		};
		converter.geomValues = creator;
	}

	@Test
	public void testConvertValueStringNode() {
		replayAll();
		Node node = NodeCreator.create(1, 45, 8).finish();
		assertEquals(PLAIN_INPUT_VALUE, converter.convertValue(
				PLAIN_INPUT_VALUE, node));
		assertEquals(CONVERTED_VALUE, converter.convertValue(
				COMMON_INPUT_VALUE, node));
		verifyAll();
		resetAll();
		expect(creator.addGeom(node)).andReturn(new StringBuffer(GEOM_VALUE));
		replayAll();
		assertEquals(GEOM_VALUE, converter.convertValue(UNCOMMON_INPUT_VALUE,
				node));
		verifyAll();
	}

	@Test
	public void testConvertValueStringWay() {
		replayAll();
		Way way = WayCreator.create(1).node(
				NodeCreator.create(2, 45, 8).finish()).node(
				NodeCreator.create(3, 35, 7).finish()).finish();
		assertEquals(PLAIN_INPUT_VALUE, converter.convertValue(
				PLAIN_INPUT_VALUE, way));
		assertEquals(CONVERTED_VALUE, converter.convertValue(
				COMMON_INPUT_VALUE, way));
		assertEquals("2;3;", converter.convertValue(ALL_NODES, way));
		verifyAll();
		resetAll();
		expect(creator.addGeom(way)).andReturn(new StringBuffer(GEOM_VALUE));
		replayAll();
		assertEquals(GEOM_VALUE, converter.convertValue(UNCOMMON_INPUT_VALUE,
				way));
		verifyAll();
	}

	@Test
	public void testConvertValueStringAreaGeomStrategy()
			throws InvalidWayException, NoWayValuesException {
		GeomStrategy geom = createMock(GeomStrategy.class);
		replayAll();
		Way way = WayCreator.create(1).node(
				NodeCreator.create(2, 45, 8).finish()).node(
				NodeCreator.create(3, 35, 7).finish()).node(
				NodeCreator.create(4, 25, 6).finish()).node(
				NodeCreator.create(2, 45, 8).finish()).finish();
		Area area = new Area(way);

		assertEquals(PLAIN_INPUT_VALUE, converter.convertValue(
				PLAIN_INPUT_VALUE, area, geom));
		assertEquals(CONVERTED_VALUE, converter.convertValue(
				COMMON_INPUT_VALUE, area, geom));
		verifyAll();
		resetAll();
		expect(geom.getGeom(area)).andReturn(new StringBuffer(GEOM_VALUE));
		replayAll();
		assertEquals(GEOM_VALUE, converter.convertValue(UNCOMMON_INPUT_VALUE,
				area, geom));
		verifyAll();
	}

	@Test
	public void testConvertValueStringRelation() {
		Relation rel = new Relation();
		rel.members
				.add(createRelationMember(1, RelationMemberType.NODE, "role"));
		rel.members
				.add(createRelationMember(2, RelationMemberType.WAY, "role2"));
		assertEquals(PLAIN_INPUT_VALUE, converter.convertValue(
				PLAIN_INPUT_VALUE, rel));
		assertEquals(CONVERTED_VALUE, converter.convertValue(
				COMMON_INPUT_VALUE, rel));
		assertEquals("1,NODE,role;2,WAY,role2", converter.convertValue(
				ALL_MEMBERS, rel));

	}

	@Test
	public void testConvertValueStringRelationMember() {
		RelationMember member = new RelationMember();
		member.osmId=1;
		member.type = RelationMemberType.NODE;
		member.role = "testrole";
		assertEquals(PLAIN_INPUT_VALUE, converter.convertValue(
				PLAIN_INPUT_VALUE, member));
		assertEquals("0", converter.convertValue(
				"%db_member_id%", member));
		assertEquals("0", converter.convertValue(
				"%db_relation_id%", member));
		assertEquals("NODE", converter.convertValue(
				"%member_type%", member));
		assertEquals("1", converter.convertValue(
				"%member_ref%", member));
		assertEquals("testrole", converter.convertValue(
				"%member_role%", member));
	}

	@Test
	public void testConvertValueStringIntIntRelationMember() {
		RelationMember member = new RelationMember();
		member.osmId=1;
		member.type = RelationMemberType.WAY;
		member.role = "testrole2";
		assertEquals(PLAIN_INPUT_VALUE, converter.convertValue(
				PLAIN_INPUT_VALUE, 2, 3, member));
		assertEquals("3", converter.convertValue(
				"%db_member_id%", 2, 3, member));
		assertEquals("2", converter.convertValue(
				"%db_relation_id%", 2, 3, member));
		assertEquals("WAY", converter.convertValue(
				"%member_type%", 2, 3, member));
		assertEquals("1", converter.convertValue(
				"%member_ref%", 2, 3, member));
		assertEquals("testrole2", converter.convertValue(
				"%member_role%", 2, 3, member));
	}

	@Test
	public void testMergeSetToDelimiterString() {
		assertEquals("", converter.mergeSetToDelimiterString(Util.asSet("")));
		assertEquals("v1", converter
				.mergeSetToDelimiterString(Util.asSet("v1")));
		assertEquals("v1;v2", converter.mergeSetToDelimiterString(Util.asSet(
				"v1", "v2")));
		assertEquals("v1;v2;v3", converter.mergeSetToDelimiterString(Util
				.asSet("v1", "v2", "v3")));
	}

	@Test
	public void testConvertTagsToHStore() {
		Node osmEntity = new Node();
		osmEntity.tags.put("k0", new HashSet<String>());
		osmEntity.tags.put("k1", Util.asSet("v1"));
		osmEntity.tags.put("k2", Util.asSet("v2", "v3"));
		assertEquals("'\"k0\"=>\"\" , \"k1\"=>\"v1\" , \"k2\"=>\"v2;v3\"'",
				converter.convertTagsToHStore(osmEntity));
	}

	@Test
	public void testAddNodesFromWay() {
		Way way = WayCreator.create(1).node(
				NodeCreator.create(2, 45, 8).finish()).node(
				NodeCreator.create(3, 35, 7).finish()).finish();
		assertEquals("2;3;", converter.addNodesFromWay(way));
	}

	private RelationMember createRelationMember(int osmId,
			RelationMemberType type, String role) {
		RelationMember relationMember = new RelationMember();
		relationMember.osmId = osmId;
		relationMember.type = type;
		relationMember.role = role;
		return relationMember;
	}
}

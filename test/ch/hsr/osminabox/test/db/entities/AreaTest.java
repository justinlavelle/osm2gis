package ch.hsr.osminabox.test.db.entities;

import static org.junit.Assert.*;

import java.util.Set;
import java.util.TreeSet;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.OSMEntity;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.RelationMember;
import ch.hsr.osminabox.db.entities.RelationMemberType;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.entities.Area.WayRole;
import ch.hsr.osminabox.db.entities.exceptions.InvalidRelationException;
import ch.hsr.osminabox.db.entities.exceptions.InvalidWayException;

public class AreaTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@Test
	public void testAreaWithCorrectWay() throws InvalidWayException {

		Way way = constructWay();
		Area a1;
		a1 = new Area(way);
		assertNotNull(a1);
		assertEquals(way.getOsmId(), a1.getOsmId());
		assertEquals(way.tags, a1.tags);
		assertEquals(1, a1.wayIds.size());
		assertEquals(WayRole.outer, a1.wayIds.get(way.getOsmId()));

	}

	@Test
	public void testAreaWithCorrectRelation() throws InvalidRelationException {

		Relation relation = constructRelation();
		Area a1;

		a1 = new Area(relation);

		assertEquals(111, a1.getOsmId());
		assertNotNull(a1.tags);
		assertNotNull(a1.originalTags.get("natural"));
		assertTrue(a1.originalTags.get("natural").contains("water"));
		assertFalse(a1.originalTags.get("name").contains("Inseli"));

	}

	@Test(expected = InvalidRelationException.class)
	public void testAreaWithNullRelation() throws InvalidRelationException {
		Relation relation = null;
		new Area(relation);
	}

	@Test(expected = InvalidWayException.class)
	public void testAreaWithNullWay() throws InvalidRelationException,
			InvalidWayException {
		Way way = null;
		new Area(way);
	}

	private Way constructWay() {
		Way way = new Way();

		// Set attributes
		way.setOsmId(257);
		way.attributes.put(OSMEntity.ATTRIBUTE_TIMESTAMP,
				"2008-07-29T08:30:07Z");

		// Set Nodes
		Node node1 = new Node();
		node1.setOsmId(22233);
		node1.attributes.put(OSMEntity.ATTRIBUTE_TIMESTAMP,
				"2009-09-19T08:33:07Z");
		node1.attributes.put(Node.NODE_LATITUDE, String.valueOf(47.312494f));
		node1.attributes.put(Node.NODE_LONGITUDE, String.valueOf(8.525556f));

		Node node2 = new Node();
		node2.setOsmId(123);
		node2.attributes.put(OSMEntity.ATTRIBUTE_TIMESTAMP,
				"2007-08-16T08:38:07Z");
		node2.attributes.put(Node.NODE_LATITUDE, String.valueOf(50.312494f));
		node2.attributes.put(Node.NODE_LONGITUDE, String.valueOf(-5.525556f));

		Node node3 = new Node();
		node3.setOsmId(789652);
		node3.attributes.put(OSMEntity.ATTRIBUTE_TIMESTAMP,
				"2006-08-23T23:38:07Z");
		node3.attributes.put(Node.NODE_LATITUDE, String.valueOf(48.334494f));
		node3.attributes.put(Node.NODE_LONGITUDE, String.valueOf(-1.987656f));

		way.nodes.add(node1);
		way.nodes.add(node2);
		way.nodes.add(node3);
		way.nodes.add(node1);

		// Set tags
		Set<String> created_by = new TreeSet<String>(
				String.CASE_INSENSITIVE_ORDER);
		created_by.add("JOSM");
		way.tags.put("created_by", created_by);

		Set<String> landuse = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		landuse.add("water");
		way.tags.put("landuse", landuse);

		Set<String> name = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		name.add("Joramsee");
		way.tags.put("name", name);

		return way;
	}

	private Relation constructRelation() {
		Relation relation = new Relation();

		// Set attributes
		relation.setOsmId(111);
		relation.attributes.put(OSMEntity.ATTRIBUTE_TIMESTAMP,
				"2009-02-21T18:33:57Z");

		// Set members
		RelationMember member = new RelationMember();

		member.osmId = 8;
		member.role = "inner";
		member.type = RelationMemberType.WAY;

		RelationMember member2 = new RelationMember();
		member2.osmId = 9;
		member2.role = "outer";
		member2.type = RelationMemberType.WAY;

		relation.members.add(member);
		relation.members.add(member2);

		// Set tags
		Set<String> type = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		type.add("multipolygon");
		relation.tags.put("type", type);

		Set<String> name = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		name.add("Joramsee");
		relation.tags.put("name", name);

		Set<String> natural = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		natural.add("water");
		relation.tags.put("natural", natural);

		return relation;
	}
}

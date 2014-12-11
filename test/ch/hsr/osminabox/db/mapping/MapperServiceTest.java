package ch.hsr.osminabox.db.mapping;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.entities.exceptions.InvalidWayException;
import ch.hsr.osminabox.schemamapping.ConfigService;
import ch.hsr.osminabox.schemamapping.ConfigServiceImpl;

public class MapperServiceTest {

	private static MapperService mapper;
	private static ConfigService config;

	@BeforeClass
	public static void init() {
		DOMConfigurator.configure("config/LogingConfigurationUnitTests.xml");

		config = new ConfigServiceImpl(
				"test/xmlfiles/mappingconfig_MappingsDictionaryTest1.xml", null);
		assertNotNull(config);

		mapper = new MapperService(config);
		assertNotNull(mapper);
	}

	@Test
	public void simpleNodeTest() {
		Set<String> railway = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		railway.add("tram_stop");

		Set<String> timestamp = new TreeSet<String>(
				String.CASE_INSENSITIVE_ORDER);
		timestamp.add("2009-04-11T08:16:16Z");

		Node node = new Node();
		node.setOsmId(1);
		node.tags.put("railway", railway);
		node.tags.put("timestamp", timestamp);

		List<Node> nodes = new LinkedList<Node>();
		nodes.add(node);

		mapper.addDbMappingsForNodes(nodes);

		assertNotNull(node.dbMappings.get("railwaystation"));
	}

	@Test
	public void simpleWayTest() {
		Set<String> railway = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		railway.add("light_rail");

		Set<String> timestamp = new TreeSet<String>(
				String.CASE_INSENSITIVE_ORDER);
		timestamp.add("2009-04-11T08:16:16Z");

		Way way = new Way();
		way.setOsmId(1);
		way.tags.put("railway", railway);
		way.tags.put("timestamp", timestamp);

		List<Way> ways = new LinkedList<Way>();
		ways.add(way);

		mapper.addDbMappingsForWays(ways);

		assertNotNull(way.dbMappings.get("railway"));
	}

	@Test
	public void simpleAreaTest() throws InvalidWayException {
		Set<String> natural = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		natural.add("water");

		Set<String> timestamp = new TreeSet<String>(
				String.CASE_INSENSITIVE_ORDER);
		timestamp.add("2009-04-11T08:16:16Z");

		Way way = new Way();
		way.setOsmId(77);
		way.tags.put("natural", natural);
		way.tags.put("timestamp", timestamp);

		Node n1 = new Node();
		n1.setOsmId(1);

		Node n2 = new Node();
		n2.setOsmId(2);

		Node n3 = new Node();
		n3.setOsmId(3);

		way.nodes.add(n1);
		way.nodes.add(n2);
		way.nodes.add(n3);
		way.nodes.add(n1);

		Area area = new Area(way);

		List<Area> areas = new LinkedList<Area>();
		areas.add(area);

		mapper.addDbMappingsForAreas(areas);

		assertNotNull(area.dbMappings.get("water"));
	}

	@Test
	public void simpleRelationTest() {

		Set<String> type = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		type.add("route");

		Set<String> route = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		route.add("train");

		Relation relation = new Relation();
		relation.setOsmId(1);
		relation.tags.put("route", route);
		relation.tags.put("type", type);

		List<Relation> relations = new LinkedList<Relation>();
		relations.add(relation);

		mapper.addDbMappingsForRelations(relations);

		assertNotNull(relation.dbMappings.get("trainroute"));

	}
}

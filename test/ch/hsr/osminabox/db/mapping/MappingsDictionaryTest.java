package ch.hsr.osminabox.db.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.hsr.osminabox.schemamapping.ConfigService;
import ch.hsr.osminabox.schemamapping.ConfigServiceImpl;
import ch.hsr.osminabox.schemamapping.exceptions.NoSchemaDefAndMappingConfigXMLException;
import ch.hsr.osminabox.schemamapping.xml.MappingType;

public class MappingsDictionaryTest {

	private static ConfigService config;
	private static MappingsDictionary mappingsDictionary;

	@BeforeClass
	public static void init() {

		config = new ConfigServiceImpl(
				"test/xmlfiles/mappingconfig_MappingsDictionaryTest1.xml", null);
		assertNotNull(config);

	}

	@Test
	public void initTest() throws NoSchemaDefAndMappingConfigXMLException {
		assertEquals("amenity", config.getXmlMapping(MappingType.POINT, 0)
				.getAndEdConditions().getTag().get(0).getK());

		mappingsDictionary = new MappingsDictionary(MappingType.POINT, config);
		assertNotNull(mappingsDictionary);
	}

	@Test
	public void SingleMatchesTest()
			throws NoSchemaDefAndMappingConfigXMLException {
		Map<String, Set<String>> map = new HashMap<String, Set<String>>();
		Set<String> amenity = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);

		amenity.add("place_of_worship");
		map.put("amenity", amenity);

		assertEquals(config.getXmlMapping(MappingType.POINT, 0),
				mappingsDictionary.getMatches(map).get(0));

		assertEquals(1, mappingsDictionary.getMatches(map).size());
	}

	@Test
	public void MultipleMatchesTest()
			throws NoSchemaDefAndMappingConfigXMLException {
		final int ANGLICAN_MAPPING_POS = 2;

		Map<String, Set<String>> map = new HashMap<String, Set<String>>();

		Set<String> amenity = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		Set<String> religion = new TreeSet<String>(
				String.CASE_INSENSITIVE_ORDER);
		Set<String> denomination = new TreeSet<String>(
				String.CASE_INSENSITIVE_ORDER);

		amenity.add("place_of_worship");
		religion.add("christian");
		denomination.add("anglican");

		map.put("amenity", amenity);
		map.put("religion", religion);
		map.put("denomination", denomination);

		assertEquals(config.getXmlMapping(MappingType.POINT,
				ANGLICAN_MAPPING_POS), mappingsDictionary.getMatches(map)
				.get(0));

		assertEquals(1, mappingsDictionary.getMatches(map).size());
	}

	@Test
	public void MultipleMappingsMatchesTest()
			throws NoSchemaDefAndMappingConfigXMLException {
		final int ANGLICAN_MAPPING_POS = 2;
		final int CATHOLIC_MAPPING_POS = 3;

		Map<String, Set<String>> map = new HashMap<String, Set<String>>();

		Set<String> amenity = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		Set<String> religion = new TreeSet<String>(
				String.CASE_INSENSITIVE_ORDER);
		Set<String> denomination = new TreeSet<String>(
				String.CASE_INSENSITIVE_ORDER);

		amenity.add("place_of_worship");
		religion.add("christian");
		denomination.add("anglican");
		denomination.add("catholic");

		map.put("amenity", amenity);
		map.put("religion", religion);
		map.put("denomination", denomination);

		assertEquals(config.getXmlMapping(MappingType.POINT,
				ANGLICAN_MAPPING_POS), mappingsDictionary.getMatches(map)
				.get(0));
		assertEquals(config.getXmlMapping(MappingType.POINT,
				CATHOLIC_MAPPING_POS), mappingsDictionary.getMatches(map)
				.get(1));

		assertEquals(2, mappingsDictionary.getMatches(map).size());
	}
}

package ch.hsr.osminabox.schemamapping;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import ch.hsr.osminabox.context.ArgumentConstants;
import ch.hsr.osminabox.context.ConfigConstants;
import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.schemamapping.exceptions.NoSchemaDefAndMappingConfigXMLException;
import ch.hsr.osminabox.schemamapping.xml.DstColumn;
import ch.hsr.osminabox.schemamapping.xml.DstJoinTableDef;
import ch.hsr.osminabox.schemamapping.xml.MappingType;
import ch.hsr.osminabox.schemamapping.xml.DstSchemaDef;
import ch.hsr.osminabox.schemamapping.xml.DstTableDef;
import ch.hsr.osminabox.schemamapping.xml.DstViewDef;
import ch.hsr.osminabox.schemamapping.xml.Mapping;
import ch.hsr.osminabox.schemamapping.xml.SchemaDefAndMapping;
import ch.hsr.osminabox.schemamapping.xml.SrcToDstMappings;
import ch.hsr.osminabox.schemamapping.xml.Tag;

/**
 * Implementation of Config Service
 * 
 * @author ameier
 * 
 */
public class ConfigServiceImpl implements ConfigService {

	private ApplicationContext context;

	/** The hole mapping config file */
	private SchemaDefAndMapping sdam;

	/** Is the mapping config file loaded */
	private boolean xmlFileLoaded;

	// -------------------------------------------------------------------------------
	// List<Map<String, Set<String>>> for searching key values in and ed
	// conditions
	// Map<Integer,Mapping> performance issues
	private List<Map<String, Set<String>>> pointMappings;
	private Map<Integer, Mapping> xmlPointMappings;

	private List<Map<String, Set<String>>> linestringMappings;
	private Map<Integer, Mapping> xmlLinestringMappings;

	private List<Map<String, Set<String>>> multipolygonMappings;
	private Map<Integer, Mapping> xmlMultipolygonMappings;

	private List<Map<String, Set<String>>> relationMappings;
	private Map<Integer, Mapping> xmlRelationMappings;

	// -------------------------------------------------------------------------------

	/**
	 * Instantiates a new config service.
	 * 
	 * @param pathToXmlConfigFile
	 *            the path to xml config file
	 */
	public ConfigServiceImpl(String pathToXmlConfigFile, ApplicationContext context) {
		this.context = context;
		if ((pathToXmlConfigFile == null) || (pathToXmlConfigFile.equals(""))) {
			pathToXmlConfigFile = getActualConfigXmlFilePath();
		}
		xmlFileLoaded = false;
		if ((pathToXmlConfigFile != null) && (!pathToXmlConfigFile.equals(""))) {
			reloadXmlFile(pathToXmlConfigFile);
		}

		pointMappings = new LinkedList<Map<String, Set<String>>>();
		linestringMappings = new LinkedList<Map<String, Set<String>>>();
		multipolygonMappings = new LinkedList<Map<String, Set<String>>>();
		relationMappings = new LinkedList<Map<String, Set<String>>>();

		xmlPointMappings = new HashMap<Integer, Mapping>();
		xmlLinestringMappings = new HashMap<Integer, Mapping>();
		xmlMultipolygonMappings = new HashMap<Integer, Mapping>();
		xmlRelationMappings = new HashMap<Integer, Mapping>();

		convertConfigMappings();
	}

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.schemamapping.IConfigService#getSchemaDefAndMapping()
	 */
	public SchemaDefAndMapping getSchemaDefAndMapping()
			throws NoSchemaDefAndMappingConfigXMLException {
		if (xmlFileLoaded)
			return sdam;

		throw new NoSchemaDefAndMappingConfigXMLException(
				"Can't load the Configfile!");
	}

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.schemamapping.IConfigService#getDstSchemaDef()
	 */
	public DstSchemaDef getDstSchemaDef()
			throws NoSchemaDefAndMappingConfigXMLException {
		if (xmlFileLoaded)
			return sdam.getDstSchemaDef();

		throw new NoSchemaDefAndMappingConfigXMLException(
				"Can't load the Configfile!");
	}

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.schemamapping.IConfigService#getSrcToDstMappings()
	 */
	public SrcToDstMappings getSrcToDstMappings()
			throws NoSchemaDefAndMappingConfigXMLException {
		if (xmlFileLoaded)
			return sdam.getSrcToDstMappings();

		throw new NoSchemaDefAndMappingConfigXMLException(
				"Can't load the Configfile!");
	}

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.schemamapping.IConfigService#reloadXmlFile(java.lang.String)
	 */
	public void reloadXmlFile(String pathToXmlConfigFile) {
		if ((pathToXmlConfigFile == null) || (pathToXmlConfigFile.equals(""))) {
			pathToXmlConfigFile = getActualConfigXmlFilePath();
		}
		sdam = (SchemaDefAndMapping) loadXmlFile(pathToXmlConfigFile,
				"ch.hsr.osminabox.schemamapping.xml");
		xmlFileLoaded = sdam != null;
	}

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.schemamapping.IConfigService#loadXmlFile(java.lang.String, java.lang.String)
	 */
	public Object loadXmlFile(String pathToXmlFile, String packageName) {
		try {
			Unmarshaller unmarshaller;

			unmarshaller = JAXBContext.newInstance(packageName)
					.createUnmarshaller();
			return (Object) unmarshaller.unmarshal(new StringReader(
					loadFromXmlFile(pathToXmlFile)));
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return null;
	}

	/**
	 * Load from xml file.
	 * 
	 * @param pathToXmlFile
	 *            the path to xml file
	 * 
	 * @return the XMLFile in a String
	 */
	private String loadFromXmlFile(String pathToXmlFile) {
		String lineSep = System.getProperty("line.separator");
		BufferedReader br;
		String nextLine = "";
		StringBuffer sb = new StringBuffer();

		try {

			br = new BufferedReader(new FileReader(pathToXmlFile));

			while ((nextLine = br.readLine()) != null) {
				sb.append(nextLine);
				sb.append(lineSep);
			}

			return sb.toString();
		} catch (FileNotFoundException e) {
			System.err.println("Could not load xml-file: " + pathToXmlFile
					+ "!");
		} catch (IOException e) {
			// e.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.schemamapping.IConfigService#getActualConfigXmlFilePath()
	 */
	public String getActualConfigXmlFilePath() {
		String xmlFilePath;
		if (context.containsArgument(ArgumentConstants.OPT_MAPPING)) {
			xmlFilePath = context.getArgument(ArgumentConstants.OPT_MAPPING);
		} else {
			xmlFilePath = context
					.getConfigParameter(ConfigConstants.CONF_MAPPING_CONFIGFILE);
		}
		return xmlFilePath;
	}

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.schemamapping.IConfigService#getMappingTables()
	 */
	public List<String> getMappingTables() {
		DstSchemaDef dsd;
		try {
			dsd = getDstSchemaDef();
			List<String> result = new LinkedList<String>();
			for (Object o : dsd.getDstTableDefOrDstTableDefUserDefined()) {
				if (o instanceof DstTableDef) {
					DstTableDef dtd = (DstTableDef) o;
					result.add(dtd.getName());
					result.remove(dtd.getInherits());
				}
			}
			Collections.sort(result);
			return result;
		} catch (NoSchemaDefAndMappingConfigXMLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.schemamapping.IConfigService#getMappingViews()
	 */
	public List<String> getMappingViews() {
		DstSchemaDef dsd;
		try {
			dsd = getDstSchemaDef();
			List<String> result = new LinkedList<String>();
			for (DstViewDef dvd : dsd.getDstViewDef()) {
				result.add(dvd.getName());
			}
			Collections.sort(result);
			return result;
		} catch (NoSchemaDefAndMappingConfigXMLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.schemamapping.IConfigService#getMappingJoinTables()
	 */
	public Set<String> getMappingJoinTables() {
		DstSchemaDef dsd;
		try {
			dsd = getDstSchemaDef();
			Set<String> result = new HashSet<String>();
			for (DstJoinTableDef djtd : dsd.getDstJoinTableDef()) {
				result.add(djtd.getName());
			}
			return result;
		} catch (NoSchemaDefAndMappingConfigXMLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.schemamapping.IConfigService#getMappingsOfTable(java.lang.String)
	 */
	public List<Mapping> getMappingsOfTable(String tablename) {
		SrcToDstMappings stdm;
		try {
			stdm = getSrcToDstMappings();
			List<Mapping> result = new LinkedList<Mapping>();
			for (Mapping m : stdm.getMapping()) {
				if (m.getDstTable().getName().toLowerCase().equals(
						tablename.toLowerCase())) {
					result.add(m);
				}
			}
			return result;
		} catch (NoSchemaDefAndMappingConfigXMLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.schemamapping.IConfigService#getGeomTypeOfTable(java.lang.String)
	 */
	public MappingType getGeomTypeOfTable(String tablename) {
		for (Object o : sdam.getDstSchemaDef()
				.getDstTableDefOrDstTableDefUserDefined()) {
			if (o instanceof DstTableDef) {
				if (((DstTableDef) o).getName().equals(tablename)) {
					for (DstColumn n : ((DstTableDef) o).getDstColumn()) {
						if (n.getType().toLowerCase().indexOf("geometry(") > -1) {
							String temp = n.getType().substring(
									n.getType().indexOf("'") + 1);
							temp = temp.substring(0, temp.indexOf("'"))
									.toLowerCase();
							if (temp
									.toLowerCase()
									.equals(
											ch.hsr.osminabox.db.DBConstants.GEOMTYPE_LINESTRING
													.toLowerCase())) {
								return MappingType.LINESTRING;
							} else if (temp
									.toLowerCase()
									.equals(
											ch.hsr.osminabox.db.DBConstants.GEOMTYPE_MULTIPOLYGON
													.toLowerCase())) {
								return MappingType.MULTIPOLYGON;
							} else if (temp
									.toLowerCase()
									.equals(
											ch.hsr.osminabox.db.DBConstants.GEOMTYPE_POINT
													.toLowerCase())) {
								return MappingType.POINT;
							}
						}
					}
				}
			}
		}
		return MappingType.RELATION;
	}

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.schemamapping.IConfigService#getTablesOfGeomType(ch.hsr.osminabox.schemamapping.xml.MappingType)
	 */
	public Set<String> getTablesOfGeomType(MappingType geomType) {
		Set<String> result = new HashSet<String>();

		for (String tablename : getMappingTables()) {
			if (getGeomTypeOfTable(tablename).equals(geomType)) {
				result.add(tablename);
			}
		}

		return result;
	}

	/**
	 * Extracts all Keys and Values from the Mappings in the given List into a
	 * List<Map<Key,Value>>.
	 * 
	 * @param configMappings
	 *            A List of ch.hsr.osminabox.mappingschemadefconfig.xml.Mapping
	 *            Classes.
	 */
	private void convertConfigMappings() {
		try {
			for (Mapping configMapping : getSrcToDstMappings().getMapping()) {
				Map<String, Set<String>> mapping = new TreeMap<String, Set<String>>(String.CASE_INSENSITIVE_ORDER);

				for (Tag nodesTags : configMapping.getAndEdConditions()
						.getTag()) {

					Set<String> values = mapping.get(nodesTags.getK());
					if (values == null)
						values = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);

					values.add(nodesTags.getV());
					mapping.put(nodesTags.getK(), values);
				}

				switch (configMapping.getType()) {
				case POINT:
					pointMappings.add(mapping);
					xmlPointMappings.put(pointMappings.size() - 1,
							configMapping);
					break;

				case LINESTRING:
					linestringMappings.add(mapping);
					xmlLinestringMappings.put(linestringMappings.size() - 1,
							configMapping);
					break;

				case MULTIPOLYGON:
					multipolygonMappings.add(mapping);
					xmlMultipolygonMappings.put(
							multipolygonMappings.size() - 1, configMapping);
					break;

				case RELATION:
					relationMappings.add(mapping);
					xmlRelationMappings.put(relationMappings.size() - 1,
							configMapping);
					break;
				}

			}
		} catch (NoSchemaDefAndMappingConfigXMLException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.schemamapping.IConfigService#getMappings(ch.hsr.osminabox.schemamapping.xml.MappingType)
	 */
	public List<Map<String, Set<String>>> getMappings(MappingType geomType) {
		switch (geomType) {
		case POINT:
			return pointMappings;
		case LINESTRING:
			return linestringMappings;
		case MULTIPOLYGON:
			return multipolygonMappings;
		case RELATION:
			return relationMappings;
		default:
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.schemamapping.IConfigService#getXmlMapping(ch.hsr.osminabox.schemamapping.xml.MappingType, int)
	 */
	public Mapping getXmlMapping(MappingType geomType, int indexOfMapping)
			throws NoSchemaDefAndMappingConfigXMLException {
		switch (geomType) {
		case POINT:
			return xmlPointMappings.get(indexOfMapping);
		case LINESTRING:
			return xmlLinestringMappings.get(indexOfMapping);
		case MULTIPOLYGON:
			return xmlMultipolygonMappings.get(indexOfMapping);
		case RELATION:
			return xmlRelationMappings.get(indexOfMapping);
		default:
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.schemamapping.IConfigService#getInheritedTable(ch.hsr.osminabox.schemamapping.xml.DstSchemaDef, java.lang.String)
	 */
	public DstTableDef getInheritedTable(DstSchemaDef theXMLDef,
			String inheritedTable) {
		if (inheritedTable != null) {
			for (Object ixt : theXMLDef
					.getDstTableDefOrDstTableDefUserDefined()) {
				if ((ixt instanceof DstTableDef)
						&& (((DstTableDef) ixt).getName().toLowerCase()
								.equals(inheritedTable.toLowerCase()))) {
					return (DstTableDef) ixt;
				}
			}
		}
		return null;
	}
	
	public String getReferenceColumnName(String joinTableName, String referenceTable){
		for (DstJoinTableDef djtd : sdam.getDstSchemaDef().getDstJoinTableDef()){
			if (djtd.getName().toLowerCase().equals(joinTableName.toLowerCase())){
				for (DstColumn column : djtd.getDstColumn()){
					if (column.getReferences().toLowerCase().equals(referenceTable.toLowerCase())){
						return column.getName();
					}
				}
			}
		}
		return null;
	}
}

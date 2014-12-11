package ch.hsr.osminabox.schemamapping;

import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.hsr.osminabox.schemamapping.exceptions.NoSchemaDefAndMappingConfigXMLException;
import ch.hsr.osminabox.schemamapping.xml.DstSchemaDef;
import ch.hsr.osminabox.schemamapping.xml.DstTableDef;
import ch.hsr.osminabox.schemamapping.xml.Mapping;
import ch.hsr.osminabox.schemamapping.xml.MappingType;
import ch.hsr.osminabox.schemamapping.xml.SchemaDefAndMapping;
import ch.hsr.osminabox.schemamapping.xml.SrcToDstMappings;

public interface ConfigService {

	/**
	 * Gets the mapping config file.
	 * 
	 * @return the schema def and mapping if it is loaded
	 * 
	 * @throws NoSchemaDefAndMappingConfigXMLException
	 *             the no schema def and mapping config xml exception
	 */
	public SchemaDefAndMapping getSchemaDefAndMapping()
			throws NoSchemaDefAndMappingConfigXMLException;

	/**
	 * Gets the dst schema def.
	 * 
	 * @return the dst schema def
	 * 
	 * @throws NoSchemaDefAndMappingConfigXMLException
	 *             the no schema def and mapping config xml exception
	 */
	public DstSchemaDef getDstSchemaDef()
			throws NoSchemaDefAndMappingConfigXMLException;

	/**
	 * Gets the src to dst mappings.
	 * 
	 * @return the src to dst mappings
	 * 
	 * @throws NoSchemaDefAndMappingConfigXMLException
	 *             the no schema def and mapping config xml exception
	 */
	public SrcToDstMappings getSrcToDstMappings()
			throws NoSchemaDefAndMappingConfigXMLException;

	/**
	 * Reload xml file.
	 * 
	 * @param pathToXmlConfigFile
	 *            the path to xml config file
	 */
	public void reloadXmlFile(String pathToXmlConfigFile);

	/**
	 * Load xml file.
	 * 
	 * Global method to load various xml files.
	 * 
	 * @param pathToXmlFile
	 *            the path to xml file
	 * @param packageName
	 *            the packagename of the package of jaxb classes
	 * 
	 * @return the object (xml file)
	 */
	public Object loadXmlFile(String pathToXmlFile, String packageName);

	/**
	 * Gets the actual config xml file path.
	 * 
	 * @return the actual config xml file path
	 */
	public String getActualConfigXmlFilePath();

	/**
	 * Gets all defined tables in the mappingconfig file
	 * 
	 * @return the mapping tables
	 */
	public List<String> getMappingTables();

	/**
	 * Gets all defined views in the mappingconfig file
	 * 
	 * @return the mapping views
	 */
	public List<String> getMappingViews();

	public Set<String> getMappingJoinTables();

	/**
	 * Gets the mappings of table or view.
	 * 
	 * @param tablename
	 *            the tablename
	 * 
	 * @return the mappings of table
	 */
	public List<Mapping> getMappingsOfTable(String tablename);

	public MappingType getGeomTypeOfTable(String tablename);

	public Set<String> getTablesOfGeomType(MappingType geomType);

	public List<Map<String, Set<String>>> getMappings(MappingType geomType);

	public Mapping getXmlMapping(MappingType geomType, int indexOfMapping)
			throws NoSchemaDefAndMappingConfigXMLException;

	public DstTableDef getInheritedTable(DstSchemaDef theXMLDef,
			String inheritedTable);
	
	public String getReferenceColumnName(String joinTableName, String referenceTable);

}